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

import java.util.Comparator;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.Validate;

/**
 * {@link Property} represents a resource property
 */
@Immutable
public final class Property {

    /**
     * {@link Comparator} that compares {@link Property} by their type and name
     */
    public static final Comparator<Property> NAME_COMPARATOR = new Comparator<Property>() {

        @Override
        public int compare(final Property rp1, final Property rp2) {
            return rp1.getName().compareTo(rp2.getName());
        }
    };

    private final String name;

    private final String nameSpace;

    private String value;

    /**
     * Create a new {@link Property} with the given {@link Type}, name and value
     *
     * @param type {@link Type} of the {@link Property}
     * @param name name of the {@link Property}
     * @param value value of the {@link Property}
     *
     * @throws NullPointerException if any parameter is {@code null}
     */
    public Property(final String nameSpace, final String name, final String value) {
        Validate.notNull(nameSpace, "nameSpace must not be null");
        Validate.notNull(name, "name must not be null");
        Validate.notNull(value, "value must not be null");

        this.nameSpace = nameSpace;
        this.name = name;
        this.value = value;
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
        if (!value.equals(that.value)) {
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

    /**
     * Returns the value of the {@link Property}
     *
     * @return the value of the {@link Property}
     */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Property [name=");
        builder.append(name);
        builder.append(", value=");
        builder.append(value);
        builder.append(']');
        return builder.toString();
    }
}
