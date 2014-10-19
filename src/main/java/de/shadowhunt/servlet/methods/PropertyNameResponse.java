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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.PropertyIdentifier;

class PropertyNameResponse extends AbstractPropertiesResponse {

    private final Map<Path, Collection<PropertyIdentifier>> entries;

    public PropertyNameResponse(final Entity entity, final String baseUri, final Map<Path, Collection<PropertyIdentifier>> entries) {
        super(entity, baseUri);
        this.entries = entries;
    }

    private void announceNameSpacePrefixes(final PrintWriter writer, final Collection<Set<PropertyIdentifier>> values, final Map<String, String> nameSpaceMapping) {
        for (final Collection<PropertyIdentifier> properties : values) {
            announceNameSpacePrefixes0(writer, properties, nameSpaceMapping);
        }
    }

    private Map<String, String> announceNameSpacePrefixes(final XMLStreamWriter writer) throws XMLStreamException {
        final Map<String, String> prefixes = new HashMap<>();
        writer.setPrefix(PropertyIdentifier.DEFAULT_DAV_PREFIX, PropertyIdentifier.DAV_NAMESPACE);
        prefixes.put(PropertyIdentifier.DAV_NAMESPACE, PropertyIdentifier.DEFAULT_DAV_PREFIX);

        for (final Collection<PropertyIdentifier> identifiers : entries.values()) {
            for (PropertyIdentifier identifier : identifiers) {
                final String nameSpace = identifier.getNameSpace();
                if (prefixes.containsKey(nameSpace)) {
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
            for (final Map.Entry<Path, Collection<PropertyIdentifier>> entry : entries.entrySet()) {
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "response");
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "href");
                final Path path = entry.getKey();
                writer.writeCharacters(baseUri + path.toString());
                writer.writeEndElement();
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "propstat");
                writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "prop");
                for (final PropertyIdentifier identifier : entry.getValue()) {
                    writer.writeEmptyElement(identifier.getNameSpace(), identifier.getName());
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
            throw new ServletException("can not write response", e);
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
