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

public class WebDavException extends RuntimeException {

    public static final int DEFAULT_HTTP_STATUS = 500;

    private static final long serialVersionUID = 1L;

    private final int httpStatus;

    public WebDavException(final String message) {
        this(message, DEFAULT_HTTP_STATUS);
    }

    public WebDavException(final String message, final int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public WebDavException(final String message, final Throwable cause) {
        this(message, cause, DEFAULT_HTTP_STATUS);
    }

    public WebDavException(final String message, final Throwable cause, final int httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
