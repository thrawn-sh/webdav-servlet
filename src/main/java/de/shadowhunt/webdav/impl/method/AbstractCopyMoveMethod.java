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

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavProperty;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponseFoo;
import de.shadowhunt.webdav.WebDavStore;

public abstract class AbstractCopyMoveMethod extends AbstractWebDavMethod {

    private final boolean deleteSource;

    protected AbstractCopyMoveMethod(final boolean deleteSource) {
        this.deleteSource = deleteSource;
    }

    protected void copy(final WebDavStore store, final WebDavPath source, final WebDavPath target, final int depth) {
        if (depth < 0) {
            return;
        }

        final WebDavEntity sourceEntity = store.getEntity(source);
        if (sourceEntity.getType() == WebDavEntity.Type.COLLECTION) {
            store.createCollection(target);
        } else {
            store.createItem(target, store.getContent(source));
        }
        final Collection<WebDavProperty> properties = store.getProperties(source);
        store.setProperties(target, properties);

        for (final WebDavPath child : store.list(source)) {
            copy(store, child, target.getChild(child.getName()), depth - 1);
        }
    }

    protected boolean determineOverwrite(final WebDavRequest request) {
        final String overwrite = request.getOption("Overwrite", "T");
        return "T".equalsIgnoreCase(overwrite);
    }

    protected WebDavPath determineTarget(final WebDavRequest request) {
        final String pathInfo = request.getBase();
        final String destination = request.getOption("Destination", "");
        final URI destinationUri = URI.create(destination);
        final String destinationPath = destinationUri.getPath();
        final int indexOf = destinationPath.indexOf(pathInfo);
        return WebDavPath.create(destinationPath.substring(indexOf + pathInfo.length()));
    }

    @Override
    public WebDavResponseFoo service(final WebDavStore store, final WebDavRequest request) throws IOException {
        final WebDavPath source = request.getPath();
        if (!store.exists(source)) {
            return AbstractBasicResponse.createNotFound();
        }

        final WebDavEntity sourceEntity = store.getEntity(source);

        final boolean overwrite = determineOverwrite(request);
        final int depth = determineDepth(request);
        final WebDavPath target = determineTarget(request);
        final boolean targetExistsBefore = store.exists(target);
        if (targetExistsBefore) {
            if (overwrite) {
                store.delete(target);
            } else {
                return AbstractBasicResponse.createPreconditionFailed(sourceEntity);
            }
        }

        // targetParent collection must exist
        final WebDavPath targetParent = target.getParent();
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
