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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Resource;
import de.shadowhunt.servlet.webdav.Store;

public class CopyMoveMethod extends AbstractWebDavMethod {

    public static final String COPY_METHOD = "COPY";

    public static final String MOVE_METHOD = "MOVE";

    private final boolean deleteSource;

    public CopyMoveMethod(final Store store, final boolean deleteSource) {
        super((deleteSource) ? MOVE_METHOD : COPY_METHOD, store);
        this.deleteSource = deleteSource;
    }

    private void copy(final Resource source, final Resource target, final int depth) {
        if (depth < 0) {
            return;
        }

        final Entity sourceEntity = store.getEntity(source);
        if (sourceEntity.getType() == Entity.Type.COLLECTION) {
            store.createCollection(target);
        } else {
            store.createItem(target, store.download(source));
        }

        for (final Resource child : store.list(source)) {
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

    protected Resource determineTarget(final HttpServletRequest request) {
        final String pathInfo = request.getServletPath();
        final String destination = request.getHeader("Destination");
        final URI destinationUri = URI.create(destination);
        final String destinationPath = destinationUri.getPath();
        final int indexOf = destinationPath.indexOf(pathInfo);
        return Resource.create(destinationPath.substring(indexOf));
    }

    @Override
    public WebDavResponse service(final Resource source, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(source)) {
            return StatusResponse.NOT_FOUND;
        }

        final boolean overwrite = determineOverwrite(request);
        final int depth = determineDepth(request);
        final Resource target = determineTarget(request);
        final boolean targetExistsBefore = store.exists(target);
        if (targetExistsBefore) {
            if (overwrite) {
                store.delete(target);
            } else {
                return StatusResponse.PRECONDITION_FAILED;
            }
        }

        // targetParent collection must exist
        final Resource targetParent = target.getParent();
        if (!store.exists(targetParent)) {
            return StatusResponse.CONFLICT;
        }

        copy(source, target, depth);

        if (deleteSource) {
            store.delete(source);
        }

        if (targetExistsBefore) {
            return StatusResponse.NO_CONTENT;
        }
        return StatusResponse.CREATED;
    }
}
