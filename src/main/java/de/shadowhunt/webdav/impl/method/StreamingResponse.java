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
import java.io.InputStream;
import java.io.OutputStream;

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavResponse;

import org.apache.commons.io.IOUtils;

class StreamingResponse extends AbstractBasicResponse {

    private final InputStream input;

    StreamingResponse(final WebDavEntity entity, final InputStream input) {
        super(entity);
        this.input = input;
    }

    @Override
    protected void write0(final WebDavResponse response) throws IOException {
        response.setStatus(WebDavResponse.Status.SC_OK);
        final OutputStream output = response.getOutputStream();
        try {
            IOUtils.copy(input, output);
        } finally {
            // output is closed by servlet api
            IOUtils.closeQuietly(input);
        }
    }
}
