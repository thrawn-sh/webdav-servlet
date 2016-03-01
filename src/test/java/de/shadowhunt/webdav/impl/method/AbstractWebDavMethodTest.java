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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.WebDavStore;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public abstract class AbstractWebDavMethodTest {

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
            if (content.size() == 0) {
                return null;
            }
            return new String(content.toByteArray());
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

    @Mock
    protected WebDavConfig config;

    @Mock
    protected WebDavEntity entity;

    @Mock
    protected WebDavLock lock;

    @Mock
    protected WebDavRequest request;

    @Mock
    protected WebDavStore store;

    protected void assertResponse(final Response response) {
        // FIXME
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

        Mockito.when(lock.getOwner()).thenReturn("testuser");
        Mockito.when(lock.getScope()).thenReturn(WebDavLock.Scope.EXCLUSIVE);
        Mockito.when(lock.getToken()).thenReturn("00000000-0000-0000-0000-000000000000");

        Mockito.when(request.getConfig()).thenReturn(config);

        Mockito.when(store.exists(WebDavPath.ROOT)).thenReturn(true);
    }
}
