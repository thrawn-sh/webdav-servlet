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

import de.shadowhunt.servlet.webdav.Resource;
import de.shadowhunt.servlet.webdav.Store;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

public abstract class AbstractWebDavMethod {

    private final boolean requiresPrincipal;

    private final String method;

    protected final Store store;

    protected AbstractWebDavMethod(final String method, final boolean requiresPrincipal, final Store store) {
        this.method = method;
        this.requiresPrincipal = requiresPrincipal;
        this.store = store;
    }

    public abstract WebDavResponse service(final Resource resource, final Principal principal, final HttpServletRequest request) throws ServletException, IOException;

    public final boolean isRequiresPrincipal() {
        return requiresPrincipal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractWebDavMethod)) return false;

        final AbstractWebDavMethod that = (AbstractWebDavMethod) o;

        if (!method.equals(that.method)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }
}
