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
package de.shadowhunt.webdav.impl.method;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

class HtmlListingResponse extends AbstractBasicResponse {

    private final String cssPath;

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private final List<WebDavEntity> entities;

    HtmlListingResponse(final WebDavEntity root, final List<WebDavEntity> entities, final Optional<String> cssPath) {
        super(root);
        this.entities = entities;
        this.cssPath = cssPath.orElse(null);
    }

    @Override
    protected void write0(final WebDavResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        // response.setCharacterEncoding("UTF-8"); FIXME
        // response.setContentType("text/html");

        final PrintWriter writer = new PrintWriter(response.getOutputStream());
        writer.print("<!DOCTYPE html><html><head>");
        writer.print("<title>Content of folder ");
        final WebDavPath path = entity.getPath();
        writer.print(StringEscapeUtils.escapeHtml4(path.getValue()));
        writer.print("</title>");

        if (cssPath != null) {
            writer.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
            writer.print(cssPath);
            writer.print("\"/>");
        }

        writer.print("</head><body><table><thead><tr><th class=\"name\">Name</th><th class=\"size\">Size</th><th class=\"modified\">Modified</th></tr></thead><tbody>");
        if (WebDavPath.ROOT.equals(path)) {
            // do not leave WebDav
            writer.print("<tr class=\"folder parent\"><td colspan=\"3\"><a href=\".\">Parent</a></td></tr>");
        } else {
            writer.print("<tr class=\"folder parent\"><td colspan=\"3\"><a href=\"..\">Parent</a></td></tr>");
        }

        Collections.sort(entities);
        for (final WebDavEntity entity : entities) {
            final String entityName = entity.getName();
            final String link = URLEncoder.encode(entityName, "UTF-8"); // FIXME
            final String entityNameHtml = StringEscapeUtils.escapeHtml4(entityName);
            if (WebDavEntity.Type.COLLECTION == entity.getType()) {
                writer.print("<tr class=\"folder\"><td colspan=\"3\"><a href=\"./");
                writer.print(link);
                writer.print("/\">");
                writer.print(entityNameHtml);
                writer.print("</a></td></tr>");
            } else {
                writer.print("<tr class=\"file\"><td class=\"name\"><a href=\"./");
                writer.print(link);
                writer.print("\">");
                writer.print(entityNameHtml);
                writer.print("</a></td><td class=\"size\">");
                final String size = FileUtils.byteCountToDisplaySize(entity.getSize());
                writer.print(size);
                writer.print("</td><td class=\"modified\">");
                final Date lastModified = entity.getLastModified();
                final String formattedDate = dateFormat.format(lastModified);
                writer.print(formattedDate);
                writer.print("</td></tr>");
            }
        }

        writer.print("</tbody></table>");
        writer.print("</body></html>");
        writer.close();
    }
}
