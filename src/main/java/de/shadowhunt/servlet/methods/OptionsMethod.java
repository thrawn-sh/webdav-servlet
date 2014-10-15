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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Resource;
import de.shadowhunt.servlet.webdav.Store;

public class OptionsMethod extends AbstractWebDavMethod {

    public static final String METHOD = "OPTIONS";

    public OptionsMethod(final Store store) {
        super(METHOD, store);
    }

    @Override
    public WebDavResponse service(final Resource resource, final HttpServletRequest request) throws ServletException, IOException {
        Entity entity = null;
        if (store.exists(resource)) {
            entity = store.info(resource);
        }

        final String allowedMethods = getAllowedMethods(entity);
        return new WebDavResponse() {

            @Override
            public void write(final HttpServletResponse response) throws ServletException, IOException {
                response.addHeader("DAV", "1, 2");
                response.addHeader("Allow", allowedMethods);
                response.addHeader("MS-Author-Via", "DAV");
            }
        };
    }
}
