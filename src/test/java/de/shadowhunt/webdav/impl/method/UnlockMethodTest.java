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
public class UnlockMethodTest extends AbstractWebDavMethodTest {

    protected static final WebDavPath LOCKED_ITEM = WebDavPath.create("/locked_item.txt");

    @BeforeClass
    public static void fillStore() {
        createItem(LOCKED_ITEM, "test", true);
    }

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new UnlockMethod();

        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, response.getStatus());
        Assert.assertNull("contentType must be null", response.getContentType());
        Assert.assertNull("content must be null", response.getContent());
        Assert.assertNull("characterEncoding must be null", response.getCharacterEncoding());
    }

    @Test
    public void test01_exisitingNotLocked() throws Exception {
        final WebDavMethod method = new UnlockMethod();

        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_BAD_REQUEST, response.getStatus());
        Assert.assertNull("contentType must be null", response.getContentType());
        Assert.assertNull("content must be null", response.getContent());
        Assert.assertNull("characterEncoding must be null", response.getCharacterEncoding());
    }

    @Test
    public void test02_exisitingLocked() throws Exception {
        final WebDavMethod method = new UnlockMethod();

        Mockito.when(config.isShowCollectionListings()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, response.getStatus());
        Assert.assertNull("contentType must be null", response.getContentType());
        Assert.assertNull("content must be null", response.getContent());
        Assert.assertNull("characterEncoding must be null", response.getCharacterEncoding());
    }
}
