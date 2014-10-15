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

import de.shadowhunt.servlet.webdav.Resource;
import de.shadowhunt.servlet.webdav.Store;

public class DeleteMethod extends AbstractWebDavMethod {

    public static final String METHOD = "DELETE";

    public DeleteMethod(final Store store) {
        super(METHOD, store);
    }

    @Override
    public WebDavResponse service(final Resource resource, final Principal principal, final HttpServletRequest request) throws ServletException, IOException {
        if (Resource.ROOT.equals(resource)) {
            return StatusResponse.FORBIDDEN;
        }
        if (!store.exists(resource)) {
            return StatusResponse.NOT_FOUND;
        }
        store.delete(resource);
        return StatusResponse.NO_CONTENT;
    }
}
