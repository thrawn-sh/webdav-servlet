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
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;

public class HttpServletResponseWrapper implements WebDavResponse {

    private final WebDavRequest request;

    private final HttpServletResponse response;

    public HttpServletResponseWrapper(final HttpServletResponse response, final WebDavRequest request) {
        this.response = response;
        this.request = request;
    }

    @Override
    public void addHeader(final String name, final String value) {
        response.addHeader(name, value);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public WebDavRequest getRequest() {
        return request;
    }

    @Override
    public void setCharacterEncoding(final String charset) {
        response.setCharacterEncoding(charset);
    }

    @Override
    public void setContentType(final String contentType) {
        response.setContentType(contentType);
    }

    @Override
    public void setStatus(final Status status) {
        response.setStatus(status.value);
    }

}
