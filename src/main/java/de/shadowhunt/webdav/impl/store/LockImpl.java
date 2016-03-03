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
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, "write"); // FIXME
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private final String owner;

    private final Scope scope;

    private final String token;

    LockImpl(final String token, final Scope scope, final String owner) {
        this.owner = owner;
        this.token = token;
        this.scope = scope;
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
    public String getOwner() {
        return owner;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public String getToken() {
        return token;
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
        final StringBuilder sb = new StringBuilder("Lock{");
        sb.append("owner=").append(owner);
        sb.append(", token=").append(token);
        sb.append(", scope=").append(scope);
        sb.append('}');
        return sb.toString();
    }
}
