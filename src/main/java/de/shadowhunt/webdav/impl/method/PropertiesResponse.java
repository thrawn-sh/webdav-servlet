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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.webdav.Entity;
import de.shadowhunt.webdav.Path;
import de.shadowhunt.webdav.Property;
import de.shadowhunt.webdav.PropertyIdentifier;

import org.apache.commons.lang3.StringUtils;

class PropertiesResponse extends AbstractPropertiesResponse {

    private final Map<Path, Collection<Property>> entries;

    private final Set<PropertyIdentifier> requested;

    PropertiesResponse(final Entity entity, final String baseUri, final Set<PropertyIdentifier> requested, final Map<Path, Collection<Property>> entries) {
        super(entity, baseUri);
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
        final Map<String, String> prefixes = new HashMap<>();
        writer.setPrefix(PropertyIdentifier.DEFAULT_DAV_PREFIX, PropertyIdentifier.DAV_NAMESPACE);
        prefixes.put(PropertyIdentifier.DAV_NAMESPACE, PropertyIdentifier.DEFAULT_DAV_PREFIX);

        for (final Collection<Property> properties : entries.values()) {
            for (final Property property : properties) {
                announce(writer, property.getIdentifier(), prefixes);
            }
        }
        for (final PropertyIdentifier identifier : requested) {
            announce(writer, identifier, prefixes);
        }
        return prefixes;
    }

    private Collection<Property> getAvailable(final Collection<Property> properties) {
        final Collection<Property> available = new ArrayList<>();

        for (final Property property : properties) {
            final PropertyIdentifier identifier = property.getIdentifier();
            if (requested.contains(identifier)) {
                available.add(property);
            }
        }
        return available;
    }

    private Collection<PropertyIdentifier> getMissing(final Collection<Property> available) {
        final Collection<PropertyIdentifier> missing = new HashSet<>(requested);

        for (final Property property : available) {
            final PropertyIdentifier identifier = property.getIdentifier();
            missing.remove(identifier);
        }
        return missing;
    }

    @Override
    protected void write0(final HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml");
        response.setStatus(207); // FIXME constant

        try {
            final XMLOutputFactory factory = XMLOutputFactory.newFactory();

            final XMLStreamWriter writer = factory.createXMLStreamWriter(response.getOutputStream(), "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            final Map<String, String> prefixes = announceNameSpacePrefixes(writer);
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "multistatus");
            writeNameSpaceDeclarations(writer, prefixes);
            for (final Map.Entry<Path, Collection<Property>> entry : entries.entrySet()) {
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "response");
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "href");
                final Path path = entry.getKey();
                writer.writeCharacters(baseUri + path.toString());
                writer.writeEndElement();
                final Collection<Property> available = getAvailable(entry.getValue());
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
            throw new ServletException("can not write response", e);
        }
    }

    private void writeAvailable(final XMLStreamWriter writer, final Collection<Property> properties) throws XMLStreamException {
        if (!properties.isEmpty()) {
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "propstat");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "prop");
            for (final Property property : properties) {
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
