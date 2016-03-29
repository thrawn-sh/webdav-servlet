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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavDispatcher;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.impl.store.FileSystemStore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class WebDavServlet extends HttpServlet {

    public static final String INFINITE = "infinite";

    public static final String LISTING = "listing";

    public static final String LISTING_CSS = "listingCss";

    private static final long serialVersionUID = 1L;

    public static final String WRITABLE = "writable";

    protected WebDavConfig config;

    protected WebDavStore store;

    protected HttpServletConfig createWebDavConfig(final ServletConfig servletConfig) throws ServletException {
        final HttpServletConfig webdavConfig = new HttpServletConfig();

        final String infiniteParameter = servletConfig.getInitParameter(INFINITE);
        final boolean infinite = Boolean.parseBoolean(infiniteParameter);
        webdavConfig.setAllowInfiniteDepthRequests(infinite);

        final String listingParameter = servletConfig.getInitParameter(LISTING);
        final boolean listing = Boolean.parseBoolean(listingParameter);
        webdavConfig.setShowCollectionListings(listing);

        final String listingCssParameter = servletConfig.getInitParameter(LISTING_CSS);
        if (StringUtils.isNotEmpty(listingCssParameter)) {
            webdavConfig.setCssForCollectionListings(listingCssParameter);
        }

        final String writeableParameter = servletConfig.getInitParameter(WRITABLE);
        final boolean writeable = Boolean.parseBoolean(writeableParameter);
        webdavConfig.setShowCollectionListings(!writeable);

        return webdavConfig;
    }

    protected WebDavRequest createWebDavRequestWrapper(final HttpServletRequest request) throws IOException {
        return new HttpServletRequestWrapper(request, config);
    }

    protected WebDavStore createWebDavStore(final ServletConfig servletConfig) {
        return new FileSystemStore(new File(FileUtils.getTempDirectory(), "webdav-servlet-repo")); // FIXME
    }

    protected WebDavResponse createWenDavResponseWrapper(final HttpServletResponse response, final WebDavRequest webDavRequest) throws IOException {
        return new HttpServletResponseWrapper(response, webDavRequest);
    }

    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        config = createWebDavConfig(servletConfig);
        store = createWebDavStore(servletConfig);
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final WebDavRequest webDavRequest = createWebDavRequestWrapper(request);
        final WebDavResponse webDavResponse = createWenDavResponseWrapper(response, webDavRequest);

        final WebDavDispatcher dispatcher = WebDavDispatcher.getInstance();
        dispatcher.service(store, webDavRequest, webDavResponse);
    }
}
