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

import java.util.Locale;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.webdav.store.SupportedLock;
import de.shadowhunt.webdav.store.WebDavLock.LockScope;
import de.shadowhunt.webdav.store.WebDavLock.LockType;

public class SupportedLocksProperty extends AbstractWebDavProperty {

    private Set<SupportedLock> locks;

    public SupportedLocksProperty(final Set<SupportedLock> locks) {
        super(PropertyIdentifier.RESOURCE_TYPE_IDENTIFIER);
        this.locks = locks;
    }

    @Override
    public String getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final XMLStreamWriter writer) throws XMLStreamException {
        for (final SupportedLock lock : locks) {
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, PropertyIdentifier.LOCK_IDENTIFIER.getName());
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "lockentry");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "lockscope");
            final LockScope scope = lock.getScope();
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, scope.name().toLowerCase(Locale.US));
            writer.writeEndElement();
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "locktype");
            final LockType type = lock.getType();
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, type.name().toLowerCase(Locale.US));
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }
}
