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

import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse.Status;
import de.shadowhunt.webdav.store.WebDavLock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Matchers;
import org.mockito.Mockito;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UnlockMethodTest extends AbstractWebDavMethodTest {

    protected static final WebDavPath LOCKED_ITEM = WebDavPath.create("/locked_item.txt");

    @BeforeClass
    public static void fillStore() {
        createItem(LOCKED_ITEM, "test", true);
    }

    protected WebDavLock lock;

    @Before
    public void ensureStore() {
        lock = ensureLocked(LOCKED_ITEM);
    }

    @Test
    public void test_deterimineLockToken() throws Exception {
        final UnlockMethod method = new UnlockMethod();

        Mockito.when(request.getHeader(Matchers.eq(WebDavRequest.LOCKTOKEN_HEADER), Matchers.anyString())).thenReturn(null);
        Assert.assertEquals("must match", Optional.empty(), method.determineLockToken(request));

        Mockito.when(request.getHeader(Matchers.eq(WebDavRequest.LOCKTOKEN_HEADER), Matchers.anyString())).thenReturn("");
        Assert.assertEquals("must match", Optional.empty(), method.determineLockToken(request));

        Mockito.when(request.getHeader(Matchers.eq(WebDavRequest.LOCKTOKEN_HEADER), Matchers.anyString())).thenReturn("<test");
        Assert.assertEquals("must match", Optional.empty(), method.determineLockToken(request));

        Mockito.when(request.getHeader(Matchers.eq(WebDavRequest.LOCKTOKEN_HEADER), Matchers.anyString())).thenReturn("test>");
        Assert.assertEquals("must match", Optional.empty(), method.determineLockToken(request));

        Mockito.when(request.getHeader(Matchers.eq(WebDavRequest.LOCKTOKEN_HEADER), Matchers.anyString())).thenReturn("<test>");
        Assert.assertEquals("must match", Optional.empty(), method.determineLockToken(request));

        Mockito.when(request.getHeader(Matchers.eq(WebDavRequest.LOCKTOKEN_HEADER), Matchers.anyString())).thenReturn("<" + WebDavLock.PREFIX + "test>");
        Assert.assertEquals("must match", Optional.empty(), method.determineLockToken(request));

        final UUID uuid = UUID.randomUUID();
        Mockito.when(request.getHeader(Matchers.eq(WebDavRequest.LOCKTOKEN_HEADER), Matchers.anyString())).thenReturn("<" + WebDavLock.PREFIX + uuid + ">");
        Assert.assertEquals("must match", Optional.of(uuid), method.determineLockToken(request));
    }

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new UnlockMethod();

        Mockito.when(request.getPath()).thenReturn(NON_EXISTING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NOT_FOUND);
    }

    @Test
    public void test01_existingNotLocked() throws Exception {
        final WebDavMethod method = new UnlockMethod();

        Mockito.when(request.getPath()).thenReturn(EXISTING_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_BAD_REQUEST);
    }

    @Test
    public void test02_existingLocked_lock() throws Exception {
        final WebDavMethod method = new UnlockMethod();

        Mockito.when(config.isShowCollectionListings()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_LOCKED);
    }

    @Test
    public void test02_existingLocked_unlock() throws Exception {
        final WebDavMethod method = new UnlockMethod();

        Mockito.when(config.isShowCollectionListings()).thenReturn(true);

        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        final String token = "<" + WebDavLock.PREFIX + lock.getToken() + ">";
        Mockito.when(request.getHeader(Matchers.eq(WebDavRequest.LOCKTOKEN_HEADER), Matchers.anyString())).thenReturn(token);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.SC_NO_CONTENT);
    }
}
