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

        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "OPTIONS", response.getHeader(AbstractBasicResponse.ALLOW_HEADER));
    }

    @Test
    public void test00_missingWritable() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(false);

        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "MKCOL, OPTIONS, PUT", response.getHeader(AbstractBasicResponse.ALLOW_HEADER));
    }

    @Test
    public void test01_exisitingItemReadOnly() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "GET, HEAD, OPTIONS, PROPFIND", response.getHeader(AbstractBasicResponse.ALLOW_HEADER));
    }

    @Test
    public void test01_exisitingItemWritable() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(false);

        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "COPY, DELETE, GET, HEAD, LOCK, MOVE, OPTIONS, PROPFIND, PROPPATCH, PUT, UNLOCK", response.getHeader(AbstractBasicResponse.ALLOW_HEADER));
    }

    @Test
    public void test02_exisitingCollectionReadOnly() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(EXISITING_COLLECTION);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "GET, HEAD, OPTIONS, PROPFIND", response.getHeader(AbstractBasicResponse.ALLOW_HEADER));
    }

    @Test
    public void test02_exisitingCollectionWritable() throws Exception {
        final WebDavMethod method = new OptionsMethod();

        Mockito.when(config.isReadOnly()).thenReturn(false);

        Mockito.when(request.getPath()).thenReturn(EXISITING_COLLECTION);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
        Assert.assertEquals("allow must match", "COPY, DELETE, GET, HEAD, LOCK, MOVE, OPTIONS, PROPFIND, PROPPATCH, UNLOCK", response.getHeader(AbstractBasicResponse.ALLOW_HEADER));
    }
}
