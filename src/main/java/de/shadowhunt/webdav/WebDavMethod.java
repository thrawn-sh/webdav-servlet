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

import javax.annotation.concurrent.Immutable;

@Immutable
public interface WebDavMethod {

    enum Method {
        COPY(false), DELETE(false), GET(true), HEAD(true), LOCK(false), MKCOL(false), MOVE(false), OPTIONS(true), PROPFIND(true), PROPPATCH(false), PUT(false), UNLOCK(false);

        private final boolean readOnly;

        Method(final boolean readOnly) {
            this.readOnly = readOnly;
        }

        public boolean isReadOnly() {
            return readOnly;
        }
    }

    Method getMethod();

    WebDavResponseWriter service(WebDavStore store, WebDavRequest request) throws IOException;

}
