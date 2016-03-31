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
package de.shadowhunt.webdav.method;

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OptionsMethodTest extends AbstractWebDavMethodTest {

    @Test
    public void test00_missingReadOnly() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(NON_EXISTING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "OPTIONS", response.getHeader(WebDavResponse.ALLOW_HEADER));
    }

    @Test
    public void test00_missingWritable() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(false);

        Mockito.when(request.getPath()).thenReturn(NON_EXISTING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "MKCOL, OPTIONS, PUT", response.getHeader(WebDavResponse.ALLOW_HEADER));
    }

    @Test
    public void test01_existingItemReadOnly() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(EXISTING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "GET, HEAD, OPTIONS, PROPFIND", response.getHeader(WebDavResponse.ALLOW_HEADER));
    }

    @Test
    public void test01_existingItemWritable() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(false);

        Mockito.when(request.getPath()).thenReturn(EXISTING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "COPY, DELETE, GET, HEAD, LOCK, MOVE, OPTIONS, PROPFIND, PROPPATCH, PUT, UNLOCK", response.getHeader(WebDavResponse.ALLOW_HEADER));
    }

    @Test
    public void test02_existingCollectionReadOnly() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(EXISTING_COLLECTION);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "GET, HEAD, OPTIONS, PROPFIND", response.getHeader(WebDavResponse.ALLOW_HEADER));
    }

    @Test
    public void test02_existingCollectionWritable() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(false);

        Mockito.when(request.getPath()).thenReturn(EXISTING_COLLECTION);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "COPY, DELETE, GET, HEAD, LOCK, MOVE, OPTIONS, PROPFIND, PROPPATCH, UNLOCK", response.getHeader(WebDavResponse.ALLOW_HEADER));
    }
}
