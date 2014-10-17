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
package de.shadowhunt.servlet.methods.properties;

import java.util.Set;

import de.shadowhunt.servlet.webdav.Property;

public class NameSpaceFilter implements PropertyFilter {

    private final Set<String> nameSpaces;

    private PropertyFilter next;

    public NameSpaceFilter(final Set<String> nameSpaces) {
        this(nameSpaces, ALLOW);
    }

    public NameSpaceFilter(final Set<String> nameSpaces, final PropertyFilter next) {
        this.nameSpaces = nameSpaces;
        this.next = next;
    }

    @Override
    public boolean filter(final Property property) {
        final String propertyNameSpace = property.getNameSpace();
        if (nameSpaces.contains(propertyNameSpace)) {
            return true;
        }
        return next.filter(property);
    }

    @Override
    public void setNext(final PropertyFilter next) {
        this.next = next;
    }
}
