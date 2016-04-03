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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavDispatcher;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.store.WebDavStore;
import de.shadowhunt.webdav.store.filesystem.FileSystemStore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class WebDavServlet extends HttpServlet {

    public static final String INFINITE = "infinite";

    public static final String LISTING = "listing";

    public static final String LISTING_CSS = "listingCss";

    private static final long serialVersionUID = 1L;

    public static final String WRITEABLE = "writeable";

    private transient WebDavConfig config;

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
            final ServletContext servletContext = servletConfig.getServletContext();
            final InputStream cssStream = servletContext.getResourceAsStream(listingCssParameter);
            if (cssStream == null) {
                throw new ServletException("could not find resource: " + listingCssParameter);
            } else {
                try {
                    final String cssData = IOUtils.toString(cssStream, StandardCharsets.UTF_8);
                    final String css = StringUtils.trimToNull(cssData);
                    if (css != null) {
                        webdavConfig.setCssForCollectionListings(css);
                    }
                } catch (final IOException e) {
                    throw new ServletException("could not set css for collections", e);
                } finally {
                    IOUtils.closeQuietly(cssStream);
                }
            }
        }

        final String writeableParameter = servletConfig.getInitParameter(WRITEABLE);
        final boolean writeable = Boolean.parseBoolean(writeableParameter);
        webdavConfig.setReadOnly(!writeable);

        return webdavConfig;
    }

    protected WebDavRequest createWebDavRequestWrapper(final HttpServletRequest request) throws IOException {
        return new HttpServletRequestWrapper(request, getWebDavConfig());
    }

    protected WebDavResponse createWebDavResponseWrapper(final HttpServletResponse response, final WebDavRequest webDavRequest) throws IOException {
        return new HttpServletResponseWrapper(response, webDavRequest);
    }

    protected WebDavConfig getWebDavConfig() {
        return config;
    }

    protected WebDavStore getWebDavStore() {
        return new FileSystemStore(new File(FileUtils.getTempDirectory(), "webdav-servlet-repo")); // FIXME
    }

    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        config = createWebDavConfig(servletConfig);
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final WebDavRequest webDavRequest = createWebDavRequestWrapper(request);
        final WebDavResponse webDavResponse = createWebDavResponseWrapper(response, webDavRequest);

        final WebDavDispatcher dispatcher = WebDavDispatcher.getInstance();
        dispatcher.service(getWebDavStore(), webDavRequest, webDavResponse);
    }
}
