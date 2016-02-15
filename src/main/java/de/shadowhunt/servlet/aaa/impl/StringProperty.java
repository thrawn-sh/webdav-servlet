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
package de.shadowhunt.servlet.aaa.impl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.servlet.aaa.PropertyIdentifier;

import org.apache.commons.lang3.StringUtils;

public class StringProperty extends AbstractProperty {

    private String value;

    public StringProperty(final PropertyIdentifier identifier, final String value) {
        super(identifier);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public void write(final XMLStreamWriter writer) throws XMLStreamException {
        final String nameSpace = identifier.getNameSpace();
        final String name = identifier.getName();
        if (StringUtils.isEmpty(nameSpace)) {
            writer.writeStartElement(name);
        } else {
            writer.writeStartElement(nameSpace, name);
        }
        writer.writeCharacters(value);
        writer.writeEndElement();
    }
}
