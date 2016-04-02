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
package de.shadowhunt.webdav.precondition;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavConstant.Header;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavLock;
import de.shadowhunt.webdav.store.WebDavLockBuilder;
import de.shadowhunt.webdav.store.WebDavStore;
import de.shadowhunt.webdav.store.memory.MemoryStore;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PreconditionTest {

    private static final String FOREIGN_RESOURCE = "http://www.example.net/webdav/non_existing.txt";

    private static final WebDavPath ITEM = WebDavPath.create("item.txt");

    private static final String ITEM_RESOURCE = "http://127.0.0.1:8080/webdav/item.txt";

    private static final WebDavPath LOCKED_ITEM = WebDavPath.create("locked.txt");

    private static final String LOCKED_RESOURCE = "http://127.0.0.1:8080/webdav/locked.txt";

    private static final WebDavPath NON_EXISTING = WebDavPath.create("non_existing.txt");

    private static final String NON_EXISTING_RESOURCE = "http://127.0.0.1:8080/webdav/non_existing.txt";

    private static WebDavStore store;

    private static final Map<WebDavPath, UUID> TOKENS = new HashMap<>();

    @Mock
    protected WebDavConfig config;

    @Mock
    protected WebDavRequest request;

    @Before
    public void initMock() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(request.getBase()).thenReturn("/webdav");
        Mockito.when(request.getConfig()).thenReturn(config);
        Mockito.when(request.toPath(Matchers.eq(FOREIGN_RESOURCE))).thenReturn(Optional.empty());
        Mockito.when(request.toPath(Matchers.eq(ITEM_RESOURCE))).thenReturn(Optional.of(ITEM));
        Mockito.when(request.toPath(Matchers.eq(LOCKED_RESOURCE))).thenReturn(Optional.of(LOCKED_ITEM));
        Mockito.when(request.toPath(Matchers.eq(NON_EXISTING_RESOURCE))).thenReturn(Optional.of(NON_EXISTING));
    }

    @Before
    public void initStore() {
        store = new MemoryStore();

        store.createItem(ITEM, new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
        store.createItem(LOCKED_ITEM, new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
        final WebDavLockBuilder lockBuilder = store.createLockBuilder();
        lockBuilder.setRoot(LOCKED_ITEM);
        final WebDavLock lock = lockBuilder.build();
        store.lock(LOCKED_ITEM, lock);

        TOKENS.clear();
        TOKENS.put(LOCKED_ITEM, lock.getToken());
    }

    @Test
    public void test_00_empty() throws Exception {
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn("");
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_00_explicit_foreign() throws Exception {
        final String precondition = "<" + FOREIGN_RESOURCE + "> (<DAV:no-lock>)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Assert.assertFalse(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertFalse(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_00_explicit_non_existing() throws Exception {
        final String precondition = "<" + NON_EXISTING_RESOURCE + "> (<DAV:no-lock>)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Assert.assertFalse(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertFalse(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_00_illegalInput() throws Exception {
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn("this input is invalid");
        Assert.assertFalse(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_00_implicit_non_existing() throws Exception {
        final String precondition = "(<DAV:no-lock>)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(NON_EXISTING);
        Assert.assertFalse(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertFalse(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_01_explicit_lock_false() throws Exception {
        final String precondition = "<" + LOCKED_RESOURCE + "> (<DAV:no-lock>)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Assert.assertFalse(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertFalse(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_01_explicit_nolock_true() throws Exception {
        final String precondition = "<" + ITEM_RESOURCE + "> (Not <DAV:no-lock>)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_01_implicit_lock_false() throws Exception {
        final String precondition = "(<DAV:no-lock>)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        Assert.assertFalse(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertFalse(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_01_implicit_nolock_true() throws Exception {
        final String precondition = "(Not <DAV:no-lock>)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_02_explicit_etag() throws Exception {
        final WebDavEntity entity = store.getEntity(ITEM);
        final String etag = entity.getEtag().get();

        final String precondition = "<" + ITEM_RESOURCE + "> ([" + etag + "])";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_02_explicit_lock() throws Exception {
        final WebDavEntity entity = store.getEntity(LOCKED_ITEM);
        final WebDavLock lock = entity.getLock().get();

        final String precondition = "<" + LOCKED_RESOURCE + "> (<" + WebDavLock.PREFIX + lock.getToken() + ">)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", TOKENS, Precondition.getTokens(request));
    }

    @Test
    public void test_02_implicit_etag() throws Exception {
        final WebDavEntity entity = store.getEntity(ITEM);
        final String etag = entity.getEtag().get();

        final String precondition = "([" + etag + "])";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", Collections.emptyMap(), Precondition.getTokens(request));
    }

    @Test
    public void test_02_implicit_lock() throws Exception {
        final WebDavEntity entity = store.getEntity(LOCKED_ITEM);
        final WebDavLock lock = entity.getLock().get();

        final String precondition = "(<" + WebDavLock.PREFIX + lock.getToken() + ">)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", TOKENS, Precondition.getTokens(request));
    }

    @Test
    public void test_03_explicit_etag_lock() throws Exception {
        final WebDavEntity entity = store.getEntity(LOCKED_ITEM);
        final String etag = entity.getEtag().get();
        final WebDavLock lock = entity.getLock().get();

        final String precondition = "<" + LOCKED_RESOURCE + "> ([" + etag + "] <" + WebDavLock.PREFIX + lock.getToken() + ">)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", TOKENS, Precondition.getTokens(request));
    }

    @Test
    public void test_03_explicit_lock_etag() throws Exception {
        final WebDavEntity entity = store.getEntity(LOCKED_ITEM);
        final String etag = entity.getEtag().get();
        final WebDavLock lock = entity.getLock().get();

        final String precondition = "<" + LOCKED_RESOURCE + "> (<" + WebDavLock.PREFIX + lock.getToken() + "> [" + etag + "])";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", TOKENS, Precondition.getTokens(request));
    }

    @Test
    public void test_03_implicit_etag_lock() throws Exception {
        final WebDavEntity entity = store.getEntity(LOCKED_ITEM);
        final String etag = entity.getEtag().get();
        final WebDavLock lock = entity.getLock().get();

        final String precondition = "([" + etag + "] <" + WebDavLock.PREFIX + lock.getToken() + ">)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", TOKENS, Precondition.getTokens(request));
    }

    @Test
    public void test_03_implicit_lock_etag() throws Exception {
        final WebDavEntity entity = store.getEntity(LOCKED_ITEM);
        final String etag = entity.getEtag().get();
        final WebDavLock lock = entity.getLock().get();

        final String precondition = "(<" + WebDavLock.PREFIX + lock.getToken() + "> [" + etag + "])";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", TOKENS, Precondition.getTokens(request));
    }

    @Test
    public void test_04_implicit_explicit() throws Exception {
        final WebDavEntity entity = store.getEntity(LOCKED_ITEM);
        final String etag = entity.getEtag().get();
        final WebDavLock lock = entity.getLock().get();

        final String precondition = "([" + etag + "]) <" + LOCKED_RESOURCE + "> (<" + WebDavLock.PREFIX + lock.getToken() + ">)";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", TOKENS, Precondition.getTokens(request));
    }

    @Test
    public void test_05_explicit_multiple() throws Exception {
        final WebDavEntity entity = store.getEntity(LOCKED_ITEM);
        final String etag = entity.getEtag().get();
        final WebDavLock lock = entity.getLock().get();

        final String precondition = "<" + LOCKED_RESOURCE + "> (<" + WebDavLock.PREFIX + lock.getToken() + ">) ([" + etag + "])";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", TOKENS, Precondition.getTokens(request));
    }

    @Test
    public void test_05_implicit_multiple() throws Exception {
        final WebDavEntity entity = store.getEntity(LOCKED_ITEM);
        final String etag = entity.getEtag().get();
        final WebDavLock lock = entity.getLock().get();

        final String precondition = "(<" + WebDavLock.PREFIX + lock.getToken() + ">) ([" + etag + "])";
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(precondition);
        Mockito.when(request.getPath()).thenReturn(LOCKED_ITEM);
        Assert.assertTrue(Precondition.verify(store, request));
        Mockito.when(request.getHeader(Header.PRECONDITION, "")).thenReturn(StringUtils.deleteWhitespace(precondition));
        Assert.assertTrue(Precondition.verify(store, request));

        Assert.assertEquals("tokens must match", TOKENS, Precondition.getTokens(request));
    }
}
