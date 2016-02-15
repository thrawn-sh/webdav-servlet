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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.webdav.Path;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavStore;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public abstract class AbstractWebDavMethodTest {

    private HttpServletResponse createHttpServletResponseMock(final Response r) throws Exception {
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        final ServletOutputStream stream = new ServletOutputStream() {

            @Override
            public void write(final int b) throws IOException {
                r.writeContent(b);
            }
        };
        final PrintWriter writer = new PrintWriter(stream);

        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();
                r.addHeader((String) arguments[0], (String) arguments[1]);
                return null;
            }
        }).when(response).addHeader(Matchers.anyString(), Matchers.anyString());

        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();
                r.setStatus((int) arguments[0]);
                return null;
            }
        }).when(response).sendError(Matchers.anyInt());

        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();
                r.setStatus((int) arguments[0]);
                return null;
            }
        }).when(response).setStatus(Matchers.anyInt());

        Mockito.when(response.getOutputStream()).thenReturn(stream);
        Mockito.when(response.getWriter()).thenReturn(writer);

        return response;
    }

    protected Response execute(final WebDavMethod method, final WebDavStore store, final Path path, final HttpServletRequest request) throws Exception {
        final Response result = new Response();
        final HttpServletResponse response = createHttpServletResponseMock(result);

        final WebDavResponse webdavResponse = method.service(store, path, request);
        webdavResponse.write(response);
        final PrintWriter writer = response.getWriter();
        writer.close();

        return result;
    }
}
