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
package de.shadowhunt.webdav.impl.store;

import java.util.Locale;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavProperty;
import de.shadowhunt.webdav.impl.AbstractWebDavProperty;

@Immutable
final class LockImpl implements WebDavLock {

    private final class LockProperty extends AbstractWebDavProperty {
        private LockProperty() {
            super(PropertyIdentifier.LOCK_IDENTIFIER);
        }

        @Override
        public String getValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(final XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, PropertyIdentifier.LOCK_IDENTIFIER.getName());
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "lockentry");
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "lockscope");
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, getScope().name().toLowerCase(Locale.US));
            writer.writeEndElement();
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, "locktype");
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, getType().name().toLowerCase(Locale.US));
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private final LockNature nature;

    private final String owner;

    private final LockScope scope;

    private final int timeoutInSeconds;

    private final UUID token;

    private final LockType type;

    LockImpl(final UUID token, final LockScope scope, final LockType type, final LockNature nature, final int timeoutInSeconds, final String owner) {
        this.nature = nature;
        this.owner = owner;
        this.scope = scope;
        this.timeoutInSeconds = timeoutInSeconds;
        this.token = token;
        this.type = type;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LockImpl other = (LockImpl) obj;
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public LockNature getNature() {
        return nature;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public LockScope getScope() {
        return scope;
    }

    @Override
    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    @Override
    public UUID getToken() {
        return token;
    }

    @Override
    public LockType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        return result;
    }

    @Override
    public WebDavProperty toProperty() {
        return new LockProperty();
    }

    @Override
    public String toString() {
        return "LockImpl [owner=" + owner + ", scope=" + scope + ", token=" + token + ", type=" + type + ", timeoutInSeconds=" + timeoutInSeconds + "]";
    }
}
