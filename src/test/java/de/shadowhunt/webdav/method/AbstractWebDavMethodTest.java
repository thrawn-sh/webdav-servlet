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
import java.util.Arrays;
import java.util.Optional;

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavConstant.Header;
import de.shadowhunt.webdav.WebDavConstant.Status;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.property.PropertyIdentifier;
import de.shadowhunt.webdav.property.StringWebDavProperty;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavLock;
import de.shadowhunt.webdav.store.WebDavLockBuilder;
import de.shadowhunt.webdav.store.WebDavStore;
import de.shadowhunt.webdav.store.memory.MemoryStore;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public abstract class AbstractWebDavMethodTest {

    protected static final WebDavPath EXISTING_COLLECTION = WebDavPath.create("/collection");

    protected static final WebDavPath EXISTING_ITEM = WebDavPath.create("/item.txt");

    protected static final WebDavPath NON_EXISTING = WebDavPath.create("/non_existing.txt");

    @Mock
    protected WebDavConfig config;

    @Mock
    protected WebDavRequest request;

    private final WebDavStore store = new MemoryStore();

    protected final void assertBasicRequirements(final TestResponse response, final Status expectedStatus) {
        Assert.assertEquals("status must match", expectedStatus, response.getStatus());
        Assert.assertNotNull("allow header must not be null", response.getHeader(Header.ALLOW));
        Assert.assertEquals("dav header must match", "1,2", response.getHeader(Header.DAV));
        Assert.assertEquals("ms-author header must match", "DAV", response.getHeader(Header.MS_AUTHOR));
    }

    protected final void assertNoContent(final TestResponse response, final Status expectedStatus) {
        assertBasicRequirements(response, expectedStatus);
        Assert.assertNull("contentType must be null", response.getContentType());
        Assert.assertNull("characterEncoding must be null", response.getCharacterEncoding());
        Assert.assertNull("content must be null", response.getContent());
    }

    protected String concat(final String... input) {
        final StringBuilder sb = new StringBuilder();
        for (final String i : input) {
            sb.append(i);
        }
        return sb.toString();
    }

    protected WebDavEntity createCollection(final WebDavPath path, final boolean locked) {
        createCollection0(path, locked);
        setProperties(path);
        return store.getEntity(path);
    }

    private void createCollection0(final WebDavPath path, final boolean locked) {
        if (WebDavPath.ROOT.equals(path)) {
            return;
        }

        createCollection0(path.getParent(), false);
        if (!store.exists(path)) {
            store.createCollection(path);
        }

        if (locked) {
            ensureLocked(path);
        }
    }

    protected WebDavEntity createItem(final WebDavPath path, final String content, final boolean locked) {
        createCollection0(path.getParent(), false);
        store.createItem(path, new ByteArrayInputStream(content.getBytes()));
        if (locked) {
            final WebDavLockBuilder lockBuilder = store.createLockBuilder();
            lockBuilder.setRoot(path);
            final WebDavLock lock = lockBuilder.build();
            store.lock(path, lock);
        }
        setProperties(path);
        return store.getEntity(path);
    }

    protected WebDavLock ensureLocked(final WebDavPath path) {
        final WebDavEntity entity = store.getEntity(path);
        final Optional<WebDavLock> existingLock = entity.getLock();
        if (existingLock.isPresent()) {
            return existingLock.get();
        }

        final WebDavLockBuilder lockBuilder = store.createLockBuilder();
        lockBuilder.setRoot(path);
        final WebDavLock lock = lockBuilder.build();
        store.lock(path, lock);
        return lock;
    }

    protected TestResponse execute(final WebDavMethod method) throws Exception {
        final TestResponse response = new TestResponse(request);

        final WebDavResponseWriter webdavResponse = method.service(store, request);
        webdavResponse.write(response);

        return response;
    }

    @Before
    public void initMock() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(request.getBase()).thenReturn("/webdav");
        Mockito.when(request.getConfig()).thenReturn(config);
    }

    @Before
    public void initStore() {
        createCollection(EXISTING_COLLECTION, false);
        createItem(EXISTING_ITEM, "example", false);
    }

    private void setProperties(final WebDavPath path) {
        store.setProperties(path,
                Arrays.asList( //
                        new StringWebDavProperty(new PropertyIdentifier("foo", "foo"), "foo_foo_content"), //
                        new StringWebDavProperty(new PropertyIdentifier("foo", "bar"), "foo_bar_content"), //
                        new StringWebDavProperty(new PropertyIdentifier("bar", "foo"), "bar_foo_content") //
                ));
    }
}
