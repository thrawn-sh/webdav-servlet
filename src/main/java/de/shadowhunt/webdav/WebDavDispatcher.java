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
package de.shadowhunt.webdav;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.webdav.WebDavMethod.Method;
import de.shadowhunt.webdav.precondition.Precondition;

public final class WebDavDispatcher {

    private static final WebDavDispatcher INSTANCE = new WebDavDispatcher();

    public static WebDavDispatcher getInstance() {
        return INSTANCE;
    }

    private final Map<Method, WebDavMethod> dispatcher = new HashMap<>();

    private WebDavDispatcher() {
        for (final WebDavMethod webDavMethod : ServiceLoader.load(WebDavMethod.class)) {
            dispatcher.put(webDavMethod.getMethod(), webDavMethod);
        }
    }

    WebDavPath getResource(final HttpServletRequest request) {
        final String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            return WebDavPath.ROOT;
        }
        return WebDavPath.create(pathInfo);
    }

    public void service(final WebDavStore store, final WebDavRequest request, final WebDavResponse response) throws ServletException, IOException {
        final UUID requestId = response.getRequest().getId();
        if (!request.getId().equals(requestId)) {
            response.setStatus(WebDavResponse.Status.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        final Method method = request.getMethod();
        final WebDavMethod dispatch = dispatcher.get(method);
        if (dispatch == null) {
            response.setStatus(WebDavResponse.Status.SC_NOT_IMPLEMENTED);
            return;
        }

        final WebDavConfig config = request.getConfig();
        if (config.isReadOnly() && !method.isReadOnly()) {
            response.setStatus(WebDavResponse.Status.SC_METHOD_NOT_ALLOWED);
            return;
        }

        try {
            if (!Precondition.verify(store, request)) {
                response.setStatus(WebDavResponse.Status.SC_PRECONDITION_FAILED);
                return;
            }

            final WebDavResponseFoo webDavResponse = dispatch.service(store, request);
            webDavResponse.write(response);
        } catch (final WebDavException e) {
            response.setStatus(e.getStatus());
        }
    }
}
