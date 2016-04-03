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
package de.shadowhunt.webdav.store.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;

import de.shadowhunt.webdav.WebDavConstant.Depth;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.method.WebDavMethod;
import de.shadowhunt.webdav.property.PropertyIdentifier;
import de.shadowhunt.webdav.property.StringWebDavProperty;
import de.shadowhunt.webdav.property.WebDavProperty;
import de.shadowhunt.webdav.store.SupportedLock;
import de.shadowhunt.webdav.store.WebDavEntity;
import de.shadowhunt.webdav.store.WebDavLock;
import de.shadowhunt.webdav.store.WebDavLock.LockScope;
import de.shadowhunt.webdav.store.WebDavLock.LockType;
import de.shadowhunt.webdav.store.WebDavLock.Timeout;
import de.shadowhunt.webdav.store.WebDavLockBuilder;
import de.shadowhunt.webdav.store.WebDavStore;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileSystemStore implements WebDavStore {

    private static final String LOCK_DEPTH = "depth";

    private static final String LOCK_OWNER = "owner";

    private static final String LOCK_ROOT = "root";

    private static final String LOCK_SCOPE = "scope";

    private static final String LOCK_TIMEOUT = "timeout";

    private static final String LOCK_TOKEN = "token";

    private static final String LOCK_TYPE = "type";

    private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

    private final File contentRoot;

    private final File metaRoot;

    private final Object monitor = new Object();

    private final Set<SupportedLock> supportedLocks;

    public FileSystemStore(final File root) {
        this(root, false);
    }

    public FileSystemStore(final File root, final boolean clear) {
        this.contentRoot = new File(root, "content");
        this.metaRoot = new File(root, "meta");

        if (clear) {
            FileUtils.deleteQuietly(contentRoot);
            FileUtils.deleteQuietly(metaRoot);
        }

        if (!contentRoot.exists() && !contentRoot.mkdirs()) {
            throw new WebDavException("contentRoot path: " + contentRoot + " does not exist and can not be created");
        }
        if (!metaRoot.exists() && !metaRoot.mkdirs()) {
            throw new WebDavException("metaRoot path: " + metaRoot + " does not exist and can not be created");
        }

        final Set<SupportedLock> locks = new TreeSet<>();
        locks.add(SupportedLock.EXCLUSIVE_WRITE_LOCK);
        supportedLocks = Collections.unmodifiableSet(locks);
    }

    private String calculateEtag(final WebDavPath path) {
        final File contentFile = getContentFile(path, false);
        final File lockFile = getLockFile(path);
        final File propertiesFile = getPropertiesFile(path);

        final long modified = max(contentFile.lastModified(), lockFile.lastModified(), propertiesFile.lastModified());
        return Long.toString(modified, Character.MAX_RADIX);
    }

    private String calculateMd5(final File file, final WebDavPath path) {
        try (final FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fis);
        } catch (final Exception e) {
            throw new WebDavException("can not calculate md5 hash for " + path, e);
        }
    }

    private long calculateSize(final File file, final WebDavPath path) {
        if (file.isDirectory()) {
            return 0L;
        }

        try {
            return file.length();
        } catch (final Exception e) {
            throw new WebDavException("can not calculate size for " + path, e);
        }
    }

    @Override
    public void createCollection(final WebDavPath path) throws WebDavException {
        if (WebDavPath.ROOT.equals(path)) {
            throw new WebDavException("can not override root");
        }

        synchronized (monitor) {
            createFolder(path, getContentFile(path, false));
            createFolder(path, new File(metaRoot, path.getValue()));
        }
    }

    private void createFolder(final WebDavPath path, final File file) {
        if (file.isDirectory()) {
            return;
        }

        if (!file.mkdir()) {
            throw new WebDavException("can not create folder " + path);
        }
    }

    @Override
    public void createItem(final WebDavPath path, final InputStream content) throws WebDavException {
        if (WebDavPath.ROOT.equals(path)) {
            throw new WebDavException("can not override root");
        }

        synchronized (monitor) {
            final File file = getContentFile(path, false);
            try (final OutputStream os = new FileOutputStream(file)) {
                IOUtils.copy(content, os);
            } catch (final IOException e) {
                throw new WebDavException("can not write to resource " + path, e);
            } finally {
                IOUtils.closeQuietly(content);
            }
        }
    }

    @Override
    public WebDavLockBuilder createLockBuilder() {
        return new FileSystemLockBuilder();
    }

    private PropertyIdentifier createPropertyIdentifier(final String elementName) {
        final String[] parts = elementName.split(" ");
        return new PropertyIdentifier(parts[0], parts[1]);
    }

    @Override
    public void delete(final WebDavPath path) throws WebDavException {
        if (WebDavPath.ROOT.equals(path)) {
            throw new WebDavException("can not delete root");
        }

        synchronized (monitor) {
            delete(path, getContentFile(path, true));
            delete(path, getLockFile(path));
            delete(path, getPropertiesFile(path));
        }
    }

    private void delete(final WebDavPath path, final File file) {
        if (file.exists() && !file.delete()) {
            throw new WebDavException("can not delete " + path);
        }
    }

    private Date determineLastModified(final File file, final WebDavPath path) {
        try {
            return new Date(file.lastModified());
        } catch (final Exception e) {
            throw new WebDavException("can not determine last modified date for " + path, e);
        }
    }

    private Optional<WebDavLock> determineLock(final WebDavPath path) {
        synchronized (monitor) {
            final File lockFile = getLockFile(path);
            if (!lockFile.exists()) {
                return Optional.empty();
            }

            final Properties properties = new Properties();
            try (final InputStream is = new FileInputStream(lockFile)) {
                properties.loadFromXML(is);
            } catch (final Exception e) {
                throw new WebDavException("can not load lock for " + path, e);
            }

            final String depthProperty = properties.getProperty(LOCK_DEPTH);
            final Depth depth = Depth.parse(depthProperty, Depth.SELF, Depth.MEMBERS, Depth.INFINITY);
            final String owner = properties.getProperty(LOCK_OWNER);
            final String rootProperty = properties.getProperty(LOCK_ROOT);
            final WebDavPath root = WebDavPath.create(rootProperty);
            final String scopeProperty = properties.getProperty(LOCK_SCOPE);
            final LockScope scope = LockScope.valueOf(scopeProperty);
            final String timeoutProperty = properties.getProperty(LOCK_TIMEOUT);
            final Timeout timeout = Timeout.parse(timeoutProperty);
            final String tokenProperty = properties.getProperty(LOCK_TOKEN);
            final UUID token = UUID.fromString(tokenProperty);
            final String typeProperty = properties.getProperty(LOCK_TYPE);
            final LockType type = LockType.valueOf(typeProperty);
            return Optional.of(new FileSystemLock(token, root, depth, scope, type, timeout, owner));
        }
    }

    @Override
    public boolean exists(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File file = getContentFile(path, false);
            return file.exists();
        }
    }

    @Override
    public InputStream getContent(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File file = getContentFile(path, true);
            try {
                return new FileInputStream(file);
            } catch (final IOException e) {
                throw new WebDavException("can not retrieve content", e);
            }
        }
    }

    private File getContentFile(final WebDavPath path, final boolean mustExist) throws WebDavException {
        final File file = new File(contentRoot, path.getValue());
        if (mustExist && !file.exists()) {
            throw new WebDavException("can not locate resource: " + path);
        }
        return file;
    }

    @Override
    public WebDavEntity getEntity(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File file = getContentFile(path, true);

            final Date lastModified = determineLastModified(file, path);
            final Optional<WebDavLock> lock = determineLock(path);
            if (file.isFile()) {
                final String hash = calculateMd5(file, path);
                final long size = calculateSize(file, path);
                final String etag = calculateEtag(path);
                final String mimeType = MIME_TYPES.getContentType(file);
                return new FileSystemEntity(path, hash, lastModified, size, mimeType, lock, etag);
            }
            return new FileSystemEntity(path, lastModified, lock);
        }
    }

    private File getLockFile(final WebDavPath path) {
        return new File(metaRoot, path.getValue() + "_lock");
    }

    @Override
    public Collection<WebDavProperty> getProperties(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            getContentFile(path, true); // ensure collection/item exists

            final File properitesFile = getPropertiesFile(path);
            if (!properitesFile.exists()) {
                return new ArrayList<>();
            }

            final Properties properties = new Properties();
            try (final InputStream is = new FileInputStream(properitesFile)) {
                properties.loadFromXML(is);

                final Collection<WebDavProperty> result = new ArrayList<>();
                final Enumeration<?> enumeration = properties.propertyNames();
                while (enumeration.hasMoreElements()) {
                    final Object element = enumeration.nextElement();
                    final Object value = properties.get(element);
                    final PropertyIdentifier identifier = createPropertyIdentifier(element.toString());
                    result.add(new StringWebDavProperty(identifier, value.toString()));
                }
                return result;
            } catch (final Exception e) {
                throw new WebDavException("can not load properties for " + path, e);
            }
        }
    }

    private File getPropertiesFile(final WebDavPath path) throws WebDavException {
        return new File(metaRoot, path.getValue() + "_dead-properties");
    }

    @Override
    public Set<SupportedLock> getSupportedLocks(final WebDavPath path) throws WebDavException {
        return supportedLocks;
    }

    @Override
    public Access grantAccess(final WebDavMethod method, final WebDavPath path, final Optional<Principal> principal) {
        return Access.ALLOW; // TODO
    }

    @Override
    public List<WebDavPath> list(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File file = getContentFile(path, true);
            if (file.isFile()) {
                return Collections.emptyList();
            }

            final List<WebDavPath> result = new ArrayList<>();
            final String[] children = file.list();
            if (children != null) {
                for (final String child : children) {
                    result.add(path.append(child));
                }
            }
            return result;
        }
    }

    @Override
    public WebDavEntity lock(final WebDavPath path, final WebDavLock lock) throws WebDavException {
        synchronized (monitor) {
            final File lockFile = getLockFile(path);

            final Properties store = new Properties();
            final Depth depth = lock.getDepth();
            store.put(LOCK_DEPTH, depth.name);
            final String owner = lock.getOwner();
            store.put(LOCK_OWNER, owner);
            final WebDavPath root = lock.getRoot();
            final String rootProperty = root.getValue();
            store.put(LOCK_ROOT, rootProperty);
            final LockScope scope = lock.getScope();
            final String scopeProperty = scope.name();
            store.put(LOCK_SCOPE, scopeProperty);
            final Timeout timeout = lock.getTimeout();
            final String timeoutProperty = timeout.toString();
            store.put(LOCK_TIMEOUT, timeoutProperty);
            final UUID token = lock.getToken();
            final String tokenProperty = token.toString();
            store.put(LOCK_TOKEN, tokenProperty);
            final LockType type = lock.getType();
            final String typeProperty = type.name();
            store.put(LOCK_TYPE, typeProperty);

            try (final OutputStream os = new FileOutputStream(lockFile)) {
                store.storeToXML(os, "", "UTF-8");
            } catch (final Exception e) {
                throw new WebDavException("can not write lock for " + path, e);
            }

            return getEntity(path);
        }
    }

    private long max(final long first, final long... values) {
        long max = first;
        for (int i = 0; i < values.length; i++) {
            max = Math.max(max, values[i]);
        }
        return max;
    }

    @Override
    public void setProperties(final WebDavPath path, final Collection<WebDavProperty> properties) throws WebDavException {
        synchronized (monitor) {
            getContentFile(path, true); // ensure collection/item exists

            final Properties store = new Properties();
            for (final WebDavProperty property : properties) {
                final PropertyIdentifier identifier = property.getIdentifier();
                store.put(identifier.getNameSpace() + " " + identifier.getName(), property.getValue());
            }

            final File propertiesFile = getPropertiesFile(path);
            try (final OutputStream os = new FileOutputStream(propertiesFile)) {
                store.storeToXML(os, "", "UTF-8");
            } catch (final Exception e) {
                throw new WebDavException("can not save properties for " + path, e);
            }
        }
    }

    @Override
    public void unlock(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File lockFile = getLockFile(path);
            if (!lockFile.delete()) {
                throw new WebDavException("can not remove lock for " + path);
            }
        }
    }
}
