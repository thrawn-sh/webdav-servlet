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
import java.util.Collection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.property.AbstractWebDavProperty;
import de.shadowhunt.webdav.property.PropertyIdentifier;
import de.shadowhunt.webdav.property.StringWebDavProperty;
import de.shadowhunt.webdav.property.WebDavProperty;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavStore;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PropPatchMethod extends AbstractWebDavMethod {

    private static final class DummyProperty extends AbstractWebDavProperty {
        private DummyProperty(final PropertyIdentifier identifier) {
            super(identifier);
        }

        @Override
        public String getValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(final XMLStreamWriter writer) throws XMLStreamException {
            // nothing to do
        }
    }

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

    @Override
    public Method getMethod() {
        return Method.PROPPATCH;
    }

    private boolean isLiveProperty(final PropertyIdentifier propertyIdentifier) {
        return PropertyIdentifier.DAV_NAMESPACE.equals(propertyIdentifier.getNameSpace());
    }

    private boolean isSaveOrUpdate(final Node node) {
        final Node parent = node.getParentNode().getParentNode(); // ... (<D:set>|<D:remove>)<prop> ...
        return "set".equalsIgnoreCase(parent.getLocalName());
    }

    @Override
    public WebDavResponseWriter service(final WebDavStore store, final WebDavRequest request) throws IOException {
        final WebDavPath path = request.getPath();
        if (!store.exists(path)) {
            return AbstractBasicResponse.createNotFound();
        }

        final WebDavEntity entity = store.getEntity(path);
        checkUp(store, path, determineLockTokens(request));

        final Document document = PropertiesMessageHelper.parse(request.getInputStream());
        if (document == null) {
            return AbstractBasicResponse.createBadRequest(entity);
        }

        final Collection<WebDavProperty> properties = store.getProperties(path);
        try {
            final NodeList nodes = (NodeList) EXPRESSION.evaluate(document, XPathConstants.NODESET);
            final int length = nodes.getLength();
            for (int i = 0; i < length; i++) {
                final Node node = nodes.item(i);
                final String nameSpace = StringUtils.trimToEmpty(node.getNamespaceURI());
                final String name = node.getLocalName();
                final PropertyIdentifier propertyIdentifier = new PropertyIdentifier(nameSpace, name);

                // DAV namespace is only for live property (can not be handled by client) => ignore silently
                if (!isLiveProperty(propertyIdentifier)) {
                    if (isSaveOrUpdate(node)) {
                        final String content = StringEscapeUtils.unescapeXml(node.getTextContent());
                        final StringWebDavProperty property = new StringWebDavProperty(propertyIdentifier, content);
                        properties.remove(property); // remove old entry
                        properties.add(property);
                    } else {
                        properties.remove(new DummyProperty(propertyIdentifier));
                    }
                }
            }
        } catch (final Exception e) {
            return AbstractBasicResponse.createBadRequest(entity);
        }

        store.setProperties(path, properties);
        return AbstractBasicResponse.createCreated(entity);
    }
}
