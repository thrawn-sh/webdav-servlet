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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Property;

public class PropertiesResponse implements WebDavResponse {

    private final String baseUri;

    private final Map<Entity, Map<Property, String>> entries;

    public PropertiesResponse(final String baseUri, final Map<Entity, Map<Property, String>> entries) {
        this.baseUri = baseUri;
        this.entries = entries;
    }

    @Override
    public void write(final HttpServletResponse response) throws ServletException, IOException {
        final PrintWriter writer = response.getWriter();
        response.setContentType("text/xml");
        response.setStatus(207); // FIXME constant

        writer.print("<?xml version=\"1.0\" encoding=\"");
        writer.print(response.getCharacterEncoding());
        writer.print("\"?>");
        writer.print("<multistatus xmlns=\"DAV:\">");
        writer.print("<response>");
        for (Map.Entry<Entity, Map<Property, String>> entry : entries.entrySet()) {
            final Entity entity = entry.getKey();

            writer.print("<href>");
            writer.print(baseUri);
            writer.print(StringEscapeUtils.escapeXml10(entity.getPath().toString()));
            writer.print("</href>");
            writer.print("<propstat>");
            writer.print("<prop>");

            // live properties
            writer.print("<getcontentlength>");
            writer.print(entity.getSize());
            writer.print("</getcontentlength>");

            writer.print("<displayname>");
            writer.print(StringEscapeUtils.escapeXml10(entity.getName()));
            writer.print("</displayname>");

            if (entity.getType() == Entity.Type.COLLECTION) {
                writer.print("<resourcetype><collection/></resourcetype>");
            }

            // dead properties
            final Map<Property, String> properties = entry.getValue();
            for (final Map.Entry<Property, String> propertyEntry : properties.entrySet()) {
                final Property property = propertyEntry.getKey();

                final String name = property.getName();
                writer.print("<");
                writer.print(name);
                writer.print(">");
                writer.print(StringEscapeUtils.escapeXml10(propertyEntry.getValue()));
                writer.print("</");
                writer.print(name);
                writer.print(">");
            }
            writer.print("</prop>");
            writer.print("<status>HTTP/1.1 200 OK</status>");
            writer.print("</propstat>");
        }
        writer.print("</response>");
        writer.print("</multistatus>");
        writer.flush();
    }
}
