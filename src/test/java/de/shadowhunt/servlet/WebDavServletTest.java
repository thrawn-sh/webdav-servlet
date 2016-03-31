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

import java.io.ByteArrayInputStream;
import java.util.Optional;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class WebDavServletTest {

    @Test
    public void test_createWebDavConfig_empty() throws Exception {
        final WebDavServlet servlet = new WebDavServlet();

        final ServletConfig servletConfig = Mockito.mock(ServletConfig.class);

        final HttpServletConfig config = servlet.createWebDavConfig(servletConfig);
        Assert.assertNotNull("config must not be null", config);

        Assert.assertFalse("infinity default must be false", config.isAllowInfiniteDepthRequests());
        Assert.assertTrue("readOnly default must be false", config.isReadOnly());
        Assert.assertFalse("show collection listing default must be false", config.isShowCollectionListings());
        Assert.assertEquals("css must match", Optional.empty(), config.getCssForCollectionListings());
    }

    @Test
    public void test_createWebDavConfig_emptyCssResource() throws Exception {
        final WebDavServlet servlet = new WebDavServlet();

        final ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
        Mockito.when(servletConfig.getInitParameter(WebDavServlet.LISTING_CSS)).thenReturn("/WEB-INF/test");
        final ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(servletContext.getResourceAsStream(Matchers.anyString())).thenReturn(new ByteArrayInputStream("".getBytes()));
        Mockito.when(servletConfig.getServletContext()).thenReturn(servletContext);

        final HttpServletConfig config = servlet.createWebDavConfig(servletConfig);
        Assert.assertNotNull("config must not be null", config);
        Assert.assertEquals("css must match", Optional.empty(), config.getCssForCollectionListings());
    }

    @Test(expected = ServletException.class)
    public void test_createWebDavConfig_missingCssResource() throws Exception {
        final WebDavServlet servlet = new WebDavServlet();

        final ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
        Mockito.when(servletConfig.getInitParameter(WebDavServlet.LISTING_CSS)).thenReturn("/WEB-INF/test");
        final ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(servletContext.getResourceAsStream(Matchers.anyString())).thenReturn(null);
        Mockito.when(servletConfig.getServletContext()).thenReturn(servletContext);

        servlet.createWebDavConfig(servletConfig);
        Assert.fail("must not complete");
    }

    @Test
    public void test_createWebDavConfig_values() throws Exception {
        final WebDavServlet servlet = new WebDavServlet();

        final ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
        Mockito.when(servletConfig.getInitParameter(WebDavServlet.INFINITE)).thenReturn("true");
        Mockito.when(servletConfig.getInitParameter(WebDavServlet.LISTING)).thenReturn("true");
        Mockito.when(servletConfig.getInitParameter(WebDavServlet.LISTING_CSS)).thenReturn("/WEB-INF/test");
        Mockito.when(servletConfig.getInitParameter(WebDavServlet.WRITEABLE)).thenReturn("true");
        final ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(servletContext.getResourceAsStream(Matchers.anyString())).thenReturn(new ByteArrayInputStream("test data".getBytes()));
        Mockito.when(servletConfig.getServletContext()).thenReturn(servletContext);

        final HttpServletConfig config = servlet.createWebDavConfig(servletConfig);
        Assert.assertNotNull("config must not be null", config);

        Assert.assertTrue("infinity must be true", config.isAllowInfiniteDepthRequests());
        Assert.assertFalse("readOnly must be false", config.isReadOnly());
        Assert.assertTrue("show collection listing  must be true", config.isShowCollectionListings());
        Assert.assertEquals("css must match", Optional.of("test data"), config.getCssForCollectionListings());
    }
}
