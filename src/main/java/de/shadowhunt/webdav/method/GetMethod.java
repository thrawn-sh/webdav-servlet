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
package de.shadowhunt.webdav.method;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.WebDavStore;

public class GetMethod extends AbstractWebDavMethod {

    protected List<WebDavEntity> getEntities(final WebDavStore store, final WebDavPath path) {
        final List<WebDavPath> children = store.list(path);
        final List<WebDavEntity> result = new ArrayList<>(children.size());
        for (final WebDavPath child : children) {
            result.add(store.getEntity(child));
        }
        return result;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public WebDavResponseWriter service(final WebDavStore store, final WebDavRequest request) {
        final WebDavPath target = request.getPath();
        if (!store.exists(target)) {
            return AbstractBasicResponse.createNotFound();
        }

        final WebDavEntity entity = store.getEntity(target);
        final WebDavEntity.Type type = entity.getType();
        if (type == WebDavEntity.Type.ITEM) {
            final InputStream content = store.getContent(target);
            return new StreamingResponse(entity, content);
        }

        final WebDavConfig config = request.getConfig();
        if (config.isShowCollectionListings()) {
            final WebDavEntity parent = store.getEntity(target.getParent());
            final List<WebDavEntity> entities = getEntities(store, target);
            return new HtmlListingResponse(parent, entity, entities, config.getCssForCollectionListings());
        }

        return AbstractBasicResponse.createForbidden(entity);
    }
}
