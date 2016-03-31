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
package de.shadowhunt.webdav.method;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

class HtmlListingResponse extends AbstractBasicResponse {

    private static final FastDateFormat ISO_DATE_FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", Locale.US);

    private final String css;

    private final List<WebDavEntity> entities;

    private final WebDavEntity parent;

    HtmlListingResponse(final WebDavEntity parent, final WebDavEntity root, final List<WebDavEntity> entities, final Optional<String> css) {
        super(root);
        this.parent = parent;
        this.entities = entities;
        this.css = css.orElse(null);
    }

    protected String convertToLink(final WebDavPath path) {
        try {
            final URI uri = new URI("http", "www.example.net", path.getValue(), null);
            return StringUtils.removeStart(uri.toASCIIString(), "http://www.example.net");
        } catch (final URISyntaxException e) {
            throw new WebDavException("", e);
        }
    }

    @Override
    protected void write0(final WebDavResponse response) throws IOException {
        response.addHeader("Cache-Control", "no-store");
        response.setCharacterEncoding(DEFAULT_ENCODING);
        response.setContentType("text/html");
        response.setStatus(WebDavResponse.Status.SC_OK);

        final WebDavRequest request = response.getRequest();
        final WebDavPath base = WebDavPath.create(request.getBase());

        final WebDavPath path = entity.getPath();
        try (final OutputStreamWriter stream = new OutputStreamWriter(response.getOutputStream(), DEFAULT_CHARSET)) {

            final PrintWriter writer = new PrintWriter(stream);
            writer.print("<!DOCTYPE html><html><head>");
            writer.print("<title>Content of folder ");
            if (WebDavPath.ROOT.equals(path)) {
                writer.print('/');
            } else {
                writer.print(StringEscapeUtils.escapeHtml4(path.getValue()));
            }
            writer.print("</title>");

            if (css != null) {
                writer.print("<style>");
                writer.print(css);
                writer.print("</style>");
            }

            writer.print("</head><body><table><thead><tr>");
            writer.print("<th class=\"name\">Name</th>");
            writer.print("<th class=\"size\">Size</th>");
            writer.print("<th class=\"modified\">Modified</th>");
            writer.print("<th class=\"operation\">Operations</th>");
            writer.print("</tr></thead><tbody>");

            writeCollection(writer, parent, base);

            Collections.sort(entities);
            for (final WebDavEntity entity : entities) {
                if (WebDavEntity.Type.COLLECTION == entity.getType()) {
                    writeCollection(writer, entity, base);
                } else {
                    writeItem(writer, entity, base);
                }
            }

            writer.print("</tbody></table>");
            writer.print("</body></html>");
            writer.close();
        }
    }

    private void writeCollection(final PrintWriter writer, final WebDavEntity entity, final WebDavPath base) {
        final WebDavPath path = entity.getPath();
        final String link = convertToLink(base.append(path));

        writer.print("<tr class=\"folder");
        if (parent.equals(entity)) {
            writer.print(" parent");
        } else {
            final Optional<WebDavLock> lock = entity.getLock();
            if (lock.isPresent()) {
                writer.print(" lock");
            }
        }
        writer.print("\"><td class=\"name\"><a href=\"");
        writer.print(link);
        writer.print("/\">");
        if (parent.equals(entity)) {
            writer.print("..");
        } else {
            final String name = entity.getName();
            final String nameHtml = StringEscapeUtils.escapeHtml4(name);
            writer.print(nameHtml);
        }
        writer.print("</a></td>");

        writer.print("<td class=\"size\">");
        final String size = FileUtils.byteCountToDisplaySize(entity.getSize());
        writer.print(size);
        writer.print("</td>");

        writer.print("<td class=\"modified\">");
        final Date lastModified = entity.getLastModified();
        final String formattedDate = ISO_DATE_FORMATTER.format(lastModified);
        writer.print(formattedDate);
        writer.print("</td>");

        writer.print("<td class=\"operation\"></td>");
        writer.print("</tr>");
    }

    private void writeItem(final PrintWriter writer, final WebDavEntity entity, final WebDavPath base) {
        final WebDavPath path = entity.getPath();
        final String link = convertToLink(base.append(path));

        final String name = entity.getName();
        final String nameHtml = StringEscapeUtils.escapeHtml4(name);

        writer.print("<tr class=\"file");
        final Optional<WebDavLock> lock = entity.getLock();
        if (lock.isPresent()) {
            writer.print(" lock");
        }
        writer.print("\">");

        writer.print("<td class=\"name\"><a href=\"");
        writer.print(link);
        writer.print("\">");
        writer.print(nameHtml);
        writer.print("</a></td>");

        writer.print("<td class=\"size\">");
        final String size = FileUtils.byteCountToDisplaySize(entity.getSize());
        writer.print(size);
        writer.print("</td>");

        writer.print("<td class=\"modified\">");
        final Date lastModified = entity.getLastModified();
        final String formattedDate = ISO_DATE_FORMATTER.format(lastModified);
        writer.print(formattedDate);
        writer.print("</td>");

        writer.print("<td class=\"operation\">");
        writer.print("<a href=\"");
        writer.print(link);
        writer.print("\" download></a>");
        writer.print("</td>");
        writer.print("</tr>");
    }
}
