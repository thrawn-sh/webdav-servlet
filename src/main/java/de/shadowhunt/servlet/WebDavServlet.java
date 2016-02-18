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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.webdav.WebDavDispatcher;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.impl.store.FileSystemStore;

import org.apache.commons.io.FileUtils;

public class WebDavServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final HttpServletConfig config = new HttpServletConfig();
        config.setAllowInfiniteDepthRequests(true);
        config.setReadOnly(false);
        config.setShowCollectionListings(true);

        final WebDavStore store = new FileSystemStore(FileUtils.getTempDirectory());
        final WebDavRequest webDavRequest = new HttpServletRequestWrapper(request, config);
        final WebDavResponse webDavResponse = new HttpServletResponseWrapper(response, webDavRequest);

        final WebDavDispatcher dispatcher = WebDavDispatcher.getInstance();
        dispatcher.service(store, webDavRequest, webDavResponse);
    }
}
