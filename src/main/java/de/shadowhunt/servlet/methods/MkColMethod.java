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
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.servlet.webdav.Resource;
import de.shadowhunt.servlet.webdav.Store;

public class MkColMethod extends AbstractWebDavMethod {

    private static final WebDavResponse FAIL_FILE_RESOURCE_EXISTS = new StatusResponse(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

    private static final WebDavResponse FAIL_PARENT_MISSING = new StatusResponse(HttpServletResponse.SC_CONFLICT);

    private static final WebDavResponse INVALID_REQUEST_BODY = new StatusResponse(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);

    public static final String METHOD = "MKCOL";

    private static final WebDavResponse SUCCESS = new StatusResponse(HttpServletResponse.SC_CREATED);

    public MkColMethod(final Store store) {
        super(METHOD, store);
    }

    @Override
    public WebDavResponse service(final Resource resource, final Principal principal, final HttpServletRequest request) throws ServletException, IOException {
        if (consume(request.getInputStream())) {
            return INVALID_REQUEST_BODY;
        }

        final Resource parent = resource.getParent();
        if (!store.exists(parent)) {
            return FAIL_PARENT_MISSING;
        }

        if (!store.exists(resource)) {
            store.mkdir(resource);
            return SUCCESS;
        }
        return FAIL_FILE_RESOURCE_EXISTS;
    }
}
