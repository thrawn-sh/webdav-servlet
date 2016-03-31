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

import de.shadowhunt.webdav.method.WebDavMethod;
import de.shadowhunt.webdav.method.WebDavMethod.Method;
import de.shadowhunt.webdav.precondition.Precondition;
import de.shadowhunt.webdav.store.WebDavStore;
import de.shadowhunt.webdav.store.WebDavStore.Access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WebDavDispatcher {

    private static final WebDavDispatcher INSTANCE = new WebDavDispatcher();

    private static final Logger LOGGER = LoggerFactory.getLogger(WebDavDispatcher.class);

    public static WebDavDispatcher getInstance() {
        return INSTANCE;
    }

    private final Map<Method, WebDavMethod> dispatcher = new HashMap<>();

    private WebDavDispatcher() {
        for (final WebDavMethod webDavMethod : ServiceLoader.load(WebDavMethod.class)) {
            dispatcher.put(webDavMethod.getMethod(), webDavMethod);
        }
    }

    void checkAuthorization(final WebDavMethod method, final WebDavStore store, final WebDavRequest request) {
        final Access access = store.grantAccess(method, request.getPath(), request.getPrincipal());
        if (access == Access.DENY) {
            throw new WebDavException("user not authorized", WebDavResponse.Status.SC_FORBIDDEN);
        }

        if (access == Access.REQUIRE_AUTHENTICATION) {
            throw new WebDavException("user must authorized", WebDavResponse.Status.SC_UNAUTHORIZED);
        }
    }

    WebDavMethod determineWebDavMethod(final WebDavRequest request) {
        final Method method = request.getMethod();
        return dispatcher.get(method);
    }

    public void service(final WebDavStore store, final WebDavRequest request, final WebDavResponse response) throws IOException {
        try {
            final WebDavMethod method = determineWebDavMethod(request);

            checkAuthorization(method, store, request);

            verifyMethod(method.getMethod(), request.getConfig());

            verifyPrecondition(store, request);

            verifyConsistency(request, response);

            final WebDavResponseWriter webDavResponse = method.service(store, request);
            webDavResponse.write(response);
        } catch (final WebDavException e) {
            LOGGER.warn("request failed", e);
            response.setStatus(e.getStatus());
        } catch (final RuntimeException e) {
            LOGGER.warn("request failed", e);
            response.setStatus(WebDavResponse.Status.SC_INTERNAL_SERVER_ERROR);
        }
    }

    void verifyConsistency(final WebDavRequest request, final WebDavResponse response) {
        final UUID requestId = response.getRequest().getId();
        if (!request.getId().equals(requestId)) {
            throw new WebDavException("response does not belong to the request", WebDavResponse.Status.SC_INTERNAL_SERVER_ERROR);
        }
    }

    void verifyMethod(final Method method, final WebDavConfig config) {
        if (config.isReadOnly() && !method.isReadOnly()) {
            throw new WebDavException("store is read-only", WebDavResponse.Status.SC_METHOD_NOT_ALLOWED);
        }
    }

    void verifyPrecondition(final WebDavStore store, final WebDavRequest request) {
        if (!Precondition.verify(store, request)) {
            throw new WebDavException("precondition not satisfaid", WebDavResponse.Status.SC_PRECONDITION_FAILED);
        }
    }
}
