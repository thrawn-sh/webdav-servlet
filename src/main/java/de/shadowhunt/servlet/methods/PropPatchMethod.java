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
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.shadowhunt.servlet.methods.properties.PropertiesMessageHelper;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Property;
import de.shadowhunt.servlet.webdav.Store;

public class PropPatchMethod extends AbstractWebDavMethod {

    static {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(Property.DAV_NS_CONTEXT);
        try {
            EXPRESSION = xpath.compile("//D:prop/*");
        } catch (final XPathExpressionException e) {
            throw new InstantiationError(e.getMessage());
        }
    }

    private static final XPathExpression EXPRESSION;

    public static final String METHOD = "PROPPATCH";

    public PropPatchMethod(final Store store) {
        super(METHOD, store);
    }

    private boolean isLiveProperty(final Property property) {
        return Property.DAV_NAMESPACE.equals(property.getNameSpace());
    }

    private boolean isSaveOrUpdate(final Node node) {
        final Node parent = node.getParentNode().getParentNode(); // ... (<D:set>|<D:remove>)<prop> ...
        return "set".equals(parent.getLocalName());
    }

    @Override
    public WebDavResponse service(final Path path, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(path)) {
            return StatusResponse.NOT_FOUND;
        }

        final Document document = PropertiesMessageHelper.parse(request.getInputStream());
        if (document == null) {
            return StatusResponse.BAD_REQUEST;
        }

        final Map<Property, String> properties = new TreeMap<>();
        properties.putAll(store.getProperties(path));
        try {
            final NodeList nodes = (NodeList) EXPRESSION.evaluate(document, XPathConstants.NODESET);
            final int length = nodes.getLength();
            for (int i = 0; i < length; i++) {
                final Node node = nodes.item(i);
                final Property property = new Property(StringUtils.trimToEmpty(node.getNamespaceURI()), node.getLocalName());

                // DAV namespace is only for live property (can not be handled by client) => ignore silently
                if (isSaveOrUpdate(node) && !isLiveProperty(property)) {
                    final String content = StringEscapeUtils.unescapeXml(node.getTextContent());
                    properties.put(property, content);
                } else {
                    properties.remove(property);
                }
            }
        } catch (final Exception e) {
            return StatusResponse.BAD_REQUEST;
        }

        store.setProperties(path, properties);
        return StatusResponse.CREATED;
    }
}
