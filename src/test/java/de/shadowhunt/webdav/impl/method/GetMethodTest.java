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

import java.util.Optional;

import de.shadowhunt.ContentNormalizer;
import de.shadowhunt.TestResponse;
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
public class GetMethodTest extends AbstractWebDavMethodTest {

    //
    private static final ContentNormalizer LAST_MODIFIED_NORMALIZER = new ContentNormalizer() {

        private static final String REGEX = "<td class=\"modified\">\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d</td>";

        private static final String REPLACEMENT = "<td class=\"modified\">1970-01-01 01:00:00</td>";

        @Override
        public String normalize(final String content) {
            return content.replaceAll(REGEX, REPLACEMENT);
        }

    };

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new GetMethod();

        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NOT_FOUND);
    }

    @Test
    public void test01_exisitingItem() throws Exception {
        final WebDavMethod method = new GetMethod();

        final String content = "example";

        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_OK);
        // Assert.assertEquals("contentType must match", "", response.getContentType()); // FIXME
        Assert.assertNull("characterEncoding must be null", response.getCharacterEncoding());
        Assert.assertEquals("content must match", content, response.getContent());
    }

    @Test
    public void test02_exisitingCollectionListing() throws Exception {
        final WebDavMethod method = new GetMethod();

        Mockito.when(config.getCssForCollectionListings()).thenReturn(Optional.empty());
        Mockito.when(config.isShowCollectionListings()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(WebDavPath.ROOT);

        final TestResponse response = execute(method);
        assertBasicRequirements(response, Status.SC_OK);
        Assert.assertEquals("contentType must match", "text/html", response.getContentType());
        Assert.assertEquals("characterEncoding must match", AbstractBasicResponse.DEFAULT_ENCODING, response.getCharacterEncoding());
        final String expected = concat("<!DOCTYPE html>", //
                "<html>", //
                "<head>", //
                "<title>Content of folder </title>", //
                "</head>", //
                "<body>", //
                "<table>", //
                "<thead>", //
                "<tr>", //
                "<th class=\"name\">Name</th>", //
                "<th class=\"size\">Size</th>", //
                "<th class=\"modified\">Modified</th>", //
                "</tr>", //
                "</thead>", //
                "<tbody>", //
                "<tr class=\"folder parent\">", //
                "<td colspan=\"3\">", //
                "<a href=\".\">Parent</a>", //
                "</td>", //
                "</tr>", //
                "<tr class=\"folder\">", //
                "<td colspan=\"3\">", //
                "<a href=\"./collection/\">collection</a>", //
                "</td>", //
                "</tr>", //
                "<tr class=\"file\">", //
                "<td class=\"name\">", //
                "<a href=\"./item.txt\">item.txt</a>", //
                "</td>", //
                "<td class=\"size\">7 bytes</td>", //
                "<td class=\"modified\">1970-01-01 01:00:00</td>", //
                "</tr>", //
                "</tbody>", //
                "</table>", //
                "</body>", //
                "</html>");
        Assert.assertEquals("content must match", expected, response.getContent(LAST_MODIFIED_NORMALIZER));
    }

    @Test
    public void test02_exisitingCollectionNoListing() throws Exception {
        final WebDavMethod method = new GetMethod();

        Mockito.when(config.isShowCollectionListings()).thenReturn(false);

        Mockito.when(request.getPath()).thenReturn(EXISITING_COLLECTION);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_FORBIDDEN);
    }
}
