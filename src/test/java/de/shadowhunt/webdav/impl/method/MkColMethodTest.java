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
package de.shadowhunt.webdav.impl.method;

import java.io.ByteArrayInputStream;

import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MkColMethodTest extends AbstractWebDavMethodTest {

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new MkColMethod();

        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test01_missingParent() throws Exception {
        final WebDavMethod method = new MkColMethod();

        final WebDavPath path = WebDavPath.create("/parent/collection/");

        Mockito.when(request.getPath()).thenReturn(path);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_CONFLICT, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test02_exisitingItem() throws Exception {
        final WebDavMethod method = new MkColMethod();

        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_METHOD_NOT_ALLOWED, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test03_exisitingCollection() throws Exception {
        final WebDavMethod method = new MkColMethod();

        Mockito.when(request.getPath()).thenReturn(EXISITING_COLLECTION);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_METHOD_NOT_ALLOWED, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test04_withContent() throws Exception {
        final WebDavMethod method = new MkColMethod();

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        Mockito.when(request.getPath()).thenReturn(EXISITING_COLLECTION);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
        assertNoContent(response);
    }
}
