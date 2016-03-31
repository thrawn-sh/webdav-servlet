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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.method.WebDavMethod.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HttpServletRequestWrapper implements WebDavRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServletRequestWrapper.class);

    private final WebDavConfig config;

    private final UUID id;

    private final HttpServletRequest request;

    HttpServletRequestWrapper(final HttpServletRequest request, final WebDavConfig config) {
        this.request = request;
        this.config = config;
        this.id = UUID.randomUUID();
    }

    @Override
    public String getBase() {
        return request.getServletPath();
    }

    @Override
    public WebDavConfig getConfig() {
        return config;
    }

    @Override
    public String getHeader(final String name, final String defaultValue) {
        final String header = request.getHeader(name);
        if (StringUtils.isBlank(header)) {
            return defaultValue;
        }
        return header;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public Method getMethod() {
        final String method = request.getMethod().toUpperCase(Locale.US);
        return Method.valueOf(method);
    }

    @Override
    public WebDavPath getPath() {
        final String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            return WebDavPath.ROOT;
        }
        return WebDavPath.create(pathInfo);
    }

    @Override
    public Optional<Principal> getPrincipal() {
        return Optional.ofNullable(request.getUserPrincipal());
    }

    private boolean isLocalUri(final URI uri) throws URISyntaxException {
        final URI requestURI = new URI(request.getRequestURL().toString());
        if (!request.getScheme().equalsIgnoreCase(uri.getScheme())) {
            return false;
        }

        if (!requestURI.getHost().equalsIgnoreCase(uri.getHost())) {
            return false;
        }

        if (requestURI.getPort() != uri.getPort()) {
            return false;
        }

        if (!uri.getPath().startsWith(getBase())) {
            return false;
        }
        return true;
    }

    @Override
    public Optional<WebDavPath> toPath(final String resource) {
        final String base = getBase();
        if (resource.startsWith("/")) {
            final String relativePath = StringUtils.removeStart(resource, base);
            return Optional.of(WebDavPath.create(relativePath));
        }

        try {
            final URI uri = new URI(resource);
            if (!isLocalUri(uri)) {
                return Optional.empty();
            }

            final String path = uri.getPath();
            final String relativePath = StringUtils.removeStart(path, base);
            return Optional.of(WebDavPath.create(relativePath));
        } catch (final URISyntaxException e) {
            LOGGER.warn("illegal resource uri '" + resource + "'", e);
            return Optional.empty();
        }
    }

}
