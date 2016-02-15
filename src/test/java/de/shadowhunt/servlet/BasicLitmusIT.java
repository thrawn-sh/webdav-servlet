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
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicLitmusIT extends AbstractLitmusIT {

    private void doPutGet(final URI uri, final String data, final String marker) throws Exception {
        final DavTemplateRequest put = new DavTemplateRequest("PUT", uri, marker);
        put.setEntity(new StringEntity(data, StandardCharsets.UTF_8));

        execute(put, new ResponseHandler<Void>() {

            @Override
            public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(HttpStatus.SC_CREATED, statusCode);

                final long contentLength = response.getEntity().getContentLength();
                Assert.assertEquals(0L, contentLength);

                return null;
            }
        });

        final DavTemplateRequest get = new DavTemplateRequest("GET", uri, marker);
        execute(get, new ResponseHandler<Void>() {

            @Override
            public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(HttpStatus.SC_OK, statusCode);

                final HttpEntity entity = response.getEntity();
                Assert.assertNotNull(entity);

                final long contentLength = entity.getContentLength();
                Assert.assertEquals(data.length(), contentLength);
                Assert.assertEquals(data, IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8));

                return null;
            }
        });
    }

    @Test
    public void test_01_OPTIONS() throws Exception {
        final DavTemplateRequest request = new DavTemplateRequest("OPTIONS", URI.create(BASE), "basic: 2 (options)");
        execute(request, new ResponseHandler<Void>() {

            @Override
            public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(HttpStatus.SC_NO_CONTENT, statusCode);
                Assert.assertNull(response.getEntity());

                final Header[] dav = response.getHeaders("DAV");
                Assert.assertNotNull(dav);
                Assert.assertEquals(1, dav.length);
                Assert.assertEquals("1,2", dav[0].getValue());

                return null;
            }
        });
    }

    @Test
    public void test_02_PUT_GET() throws Exception {
        final URI uri = URI.create(BASE + "/res");
        final String data = "This is a test file for litmus testing.";

        doPutGet(uri, data, "basic: 3 (put_get)");
    }
    
    @Test
    public void test_03_PUT_GET_utf8() throws Exception {
        final URI uri = URI.create(BASE + "/res-€");
        final String data = "This is a test file for litmus testing.";

        doPutGet(uri, data, "basic: 4 (put_get_utf8_segment)");
    }
    
    @Test
    public void test_04_PUT_GET_utf8() throws Exception {
        final URI uri = URI.create(BASE + "/409me/noparent.txt/");

        final DavTemplateRequest request = new DavTemplateRequest("MKCOL", uri, "basic: 5 (put_no_parent)");
        execute(request, new ResponseHandler<Void>() {

            @Override
            public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(HttpStatus.SC_CONFLICT, statusCode);

                final HttpEntity entity = response.getEntity();
                Assert.assertNotNull(entity);
                final long contentLength = entity.getContentLength();
                Assert.assertEquals(0L, contentLength);

                return null;
            }
        });
    }
    
    @Test
    public void test_05_PUT_GET_utf8() throws Exception {
        final URI uri = URI.create(BASE + "/res-€");

        final DavTemplateRequest request = new DavTemplateRequest("MKCOL", uri, "basic: 6 (mkcol_over_plain)");
        execute(request, new ResponseHandler<Void>() {

            @Override
            public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, statusCode);

                final HttpEntity entity = response.getEntity();
                Assert.assertNotNull(entity);
                final long contentLength = entity.getContentLength();
                Assert.assertEquals(0L, contentLength);

                return null;
            }
        });
    }
    
    @Test
    public void test_06_PUT_GET_utf8() throws Exception {
        final URI uri = URI.create(BASE + "/res-€");

        final DavTemplateRequest request = new DavTemplateRequest("DELETE", uri, "basic: 7 (delete)");
        execute(request, new ResponseHandler<Void>() {

            @Override
            public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(HttpStatus.SC_NO_CONTENT, statusCode);

                final HttpEntity entity = response.getEntity();
                Assert.assertNull(entity);

                return null;
            }
        });
    }
    
    @Test
    public void test_07_PUT_GET_utf8() throws Exception {
        final URI uri = URI.create(BASE + "/404me");

        final DavTemplateRequest request = new DavTemplateRequest("DELETE", uri, "basic: 8 (delete_null)");
        execute(request, new ResponseHandler<Void>() {

            @Override
            public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                final int statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(HttpStatus.SC_NOT_FOUND, statusCode);

                final HttpEntity entity = response.getEntity();
                Assert.assertNotNull(entity);
                final long contentLength = entity.getContentLength();
                Assert.assertEquals(0L, contentLength);

                return null;
            }
        });
    }
    
    @Test
    public void test_08_PUT_GET_utf8() throws Exception {
        final URI uri = URI.create(BASE + "/coll");

        final DavTemplateRequest request = new DavTemplateRequest("MKCOL", uri, "basic: 10 (mkcol)");
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
}
