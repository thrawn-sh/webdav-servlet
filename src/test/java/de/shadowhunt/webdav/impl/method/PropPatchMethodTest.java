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
import java.nio.charset.StandardCharsets;

import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PropPatchMethodTest extends AbstractWebDavMethodTest {

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new PropPatchMethod();

        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test01_missing_request_body() throws Exception {
        final WebDavMethod method = new PropPatchMethod();

        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_BAD_REQUEST, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test02_add_new_property() throws Exception {
        final WebDavMethod method = new PropPatchMethod();

        final String input = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<propertyupdate xmlns=\"DAV:\" xmlns:t=\"test\">", //
                "<set>", //
                "<prop>", //
                "<t:test>test_content</t:test>", //
                "</prop>", //
                "</set>", //
                "</propertyupdate>");

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test03_update_property() throws Exception {
        final WebDavMethod method = new PropPatchMethod();

        final String input = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<propertyupdate xmlns=\"DAV:\" xmlns:f=\"foo\">", //
                "<set>", //
                "<prop>", //
                "<f:foo>foo_foo_content_update</f:foo>", //
                "</prop>", //
                "</set>", //
                "</propertyupdate>");

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test04_delete_property() throws Exception {
        final WebDavMethod method = new PropPatchMethod();

        final String input = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<propertyupdate xmlns=\"DAV:\" xmlns:f=\"foo\">", //
                "<remove>", //
                "<prop>", //
                "<f:foo/>", //
                "</prop>", //
                "</remove>", //
                "</propertyupdate>");

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test05_delete_dav_property() throws Exception {
        final WebDavMethod method = new PropPatchMethod();

        final String input = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<propertyupdate xmlns=\"DAV:\">", //
                "<remove>", //
                "<prop>", //
                "<foo/>", //
                "</prop>", //
                "</remove>", //
                "</propertyupdate>");

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
        assertNoContent(response);
    }
}
