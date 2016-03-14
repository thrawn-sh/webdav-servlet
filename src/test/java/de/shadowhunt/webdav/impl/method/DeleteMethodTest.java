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

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Matchers;
import org.mockito.Mockito;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeleteMethodTest extends AbstractWebDavMethodTest {

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new DeleteMethod();

        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn(AbstractWebDavMethod.INFINITY);
        Mockito.when(request.getPath()).thenReturn(NON_EXISITING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NOT_FOUND);
    }

    @Test
    public void test01_exisitingItem() throws Exception {
        final WebDavMethod method = new DeleteMethod();

        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn(AbstractWebDavMethod.INFINITY);
        Mockito.when(request.getPath()).thenReturn(EXISITING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
    }

    @Test
    public void test02_exisitingCollection() throws Exception {
        final WebDavMethod method = new DeleteMethod();

        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn(AbstractWebDavMethod.INFINITY);
        Mockito.when(request.getPath()).thenReturn(EXISITING_COLLECTION);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
    }

    @Test
    public void test02_exisitingRootCollection() throws Exception {
        final WebDavMethod method = new DeleteMethod();

        Mockito.when(config.isShowCollectionListings()).thenReturn(false);

        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn(AbstractWebDavMethod.INFINITY);
        Mockito.when(request.getPath()).thenReturn(WebDavPath.ROOT);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_FORBIDDEN);
    }
}
