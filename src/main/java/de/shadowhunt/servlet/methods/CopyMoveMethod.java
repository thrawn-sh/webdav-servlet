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
import java.net.URI;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Store;
import de.shadowhunt.servlet.webdav.StringProperty;

public class CopyMoveMethod extends AbstractWebDavMethod {

    public static final String COPY_METHOD = "COPY";

    public static final String MOVE_METHOD = "MOVE";

    protected final boolean deleteSource;

    public CopyMoveMethod(final Store store, final boolean deleteSource) {
        super((deleteSource) ? MOVE_METHOD : COPY_METHOD, store);
        this.deleteSource = deleteSource;
    }

    protected void copy(final Path source, final Path target, final int depth) {
        if (depth < 0) {
            return;
        }

        final Entity sourceEntity = store.getEntity(source);
        if (sourceEntity.getType() == Entity.Type.COLLECTION) {
            store.createCollection(target);
        } else {
            store.createItem(target, store.download(source));
        }
        final Collection<StringProperty> properties = store.getProperties(source);
        store.setProperties(target, properties);

        for (final Path child : store.list(source)) {
            copy(child, target.getChild(child.getName()), depth - 1);
        }
    }

    protected int determineDepth(final HttpServletRequest request) {
        final String depth = request.getHeader("Depth");
        if (StringUtils.isEmpty(depth) || "infinity".equalsIgnoreCase(depth)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(depth);
    }

    protected boolean determineOverwrite(final HttpServletRequest request) {
        final String overwrite = request.getHeader("Overwrite");
        return "T".equalsIgnoreCase(overwrite);
    }

    protected Path determineTarget(final HttpServletRequest request) {
        final String pathInfo = request.getServletPath();
        final String destination = request.getHeader("Destination");
        final URI destinationUri = URI.create(destination);
        final String destinationPath = destinationUri.getPath();
        final int indexOf = destinationPath.indexOf(pathInfo);
        return Path.create(destinationPath.substring(indexOf + pathInfo.length()));
    }

    @Override
    public WebDavResponse service(final Path source, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(source)) {
            return BasicResponse.createNotFound();
        }

        final Entity sourceEntity = store.getEntity(source);
        if (deleteSource) {
            if (hasLockProblem(sourceEntity, request, "If")) {
                return BasicResponse.createLocked(sourceEntity);
            }
        }

        final boolean overwrite = determineOverwrite(request);
        final int depth = determineDepth(request);
        final Path target = determineTarget(request);
        final boolean targetExistsBefore = store.exists(target);
        if (targetExistsBefore) {
            if (overwrite) {
                final Entity targetEntity = store.getEntity(target);
                if (hasLockProblem(targetEntity, request, "If")) {
                    return BasicResponse.createLocked(sourceEntity);
                }
                store.delete(target);
            } else {
                return BasicResponse.createPreconditionFailed(sourceEntity);
            }
        }

        // targetParent collection must exist
        final Path targetParent = target.getParent();
        if (!store.exists(targetParent)) {
            return BasicResponse.createConflict(sourceEntity);
        }

        copy(source, target, depth);

        if (deleteSource) {
            store.delete(source);
        }

        if (targetExistsBefore) {
            return BasicResponse.createNoContent(sourceEntity);
        }
        return BasicResponse.createCreated(sourceEntity);
    }
}
