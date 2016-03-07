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

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForNull;

import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavRequest;

abstract class AbstractWebDavMethod implements WebDavMethod {

    public static final String INFINITY = "infinity";

    protected boolean consume(@CheckForNull final InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return false;
        }

        final boolean data = (inputStream.read() != -1);
        if (data) {
            while (inputStream.read() != -1) {
                // just deplete inputStream
            }
        }
        return data;
    }

    protected int determineDepth(final WebDavRequest request) {
        final String depth = request.getOption("Depth", INFINITY);
        if (INFINITY.equalsIgnoreCase(depth)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(depth);
    }

    @Override
    public final String toString() {
        return "WebDavMethod [method=" + getMethod() + "]";
    }
}
