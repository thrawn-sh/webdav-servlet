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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavLock.LockScope;
import de.shadowhunt.webdav.WebDavLock.LockType;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse.Status;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.impl.StringWebDavProperty;
import de.shadowhunt.webdav.impl.store.FileSystemStore;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public abstract class AbstractWebDavMethodTest {

    protected static final WebDavPath EXISITING_COLLECTION = WebDavPath.create("/collection");

    protected static final WebDavPath EXISITING_ITEM = WebDavPath.create("/item.txt");

    protected static final WebDavPath NON_EXISITING = WebDavPath.create("/non_exisiting.txt");

    private static File root;

    private static WebDavStore store;

    protected static void createCollection(final WebDavPath path, final boolean locked) {
        createCollection0(path, locked);
        setProperties(path);
    }

    private static void createCollection0(final WebDavPath path, final boolean locked) {
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

    protected static void createItem(final WebDavPath path, final String content, final boolean locked) {
        createCollection0(path.getParent(), false);
        store.createItem(path, new ByteArrayInputStream(content.getBytes()));
        if (locked) {
            final WebDavLock lock = store.createLock(Optional.of(LockScope.EXCLUSIVE), Optional.of(LockType.WRITE), Optional.empty(), Optional.empty());
            store.lock(path, lock);
        }
        setProperties(path);
    }

    @AfterClass
    public static void destroyStore() {
        FileUtils.deleteQuietly(root);
    }

    protected static WebDavLock ensureLocked(final WebDavPath path) {
        final WebDavEntity entity = store.getEntity(path);
        final Optional<WebDavLock> exisitingLock = entity.getLock();
        if (exisitingLock.isPresent()) {
            return exisitingLock.get();
        }

        final WebDavLock lock = store.createLock(Optional.of(LockScope.EXCLUSIVE), Optional.of(LockType.WRITE), Optional.empty(), Optional.empty());
        store.lock(path, lock);
        return lock;
    }

    @BeforeClass
    public static void initStore() {
        root = new File(new File(FileUtils.getTempDirectory(), "webdav-servlet-test"), UUID.randomUUID().toString());
        store = new FileSystemStore(root, true);

        createCollection(EXISITING_COLLECTION, false);
        createItem(EXISITING_ITEM, "example", false);
    }

    private static void setProperties(final WebDavPath path) {
        store.setProperties(path,
                Arrays.asList( //
                        new StringWebDavProperty(new PropertyIdentifier("foo", "foo"), "foo_foo_content"), //
                        new StringWebDavProperty(new PropertyIdentifier("foo", "bar"), "foo_bar_content"), //
                        new StringWebDavProperty(new PropertyIdentifier("bar", "foo"), "bar_foo_content") //
                ));
    }

    @Mock
    protected WebDavConfig config;

    @Mock
    protected WebDavRequest request;

    protected final void assertBasicRequirements(final TestResponse response, final Status expectedStatus) {
        Assert.assertEquals("status must match", expectedStatus, response.getStatus());
        Assert.assertNotNull("allow header must not be null", response.getHeader(AbstractBasicResponse.ALLOW_HEADER));
        Assert.assertEquals("dav header must match", "1,2", response.getHeader(AbstractBasicResponse.DAV_HEADER));
        Assert.assertEquals("ms-author header must match", "DAV", response.getHeader(AbstractBasicResponse.MS_AUTHOR_HEADER));
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
}
