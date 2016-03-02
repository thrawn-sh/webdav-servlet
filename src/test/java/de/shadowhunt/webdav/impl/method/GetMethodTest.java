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

import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GetMethodTest extends AbstractWebDavMethodTest {

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new GetMethod();

        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_NOT_FOUND);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test01_exisitingItem() throws Exception {
        final WebDavMethod method = new GetMethod();

        final String content = "example";

        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_OK);
        Assert.assertEquals("content must match", response.getContent(), content);
    }

    @Test
    public void test02_exisitingCollectionListing() throws Exception {
        final WebDavMethod method = new GetMethod();

        Mockito.when(config.getCssForCollectionListings()).thenReturn(Optional.empty());
        Mockito.when(config.isShowCollectionListings()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(EXISITING_COLLECTION);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_OK);
        Assert.assertNotNull("content must not be null", response.getContent());
    }

    @Test
    public void test02_exisitingCollectionNoListing() throws Exception {
        final WebDavMethod method = new GetMethod();

        Mockito.when(config.isShowCollectionListings()).thenReturn(false);

        Mockito.when(request.getPath()).thenReturn(EXISITING_COLLECTION);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_FORBIDDEN);
        Assert.assertNull("content must be null", response.getContent());
    }
}
