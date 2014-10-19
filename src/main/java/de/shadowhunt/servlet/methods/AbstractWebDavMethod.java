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
import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Store;

public abstract class AbstractWebDavMethod {

    private final String method;

    protected final Store store;

    protected AbstractWebDavMethod(final String method, final Store store) {
        this.method = method;
        this.store = store;
    }

    protected boolean consume(final InputStream inputStream) throws IOException {
        final boolean data = (inputStream.read() != -1);
        if (data) {
            while (inputStream.read() != -1) {
                // just deplete inputStream
            }
        }
        return data;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractWebDavMethod)) {
            return false;
        }

        final AbstractWebDavMethod that = (AbstractWebDavMethod) o;

        if (!method.equals(that.method)) {
            return false;
        }

        return true;
    }

    protected boolean hasLockProblem(final Entity entity, final HttpServletRequest request, final String headerName) {
        if (entity.isLocked()) {
            final String token = entity.getLock().getToken();
            final String requestToken = determineLockToken(request, headerName);
            return !token.equals(requestToken);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    public abstract WebDavResponse service(final Path path, final HttpServletRequest request) throws ServletException, IOException;
}
