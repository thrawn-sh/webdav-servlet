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

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.shadowhunt.servlet.webdav.Property;

public abstract class AbstractPropertiesResponse implements WebDavResponse {

    protected final String baseUri;

    protected AbstractPropertiesResponse(final String baseUri) {
        this.baseUri = baseUri;
    }

    protected void announceNameSpacePrefixes0(final PrintWriter writer, final Collection<Property> properties, final Map<String, String> nameSpaceMapping) {
        for (final Property property : properties) {
            final String nameSpace = property.getNameSpace();
            if (nameSpaceMapping.containsKey(nameSpace)) {
                continue;
            }

            if (StringUtils.isEmpty(nameSpace)) {
                nameSpaceMapping.put("", "");
                continue;
            }

            final String prefix = "ns" + nameSpaceMapping.size();
            nameSpaceMapping.put(nameSpace, prefix + ":");

            writer.print(" xmlns:");
            writer.print(prefix);
            writer.print("=\"");
            writer.print(nameSpace);
            writer.print("\"");
        }
    }
}
