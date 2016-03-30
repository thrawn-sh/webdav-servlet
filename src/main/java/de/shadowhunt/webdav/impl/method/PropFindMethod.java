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
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavProperty;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.impl.AbstractWebDavProperty;
import de.shadowhunt.webdav.impl.StringWebDavProperty;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PropFindMethod extends AbstractWebDavMethod {

    private static final class CollectionProperty extends AbstractWebDavProperty {
        private CollectionProperty() {
            super(PropertyIdentifier.RESOURCE_TYPE_IDENTIFIER);
        }

        @Override
        public String getValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(final XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement(PropertyIdentifier.DAV_NAMESPACE, PropertyIdentifier.RESOURCE_TYPE_IDENTIFIER.getName());
            writer.writeEmptyElement(PropertyIdentifier.DAV_NAMESPACE, "collection");
            writer.writeEndElement();
        }
    }

    private static final Set<PropertyIdentifier> ALL = new AbstractSet<PropertyIdentifier>() {

        @Override
        public boolean contains(final Object o) {
            return true;
        }

        @Override
        public Iterator<PropertyIdentifier> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public int size() {
            return Integer.MAX_VALUE;
        }
    };

    private static final XPathExpression ALL_EXPRESSION;

    private static final FastDateFormat ISO_DATE_FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    private static final XPathExpression NAME_EXPRESSION;

    private static final XPathExpression PROPERTIES_EXPRESSION;

    static {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(PropertyIdentifier.DAV_NS_CONTEXT);
        try {
            ALL_EXPRESSION = xpath.compile("/D:propfind/D:allprop");
            PROPERTIES_EXPRESSION = xpath.compile("/D:propfind/D:prop/*");
            NAME_EXPRESSION = xpath.compile("/D:propfind/D:propname");
        } catch (final XPathExpressionException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Collection<WebDavProperty> entityToProperties(final WebDavEntity entity) {
        final Collection<WebDavProperty> result = new ArrayList<>();
        result.add(new StringWebDavProperty(PropertyIdentifier.DISPLAY_NAME_IDENTIFIER, entity.getName()));
        result.add(new StringWebDavProperty(PropertyIdentifier.CONTENT_LENGTH_IDENTIFIER, Long.toString(entity.getSize())));
        result.add(new StringWebDavProperty(PropertyIdentifier.LAST_MODIFIED_IDENTIFIER, ISO_DATE_FORMATTER.format(entity.getLastModified())));
        if (entity.getType() == WebDavEntity.Type.COLLECTION) {
            result.add(new CollectionProperty());
        }

        final Optional<String> etag = entity.getEtag();
        etag.ifPresent(x -> result.add(new StringWebDavProperty(PropertyIdentifier.ETAG_IDENTIFIER, etag.get())));

        final Optional<WebDavLock> lock = entity.getLock();
        lock.ifPresent(x -> result.add(x.toProperty()));

        return result;

    }

    private boolean allProperties(final Document document) {
        try {
            final NodeList nodeList = (NodeList) ALL_EXPRESSION.evaluate(document, XPathConstants.NODESET);
            return (nodeList.getLength() > 0);
        } catch (final XPathExpressionException e) {
            return false;
        }
    }

    private void collectProperties(final WebDavStore store, final WebDavPath path, final int depth, final Map<WebDavPath, Collection<WebDavProperty>> map) {
        if (depth < 0) {
            return;
        }

        final WebDavEntity entity = store.getEntity(path);
        final Collection<WebDavProperty> liveProperties = entityToProperties(entity);
        final Collection<WebDavProperty> deadProperties = store.getProperties(path);
        final Collection<WebDavProperty> merged = merge(liveProperties, deadProperties);
        map.put(path, merged);
        for (final WebDavPath child : store.list(path)) {
            collectProperties(store, child, depth - 1, map);
        }
    }

    private void collectPropertyNames(final WebDavStore store, final WebDavPath path, final int depth, final Map<WebDavPath, Collection<PropertyIdentifier>> map) {
        if (depth < 0) {
            return;
        }

        map.put(path, getPropertyIdentifiers(store, path));
        for (final WebDavPath child : store.list(path)) {
            collectPropertyNames(store, child, depth - 1, map);
        }
    }

    @Override
    public Method getMethod() {
        return Method.PROPFIND;
    }

    private Collection<PropertyIdentifier> getPropertyIdentifiers(final WebDavStore store, final WebDavPath path) {
        final Collection<PropertyIdentifier> result = new TreeSet<>();
        // add live properties
        result.addAll(PropertyIdentifier.SUPPORTED_LIVE_PROPERTIES);

        for (final WebDavProperty property : store.getProperties(path)) {
            result.add(property.getIdentifier());
        }

        return result;
    }

    private Set<PropertyIdentifier> getRequestedProperties(final Document document) {
        try {
            final NodeList nodes = (NodeList) PROPERTIES_EXPRESSION.evaluate(document, XPathConstants.NODESET);
            final int length = nodes.getLength();
            final Set<PropertyIdentifier> properties = new TreeSet<>();
            for (int i = 0; i < length; i++) {
                final Node node = nodes.item(i);
                final String namespace = StringUtils.trimToEmpty(node.getNamespaceURI());
                final String name = node.getLocalName();
                properties.add(new PropertyIdentifier(namespace, name));
            }
            return properties;
        } catch (final XPathExpressionException e) {
            return Collections.emptySet();
        }
    }

    private boolean listPropertyNames(final Document document) {
        try {
            final NodeList nodeList = (NodeList) NAME_EXPRESSION.evaluate(document, XPathConstants.NODESET);
            return (nodeList.getLength() > 0);
        } catch (final XPathExpressionException e) {
            return false;
        }
    }

    private Collection<WebDavProperty> merge(final Collection<WebDavProperty> live, final Collection<WebDavProperty> dead) {
        live.addAll(dead);
        return live;
    }

    @Override
    public WebDavResponseWriter service(final WebDavStore store, final WebDavRequest request) throws IOException {
        final WebDavPath target = request.getPath();
        if (!store.exists(target)) {
            return AbstractBasicResponse.createNotFound();
        }

        final WebDavEntity entity = store.getEntity(target);

        final WebDavConfig config = request.getConfig();
        final int depth = determineDepth(request);
        if ((depth == Integer.MAX_VALUE) && !config.isAllowInfiniteDepthRequests()) {
            return AbstractBasicResponse.createForbidden(entity);
        }

        final Document document = PropertiesMessageHelper.parse(request.getInputStream());
        if (document == null) {
            return AbstractBasicResponse.createBadRequest(entity);
        }

        if (listPropertyNames(document)) {
            final Map<WebDavPath, Collection<PropertyIdentifier>> result = new TreeMap<>();
            collectPropertyNames(store, target, depth, result);
            return new PropertyNameResponse(entity, request.getBase(), result);
        }

        final Set<PropertyIdentifier> requested;
        if (allProperties(document)) {
            requested = ALL;
        } else {
            requested = getRequestedProperties(document);
        }

        if (requested.isEmpty()) {
            return AbstractBasicResponse.createBadRequest(entity);
        }

        final Map<WebDavPath, Collection<WebDavProperty>> result = new TreeMap<>();
        collectProperties(store, target, depth, result);
        return new PropertiesResponse(entity, request.getBase(), requested, result);
    }
}
