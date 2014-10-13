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
package de.shadowhunt.servlet.webdav;

import javax.servlet.http.HttpServletResponse;

public class WebDavException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int httpStatusCode;

    public WebDavException(final String message) {
        this(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }

    public WebDavException(final String message, final Throwable cause) {
        this(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message, cause);
    }

    public WebDavException(final int httpStatusCode, final String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public WebDavException(final int httpStatusCode, final String message, final Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public final int getHttpStatusCode() {
        return httpStatusCode;
    }
}
