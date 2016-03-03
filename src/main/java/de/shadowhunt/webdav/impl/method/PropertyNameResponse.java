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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse;

import org.apache.commons.lang3.StringUtils;

class PropertyNameResponse extends AbstractBasicResponse {

    private final String baseUri;

    private final Map<WebDavPath, Collection<PropertyIdentifier>> entries;

    PropertyNameResponse(final WebDavEntity entity, final String baseUri, final Map<WebDavPath, Collection<PropertyIdentifier>> entries) {
        super(entity);
        this.baseUri = baseUri;
        this.entries = entries;
    }

    private Map<String, String> announceNameSpacePrefixes(final XMLStreamWriter writer) throws XMLStreamException {
        final Map<String, String> prefixes = new HashMap<>();
        writer.setPrefix(PropertyIdentifier.DEFAULT_DAV_PREFIX, PropertyIdentifier.DAV_NAMESPACE);
        prefixes.put(PropertyIdentifier.DAV_NAMESPACE, PropertyIdentifier.DEFAULT_DAV_PREFIX);

        for (final Collection<PropertyIdentifier> identifiers : entries.values()) {
            for (final PropertyIdentifier identifier : identifiers) {
                final String nameSpace = identifier.getNameSpace();
                if (prefixes.containsKey(nameSpace)) {
                    continue;
                }

                if (StringUtils.isEmpty(nameSpace)) {
                    continue;
                }

                final String prefix = "ns" + prefixes.size();
                writer.setPrefix(prefix, nameSpace);
                prefixes.put(nameSpace, prefix);
            }
        }
        return prefixes;
    }

    @Override
    protected void write0(final WebDavResponse response) throws IOException {
        response.setCharacterEncoding(DEFAULT_ENCODING);
        response.setContentType("application/xml");
        response.setStatus(WebDavResponse.Status.SC_MULTISTATUS);

        try {
            final XMLOutputFactory factory = XMLOutputFactory.newFactory();

            final XMLStreamWriter writer = factory.createXMLStreamWriter(response.getOutputStream(), DEFAULT_ENCODING);
            writer.writeStartDocument(DEFAULT_ENCODING, "1.0");
            final Map<String, String> prefixes = announceNameSpacePrefixes(writer);
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "multistatus");
            writeNameSpaceDeclarations(writer, prefixes);
            for (final Map.Entry<WebDavPath, Collection<PropertyIdentifier>> entry : entries.entrySet()) {
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "response");
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "href");
                final WebDavPath path = entry.getKey();
                writer.writeCharacters(baseUri + path.toString());
                writer.writeEndElement();
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "propstat");
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "prop");
                for (final PropertyIdentifier identifier : entry.getValue()) {
                    final String nameSpace = identifier.getNameSpace();
                    final String name = identifier.getName();
                    if (StringUtils.isEmpty(nameSpace)) {
                        writer.writeEmptyElement(name);
                    } else {
                        writer.writeEmptyElement(nameSpace, name);
                    }
                }
                writer.writeEndElement();
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "status");
                writer.writeCharacters("HTTP/1.1 200 OK");
                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.writeCharacters("\r\n"); // required by some clients
            writer.close();
        } catch (final XMLStreamException e) {
            throw new WebDavException("can not write response", e);
        }
    }

    private void writeNameSpaceDeclarations(final XMLStreamWriter writer, final Map<String, String> prefixes) throws XMLStreamException {
        for (final Map.Entry<String, String> entry : prefixes.entrySet()) {
            final String nameSpace = entry.getKey();
            final String prefix = entry.getValue();
            writer.writeNamespace(prefix, nameSpace);
        }
    }
}
