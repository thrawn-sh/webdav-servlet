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
package de.shadowhunt.servlet.aaa;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.servlet.methods.WebDavResponse;

public final class WebDavDispatcher {

    private static final WebDavDispatcher INSTANCE = new WebDavDispatcher();
    
    public static WebDavDispatcher getInstance() {
        return INSTANCE;
    }
    
    private final Map<String, WebDavMethod> dispatcher = new HashMap<>();
    
    private WebDavDispatcher() {
        // prevent instantiation
    }
    
    Path getResource(final HttpServletRequest request) {
        final String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            return Path.ROOT;
        }
        return Path.create(pathInfo);
    }
    
    public void service(final Store store, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String method = request.getMethod();
        final WebDavMethod dispatch = dispatcher.get(method);
        if (dispatch == null) {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }

        try {
            final Path path = getResource(request);
            final WebDavResponse webDavResponse = dispatch.service(store, path, request);
            webDavResponse.write(response);
        } catch (final WebDavException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
