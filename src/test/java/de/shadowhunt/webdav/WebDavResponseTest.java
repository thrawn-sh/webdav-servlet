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
package de.shadowhunt.webdav;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

public class WebDavResponseTest {

    @Test
    public void testStatusCodes() throws Exception {
        Assert.assertEquals("status code must match", HttpStatus.SC_BAD_REQUEST, WebDavResponse.Status.SC_BAD_REQUEST.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_CONFLICT, WebDavResponse.Status.SC_CONFLICT.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_CREATED, WebDavResponse.Status.SC_CREATED.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_FORBIDDEN, WebDavResponse.Status.SC_FORBIDDEN.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_INTERNAL_SERVER_ERROR, WebDavResponse.Status.SC_INTERNAL_SERVER_ERROR.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_LOCKED, WebDavResponse.Status.SC_LOCKED.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_METHOD_NOT_ALLOWED, WebDavResponse.Status.SC_METHOD_NOT_ALLOWED.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_MULTI_STATUS, WebDavResponse.Status.SC_MULTISTATUS.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_NOT_FOUND, WebDavResponse.Status.SC_NOT_FOUND.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_NOT_IMPLEMENTED, WebDavResponse.Status.SC_NOT_IMPLEMENTED.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_NO_CONTENT, WebDavResponse.Status.SC_NO_CONTENT.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_OK, WebDavResponse.Status.SC_OK.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_PRECONDITION_FAILED, WebDavResponse.Status.SC_PRECONDITION_FAILED.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_UNAUTHORIZED, WebDavResponse.Status.SC_UNAUTHORIZED.value);
        Assert.assertEquals("status code must match", HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, WebDavResponse.Status.SC_UNSUPPORTED_MEDIA_TYPE.value);
    }
}
