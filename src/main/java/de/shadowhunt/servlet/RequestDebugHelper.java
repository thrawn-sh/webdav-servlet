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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

final class RequestDebugHelper {

    private static class HttpServletRequestDebugWrapper extends HttpServletRequestWrapper {

        private final InputStream is;

        HttpServletRequestDebugWrapper(final HttpServletRequest request, final WebDavConfig config, final InputStream is) {
            super(request, config);
            this.is = is;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return is;
        }
    }

    static int counter = 1;

    static WebDavRequest generateAndDump(final HttpServletRequest request, final WebDavConfig config) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final int size = IOUtils.copy(request.getInputStream(), buffer);

        final File output = new File(FileUtils.getTempDirectory(), String.format("%05d", counter++) + "_" + request.getMethod() + ".xml");
        try {
            final XMLOutputFactory factory = XMLOutputFactory.newFactory();

            final XMLStreamWriter writer = factory.createXMLStreamWriter(new FileOutputStream(output), "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");

            writer.writeStartElement("request");

            writer.writeStartElement("method");
            writer.writeCharacters(request.getMethod());
            writer.writeEndElement();

            writer.writeStartElement("uri");
            writer.writeCharacters(request.getRequestURL().toString());
            writer.writeEndElement();

            writer.writeStartElement("base");
            writer.writeCharacters(request.getServletPath());
            writer.writeEndElement();

            writer.writeStartElement("headers");
            final Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                final String name = headerNames.nextElement();

                writer.writeStartElement("header");
                writer.writeAttribute("name", name);
                writer.writeCharacters(request.getHeader(name));
                writer.writeEndElement();
            }
            writer.writeEndElement();

            writer.writeStartElement("content");
            if (size > 0) {
                writer.writeCharacters(Base64.getEncoder().encodeToString(buffer.toByteArray()));
            }
            writer.writeEndElement();

            writer.writeEndElement(); // request
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new WebDavException("can not write response", e);
        }

        return new HttpServletRequestDebugWrapper(request, config, new ByteArrayInputStream(buffer.toByteArray()));
    }

    private RequestDebugHelper() {
        // prevent instantiation
    }
}
