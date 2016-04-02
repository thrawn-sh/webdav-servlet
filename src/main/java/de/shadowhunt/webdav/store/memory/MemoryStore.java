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
package de.shadowhunt.webdav.store.memory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.CheckForNull;

import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.method.WebDavMethod;
import de.shadowhunt.webdav.property.WebDavProperty;
import de.shadowhunt.webdav.store.SupportedLock;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavLock;
import de.shadowhunt.webdav.store.WebDavLockBuilder;
import de.shadowhunt.webdav.store.WebDavStore;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

public class MemoryStore implements WebDavStore {

    private static class Node implements Comparable<Node> {

        private final Map<String, Node> children = new TreeMap<>();

        private final byte[] data;

        private final MemoryEntity entity;

        private final String name;

        private Set<WebDavProperty> properties = Collections.emptySet();

        Node(final String name, final MemoryEntity entity) {
            this(name, entity, new byte[0]);
        }

        Node(final String name, final MemoryEntity entity, final byte[] data) {
            this.entity = entity;
            this.data = data;
            this.name = name;
        }

        @Override
        public int compareTo(final Node o) {
            return name.compareTo(o.name);
        }
    }

    private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

    private final Node root = new Node("", new MemoryEntity(WebDavPath.ROOT));

    private final Set<SupportedLock> supportedLocks;

    public MemoryStore() {
        final Set<SupportedLock> locks = new TreeSet<>();
        locks.add(SupportedLock.EXCLUSIVE_WRITE_LOCK);
        supportedLocks = Collections.unmodifiableSet(locks);
    }

    private String calculateEtag() {
        return Long.toString(System.currentTimeMillis(), Character.MAX_RADIX);
    }

    private String calculateMd5(final byte[] data, final WebDavPath path) {
        try (final InputStream fis = new ByteArrayInputStream(data)) {
            return DigestUtils.md5Hex(fis);
        } catch (final Exception e) {
            throw new WebDavException("can not calculate md5 hash for " + path, e);
        }
    }

    @Override
    public void createCollection(final WebDavPath path) throws WebDavException {
        final String[] segments = path.getSegments();

        final Node parent = navigate(root, segments, 1, segments.length - 1);
        final String name = segments[segments.length - 1];
        if (parent.children.containsKey(name)) {
            throw new WebDavException("can not create collection " + path);
        }

        parent.children.put(name, new Node(name, new MemoryEntity(path)));
    }

    @Override
    public void createItem(final WebDavPath path, final InputStream content) throws WebDavException {
        final String[] segments = path.getSegments();

        final Node parent = navigate(root, segments, 1, segments.length - 1);
        final String name = segments[segments.length - 1];

        final byte[] data;
        try {
            data = IOUtils.toByteArray(content);
        } catch (final IOException e) {
            throw new WebDavException("can not read content", e);
        }

        final String hash = calculateMd5(data, path);
        final String etag = calculateEtag();
        final String type = MIME_TYPES.getContentType(path.getName());
        final MemoryEntity entity = new MemoryEntity(path, hash, data.length, type, etag);
        final Node node = new Node(name, entity, data);
        parent.children.put(name, node);
    }

    @Override
    public WebDavLockBuilder createLockBuilder() {
        return new MemoryLockBuilder();
    }

    @Override
    public void delete(final WebDavPath path) throws WebDavException {
        final String[] segments = path.getSegments();
        final Node parent = navigate(root, segments, 1, segments.length - 1);
        final Node node = navigate(root, segments, 1, segments.length);
        if (!node.children.isEmpty()) {
            throw new WebDavException("not empty collection");
        }

        parent.children.remove(segments[segments.length - 1]);
    }

    @Override
    public boolean exists(final WebDavPath path) throws WebDavException {
        final String[] segments = path.getSegments();
        final Node node = navigate0(root, segments, 1, segments.length);
        return node != null;
    }

    @Override
    public InputStream getContent(final WebDavPath path) throws WebDavException {
        final String[] segments = path.getSegments();
        final Node node = navigate(root, segments, 1, segments.length);
        return new ByteArrayInputStream(node.data);
    }

    @Override
    public WebDavEntity getEntity(final WebDavPath path) throws WebDavException {
        final String[] segments = path.getSegments();
        final Node node = navigate(root, segments, 1, segments.length);
        return node.entity;
    }

    @Override
    public Collection<WebDavProperty> getProperties(final WebDavPath path) throws WebDavException {
        final String[] segments = path.getSegments();
        final Node node = navigate(root, segments, 1, segments.length);
        return new TreeSet<>(node.properties);
    }

    @Override
    public Set<SupportedLock> getSupportedLocks(final WebDavPath path) throws WebDavException {
        return supportedLocks;
    }

    @Override
    public Access grantAccess(final WebDavMethod method, final WebDavPath path, final Optional<Principal> principal) {
        return Access.ALLOW; // FIXME
    }

    @Override
    public List<WebDavPath> list(final WebDavPath path) throws WebDavException {
        final String[] segments = path.getSegments();
        final Node node = navigate(root, segments, 1, segments.length);

        final List<WebDavPath> paths = new ArrayList<>(node.children.size());
        for (final Node child : node.children.values()) {
            final WebDavPath childPath = child.entity.getPath();
            paths.add(childPath);
        }
        return paths;
    }

    @Override
    public WebDavEntity lock(final WebDavPath path, final WebDavLock lock) throws WebDavException {
        final String[] segments = path.getSegments();
        final Node node = navigate(root, segments, 1, segments.length);
        node.entity.setLock(lock);
        return node.entity;
    }

    @CheckForNull
    private Node navigate(final Node node, final String[] segements, final int index, final int max) {
        final Node current = navigate0(node, segements, index, max);
        if (current == null) {
            throw new WebDavException("incomplete path");
        }
        return current;
    }

    @CheckForNull
    private Node navigate0(final Node node, final String[] segements, final int index, final int max) {
        if (index >= max) {
            return node;
        }

        final Node current = node.children.get(segements[index]);
        if (current == null) {
            return null;
        }
        return navigate0(current, segements, index + 1, max);
    }

    @Override
    public void setProperties(final WebDavPath path, final Collection<WebDavProperty> properties) throws WebDavException {
        final String[] segments = path.getSegments();
        final Node node = navigate(root, segments, 1, segments.length);
        node.properties = new TreeSet<>(properties);
    }

    @Override
    public void unlock(final WebDavPath path) throws WebDavException {
        final String[] segments = path.getSegments();
        final Node node = navigate(root, segments, 1, segments.length);
        node.entity.deleteLock();
    }
}
