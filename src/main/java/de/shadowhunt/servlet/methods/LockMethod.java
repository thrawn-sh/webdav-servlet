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
package de.shadowhunt.servlet.methods;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Lock;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Store;

public class LockMethod extends AbstractWebDavMethod {

    public static final String METHOD = "LOCK";

    public LockMethod(final Store store) {
        super(METHOD, store);
    }

    protected int determineDepth(final HttpServletRequest request) {
        final String depth = request.getHeader("Depth");
        if (StringUtils.isEmpty(depth) || "infinity".equalsIgnoreCase(depth)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(depth);
    }

    @Override
    public WebDavResponse service(final Path path, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(path)) {
            return BasicResponse.createNotFound();
        }

//        if (determineDepth(request) > 0) {
//            return StatusResponse.BAD_REQUEST;
//        }

        final Entity entity = store.getEntity(path);
        if (entity.isLocked()) {
            if (hasLockProblem(entity, request, "If")) {
                return BasicResponse.createLocked(entity);
            }
            return new LockResponse(entity);
        }

        final Lock lock = new Lock("opaquelocktoken:" + UUID.randomUUID().toString(), Lock.Scope.EXCLUSIVE, "");
        store.lock(path, lock);
        return new LockResponse(store.getEntity(path)); // refreshed entity
    }
}
