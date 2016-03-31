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
package de.shadowhunt.webdav.method;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavResponse.Status;
import de.shadowhunt.webdav.property.PropertyIdentifier;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavLock;

class LockDiscoveryResponse extends AbstractBasicResponse {

    public static final String LOCK_TOKEN = "Lock-Token";

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
        response.addHeader(LOCK_TOKEN, "<" + WebDavLock.PREFIX + token + ">");
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

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "depth");
            final int depth = lock.getDepth();
            if (depth == Integer.MAX_VALUE) {
                writer.writeCharacters("infinity");
            } else {
                writer.writeCharacters(Integer.toString(depth));
            }
            writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "owner");
            writer.writeCharacters(lock.getOwner());
            writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "timeout");
            final int timeout = lock.getTimeoutInSeconds();
            if (timeout >= 0) {
                writer.writeCharacters("Seconds-");
                writer.writeCharacters(Integer.toString(timeout));
            } else {
                writer.writeCharacters("Infinite");
            }
            writer.writeEndElement();

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "locktoken");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "href");
            writer.writeCharacters(WebDavLock.PREFIX);
            writer.writeCharacters(token.toString());
            writer.writeEndElement(); // href
            writer.writeEndElement(); // locktoken

            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "lockroot");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "href");
            final WebDavPath root = lock.getRoot();
            writer.writeCharacters(root.getValue());
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
