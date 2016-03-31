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

import java.io.ByteArrayInputStream;

import de.shadowhunt.CombinedNormalizer;
import de.shadowhunt.ContentNormalizer;
import de.shadowhunt.EtagNormalizer;
import de.shadowhunt.LastModifiedNormalizer;
import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Matchers;
import org.mockito.Mockito;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PropFindMethodTest extends AbstractWebDavMethodTest {

    private static final ContentNormalizer NORMALIZER = new CombinedNormalizer(new LastModifiedNormalizer(), new EtagNormalizer());

    @BeforeClass
    public static void fillStore() {
        createItem(EXISITING_COLLECTION.append(WebDavPath.create("/item.txt")), "test", true);
        createCollection(EXISITING_COLLECTION.append(WebDavPath.create("/level1/level2")), false);
    }

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NOT_FOUND);
    }

    @Test
    public void test01_infinity_not_supported() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(request.getHeader(Matchers.eq("Depth"), Matchers.anyString())).thenReturn(AbstractWebDavMethod.INFINITY);
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_FORBIDDEN);
    }

    @Test
    public void test02_broken_request_body() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(request.getHeader(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><bad:a xmlns:bad=\"foo:\"/>".getBytes()));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_BAD_REQUEST);
    }

    @Test
    public void test02_missing_request_body() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(request.getHeader(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_BAD_REQUEST);
    }

    @Test
    public void test03_no_properties() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(config.isAllowInfiniteDepthRequests()).thenReturn(true);

        Mockito.when(request.getHeader(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"/>".getBytes()));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_BAD_REQUEST);
    }

    @Test
    public void test04_missing_selected_properties() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(config.isAllowInfiniteDepthRequests()).thenReturn(true);

        Mockito.when(request.getHeader(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><prop xmlns:t=\"missing\"><t:foo/></prop></propfind>".getBytes()));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_MULTISTATUS);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"bar\" xmlns:ns2=\"foo\" xmlns:ns3=\"missing\">", //
                "<D:response>", //
                "<D:href>/webdav/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns3:foo/>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 404 Not Found</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent());
    }

    @Test
    public void test05_selected_properties() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(request.getHeader(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><prop xmlns:t=\"foo\"><t:foo/></prop></propfind>".getBytes()));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_MULTISTATUS);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"bar\" xmlns:ns2=\"foo\">", //
                "<D:response>", //
                "<D:href>/webdav/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns2:foo>foo_foo_content</ns2:foo>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent());
    }

    @Test
    public void test06_all_properties() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(request.getHeader(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><allprop/></propfind>".getBytes()));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_MULTISTATUS);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"bar\" xmlns:ns2=\"foo\">", //
                "<D:response>", //
                "<D:href>/webdav/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname>item.txt</D:displayname>", //
                "<D:getcontentlength>7</D:getcontentlength>", //
                "<D:getetag>0000000000</D:getetag>", //
                "<D:getlastmodified>Thu, 01 Jan 1970 01:00:00 +0100</D:getlastmodified>", //
                "<D:supportedlock><D:lockentry><D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry></D:supportedlock>", //
                "<ns1:foo>bar_foo_content</ns1:foo>", //
                "<ns2:bar>foo_bar_content</ns2:bar>", //
                "<ns2:foo>foo_foo_content</ns2:foo>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test07_property_names() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(request.getHeader(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><propname/></propfind>".getBytes()));
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_MULTISTATUS);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"bar\" xmlns:ns2=\"foo\">", //
                "<D:response>", //
                "<D:href>/webdav/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname/>", //
                "<D:getcontentlength/>", //
                "<D:getetag/>", //
                "<D:getlastmodified/>", //
                "<D:resourcetype/>", //
                "<D:supportedlock/>", //
                "<ns1:foo/>", //
                "<ns2:bar/>", //
                "<ns2:foo/>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent());
    }

    @Test
    public void test08_all_collection() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        Mockito.when(request.getHeader(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("1");
        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><allprop/></propfind>".getBytes()));
        Mockito.when(request.getPath()).thenReturn(EXISITING_COLLECTION);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_MULTISTATUS);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"bar\" xmlns:ns2=\"foo\">", //
                "<D:response>", //
                "<D:href>/webdav/collection</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname>collection</D:displayname>", //
                "<D:getcontentlength>0</D:getcontentlength>", //
                "<D:getlastmodified>Thu, 01 Jan 1970 01:00:00 +0100</D:getlastmodified>", //
                "<D:resourcetype>", //
                "<D:collection/>", //
                "</D:resourcetype>", //
                "<ns1:foo>bar_foo_content</ns1:foo>", //
                "<ns2:bar>foo_bar_content</ns2:bar>", //
                "<ns2:foo>foo_foo_content</ns2:foo>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "<D:response>", //
                "<D:href>/webdav/collection/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname>item.txt</D:displayname>", //
                "<D:getcontentlength>4</D:getcontentlength>", //
                "<D:getetag>0000000000</D:getetag>", //
                "<D:getlastmodified>Thu, 01 Jan 1970 01:00:00 +0100</D:getlastmodified>", //
                "<D:supportedlock>", //
                "<D:lockentry>", //
                "<D:lockscope>", //
                "<D:exclusive/>", //
                "</D:lockscope>", //
                "<D:locktype>", //
                "<D:write/>", //
                "</D:locktype>", //
                "</D:lockentry>", //
                "</D:supportedlock>", //
                "<ns1:foo>bar_foo_content</ns1:foo>", //
                "<ns2:bar>foo_bar_content</ns2:bar>", //
                "<ns2:foo>foo_foo_content</ns2:foo>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "<D:response>", //
                "<D:href>/webdav/collection/level1</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname>level1</D:displayname>", //
                "<D:getcontentlength>0</D:getcontentlength>", //
                "<D:getlastmodified>Thu, 01 Jan 1970 01:00:00 +0100</D:getlastmodified>", //
                "<D:resourcetype>", //
                "<D:collection/>", //
                "</D:resourcetype>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }
}
