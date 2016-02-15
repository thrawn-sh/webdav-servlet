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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.webdav.Entity;
import de.shadowhunt.webdav.Path;
import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavStore;

public class GetMethod extends AbstractWebDavMethod {

    protected List<Entity> getEntities(final WebDavStore store, final Path path) {
        final List<Path> children = store.list(path);
        final List<Entity> result = new ArrayList<>(children.size());
        for (final Path child : children) {
            result.add(store.getEntity(child));
        }
        return result;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public WebDavResponse service(final WebDavStore store, final Path path, final HttpServletRequest request) {
        if (!store.exists(path)) {
            return AbstractBasicResponse.createNotFound();
        }
        final Entity entity = store.getEntity(path);
        final Entity.Type type = entity.getType();
        if (type == Entity.Type.ITEM) {
            return new StreamingResponse(entity, store.download(path));
        }

        final WebDavConfig config = WebDavConfig.getInstance();
        if ((type == Entity.Type.COLLECTION) && config.isShowCollectionListings()) {
            final List<Entity> entities = getEntities(store, path);
            return new HtmlListingResponse(entity, entities, "/style.css"); // FIXME
        }

        return AbstractBasicResponse.createForbidden(entity);
    }
}
