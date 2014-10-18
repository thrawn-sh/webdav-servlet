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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Property;

class PropertiesResponse extends AbstractPropertiesResponse {

    private final Map<Path, Map<Property, String>> entries;

    private final Set<Property> requested;

    public PropertiesResponse(final String baseUri, final Set<Property> requested, final Map<Path, Map<Property, String>> entries) {
        super(baseUri);
        this.requested = requested;
        this.entries = entries;
    }

    private void announceNameSpacePrefixes(final PrintWriter writer, final Map<String, String> nameSpaceMapping) {
        for (final Map<Property, String> properties : entries.values()) {
            announceNameSpacePrefixes0(writer, properties.keySet(), nameSpaceMapping);
        }
        announceNameSpacePrefixes0(writer, requested, nameSpaceMapping);
    }

    private String collectMissingProperties(final Map<String, String> nameSpaceMapping, final Set<Property> available) {
        final StringBuilder sb = new StringBuilder();
        for (final Property property : requested) {
            if (available.contains(property)) {
                continue;
            }
            sb.append('<');
            final String nameSpace = property.getNameSpace();
            sb.append(nameSpaceMapping.get(nameSpace));
            sb.append(property.getName());
            sb.append("/>");
        }
        return sb.toString();
    }

    @Override
    public void write(final HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml");
        response.setStatus(207); // FIXME constant

        final Map<String, String> nameSpaceMapping = new HashMap<>();
        nameSpaceMapping.put(Property.DAV_NAMESPACE, Property.DEFAULT_DAV_PREFIX + ":");

        final PrintWriter writer = response.getWriter();
        writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.print("<D:multistatus xmlns:D=\"");
        writer.print(Property.DAV_NAMESPACE);
        writer.print("\"");
        announceNameSpacePrefixes(writer, nameSpaceMapping);
        writer.write('>'); // multistatus

        for (final Map.Entry<Path, Map<Property, String>> entry : entries.entrySet()) {
            writer.print("<D:response><D:href>");
            writer.print(StringEscapeUtils.escapeXml10(baseUri));
            writer.print(StringEscapeUtils.escapeXml10(entry.getKey().toString()));
            writer.print("</D:href>");

            final Map<Property, String> map = entry.getValue();
            final String available = collectAvailableProperties(nameSpaceMapping, map);
            if (StringUtils.isNotEmpty(available)) {
                writer.print("<D:propstat><D:prop>");
                writer.print(available);
                writer.print("</D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat>");
            }

            final String missing = collectMissingProperties(nameSpaceMapping, map.keySet());
            if (StringUtils.isNotEmpty(missing)) {
                writer.print("<D:propstat><D:prop>");
                writer.print(missing);
                writer.print("</D:prop><D:status>HTTP/1.1 404 Not Found</D:status></D:propstat>");
            }
            writer.print("</D:response>");
        }
        writer.print("</D:multistatus>");
    }

    private String collectAvailableProperties(final Map<String, String> nameSpaceMapping, final Map<Property, String> properties) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<Property, String> propertyEntry : properties.entrySet()) {
            final Property property = propertyEntry.getKey();
            if (!requested.contains(property)) {
                continue;
            }

            final String nameSpace = property.getNameSpace();
            final String name = nameSpaceMapping.get(nameSpace) + property.getName();
            sb.append('<');
            sb.append(name);
            sb.append('>');
            if (Property.DAV_NAMESPACE.equals(nameSpace)) {
                sb.append(propertyEntry.getValue());
            } else {
                sb.append(StringEscapeUtils.escapeXml10(propertyEntry.getValue()));
            }
            sb.append("</");
            sb.append(name);
            sb.append('>');
        }
        return sb.toString();
    }
}
