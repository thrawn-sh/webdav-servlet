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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse.Status;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.property.PropertyIdentifier;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavLock;
import de.shadowhunt.webdav.store.WebDavLock.LockScope;
import de.shadowhunt.webdav.store.WebDavLock.LockType;
import de.shadowhunt.webdav.store.WebDavLockBuilder;
import de.shadowhunt.webdav.store.WebDavStore;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LockMethod extends AbstractWebDavMethod {

    public static final String INFINITE = "infinite";

    private static final XPathExpression LOCK_OWNER;

    private static final XPathExpression LOCK_SCOPE;

    private static final XPathExpression LOCK_TYPE;

    static {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(PropertyIdentifier.DAV_NS_CONTEXT);
        try {
            LOCK_OWNER = xpath.compile("/D:lockinfo/D:owner");
            LOCK_SCOPE = xpath.compile("/D:lockinfo/D:lockscope/*");
            LOCK_TYPE = xpath.compile("/D:lockinfo/D:locktype/*");
        } catch (final XPathExpressionException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private WebDavLock determineLock(final WebDavStore store, final WebDavRequest request) throws IOException {
        final WebDavLockBuilder lockBuilder = store.createLockBuilder();

        final WebDavPath path = request.getPath();
        lockBuilder.setRoot(path);

        final Optional<Integer> timeoutInSeconds = getTimeoutInSeconds(request);
        timeoutInSeconds.ifPresent(x -> lockBuilder.setTimeoutInSeconds(x));

        final Document document = PropertiesMessageHelper.parse(request.getInputStream());
        if (document != null) {
            final WebDavEntity entity = store.getEntity(path);
            final Map<WebDavPath, UUID> tokens = deterimineLockTokens(request);
            checkLockTokenOnEntity(entity, tokens);

            final Optional<String> owner = getOwner(document);
            owner.ifPresent(x -> lockBuilder.setOwner(x));

            final Optional<LockScope> scope = getScope(document);
            scope.ifPresent(x -> lockBuilder.setScope(x));

            final Optional<LockType> type = getType(document);
            type.ifPresent(x -> lockBuilder.setType(x));
            return lockBuilder.build();
        }

        final WebDavEntity entity = store.getEntity(path);
        final Optional<WebDavLock> lock = entity.getLock();
        if (lock.isPresent()) {
            return lock.get();
        }

        return lockBuilder.build();
    }

    @Override
    public Method getMethod() {
        return Method.LOCK;
    }

    private Optional<String> getOwner(final Document document) {
        try {
            final Node node = (Node) LOCK_OWNER.evaluate(document, XPathConstants.NODE);
            if (node == null) {
                return Optional.empty();
            }

            final String owner = node.getTextContent();
            return Optional.of(owner);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    private Optional<LockScope> getScope(final Document document) {
        try {
            final NodeList nodelist = (NodeList) LOCK_SCOPE.evaluate(document, XPathConstants.NODESET);
            if (nodelist.getLength() <= 0) {
                return Optional.empty();
            }
            final Node node = nodelist.item(0);
            final String name = node.getLocalName().toUpperCase(Locale.US);
            final LockScope scope = LockScope.valueOf(name);
            return Optional.of(scope);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Integer> getTimeoutInSeconds(final WebDavRequest request) {
        final String timeout = StringUtils.trimToEmpty(request.getHeader("Timeout", INFINITE));

        if (StringUtils.startsWithIgnoreCase(timeout, INFINITE)) {
            return Optional.of(-1);
        }

        final String digits = StringUtils.removeStartIgnoreCase(timeout, "Seconds-");

        try {
            final int seconds = Integer.parseInt(digits);
            return Optional.of(seconds);
        } catch (final NumberFormatException e) {
            return Optional.of(-1);
        }
    }

    private Optional<LockType> getType(final Document document) {
        try {
            final NodeList nodelist = (NodeList) LOCK_TYPE.evaluate(document, XPathConstants.NODESET);
            if (nodelist.getLength() <= 0) {
                return Optional.empty();
            }
            final Node node = nodelist.item(0);
            final String name = node.getLocalName().toUpperCase(Locale.US);
            final LockType type = LockType.valueOf(name);
            return Optional.of(type);
        } catch (final XPathExpressionException e) {
            return Optional.empty();
        }
    }

    @Override
    public WebDavResponseWriter service(final WebDavStore store, final WebDavRequest request) throws IOException {
        Status status = Status.SC_OK;

        final WebDavPath path = request.getPath();
        if (!store.exists(path)) {
            store.createItem(path, new ByteArrayInputStream(new byte[0]));
            status = Status.SC_CREATED;
        }

        final WebDavLock lock = determineLock(store, request);
        final WebDavEntity lockedEntity = lockRecursively(store, path, lock);
        return new LockDiscoveryResponse(lockedEntity, status);
    }

    private WebDavEntity lockRecursively(final WebDavStore store, final WebDavPath path, final WebDavLock lock) {
        for (final WebDavPath child : store.list(path)) {
            lockRecursively(store, child, lock);
        }
        return store.lock(path, lock);
    }
}
