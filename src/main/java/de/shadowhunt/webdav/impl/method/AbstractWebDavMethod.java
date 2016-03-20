/**
 * This file is part of Shadowhunt WebDav Servlet.
 *
 * Shadowhunt WebDav Servlet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shadowhunt WebDav Servlet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shadowhunt WebDav Servlet.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.webdav.impl.method;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse.Status;
import de.shadowhunt.webdav.WebDavStore;

import org.apache.commons.lang3.StringUtils;

abstract class AbstractWebDavMethod implements WebDavMethod {

    public static final String INFINITY = "infinity";

    static void checkDown(final WebDavStore store, final WebDavPath path, final int depth, final Set<UUID> tokens) {
        if (depth < 0) {
            return; // FIXME
        }

        final WebDavEntity entity = store.getEntity(path);
        checkLockTokenOnEntity(entity, tokens);

        for (final WebDavPath child : store.list(path)) {
            checkDown(store, child, depth - 1, tokens);
        }
    }

    static void checkLockTokenOnEntity(final WebDavEntity entity, final Set<UUID> tokens) {
        final Optional<WebDavLock> lock = entity.getLock();
        if (!lock.isPresent()) {
            return;
        }

        if (tokens.contains(lock.get().getToken())) {
            return;
        }
        throw new WebDavException("", Status.SC_LOCKED); // TODO
    }

    static void checkUp(final WebDavStore store, final WebDavPath path, final Set<UUID> tokens) {
        final WebDavEntity entity = store.getEntity(path);
        checkLockTokenOnEntity(entity, tokens);

        if (WebDavPath.ROOT.equals(path)) {
            return;
        }

        checkUp(store, path.getParent(), tokens);
    }

    static Optional<UUID> convert(final String token) {
        String value = token;
        if (value.charAt(0) != '<' || value.charAt(value.length() - 1) != '>') {
            return Optional.empty();
        }
        value = value.substring(1, value.length() - 1);

        if (!value.startsWith(WebDavLock.PREFIX)) {
            return Optional.empty();
        }
        value = value.substring(WebDavLock.PREFIX.length());

        try {
            return Optional.of(UUID.fromString(value));
        } catch (final RuntimeException e) {
            return Optional.empty();
        }
    }

    protected Set<UUID> deterimineLockTokens(final WebDavRequest request) {
        final String tokens = request.getHeader("Lock-Token", "");
        if (StringUtils.isBlank(tokens)) {
            return Collections.emptySet();
        }

        final Set<UUID> uuids = new HashSet<>();
        final Optional<UUID> token = convert(tokens);
        token.ifPresent(x -> uuids.add(x));
        return uuids;
    }

    protected int determineDepth(final WebDavRequest request) {
        final String depth = request.getHeader("Depth", INFINITY);
        if (INFINITY.equalsIgnoreCase(depth)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(depth);
    }

    @Override
    public final String toString() {
        return "WebDavMethod [method=" + getMethod() + "]";
    }
}
