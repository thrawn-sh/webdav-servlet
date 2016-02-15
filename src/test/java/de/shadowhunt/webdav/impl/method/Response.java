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
package de.shadowhunt.webdav.impl.method;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

public class Response {

    private final StringBuilder content = new StringBuilder();

    private final Collection<Header> headers = new ArrayList<>();

    private int status = -1;

    public void addHeader(final String name, final String value) {
        headers.add(new Header(name, value));
    }

    public String getContent() {
        final String result = content.toString();
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        return result;
    }

    public Collection<Header> getHeaders() {
        return headers;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public void writeContent(final int character) {
        content.append((char) character);
    }
}
