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
import java.io.InputStream;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.webdav.method.WebDavMethod.Method;

public interface WebDavRequest {

    String getBase();

    WebDavConfig getConfig();

    String getHeader(String name, String defaultValue);

    UUID getId();

    InputStream getInputStream() throws IOException;

    Method getMethod();

    WebDavPath getPath();

    Optional<Principal> getPrincipal();

    Optional<WebDavPath> toPath(String resource);
}
