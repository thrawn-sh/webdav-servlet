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
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavProperty;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.impl.StringWebDavProperty;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileSystemStore implements WebDavStore {

    private static final WebDavPath LOCK_SUFFIX = WebDavPath.create("_lock");

    private static final WebDavPath PROPERTIES_SUFFIX = WebDavPath.create("_xml");

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
        final File content = getFile(path, false);
        final File meta = getMetaFile(path);

        final long modified = Math.max(content.lastModified(), meta.lastModified());
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
            createFolder(path, getFile(path, false));
            createFolder(path, getMetaFile(path));
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
            final File file = getFile(path, false);
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
    public WebDavLock createLock(final Optional<Principal> principal) {
        final String username = principal.map(Principal::getName).orElse("");
        return new LockImpl("opaquelocktoken:" + UUID.randomUUID().toString(), WebDavLock.Scope.EXCLUSIVE, username);
    }

    private PropertyIdentifier createPropertyIdentifier(final String elementName) {
        final String[] parts = elementName.split(" ");
        return new PropertyIdentifier(parts[0], parts[1]);
    }

    @Override
    public void delete(final WebDavPath path) throws WebDavException {
        delete(path, getFile(path, true));
        delete(path, getMetaFile(path));
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
            final File lock = getMetaFile(path.append(LOCK_SUFFIX));
            if (!lock.exists()) {
                return Optional.empty();
            }

            try {
                return Optional.of(new LockImpl(FileUtils.readFileToString(lock), WebDavLock.Scope.EXCLUSIVE, ""));
            } catch (final IOException e) {
                throw new WebDavException("can not read lock for " + path, e);
            }
        }
    }

    @Override
    public boolean exists(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File file = getFile(path, false);
            return file.exists();
        }
    }

    @Override
    public InputStream getContent(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File file = getFile(path, true);
            try {
                return new FileInputStream(file);
            } catch (final IOException e) {
                throw new WebDavException("can not retrieve content", e);
            }
        }
    }

    @Override
    public WebDavEntity getEntity(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File file = getFile(path, true);

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

    private File getFile(final WebDavPath path, final boolean mustExist) throws WebDavException {
        final File file = new File(resourceRoot, path.getValue());
        if (mustExist && !file.exists()) {
            throw new WebDavException("can not locate resource: " + path);
        }
        return file;
    }

    private File getMetaFile(final WebDavPath path) throws WebDavException {
        return new File(metaRoot, path.getValue());
    }

    @Override
    public Collection<WebDavProperty> getProperties(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            getFile(path, true); // ensure collection/item exists

            final File meta = getMetaFile(path.append(PROPERTIES_SUFFIX));
            if (!meta.exists()) {
                return new ArrayList<>();
            }

            final Properties properties = new Properties();
            try (final InputStream is = new FileInputStream(meta)) {
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

    @Override
    public List<WebDavPath> list(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File file = getFile(path, true);
            if (file.isFile()) {
                return Collections.emptyList();
            }

            final List<WebDavPath> result = new ArrayList<>();
            final String[] children = file.list();
            if (children != null) {
                for (final String child : children) {
                    result.add(path.getChild(child));
                }
            }
            return result;
        }
    }

    @Override
    public WebDavEntity lock(final WebDavPath path, final WebDavLock lock) throws WebDavException {
        synchronized (monitor) {
            final File lockFile = getMetaFile(path.append(LOCK_SUFFIX));
            try {
                FileUtils.write(lockFile, lock.getToken());
            } catch (final Exception e) {
                throw new WebDavException("can not write lock for " + path, e);
            }

            return getEntity(path);
        }
    }

    @Override
    public void setProperties(final WebDavPath path, final Collection<WebDavProperty> properties) throws WebDavException {
        synchronized (monitor) {
            getFile(path, true); // ensure collection/item exists

            final File meta = getMetaFile(path.append(PROPERTIES_SUFFIX));
            if (meta.exists() && properties.isEmpty()) {
                delete(path, meta);
                return;
            }

            final Properties store = new Properties();
            for (final WebDavProperty property : properties) {
                final PropertyIdentifier identifier = property.getIdentifier();
                store.put(identifier.getNameSpace() + " " + identifier.getName(), property.getValue());
            }

            try (final OutputStream os = new FileOutputStream(meta)) {
                store.storeToXML(os, "", "UTF-8");
            } catch (final Exception e) {
                throw new WebDavException("can not save properties for " + path, e);
            }
        }
    }

    @Override
    public void unlock(final WebDavPath path) throws WebDavException {
        synchronized (monitor) {
            final File lock = getMetaFile(path.append(LOCK_SUFFIX));
            if (!lock.delete()) {
                throw new WebDavException("can not remove lock for " + path);
            }
        }
    }
}
