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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Store;

public class GetMethod extends AbstractWebDavMethod {

    public static final String METHOD = "GET";

    protected final String cssPath;

    protected final boolean htmlListing;

    public GetMethod(final Store store, final boolean htmlListing, final String cssPath) {
        super(METHOD, store);
        this.htmlListing = htmlListing;
        this.cssPath = cssPath;
    }

    protected List<Entity> getEntities(final Path path) {
        final List<Path> children = store.list(path);
        final List<Entity> result = new ArrayList<>();
        for (final Path child : children) {
            result.add(store.getEntity(child));
        }
        return result;
    }

    @Override
    public WebDavResponse service(final Path path, final HttpServletRequest request) {
        if (!store.exists(path)) {
            return StatusResponse.NOT_FOUND;
        }
        final Entity entity = store.getEntity(path);
        final Entity.Type type = entity.getType();
        if (type == Entity.Type.ITEM) {
            return new StreamingResponse(store.download(path));
        }

        if ((type == Entity.Type.COLLECTION) && htmlListing) {
            final List<Entity> entities = getEntities(path);
            return new HtmlListingResponse(entity, entities, cssPath);
        }

        return StatusResponse.FORBIDDEN;
    }
}
