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
package de.shadowhunt.servlet;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

public abstract class AbstractLitmusIT {

    public static final String BASE;

    static {
        final String port = System.getProperty("jetty.http.port", "8080");
        BASE = "http://127.0.0.1:" + port + "/webdav/litmus";
    }

    @AfterClass
    public static void after() throws Exception {
        deleteBaseFolder();
    }

    @BeforeClass
    public static void before() throws Exception {
        deleteBaseFolder();
        createBaseFolder();
    }

    private static void createBaseFolder() throws Exception {
        final DavTemplateRequest request = new DavTemplateRequest("MKCOL", URI.create(BASE));
        execute(request, new ResponseHandler<Void>() {

            @Override
            public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(HttpStatus.SC_CREATED, statusCode);

                final HttpEntity entity = response.getEntity();
                Assert.assertNotNull(entity);
                final long contentLength = entity.getContentLength();
                Assert.assertEquals(0L, contentLength);

                return null;
            }
        });
    }

    private static void deleteBaseFolder() throws Exception {
        final DavTemplateRequest request = new DavTemplateRequest("DELETE", URI.create(BASE));
        execute(request, new ResponseHandler<Void>() {

            @Override
            public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int statusCode = response.getStatusLine().getStatusCode();
                final HttpEntity entity = response.getEntity();
                if (statusCode == HttpStatus.SC_NO_CONTENT) {
                    Assert.assertNull(entity);
                } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    Assert.assertNotNull(entity);
                    final long contentLength = entity.getContentLength();
                    Assert.assertEquals(0L, contentLength);
                } else {
                    Assert.fail();
                }

                return null;
            }
        });
    }

    protected static void execute(final HttpUriRequest request, final ResponseHandler<Void> handler) throws Exception {
        Assert.assertNotNull(request);
        Assert.assertNotNull(handler);

        final HttpClientBuilder builder = HttpClientBuilder.create();
        try (final CloseableHttpClient client = builder.build()) {
            Assert.assertNotNull(client);
            client.execute(request, handler);
        }
    }
}
