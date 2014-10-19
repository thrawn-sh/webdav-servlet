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
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Path;

class HtmlListingResponse extends BasicResponse {

    private static final Comparator<Entity> LISTING_COMPARATOR = new Comparator<Entity>() {

        @Override
        public int compare(final Entity e1, final Entity e2) {
            final int result = Integer.compare(e1.getType().priority, e2.getType().priority);
            if (result != 0) {
                return result;
            }
            return e1.getPath().compareTo(e2.getPath());
        }
    };

    private final String cssPath;

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private final List<Entity> entities;

    public HtmlListingResponse(final Entity root, final List<Entity> entities, @Nullable final String cssPath) {
        super(root);
        this.entities = entities;
        this.cssPath = cssPath;
    }

    @Override
    protected void write0(final HttpServletResponse response) throws ServletException, IOException {
        Collections.sort(entities, LISTING_COMPARATOR);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");

        final PrintWriter writer = response.getWriter();
        writer.print("<!DOCTYPE html><html><head>");
        writer.print("<title>Content of folder ");
        final Path path = entity.getPath();
        writer.print(StringEscapeUtils.escapeHtml4(path.toString()));
        writer.print("</title>");

        if (cssPath != null) {
            writer.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
            writer.print(cssPath);
            writer.print("\"/>");
        }

        writer.print("</head><body><table><thead><tr><th class=\"name\">Name</th><th class=\"size\">Size</th><th class=\"modified\">Modified</th></tr></thead><tbody>");
        if (Path.ROOT.equals(path)) {
            // do not leave WebDav
            writer.print("<tr class=\"folder parent\"><td colspan=\"3\"><a href=\".\">Parent</a></td></tr>");
        } else {
            writer.print("<tr class=\"folder parent\"><td colspan=\"3\"><a href=\"..\">Parent</a></td></tr>");
        }

        for (final Entity entity : entities) {
            final String entityName = entity.getName();
            final String link = URLEncoder.encode(entityName, response.getCharacterEncoding());
            final String entityNameHtml = StringEscapeUtils.escapeHtml4(entityName);
            if (Entity.Type.COLLECTION == entity.getType()) {
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
    }
}
