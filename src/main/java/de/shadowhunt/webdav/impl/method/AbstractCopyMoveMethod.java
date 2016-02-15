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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.webdav.Entity;
import de.shadowhunt.webdav.Path;
import de.shadowhunt.webdav.Property;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.WebDavResponse;

public abstract class AbstractCopyMoveMethod extends AbstractWebDavMethod {

    private final boolean deleteSource;
    
    protected AbstractCopyMoveMethod(final boolean deleteSource) {
        this.deleteSource = deleteSource;
    }
    
    protected void copy(final WebDavStore store, final Path source, final Path target, final int depth) {
        if (depth < 0) {
            return;
        }

        final Entity sourceEntity = store.getEntity(source);
        if (sourceEntity.getType() == Entity.Type.COLLECTION) {
            store.createCollection(target);
        } else {
            store.createItem(target, store.download(source));
        }
        final Collection<Property> properties = store.getProperties(source);
        store.setProperties(target, properties);

        for (final Path child : store.list(source)) {
            copy(store, child, target.getChild(child.getName()), depth - 1);
        }
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
    public WebDavResponse service(final WebDavStore store, final Path source, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(source)) {
            return AbstractBasicResponse.createNotFound();
        }

        final Entity sourceEntity = store.getEntity(source);

        final boolean overwrite = determineOverwrite(request);
        final int depth = determineDepth(request);
        final Path target = determineTarget(request);
        final boolean targetExistsBefore = store.exists(target);
        if (targetExistsBefore) {
            if (overwrite) {
                store.delete(target);
            } else {
                return AbstractBasicResponse.createPreconditionFailed(sourceEntity);
            }
        }

        // targetParent collection must exist
        final Path targetParent = target.getParent();
        if (!store.exists(targetParent)) {
            return AbstractBasicResponse.createConflict(sourceEntity);
        }

        copy(store, source, target, depth);

        if (deleteSource) {
            store.delete(source);
        }

        if (targetExistsBefore) {
            return AbstractBasicResponse.createNoContent(sourceEntity);
        }
        return AbstractBasicResponse.createCreated(sourceEntity);
    }
}
