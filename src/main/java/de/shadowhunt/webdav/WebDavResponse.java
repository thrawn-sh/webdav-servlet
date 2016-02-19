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

public interface WebDavResponse {

    enum Status {

        SC_BAD_REQUEST(0), //
        SC_CONFLICT(0), //
        SC_CREATED(0), //
        SC_FORBIDDEN(403), //
        SC_INTERNAL_SERVER_ERROR(500), //
        SC_LOCKED(423), //
        SC_METHOD_NOT_ALLOWED(0), //
        SC_MULTISTATUS(207), //
        SC_NO_CONTENT(0), //
        SC_NOT_FOUND(404), //
        SC_NOT_IMPLEMENTED(0), //
        SC_OK(200), //
        SC_PRECONDITION_FAILED(0), //
        SC_UNSUPPORTED_MEDIA_TYPE(0);

        public final int value;

        Status(final int value) {
            this.value = value;
        }
    }

    void addHeader(final String name, final String value);

    OutputStream getOutputStream() throws IOException;

    WebDavRequest getRequest();

    void setCharacterEncoding(String charset);

    void setContentType(String contentType);

    void setStatus(Status status);
}
