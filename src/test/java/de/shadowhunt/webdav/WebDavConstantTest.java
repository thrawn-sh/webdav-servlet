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

import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.webdav.WebDavConstant.Status;

import org.junit.Assert;
import org.junit.Test;

public class WebDavConstantTest {

    @Test
    public void testStatusCodes() throws Exception {
        Assert.assertEquals("status code must match", HttpServletResponse.SC_BAD_REQUEST, Status.BAD_REQUEST.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_CONFLICT, Status.CONFLICT.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_CREATED, Status.CREATED.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_FORBIDDEN, Status.FORBIDDEN.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Status.INTERNAL_SERVER_ERROR.value);
        Assert.assertEquals("status code must match", 423, Status.LOCKED.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_METHOD_NOT_ALLOWED, Status.METHOD_NOT_ALLOWED.value);
        Assert.assertEquals("status code must match", 207, Status.MULTI_STATUS.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_NOT_FOUND, Status.NOT_FOUND.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_NOT_IMPLEMENTED, Status.NOT_IMPLEMENTED.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_NO_CONTENT, Status.NO_CONTENT.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_OK, Status.OK.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_PRECONDITION_FAILED, Status.PRECONDITION_FAILED.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_UNAUTHORIZED, Status.UNAUTHORIZED.value);
        Assert.assertEquals("status code must match", HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, Status.UNSUPPORTED_MEDIA_TYPE.value);
    }
}
