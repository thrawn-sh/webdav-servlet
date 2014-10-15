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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import de.shadowhunt.servlet.methods.AbstractWebDavMethod;
import de.shadowhunt.servlet.methods.CopyMoveMethod;
import de.shadowhunt.servlet.methods.DeleteMethod;
import de.shadowhunt.servlet.methods.GetMethod;
import de.shadowhunt.servlet.methods.MkColMethod;
import de.shadowhunt.servlet.methods.OptionsMethod;
import de.shadowhunt.servlet.methods.PutMethod;
import de.shadowhunt.servlet.methods.WebDavResponse;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Store;
import de.shadowhunt.servlet.webdav.WebDavException;
import de.shadowhunt.servlet.webdav.internal.FileSystemStore;

public class WebDavServlet extends HttpServlet {

    private final Map<String, AbstractWebDavMethod> dispatcher = new HashMap<>();

    @Override
    public void destroy() {
        dispatcher.clear();
        super.destroy();
    }

    protected Path getResource(final HttpServletRequest request) {
        final String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            return Path.ROOT;
        }
        return Path.create(pathInfo);
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        final File root = new File(FileUtils.getTempDirectory(), "webdav");
        FileUtils.deleteQuietly(root);

        final Store store = new FileSystemStore(root);

        final boolean writeable = Boolean.parseBoolean(config.getInitParameter("writeable"));

        final String listingCss = config.getInitParameter("listingCss");
        final boolean listing = Boolean.parseBoolean(config.getInitParameter("listing"));
        dispatcher.put(GetMethod.METHOD, new GetMethod(store, listing, listingCss));
        dispatcher.put(OptionsMethod.METHOD, new OptionsMethod(store));

        if (writeable) {
            dispatcher.put(DeleteMethod.METHOD, new DeleteMethod(store));
            dispatcher.put(MkColMethod.METHOD, new MkColMethod(store));
            dispatcher.put(PutMethod.METHOD, new PutMethod(store));
            dispatcher.put(CopyMoveMethod.COPY_METHOD, new CopyMoveMethod(store, false));
            dispatcher.put(CopyMoveMethod.MOVE_METHOD, new CopyMoveMethod(store, true));
        }
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String method = request.getMethod();
        final AbstractWebDavMethod dispatch = dispatcher.get(method);
        if (dispatch == null) {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }

        try {
            final Path path = getResource(request);
            final WebDavResponse webDavResponse = dispatch.service(path, request);
            webDavResponse.write(response);
        } catch (final WebDavException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
