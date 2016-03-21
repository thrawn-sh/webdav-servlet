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
package de.shadowhunt.litmus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavMethod.Method;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "request")
class XmlRequest implements WebDavRequest {

    @XmlElement(name = "base")
    private String base;

    @XmlTransient
    private WebDavConfig config;

    @XmlElement(name = "content")
    private String content;

    @XmlElementWrapper(name = "headers")
    private List<XmlHeader> headers;

    @XmlTransient
    private UUID id = UUID.randomUUID();

    @XmlElement(name = "method")
    private Method method;

    @XmlElement(name = "uri")
    private String url;

    @Override
    public String getBase() {
        return base;
    }

    @Override
    public WebDavConfig getConfig() {
        return config;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(Base64.decodeBase64(content));
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String getOption(final String name, final String defaultValue) {
        for (final XmlHeader header : headers) {
            if (name.equals(header.getName())) {
                return header.getValue();
            }
        }
        return defaultValue;
    }

    @Override
    public WebDavPath getPath() {
        final int indexOf = url.indexOf(base);
        if (indexOf < 0) {
            throw new WebDavException("base is not part of uri");
        }

        final int start = (indexOf + base.length());
        final String path = url.substring(start);
        return WebDavPath.create(path);
    }

    @Override
    public Optional<Principal> getPrincipal() {
        return Optional.empty(); // FIXME
    }

    private boolean isLocalUri(final URI uri) throws URISyntaxException {
        final URI requestURI = new URI(url);
        if (!requestURI.getScheme().equalsIgnoreCase(uri.getScheme())) {
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

    public void setBase(final String base) {
        this.base = base;
    }

    public void setConfig(final WebDavConfig config) {
        this.config = config;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setMethod(final Method method) {
        this.method = method;
    }

    @Override
    public Optional<WebDavPath> toPath(final String resource) {
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
            // TODO logging
            return Optional.empty();
        }
    }
}
