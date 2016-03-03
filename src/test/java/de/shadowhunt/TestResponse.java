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
package de.shadowhunt;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;

import org.junit.Assert;

public class TestResponse implements WebDavResponse {

    private static final ContentNormalizer DUMMY = new ContentNormalizer() {

        @Override
        public String normalize(final String content) {
            return content;
        }

    };
    
    private String characterEncoding = null;

    private final ByteArrayOutputStream content = new ByteArrayOutputStream();

    private String contentType = null;

    private final Map<String, String> headers = new HashMap<>();

    private final WebDavRequest request;

    private Status status = null;

    public TestResponse(final WebDavRequest request) {
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

    @CheckForNull
    public String getContent() {
        return getContent(DUMMY);
    }

    @CheckForNull
    public String getContent(final ContentNormalizer replacer) {
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
