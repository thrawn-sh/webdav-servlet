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
package de.shadowhunt.servlet.aaa.impl.method;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.servlet.aaa.WebDavMethod;

import org.apache.commons.lang3.StringUtils;

abstract class AbstractWebDavMethod implements WebDavMethod {

    protected boolean consume(final InputStream inputStream) throws IOException {
        final boolean data = (inputStream.read() != -1);
        if (data) {
            while (inputStream.read() != -1) {
                // just deplete inputStream
            }
        }
        return data;
    }

    protected int determineDepth(final HttpServletRequest request) {
        final String depth = request.getHeader("Depth");
        if (StringUtils.isEmpty(depth) || "infinity".equalsIgnoreCase(depth)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(depth);
    }

    @CheckForNull
    protected String determineLockToken(final HttpServletRequest request, final String headerName) {
        final String tokenHeader = request.getHeader(headerName);
        if (tokenHeader != null) {
            final int index = Math.max(0, tokenHeader.indexOf("opaquelocktoken:"));
            final String token = tokenHeader.substring(index);
            return StringUtils.replaceChars(token, "(<>)", null);
        }
        return null;
    }
}
