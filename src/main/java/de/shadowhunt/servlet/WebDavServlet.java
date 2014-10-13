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
package de.shadowhunt.servlet;

import javax.annotation.CheckForNull;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.servlet.webdav.Resource;
import de.shadowhunt.servlet.webdav.Store;
import de.shadowhunt.servlet.webdav.WebDavException;
import de.shadowhunt.servlet.webdav.internal.FileSystemStore;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public class WebDavServlet extends HttpServlet {

    private final Map<String, AbstractWebDavMethod> dispatcher = new HashMap<>();

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        final Store store = new FileSystemStore(FileUtils.getTempDirectory());
        dispatcher.put(GetMethod.METHOD, new GetMethod(false, store));
    }

    @Override
    public void destroy() {
        dispatcher.clear();
        super.destroy();
    }

    @CheckForNull
    protected Principal getPrincipal(final HttpServletRequest request) {
        return request.getUserPrincipal();
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String method = request.getMethod();
        final AbstractWebDavMethod dispatch = dispatcher.get(method);
        if (dispatch == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST); // TODO
            return;
        }

        final Principal principal = getPrincipal(request);
        if (dispatch.isRequiresPrincipal() && (principal == null)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // TODO
        }

        try {
            Resource resource = getResource(request);
            final WebDavResponse webDavResponse = dispatch.service(resource, principal, request);
            webDavResponse.write(response);
        } catch(WebDavException e) {
            response.sendError(e.getHttpStatusCode()); // TODO
        }
    }

    protected Resource getResource(final HttpServletRequest request) {
        final String servletPath = request.getServletPath();
        return null;
    }
}
