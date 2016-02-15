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
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.shadowhunt.webdav.Entity;
import de.shadowhunt.webdav.Path;
import de.shadowhunt.webdav.Property;
import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.impl.AbstractProperty;
import de.shadowhunt.webdav.impl.StringProperty;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PropPatchMethod extends AbstractWebDavMethod {

    private static final XPathExpression EXPRESSION;

    static {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(PropertyIdentifier.DAV_NS_CONTEXT);
        try {
            EXPRESSION = xpath.compile("//D:prop/*");
        } catch (final XPathExpressionException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private boolean isLiveProperty(final PropertyIdentifier propertyIdentifier) {
        return PropertyIdentifier.DAV_NAMESPACE.equals(propertyIdentifier.getNameSpace());
    }

    private boolean isSaveOrUpdate(final Node node) {
        final Node parent = node.getParentNode().getParentNode(); // ... (<D:set>|<D:remove>)<prop> ...
        return "set".equals(parent.getLocalName());
    }

    @Override
    public WebDavResponse service(final WebDavStore store, final Path path, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(path)) {
            return AbstractBasicResponse.createNotFound();
        }

        final Entity entity = store.getEntity(path);

        final Document document = PropertiesMessageHelper.parse(request.getInputStream());
        if (document == null) {
            return AbstractBasicResponse.createBadRequest(entity);
        }

        final Collection<Property> properties = new HashSet<>();
        properties.addAll(store.getProperties(path));
        try {
            final NodeList nodes = (NodeList) EXPRESSION.evaluate(document, XPathConstants.NODESET);
            final int length = nodes.getLength();
            for (int i = 0; i < length; i++) {
                final Node node = nodes.item(i);
                final String nameSpace = StringUtils.trimToEmpty(node.getNamespaceURI());
                final String name = node.getLocalName();
                final PropertyIdentifier propertyIdentifier = new PropertyIdentifier(nameSpace, name);

                // DAV namespace is only for live property (can not be handled by client) => ignore silently
                if (isSaveOrUpdate(node) && !isLiveProperty(propertyIdentifier)) {
                    final String content = StringEscapeUtils.unescapeXml(node.getTextContent());
                    final StringProperty property = new StringProperty(propertyIdentifier, content);
                    properties.remove(property); // remove old entry
                    properties.add(property);
                } else {
                    properties.remove(new AbstractProperty(propertyIdentifier) {

                        @Override
                        public void write(final XMLStreamWriter writer) throws XMLStreamException {
                            // nothing to do
                        }

                        @Override
                        public String getValue() {
                            return null;
                        }
                    });
                }
            }
        } catch (final Exception e) {
            return AbstractBasicResponse.createBadRequest(entity);
        }

        store.setProperties(path, properties);
        return AbstractBasicResponse.createCreated(entity);
    }

    @Override
    public Method getMethod() {
        return Method.PROPPATCH;
    }
}
