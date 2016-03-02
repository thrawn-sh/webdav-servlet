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

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LockMethodTest extends AbstractWebDavMethodTest {

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new LockMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(false);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_NOT_FOUND);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test01_exisitingNotLocked() throws Exception {
        final WebDavMethod method = new LockMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getLock()).thenReturn(Optional.empty());
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.createLock()).thenReturn(lock);
        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);
        Mockito.when(store.lock(path, lock)).thenAnswer(new Answer<WebDavEntity>() {

            @Override
            public WebDavEntity answer(final InvocationOnMock invocation) throws Throwable {
                final WebDavLock suppliedLock = invocation.getArgumentAt(1, WebDavLock.class);
                final WebDavEntity mock = Mockito.mock(WebDavEntity.class);
                Mockito.when(mock.getHash()).thenReturn(Optional.empty());
                Mockito.when(mock.getLastModified()).thenReturn(new Date(0L));
                Mockito.when(mock.getLock()).thenReturn(Optional.of(suppliedLock));
                Mockito.when(mock.getType()).thenReturn(WebDavEntity.Type.ITEM);
                return mock;
            }
        });

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_MULTISTATUS);
    }

    @Test
    public void test02_exisitingLocked() throws Exception {
        final WebDavMethod method = new LockMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(config.isShowCollectionListings()).thenReturn(true);

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getLock()).thenReturn(Optional.of(lock));
        Mockito.when(entity.getPath()).thenReturn(path);
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_MULTISTATUS);
        Assert.assertNotNull("content must not be null", response.getContent());
    }
}
