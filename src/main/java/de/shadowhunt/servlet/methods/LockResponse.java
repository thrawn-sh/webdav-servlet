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
package de.shadowhunt.servlet.methods;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Lock;
import de.shadowhunt.servlet.webdav.PropertyIdentifier;

class LockResponse extends BasicResponse {

    public LockResponse(final Entity entity) {
        super(entity);
    }

    @Override
    protected void write0(final HttpServletResponse response) throws ServletException, IOException {
        final Lock lock = entity.getLock();

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml");
        response.addHeader("Lock-Token", "<" + lock.getToken() + ">");
        // response.setStatus(207); // FIXME

        try {
            final XMLOutputFactory factory = XMLOutputFactory.newFactory();

            final XMLStreamWriter writer = factory.createXMLStreamWriter(response.getOutputStream(), "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.setPrefix(PropertyIdentifier.DEFAULT_DAV_PREFIX, PropertyIdentifier.DAV_NAMESPACE);
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "prop");
            writer.writeNamespace(PropertyIdentifier.DEFAULT_DAV_PREFIX, PropertyIdentifier.DAV_NAMESPACE);

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "lockdiscovery");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "activelock");

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "lockscope");
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, lock.getScope().name().toLowerCase(Locale.US));
            writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "locktype");
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, "write");
            writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "timeout");
            writer.writeCharacters("Seconds-3600");
            writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "depth");
            writer.writeCharacters("0");
            writer.writeEndElement();

            final String owner = lock.getOwner();
            if (StringUtils.isNotEmpty(owner)) {
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "owner");
                writer.writeCharacters(owner);
                writer.writeEndElement();
            }
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "locktoken");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "href");
            writer.writeCharacters(lock.getToken());
            writer.writeEndElement(); // href
            writer.writeEndElement(); // locktoken
            writer.writeEndElement(); // activelock
            writer.writeEndElement(); // lockdiscovery
            writer.writeEndElement(); // prop
            writer.writeCharacters("\r\n"); // required by some clients
            writer.close();
        } catch (final XMLStreamException e) {
            throw new ServletException("can not write response", e);
        }
    }
}
