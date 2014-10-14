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

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.servlet.webdav.Depth;
import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Resource;
import de.shadowhunt.servlet.webdav.Store;

public class GetMethod extends AbstractWebDavMethod {

    public static final String METHOD = "GET";

    private final String cssPath;

    private final boolean htmlListing;

    public GetMethod(final Store store, final boolean htmlListing, final String cssPath) {
        super(METHOD, store);
        this.htmlListing = htmlListing;
        this.cssPath = cssPath;
    }

    @Override
    public WebDavResponse service(final Resource resource, final Principal principal, final HttpServletRequest request) {
        final Entity entity = store.info(resource);
        final Entity.Type type = entity.getType();
        if (type == Entity.Type.FILE) {
            return new StreamingResponse(store.download(resource));
        }

        if ((type == Entity.Type.COLLECTION) && htmlListing) {
            final List<Entity> entities = store.list(resource, Depth.IMMEDIATE);
            return new HtmlListingResponse(entity, entities, cssPath);
        }

        return new StatusResponse(HttpServletResponse.SC_FORBIDDEN);
    }
}
