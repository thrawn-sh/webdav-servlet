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
package de.shadowhunt.servlet.webdav;

public abstract class AbstractProperty implements Property {

    protected final PropertyIdentifier identifier;

    protected AbstractProperty(final PropertyIdentifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractProperty)) {
            return false;
        }

        final AbstractProperty property = (AbstractProperty) o;

        if (!identifier.equals(property.identifier)) {
            return false;
        }

        return true;
    }

    @Override
    public final PropertyIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public final int hashCode() {
        return identifier.hashCode();
    }
}
