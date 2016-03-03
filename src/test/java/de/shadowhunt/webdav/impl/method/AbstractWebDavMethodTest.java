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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;
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

    interface Normalizer {
        String normalize(final String content);
    }

    static class Response implements WebDavResponse {

        private String characterEncoding = null;

        private final ByteArrayOutputStream content = new ByteArrayOutputStream();

        private String contentType = null;

        private final Map<String, String> headers = new HashMap<>();

        private final WebDavRequest request;

        private Status status = null;

        Response(final WebDavRequest request) {
            this.request = request;
        }

        @Override
        public void addHeader(final String name, final String value) {
            final String previous = headers.put(name, value);
            Assert.assertNull("header '" + name + "' must not be defined multiple times", previous);
        }

        public String getCharacterEncoding() {
            return characterEncoding;
        }

        @Nullable
        public String getContent() {
            return getContent(DUMMY);
        }

        @Nullable
        public String getContent(final Normalizer replacer) {
            if (content.size() == 0) {
                return null;
            }

            return replacer.normalize(new String(content.toByteArray(), StandardCharsets.UTF_8));
        }

        public String getContentType() {
            return contentType;
        }

        public String getHeader(final String name) {
            return headers.get(name);
        }

        @Override
        public OutputStream getOutputStream() {
            return content;
        }

        @Override
        public WebDavRequest getRequest() {
            return request;
        }

        public Status getStatus() {
            return status;
        }

        @Override
        public void setCharacterEncoding(final String characterEncoding) {
            this.characterEncoding = characterEncoding;
        }

        @Override
        public void setContentType(final String contentType) {
            this.contentType = contentType;
        }

        @Override
        public void setStatus(final Status status) {
            this.status = status;
        }
    }

    private static final Normalizer DUMMY = new Normalizer() {

        @Override
        public String normalize(final String content) {
            return content;
        }

    };

    protected static final WebDavPath EXISITING_COLLECTION = WebDavPath.create("/collection");

    protected static final WebDavPath EXISITING_ITEM = WebDavPath.create("/item.txt");

    protected static final WebDavPath NON_EXISITING = WebDavPath.create("/non_exisiting.txt");

    private static File root;

    private static WebDavStore store;

    public static final String UTF_8 = "UTF-8";

    protected static void createCollection(final WebDavPath path, final boolean locked) {
        createCollection0(path, locked);
        setProperties(path);
    }

    protected static void createCollection0(final WebDavPath path, final boolean locked) {
        if (WebDavPath.ROOT.equals(path)) {
            return;
        }

        createCollection0(path.getParent(), false);
        if (!store.exists(path)) {
            store.createCollection(path);
        }

        if (locked) {
            final WebDavLock lock = store.createLock();
            store.lock(path, lock);
        }
    }

    protected static void createItem(final WebDavPath path, final String content, final boolean locked) {
        createCollection0(path.getParent(), false);
        store.createItem(path, new ByteArrayInputStream(content.getBytes()));
        if (locked) {
            final WebDavLock lock = store.createLock();
            store.lock(path, lock);
        }
        setProperties(path);
    }

    @AfterClass
    public static void destroyStore() {
        FileUtils.deleteQuietly(root);
    }

    @BeforeClass
    public static void initStore() {
        root = new File(new File(FileUtils.getTempDirectory(), "webdav-servlet"), UUID.randomUUID().toString());
        store = new FileSystemStore(root);

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

    protected final void assertNoContent(final Response response) {
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

    protected Response execute(final WebDavMethod method) throws Exception {
        final Response response = new Response(request);

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
