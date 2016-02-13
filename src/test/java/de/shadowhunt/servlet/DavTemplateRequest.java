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

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public final class DavTemplateRequest extends HttpEntityEnclosingRequestBase {

    private final String method;

    public DavTemplateRequest(final String method, final URI uri) {
        this.method = method;
        setURI(uri);
    }

    public DavTemplateRequest(final String method, final URI uri, final String litmusMarker) {
        this(method, uri);
        setHeader("X-Litmus", litmusMarker);
    }

    @Override
    public String getMethod() {
        return method;
    }
}
