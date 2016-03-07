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
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavResponse.Status;

class LockDiscoveryResponse extends AbstractBasicResponse {

    private final Status status;

    LockDiscoveryResponse(final WebDavEntity entity, final Status status) {
        super(entity);
        this.status = status;
    }

    @Override
    protected void write0(final WebDavResponse response) throws IOException {
        final Optional<WebDavLock> l = entity.getLock();
        final WebDavLock lock = l.get();

        response.setCharacterEncoding(DEFAULT_ENCODING);
        response.setContentType("application/xml");
        final UUID token = lock.getToken();
        response.addHeader("Lock-Token", "<" + WebDavLock.PREFIX + token + ">");
        response.setStatus(status);

        try {
            final XMLOutputFactory factory = XMLOutputFactory.newFactory();

            final XMLStreamWriter writer = factory.createXMLStreamWriter(response.getOutputStream(), DEFAULT_ENCODING);
            writer.writeStartDocument(DEFAULT_ENCODING, "1.0");
            writer.setPrefix(PropertyIdentifier.DEFAULT_DAV_PREFIX, PropertyIdentifier.DAV_NAMESPACE);
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "prop");
            writer.writeNamespace(PropertyIdentifier.DEFAULT_DAV_PREFIX, PropertyIdentifier.DAV_NAMESPACE);

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "lockdiscovery");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "activelock");

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "lockscope");
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, lock.getScope().name().toLowerCase(Locale.US));
            writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "locktype");
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, lock.getType().name().toLowerCase(Locale.US));
            writer.writeEndElement();

            // TODO timeout not supported
            // writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "timeout");
            // writer.writeCharacters("Seconds-3600");
            // writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "depth");
            writer.writeCharacters("0");
            writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "owner");
            writer.writeCharacters(lock.getOwner());
            writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "locktoken");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "href");
            writer.writeCharacters(WebDavLock.PREFIX);
            writer.writeCharacters(token.toString());
            writer.writeEndElement(); // href
            writer.writeEndElement(); // locktoken
            writer.writeEndElement(); // activelock
            writer.writeEndElement(); // lockdiscovery
            writer.writeEndElement(); // prop
            writer.writeCharacters("\r\n"); // required by some clients
            writer.close();
        } catch (final XMLStreamException e) {
            throw new WebDavException("can not write response", e);
        }
    }
}
