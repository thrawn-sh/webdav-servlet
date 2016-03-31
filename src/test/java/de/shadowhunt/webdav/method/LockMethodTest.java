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
import java.util.UUID;

import de.shadowhunt.ContentNormalizer;
import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Matchers;
import org.mockito.Mockito;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LockMethodTest extends AbstractWebDavMethodTest {

    private static final ContentNormalizer LOCK_TOKEN_NORMALIZER = new ContentNormalizer() {

        private static final String REGEX = "<D:href>urn:uuid:........-....-....-....-............</D:href>";

        private static final String REPLACEMENT = "<D:href>urn:uuid:00000000-0000-0000-0000-000000000000</D:href>";

        @Override
        public String normalize(final String content) {
            return content.replaceAll(REGEX, REPLACEMENT);
        }

    };

    protected static final WebDavPath LOCKED_ITEM = WebDavPath.create("/locked_item.txt");

    @BeforeClass
    public static void fillStore() {
        createItem(LOCKED_ITEM, "test", true);
    }

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new LockMethod();

        Mockito.when(request.getHeader(Matchers.eq("Timeout"), Matchers.anyString())).thenReturn(LockMethod.INFINITE);
        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_CREATED);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String content = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:prop xmlns:D=\"DAV:\">", //
                "<D:lockdiscovery>", //
                "<D:activelock>", //
                "<D:lockscope><D:exclusive/></D:lockscope>", //
                "<D:locktype><D:write/></D:locktype>", //
                "<D:depth>infinity</D:depth>", //
                "<D:owner></D:owner>", //
                "<D:timeout>Infinite</D:timeout>", //
                "<D:locktoken><D:href>urn:uuid:00000000-0000-0000-0000-000000000000</D:href></D:locktoken>", //
                "<D:lockroot><D:href>/non_exisiting.txt</D:href></D:lockroot>", //
                "</D:activelock>", //
                "</D:lockdiscovery>", //
                "</D:prop>", //
                "\r\n");
        Assert.assertEquals("content must match", content, response.getContent(LOCK_TOKEN_NORMALIZER));
    }

    @Test
    public void test01_exisitingNotLocked() throws Exception {
        final WebDavMethod method = new LockMethod();

        Mockito.when(request.getHeader(Matchers.eq("Timeout"), Matchers.anyString())).thenReturn(LockMethod.INFINITE);
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_OK);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String content = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:prop xmlns:D=\"DAV:\">", //
                "<D:lockdiscovery>", //
                "<D:activelock>", //
                "<D:lockscope><D:exclusive/></D:lockscope>", //
                "<D:locktype><D:write/></D:locktype>", //
                "<D:depth>infinity</D:depth>", //
                "<D:owner></D:owner>", //
                "<D:timeout>Infinite</D:timeout>", //
                "<D:locktoken><D:href>urn:uuid:00000000-0000-0000-0000-000000000000</D:href></D:locktoken>", //
                "<D:lockroot><D:href>/item.txt</D:href></D:lockroot>", //
                "</D:activelock>", //
                "</D:lockdiscovery>", //
                "</D:prop>", //
                "\r\n");
        Assert.assertEquals("content must match", content, response.getContent(LOCK_TOKEN_NORMALIZER));
    }

    @Test
    public void test01_exisitingNotLocked_complete_body() throws Exception {
        final WebDavPath path = WebDavPath.create(UUID.randomUUID() + ".txt");
        final WebDavEntity item = createItem(path, "test", false);
        final WebDavMethod method = new LockMethod();

        final String input = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:lockinfo xmlns:D='DAV:'>", //
                "<D:lockscope><D:exclusive/></D:lockscope>", //
                "<D:locktype><D:write/></D:locktype>", //
                "<D:owner>test</D:owner>", //
                "</D:lockinfo>");

        Mockito.when(request.getHeader(Matchers.eq("Timeout"), Matchers.anyString())).thenReturn("Seconds-3600");
        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream(input.getBytes()));
        Mockito.when(request.getPath()).thenReturn(path);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_OK);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String content = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:prop xmlns:D=\"DAV:\">", //
                "<D:lockdiscovery>", //
                "<D:activelock>", //
                "<D:lockscope><D:exclusive/></D:lockscope>", //
                "<D:locktype><D:write/></D:locktype>", //
                "<D:depth>infinity</D:depth>", //
                "<D:owner>test</D:owner>", //
                "<D:timeout>Infinite</D:timeout>", //
                "<D:locktoken><D:href>urn:uuid:00000000-0000-0000-0000-000000000000</D:href></D:locktoken>", //
                "<D:lockroot><D:href>" + item.getPath() + "</D:href></D:lockroot>", //
                "</D:activelock>", //
                "</D:lockdiscovery>", //
                "</D:prop>", //
                "\r\n");
        Assert.assertEquals("content must match", content, response.getContent(LOCK_TOKEN_NORMALIZER));
    }

    @Test
    public void test01_exisitingNotLocked_incomplete_body() throws Exception {
        final WebDavPath path = WebDavPath.create(UUID.randomUUID() + ".txt");
        final WebDavEntity item = createItem(path, "test", false);
        final WebDavMethod method = new LockMethod();

        final String input = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:lockinfo xmlns:D='DAV:'/>");

        Mockito.when(request.getHeader(Matchers.eq("Timeout"), Matchers.anyString())).thenReturn("Minutes-5");
        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream(input.getBytes()));
        Mockito.when(request.getPath()).thenReturn(path);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_OK);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String content = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:prop xmlns:D=\"DAV:\">", //
                "<D:lockdiscovery>", //
                "<D:activelock>", //
                "<D:lockscope><D:exclusive/></D:lockscope>", //
                "<D:locktype><D:write/></D:locktype>", //
                "<D:depth>infinity</D:depth>", //
                "<D:owner></D:owner>", //
                "<D:timeout>Infinite</D:timeout>", //
                "<D:locktoken><D:href>urn:uuid:00000000-0000-0000-0000-000000000000</D:href></D:locktoken>", //
                "<D:lockroot><D:href>" + item.getPath() + "</D:href></D:lockroot>", //
                "</D:activelock>", //
                "</D:lockdiscovery>", //
                "</D:prop>", //
                "\r\n");
        Assert.assertEquals("content must match", content, response.getContent(LOCK_TOKEN_NORMALIZER));
    }

    @Test
    public void test02_exisitingLocked() throws Exception {
        final WebDavMethod method = new LockMethod();

        Mockito.when(config.isShowCollectionListings()).thenReturn(true);

        Mockito.when(request.getHeader(Matchers.eq("Timeout"), Matchers.anyString())).thenReturn(LockMethod.INFINITE);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_OK);
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String content = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:prop xmlns:D=\"DAV:\">", //
                "<D:lockdiscovery>", //
                "<D:activelock>", //
                "<D:lockscope><D:exclusive/></D:lockscope>", //
                "<D:locktype><D:write/></D:locktype>", //
                "<D:depth>infinity</D:depth>", //
                "<D:owner></D:owner>", //
                "<D:timeout>Infinite</D:timeout>", //
                "<D:locktoken><D:href>urn:uuid:00000000-0000-0000-0000-000000000000</D:href></D:locktoken>", //
                "<D:lockroot><D:href>/locked_item.txt</D:href></D:lockroot>", //
                "</D:activelock>", //
                "</D:lockdiscovery>", //
                "</D:prop>", //
                "\r\n");
        Assert.assertEquals("content must match", content, response.getContent(LOCK_TOKEN_NORMALIZER));
    }
}
