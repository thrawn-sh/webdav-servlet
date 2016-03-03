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

import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LockMethodTest extends AbstractWebDavMethodTest {

    private static final Normalizer LOCK_TOKEN_NORMALIZER = new Normalizer() {

        private static final String REGEX = "<D:href>opaquelocktoken:........-....-....-....-............</D:href>";

        private static final String REPLACEMENT = "<D:href>opaquelocktoken:00000000-0000-0000-0000-000000000000</D:href>";

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

        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, response.getStatus());
        assertNoContent(response);
    }

    @Test
    public void test01_exisitingNotLocked() throws Exception {
        final WebDavMethod method = new LockMethod();

        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", UTF_8, response.getCharacterEncoding());
        final String content = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:prop xmlns:D=\"DAV:\">", //
                "<D:lockdiscovery>", //
                "<D:activelock>", //
                "<D:lockscope>", //
                "<D:exclusive/>", //
                "</D:lockscope>", //
                "<D:locktype>", //
                "<D:write/>", //
                "</D:locktype>", //
                "<D:timeout>Seconds-3600</D:timeout>", //
                "<D:depth>0</D:depth>", //
                "<D:owner>", //
                "</D:owner>", //
                "<D:locktoken>", //
                "<D:href>opaquelocktoken:00000000-0000-0000-0000-000000000000</D:href>", //
                "</D:locktoken>", //
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

        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        Assert.assertEquals("characterEncoding must match", UTF_8, response.getCharacterEncoding());
        final String content = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:prop xmlns:D=\"DAV:\">", //
                "<D:lockdiscovery>", //
                "<D:activelock>", //
                "<D:lockscope>", //
                "<D:exclusive/>", //
                "</D:lockscope>", //
                "<D:locktype>", //
                "<D:write/>", //
                "</D:locktype>", //
                "<D:timeout>Seconds-3600</D:timeout>", //
                "<D:depth>0</D:depth>", //
                "<D:owner>", //
                "</D:owner>", //
                "<D:locktoken>", //
                "<D:href>opaquelocktoken:00000000-0000-0000-0000-000000000000</D:href>", //
                "</D:locktoken>", //
                "</D:activelock>", //
                "</D:lockdiscovery>", //
                "</D:prop>", //
                "\r\n");
        Assert.assertEquals("content must match", content, response.getContent(LOCK_TOKEN_NORMALIZER));
    }
}
