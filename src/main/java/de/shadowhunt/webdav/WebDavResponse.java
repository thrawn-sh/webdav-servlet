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
import java.io.OutputStream;

import de.shadowhunt.webdav.WebDavConstant.Header;
import de.shadowhunt.webdav.WebDavConstant.Status;

public interface WebDavResponse {

    void addHeader(final Header header, final String value);

    OutputStream getOutputStream() throws IOException;

    WebDavRequest getRequest();

    void setCharacterEncoding(String charset);

    void setContentType(String contentType);

    void setStatus(Status status);
}
