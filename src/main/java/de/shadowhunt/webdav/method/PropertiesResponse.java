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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.webdav.WebDavConstant.Status;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.property.PropertyIdentifier;
import de.shadowhunt.webdav.property.WebDavProperty;
import de.shadowhunt.webdav.store.WebDavEntity;

import org.apache.commons.lang3.StringUtils;

class PropertiesResponse extends AbstractBasicResponse {

    private final String baseUri;

    private final Map<WebDavPath, Collection<WebDavProperty>> entries;

    private final Set<PropertyIdentifier> requested;

    PropertiesResponse(final WebDavEntity entity, final String baseUri, final Set<PropertyIdentifier> requested, final Map<WebDavPath, Collection<WebDavProperty>> entries) {
        super(entity);
        this.baseUri = baseUri;
        this.requested = requested;
        this.entries = entries;
    }

    private void announce(final XMLStreamWriter writer, final PropertyIdentifier identifier, final Map<String, String> prefixes) throws XMLStreamException {
        final String nameSpace = identifier.getNameSpace();
        if (prefixes.containsKey(nameSpace)) {
            return;
        }

        if (StringUtils.isEmpty(nameSpace)) {
            return;
        }

        final String prefix = "ns" + prefixes.size();
        writer.setPrefix(prefix, nameSpace);
        prefixes.put(nameSpace, prefix);
    }

    private Map<String, String> announceNameSpacePrefixes(final XMLStreamWriter writer) throws XMLStreamException {
        final Map<String, String> prefixes = new TreeMap<>();
        writer.setPrefix(PropertyIdentifier.DEFAULT_DAV_PREFIX, PropertyIdentifier.DAV_NAMESPACE);
        prefixes.put(PropertyIdentifier.DAV_NAMESPACE, PropertyIdentifier.DEFAULT_DAV_PREFIX);

        final Set<PropertyIdentifier> all = new TreeSet<>();
        all.addAll(requested);
        for (final Collection<WebDavProperty> properties : entries.values()) {
            for (final WebDavProperty property : properties) {
                all.add(property.getIdentifier());
            }
        }

        for (final PropertyIdentifier identifier : all) {
            announce(writer, identifier, prefixes);
        }
        return prefixes;
    }

    private Collection<WebDavProperty> getAvailable(final Collection<WebDavProperty> properties) {
        final Collection<WebDavProperty> available = new TreeSet<>();

        for (final WebDavProperty property : properties) {
            final PropertyIdentifier identifier = property.getIdentifier();
            if (requested.contains(identifier)) {
                available.add(property);
            }
        }
        return available;
    }

    private Collection<PropertyIdentifier> getMissing(final Collection<WebDavProperty> available) {
        final Collection<PropertyIdentifier> missing = new TreeSet<>(requested);

        for (final WebDavProperty property : available) {
            final PropertyIdentifier identifier = property.getIdentifier();
            missing.remove(identifier);
        }
        return missing;
    }

    @Override
    protected void write0(final WebDavResponse response) throws IOException {
        response.setCharacterEncoding(DEFAULT_ENCODING);
        response.setContentType("application/xml");
        response.setStatus(Status.MULTI_STATUS);

        try {
            final XMLOutputFactory factory = XMLOutputFactory.newFactory();

            final XMLStreamWriter writer = factory.createXMLStreamWriter(response.getOutputStream(), DEFAULT_ENCODING);
            writer.writeStartDocument(DEFAULT_ENCODING, "1.0");
            final Map<String, String> prefixes = announceNameSpacePrefixes(writer);
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "multistatus");
            writeNameSpaceDeclarations(writer, prefixes);
            for (final Map.Entry<WebDavPath, Collection<WebDavProperty>> entry : entries.entrySet()) {
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "response");
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "href");
                final WebDavPath path = entry.getKey();
                writer.writeCharacters(baseUri + path.toString());
                writer.writeEndElement();
                final Collection<WebDavProperty> available = getAvailable(entry.getValue());
                writeAvailable(writer, available);
                final Collection<PropertyIdentifier> missing = getMissing(available);
                writeMissing(writer, missing);
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

    private void writeAvailable(final XMLStreamWriter writer, final Collection<WebDavProperty> properties) throws XMLStreamException {
        if (!properties.isEmpty()) {
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "propstat");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "prop");
            for (final WebDavProperty property : properties) {
                property.write(writer);
            }
            writer.writeEndElement();
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "status");
            writer.writeCharacters("HTTP/1.1 200 OK");
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private void writeMissing(final XMLStreamWriter writer, final Collection<PropertyIdentifier> identifiers) throws XMLStreamException {
        if (!identifiers.isEmpty()) {
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "propstat");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "prop");
            for (final PropertyIdentifier identifier : identifiers) {
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
            writer.writeCharacters("HTTP/1.1 404 Not Found");
            writer.writeEndElement();
            writer.writeEndElement();
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
