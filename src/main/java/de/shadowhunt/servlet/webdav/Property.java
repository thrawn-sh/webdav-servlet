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

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.Validate;

/**
 * {@link Property} represents a resource property
 */
@Immutable
public final class Property implements Comparable<Property> {

    private final String name;

    private final String nameSpace;

    /**
     * Create a new {@link Property} with the given {@link Type}, name and value
     *
     * @param type {@link Type} of the {@link Property}
     * @param name name of the {@link Property}
     * @param value value of the {@link Property}
     *
     * @throws NullPointerException if any parameter is {@code null}
     */
    public Property(final String nameSpace, final String name) {
        Validate.notNull(nameSpace, "nameSpace must not be null");
        Validate.notNull(name, "name must not be null");

        this.nameSpace = nameSpace;
        this.name = name;
    }

    @Override
    public int compareTo(final Property o) {
        int result = nameSpace.compareTo(o.nameSpace);
        if (result != 0) {
            return result;
        }

        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Property)) {
            return false;
        }

        final Property that = (Property) o;

        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    /**
     * Returns the name of the {@link Property}
     *
     * @return the name of the {@link Property}
     */
    public String getName() {
        return name;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + nameSpace.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return nameSpace + ":" + name;
    }

}
