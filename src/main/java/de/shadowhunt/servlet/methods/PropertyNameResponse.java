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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Property;

class PropertyNameResponse extends AbstractPropertiesResponse {

    private final Map<Path, Set<Property>> entries;

    public PropertyNameResponse(final String baseUri, final Map<Path, Set<Property>> entries) {
        super(baseUri);
        this.entries = entries;
    }

    private void announceNameSpacePrefixes(final PrintWriter writer, final Collection<Set<Property>> values, final Map<String, String> nameSpaceMapping) {
        for (final Collection<Property> properties : values) {
            announceNameSpacePrefixes0(writer, properties, nameSpaceMapping);
        }
    }

    @Override
    public void write(final HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml");
        response.setStatus(207); // FIXME constant

        final Map<String, String> nameSpaceMapping = new HashMap<>();
        nameSpaceMapping.put(Property.DAV_NAMESPACE, Property.DEFAULT_DAV_PREFIX + ":");

        final PrintWriter writer = response.getWriter();
        writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?><D:multistatus xmlns:D=\"");
        writer.print(Property.DAV_NAMESPACE);
        writer.print("\"");
        announceNameSpacePrefixes(writer, entries.values(), nameSpaceMapping);
        writer.write('>'); // multistatus

        for (final Map.Entry<Path, Set<Property>> entry : entries.entrySet()) {
            writer.print("<D:response>");
            final Path path = entry.getKey();
            final Set<Property> properties = entry.getValue();

            writer.print("<D:href>");
            writer.print(baseUri);
            writer.print(StringEscapeUtils.escapeXml10(path.toString()));
            writer.print("</D:href><D:propstat><D:prop>");

            for (final Property property : properties) {
                writer.print("<");
                writer.print(nameSpaceMapping.get(property.getNameSpace()));
                writer.print(property.getName());
                writer.print("/>");
            }

            writer.print("</D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response>");
        }
        writer.print("</D:multistatus>");
    }
}
