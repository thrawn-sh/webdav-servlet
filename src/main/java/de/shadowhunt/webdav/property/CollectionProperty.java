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
package de.shadowhunt.webdav.property;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public final class CollectionProperty extends AbstractWebDavProperty {

    public CollectionProperty() {
        super(PropertyIdentifier.RESOURCE_TYPE_IDENTIFIER);
    }

    @Override
    public String getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, PropertyIdentifier.RESOURCE_TYPE_IDENTIFIER.getName());
        writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, "collection");
        writer.writeEndElement();
    }
}
