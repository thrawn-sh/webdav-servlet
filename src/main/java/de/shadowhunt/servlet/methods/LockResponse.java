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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Lock;
import de.shadowhunt.servlet.webdav.PropertyIdentifier;

class LockResponse extends BasicResponse {

    public LockResponse(final Entity entity) {
        super(entity);
    }

    @Override
    protected void write0(final HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml");

        final Lock lock = entity.getLock();
        response.addHeader("Lock-Token", "<" + lock.getToken() + ">");

        final Map<String, String> nameSpaceMapping = new HashMap<>();
        nameSpaceMapping.put(PropertyIdentifier.DAV_NAMESPACE, PropertyIdentifier.DEFAULT_DAV_PREFIX + ":");

        final PrintWriter writer = response.getWriter();
        writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?><prop xmlns=\"");
        writer.print(PropertyIdentifier.DAV_NAMESPACE);
        writer.print("\"");
        writer.write('>'); // multistatus
        writer.print("<lockdiscovery><activelock>");
        writer.print("<lockscope><exclusive/></lockscope>"); // FIXME
        String owner = lock.getOwner();
        if (owner != null) {
            writer.print("<owner>");
            writer.print(StringEscapeUtils.escapeXml10(owner));
            writer.print("</owner>");
        }
        writer.print("<locktoken><href>");
        writer.print(lock.getToken());
        writer.print("</href></locktoken>");
        writer.print("</activelock></lockdiscovery>");
        writer.print("</prop>");
    }
}
