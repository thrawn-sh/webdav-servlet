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

import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.webdav.Entity;
import de.shadowhunt.webdav.Path;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavStore;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PutMethodTest extends AbstractWebDavMethodTest {

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new PutMethod();

        final Path path = Path.create("/item.txt");
        final WebDavStore store = Mockito.mock(WebDavStore.class);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(store.exists(path)).thenReturn(false);

        final Response response = execute(method, store, path, request);
        Assert.assertEquals("status must match", response.getStatus(), HttpStatus.SC_CREATED);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test01_exisitingItem() throws Exception {
        final WebDavMethod method = new PutMethod();

        final Path path = Path.create("/item.txt");
        final WebDavStore store = Mockito.mock(WebDavStore.class);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final Entity entity = Mockito.mock(Entity.class);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);
        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getType()).thenReturn(Entity.Type.ITEM);

        final Response response = execute(method, store, path, request);
        Assert.assertEquals("status must match", response.getStatus(), HttpStatus.SC_CREATED);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test02_exisitingCollection() throws Exception {
        final WebDavMethod method = new PutMethod();

        final Path path = Path.create("/collection/");
        final WebDavStore store = Mockito.mock(WebDavStore.class);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final Entity entity = Mockito.mock(Entity.class);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);
        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getPath()).thenReturn(path);
        Mockito.when(entity.getType()).thenReturn(Entity.Type.COLLECTION);

        final Response response = execute(method, store, path, request);
        Assert.assertEquals("status must match", response.getStatus(), HttpStatus.SC_METHOD_NOT_ALLOWED);
        Assert.assertNull("content must be null", response.getContent());
    }
}
