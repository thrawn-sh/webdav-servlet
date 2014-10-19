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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.shadowhunt.servlet.methods.properties.PropertiesMessageHelper;
import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Property;
import de.shadowhunt.servlet.webdav.PropertyIdentifier;
import de.shadowhunt.servlet.webdav.Store;
import de.shadowhunt.servlet.webdav.StringProperty;

public class PropFindMethod extends AbstractWebDavMethod {

    private static final XPathExpression ALL_EXPRESSION;

    public static final String METHOD = "PROPFIND";

    private static final XPathExpression NAME_EXPRESSION;

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

    private static final XPathExpression PROPERTIES_EXPRESSION;

    protected final boolean supportInfiniteDepth;

    public PropFindMethod(final Store store, final boolean supportInfiniteDepth) {
        super(METHOD, store);
        this.supportInfiniteDepth = supportInfiniteDepth;
    }

    private void collectProperties(final Path path, final int depth, final Map<Path, Collection<Property>> map) {
        if (depth < 0) {
            return;
        }

        final Entity entity = store.getEntity(path);
        final Collection<Property> liveProperties = Entity.entityToProperties(entity);
        final Collection<StringProperty> deadProperties = store.getProperties(path);
        map.put(path, merge(liveProperties, deadProperties));
        for (final Path child : store.list(path)) {
            collectProperties(child, depth - 1, map);
        }
    }

    private void collectPropertyNames(final Path path, final int depth, final Map<Path, Collection<PropertyIdentifier>> map) {
        if (depth < 0) {
            return;
        }

        map.put(path, getPropertyIdentifiers(path));
        for (final Path child : store.list(path)) {
            collectPropertyNames(child, depth - 1, map);
        }
    }

    protected int determineDepth(final HttpServletRequest request) {
        final String depth = request.getHeader("Depth");
        if (StringUtils.isEmpty(depth) || "infinity".equalsIgnoreCase(depth)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(depth);
    }

    private Collection<PropertyIdentifier> getPropertyIdentifiers(final Path path) {
        final Collection<PropertyIdentifier> result = new TreeSet<>();
        // add live properties
        result.addAll(Entity.SUPPORTED_LIVE_PROPERTIES);

        for (final Property property : store.getProperties(path)) {
            result.add(property.getIdentifier());
        }

        return result;
    }

    @CheckForNull
    private Set<PropertyIdentifier> getRequestedProperties(final Document document) {
        try {
            final NodeList nodes = (NodeList) PROPERTIES_EXPRESSION.evaluate(document, XPathConstants.NODESET);
            final int length = nodes.getLength();
            final Set<PropertyIdentifier> properties = new TreeSet<>();
            for (int i = 0; i < length; i++) {
                final Node node = nodes.item(i);
                properties.add(new PropertyIdentifier(StringUtils.trimToEmpty(node.getNamespaceURI()), node.getLocalName()));
            }
            return properties;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean listPropertyNames(final Document document) {
        try {
            final NodeList nodeList = (NodeList) NAME_EXPRESSION.evaluate(document, XPathConstants.NODESET);
            return (nodeList.getLength() > 0);
        } catch (XPathExpressionException e) {
            return false;
        }
    }

    private Collection<Property> merge(final Collection<Property> live, final Collection<StringProperty> dead) {
        live.addAll(dead);
        return live;
    }

    @Override
    public WebDavResponse service(final Path path, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(path)) {
            return BasicResponse.createNotFound();
        }

        final Entity entity = store.getEntity(path);

        final int depth = determineDepth(request);
        if ((depth == Integer.MAX_VALUE) && !supportInfiniteDepth) {
            return BasicResponse.createForbidden(entity);
        }

        final Document document = PropertiesMessageHelper.parse(request.getInputStream());
        if (document == null) {
            return BasicResponse.createBadRequest(entity);
        }

        if (listPropertyNames(document)) {
            final Map<Path, Collection<PropertyIdentifier>> result = new LinkedHashMap<>();
            collectPropertyNames(path, depth, result);
            return new PropertyNameResponse(entity, request.getServletPath(), result);
        }

        final Set<PropertyIdentifier> requested = getRequestedProperties(document);
        if (requested == null) {
            return BasicResponse.createBadRequest(entity);
        }

        final Map<Path, Collection<Property>> result = new LinkedHashMap<>();
        collectProperties(path, depth, result);
        return new PropertiesResponse(entity, request.getServletPath(), requested, result);
    }
}
