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
package de.shadowhunt.servlet.methods;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

final class StatusResponse implements WebDavResponse {

    public static final WebDavResponse CONFLICT = new StatusResponse(HttpServletResponse.SC_CONFLICT);

    public static final WebDavResponse CREATED = new StatusResponse(HttpServletResponse.SC_CREATED);

    public static final WebDavResponse FORBIDDEN = new StatusResponse(HttpServletResponse.SC_FORBIDDEN);

    public static final WebDavResponse METHOD_NOT_ALLOWED = new StatusResponse(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

    public static final WebDavResponse NOT_FOUND = new StatusResponse(HttpServletResponse.SC_NOT_FOUND);

    public static final WebDavResponse NO_CONTENT = new StatusResponse(HttpServletResponse.SC_NO_CONTENT);

    public static final WebDavResponse UNSUPPORTED_MEDIA_TYPE = new StatusResponse(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);

    public static final WebDavResponse PRECONDITION_FAILED = new StatusResponse(HttpServletResponse.SC_PRECONDITION_FAILED);

    private final int httpStatus;

    private StatusResponse(final int httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public void write(final HttpServletResponse response) throws ServletException, IOException {
        response.sendError(httpStatus);
    }
}
