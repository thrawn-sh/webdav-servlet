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

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractCopyMoveMethodTest extends AbstractWebDavMethodTest {

    protected abstract WebDavMethod createMethod();

    @Test
    public void test00_missingSource() throws Exception {
        final WebDavMethod method = createMethod();

        final Path source = Path.create("/source_item.txt");
        final WebDavStore store = Mockito.mock(WebDavStore.class);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(store.exists(source)).thenReturn(false);

        final Response response = execute(method, store, source, request);
        Assert.assertEquals("status must match", response.getStatus(), HttpStatus.SC_NOT_FOUND);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test01_exisitingSourceItem_missingTargetItem() throws Exception {
        final WebDavMethod method = createMethod();

        final Path source = Path.create("/source_item.txt");
        final Path target = Path.create("/target_item.txt");
        final WebDavStore store = Mockito.mock(WebDavStore.class);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final Entity entity = Mockito.mock(Entity.class);

        Mockito.when(store.exists(Path.ROOT)).thenReturn(true);
        Mockito.when(store.exists(source)).thenReturn(true);
        Mockito.when(store.exists(target)).thenReturn(false);
        Mockito.when(store.getEntity(source)).thenReturn(entity);
        Mockito.when(request.getHeader("Destination")).thenReturn("http://127.0.0.1/webdav/target_item.txt");
        Mockito.when(request.getServletPath()).thenReturn("/webdav");
        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getType()).thenReturn(Entity.Type.ITEM);

        final Response response = execute(method, store, source, request);
        Assert.assertEquals("status must match", response.getStatus(), HttpStatus.SC_CREATED);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test02_exisitingSourceItem_exisitingTargetItem_noOverride() throws Exception {
        final WebDavMethod method = createMethod();

        final Path source = Path.create("/source_item.txt");
        final Path target = Path.create("/target_item.txt");
        final WebDavStore store = Mockito.mock(WebDavStore.class);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final Entity entity = Mockito.mock(Entity.class);

        Mockito.when(store.exists(Path.ROOT)).thenReturn(true);
        Mockito.when(store.exists(source)).thenReturn(true);
        Mockito.when(store.exists(target)).thenReturn(true);
        Mockito.when(store.getEntity(source)).thenReturn(entity);
        Mockito.when(request.getHeader("Destination")).thenReturn("http://127.0.0.1/webdav/target_item.txt");
        Mockito.when(request.getHeader("Overwrite")).thenReturn("F");
        Mockito.when(request.getServletPath()).thenReturn("/webdav");
        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getType()).thenReturn(Entity.Type.ITEM);

        final Response response = execute(method, store, source, request);
        Assert.assertEquals("status must match", response.getStatus(), HttpStatus.SC_PRECONDITION_FAILED);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test03_exisitingSourceItem_exisitingTargetItem_override() throws Exception {
        final WebDavMethod method = createMethod();

        final Path source = Path.create("/source_item.txt");
        final Path target = Path.create("/target_item.txt");
        final WebDavStore store = Mockito.mock(WebDavStore.class);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final Entity entity = Mockito.mock(Entity.class);

        Mockito.when(store.exists(Path.ROOT)).thenReturn(true);
        Mockito.when(store.exists(source)).thenReturn(true);
        Mockito.when(store.exists(target)).thenReturn(true);
        Mockito.when(store.getEntity(source)).thenReturn(entity);
        Mockito.when(request.getHeader("Destination")).thenReturn("http://127.0.0.1/webdav/target_item.txt");
        Mockito.when(request.getHeader("Overwrite")).thenReturn("T");
        Mockito.when(request.getServletPath()).thenReturn("/webdav");
        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getType()).thenReturn(Entity.Type.ITEM);

        final Response response = execute(method, store, source, request);
        Assert.assertEquals("status must match", response.getStatus(), HttpStatus.SC_NO_CONTENT);
        Assert.assertNull("content must be null", response.getContent());
    }

    // FIXME target parent non existing
    // copy/move collection
    // merge collections ???
    // copy/move item to collection (with override) ???
}
