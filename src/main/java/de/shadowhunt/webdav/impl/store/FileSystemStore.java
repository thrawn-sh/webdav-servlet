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
package de.shadowhunt.webdav.impl.store;

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
import java.util.UUID;

import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavEntity.Type;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavLock.LockScope;
import de.shadowhunt.webdav.WebDavLock.LockType;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavProperty;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.impl.StringWebDavProperty;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileSystemStore implements WebDavStore {

    private static final String LOCK_OWNER = "owner";

    private static final String LOCK_SCOPE = "scope";

    private static final String LOCK_TOKEN = "token";

    private static final String LOCK_TYPE = "type";

    private final File metaRoot;

    private final Object monitor = new Object();

    private final File resourceRoot;

    public FileSystemStore(final File root) {
        this.resourceRoot = new File(root, "content");
        if (!resourceRoot.exists() && !resourceRoot.mkdirs()) {
            throw new WebDavException("resourceRoot path: " + resourceRoot + " does not exist and can not be created");
        }

        this.metaRoot = new File(root, "meta");
        if (!metaRoot.exists() && !metaRoot.mkdirs()) {
            throw new WebDavException("metaRoot path: " + metaRoot + " does not exist and can not be created");
        }
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
        synchronized (monitor) {
            createFolder(path, getContentFile(path, false));
            createFolder(path, new File(metaRoot, path.getValue()));
        }
    }

    private void createFolder(final WebDavPath path, final File file) {
        if (!file.mkdir()) {
            throw new WebDavException("can not create folder " + path);
        }
    }

    @Override
    public void createItem(final WebDavPath path, final InputStream content) throws WebDavException {
        synchronized (monitor) {
            final File file = getContentFile(path, false);
            try (final OutputStream os = FileUtils.openOutputStream(file)) {
                IOUtils.copy(content, os);
            } catch (final IOException e) {
                throw new WebDavException("can not write to resource " + path, e);
            } finally {
                IOUtils.closeQuietly(content);
            }
        }
    }

    @Override
    public WebDavLock createLock(final LockScope scope, final LockType type, final String owner) {
        return new LockImpl(UUID.randomUUID(), LockScope.EXCLUSIVE, LockType.WRITE, owner);
    }

    private PropertyIdentifier createPropertyIdentifier(final String elementName) {
        final String[] parts = elementName.split(" ");
        return new PropertyIdentifier(parts[0], parts[1]);
    }

    @Override
    public void delete(final WebDavPath path) throws WebDavException {
        delete(path, getContentFile(path, true));
        delete(path, getLockFile(path));
        delete(path, getPropertiesFile(path));
    }

    private void delete(final WebDavPath path, final File file) {
        if (file.isFile()) {
            if (!file.delete()) {
                throw new WebDavException("can not delete " + path);
            }
        } else {
            try {
                FileUtils.deleteDirectory(file);
            } catch (final IOException e) {
                throw new WebDavException("can not delete " + path, e);
            }
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

            final String owner = properties.getProperty(LOCK_OWNER);
            final String scopeProperty = properties.getProperty(LOCK_SCOPE);
            final LockScope scope = LockScope.valueOf(scopeProperty);
            final String tokenProperty = properties.getProperty(LOCK_TOKEN);
            final UUID token = UUID.fromString(tokenProperty);
            final String typeProperty = properties.getProperty(LOCK_TYPE);
            final LockType type = LockType.valueOf(typeProperty);
            return Optional.of(new LockImpl(token, scope, type, owner));
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
        final File file = new File(resourceRoot, path.getValue());
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
                return new EntiyImpl(path, Type.ITEM, Optional.of(hash), lastModified, size, lock, Optional.of(etag));
            }
            return new EntiyImpl(path, Type.COLLECTION, Optional.empty(), lastModified, 0L, lock, Optional.empty());
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
            try {
                final Properties store = new Properties();
                final String owner = lock.getOwner();
                store.put(LOCK_OWNER, owner);
                final LockScope scope = lock.getScope();
                final String scopeProperty = scope.name();
                store.put(LOCK_SCOPE, scopeProperty);
                final UUID token = lock.getToken();
                final String tokenProperty = token.toString();
                store.put(LOCK_TOKEN, tokenProperty);
                final LockType type = lock.getType();
                final String typeProperty = type.name();
                store.put(LOCK_TYPE, typeProperty);

                try (final OutputStream os = new FileOutputStream(lockFile)) {
                    store.storeToXML(os, "", "UTF-8");
                } catch (final Exception e) {
                    throw new WebDavException("can not save properties for " + path, e);
                }
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

            final File propertiesFile = getPropertiesFile(path);
            if (propertiesFile.exists() && properties.isEmpty()) {
                delete(path, propertiesFile);
                return;
            }

            final Properties store = new Properties();
            for (final WebDavProperty property : properties) {
                final PropertyIdentifier identifier = property.getIdentifier();
                store.put(identifier.getNameSpace() + " " + identifier.getName(), property.getValue());
            }

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
