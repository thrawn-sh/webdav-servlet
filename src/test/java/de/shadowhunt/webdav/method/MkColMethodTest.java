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

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavConstant.Status;
import de.shadowhunt.webdav.WebDavPath;

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

        Mockito.when(request.getPath()).thenReturn(NON_EXISTING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.CREATED);
    }

    @Test
    public void test01_missingParent() throws Exception {
        final WebDavMethod method = new MkColMethod();

        final WebDavPath path = WebDavPath.create("/parent/collection/");

        Mockito.when(request.getPath()).thenReturn(path);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.CONFLICT);
    }

    @Test
    public void test02_existingItem() throws Exception {
        final WebDavMethod method = new MkColMethod();

        Mockito.when(request.getPath()).thenReturn(EXISTING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.METHOD_NOT_ALLOWED);
    }

    @Test
    public void test03_existingCollection() throws Exception {
        final WebDavMethod method = new MkColMethod();

        Mockito.when(request.getPath()).thenReturn(EXISTING_COLLECTION);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.METHOD_NOT_ALLOWED);
    }

    @Test
    public void test04_withContent() throws Exception {
        final WebDavMethod method = new MkColMethod();

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        Mockito.when(request.getPath()).thenReturn(EXISTING_COLLECTION);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.UNSUPPORTED_MEDIA_TYPE);
    }
}
