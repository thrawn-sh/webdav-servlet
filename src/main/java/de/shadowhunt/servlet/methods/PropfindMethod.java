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
import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Store;

public class PropfindMethod extends AbstractWebDavMethod {

    public static final String METHOD = "PROPFIND";

    public PropfindMethod(final Store store) {
        super(METHOD, store);
    }

    @Override
    public WebDavResponse service(final Path path, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(path)) {
            return StatusResponse.NOT_FOUND;
        }

        final NodeList properties = getProperties(request.getInputStream());
        if (properties == null) {
            return StatusResponse.BAD_REQUEST;
        }


        return StatusResponse.CREATED;
    }

    @CheckForNull
    protected NodeList getProperties(final InputStream inputStream) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = factory.newDocumentBuilder();
            final Document doc = db.parse(inputStream);
            final Element propfind = doc.getDocumentElement();
            if (!"propfind".equalsIgnoreCase(propfind.getNodeName())) {
                return null;
            }
            final NodeList childNodes = propfind.getChildNodes();
            if (childNodes.getLength() != 1) {
                return null;
            }
            final Node prop = childNodes.item(0);
            if (!"prop".equalsIgnoreCase(prop.getNodeName())) {
                return null;
            }
            return prop.getChildNodes();
        } catch (Exception e) {
            return null;
        }
    }
}
