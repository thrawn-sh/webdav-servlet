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
import de.shadowhunt.servlet.webdav.Store;

public class PropFindMethod extends AbstractWebDavMethod {

    private static final XPathExpression ALL_EXPRESSION;

    public static final String METHOD = "PROPFIND";

    private static final XPathExpression PROPERTIES_EXPRESSION;

    static {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();

        try {
            ALL_EXPRESSION = xpath.compile("//*[local-name()='propfind']/*[local-name()='allprop']");
            PROPERTIES_EXPRESSION = xpath.compile("//*[local-name()='propfind']/*[local-name()='prop']/node()");
        } catch (final XPathExpressionException e) {
            throw new InstantiationError(e.getMessage());
        }
    }

    protected final boolean supportInfiniteDepth;

    public PropFindMethod(final Store store, final boolean supportInfiniteDepth) {
        super(METHOD, store);
        this.supportInfiniteDepth = supportInfiniteDepth;
    }

    private void collectProperties(final Path path, final int depth, final Map<Entity, Map<Property, String>> map) {
        if (depth < 0) {
            return;
        }

        map.put(store.getEntity(path), store.getProperties(path));
        for (final Path child : store.list(path)) {
            collectProperties(child, depth - 1, map);
        }
    }

    private String determineBaseUri(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();

        final String scheme = request.getScheme();
        builder.append(scheme);
        builder.append("://");
        builder.append(request.getServerName());
        final int port = request.getServerPort();
        if (("http".equals(scheme) && (port != 80)) || ("https".equals(scheme) && (port != 443))) {
            builder.append(':');
            builder.append(port);
        }
        builder.append(request.getServletPath());
        return builder.toString();
    }

    protected int determineDepth(final HttpServletRequest request) {
        final String depth = request.getHeader("Depth");
        if (StringUtils.isEmpty(depth) || "infinity".equalsIgnoreCase(depth)) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(depth);
    }

    @CheckForNull
    private Set<Property> filter(final Document document, final XPathExpression expression) {
        try {
            final NodeList nodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            final int length = nodes.getLength();
            final Set<Property> properties = new TreeSet<>();
            for (int i = 0; i < length; i++) {
                final Node node = nodes.item(i);
                String nameSpace = node.getNamespaceURI();
                if (StringUtils.isEmpty(nameSpace)) {
                    nameSpace = "DAV";
                }
                final String name = node.getNodeName();
                properties.add(new Property(nameSpace, name));
            }
            return properties;
        } catch (final XPathExpressionException e) {
            return null;
        }
    }

    @Override
    public WebDavResponse service(final Path path, final HttpServletRequest request) throws ServletException, IOException {
        if (!store.exists(path)) {
            return StatusResponse.NOT_FOUND;
        }

        final int depth = determineDepth(request);
        if ((depth == Integer.MAX_VALUE) && !supportInfiniteDepth) {
            return StatusResponse.FORBIDDEN;
        }

        final Document document = PropertiesMessageHelper.parse(request.getInputStream());
        if (document == null) {
            return StatusResponse.BAD_REQUEST;
        }

        final Map<Entity, Map<Property, String>> result = new LinkedHashMap<>();
        collectProperties(path, depth, result);

        return new PropertiesResponse(determineBaseUri(request), result);
    }
}
