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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.CheckForNull;

import de.shadowhunt.webdav.Entity;
import de.shadowhunt.webdav.Entity.Type;
import de.shadowhunt.webdav.Lock;
import de.shadowhunt.webdav.Path;
import de.shadowhunt.webdav.Property;
import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.impl.StringProperty;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileSystemStore implements WebDavStore {

    private static final Path LOCK_SUFFIX = Path.create("_lock");

    private static final Path PROPERTIES_SUFFIX = Path.create("_xml");

    private final File metaRoot;

    private final File resourceRoot;

    private final Object monitor = new Object();

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

    private String calculateMd5(final File file, final Path path) {
        try (final FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fis);
        } catch (final Exception e) {
            throw new WebDavException("can not calculate md5 hash for " + path, e);
        }
    }

    private long calculateSize(final File file, final Path path) {
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
    public void createCollection(final Path path) throws WebDavException {
        synchronized (monitor) {
            createFolder(path, getFile(path, false));
            createFolder(path, getMetaFile(path));
        }
    }

    private void createFolder(final Path path, final File file) {
        if (!file.mkdir()) {
            throw new WebDavException("can not create folder " + path);
        }
    }

    @Override
    public void createItem(final Path path, final InputStream content) throws WebDavException {
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
    public Lock createLock() {
        return new LockImpl("opaquelocktoken:" + UUID.randomUUID().toString(), Lock.Scope.EXCLUSIVE, "");
    }

    private PropertyIdentifier createPropertyIdentifier(final String elementName) {
        final String[] parts = elementName.split(" ");
        return new PropertyIdentifier(parts[0], parts[1]);
    }

    @Override
    public void delete(final Path path) throws WebDavException {
        delete(path, getFile(path, true));
        delete(path, getMetaFile(path));
    }

    private void delete(final Path path, final File file) {
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

    private Date determineLastModified(final File file, final Path path) {
        try {
            return new Date(file.lastModified());
        } catch (final Exception e) {
            throw new WebDavException("can not determine last modified date for " + path, e);
        }
    }

    @CheckForNull
    private Lock determineLock(final Path path) {
        synchronized (monitor) {
            final File lock = getMetaFile(path.append(LOCK_SUFFIX));
            if (!lock.exists()) {
                return null;
            }

            try {
                return new LockImpl(FileUtils.readFileToString(lock), Lock.Scope.EXCLUSIVE, "");
            } catch (final IOException e) {
                throw new WebDavException("can not read lock for " + path, e);
            }
        }
    }

    @Override
    public InputStream getContent(final Path path) throws WebDavException {
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
    public boolean exists(final Path path) throws WebDavException {
        synchronized (monitor) {
            final File file = getFile(path, false);
            return file.exists();
        }
    }

    @Override
    public Entity getEntity(final Path path) throws WebDavException {
        synchronized (monitor) {
            final File file = getFile(path, true);

            final Date lastModified = determineLastModified(file, path);
            final Lock lock = determineLock(path);
            if (file.isFile()) {
                final String hash = calculateMd5(file, path);
                final long size = calculateSize(file, path);
                return new EntiyImpl(path, Type.ITEM, hash, lastModified, size, lock);
            }
            return new EntiyImpl(path, Type.COLLECTION, null, lastModified, 0L, lock);
        }
    }

    private File getFile(final Path path, final boolean mustExist) throws WebDavException {
        final File file = new File(resourceRoot, path.getValue());
        if (mustExist && !file.exists()) {
            throw new WebDavException("can not locate resource: " + path);
        }
        return file;
    }

    private File getMetaFile(final Path path) throws WebDavException {
        return new File(metaRoot, path.getValue());
    }

    @Override
    public Collection<Property> getProperties(final Path path) throws WebDavException {
        synchronized (monitor) {
            getFile(path, true); // ensure collection/item exists

            final File meta = getMetaFile(path.append(PROPERTIES_SUFFIX));
            if (!meta.exists()) {
                return new ArrayList<>();
            }

            final Properties properties = new Properties();
            try (final InputStream is = new FileInputStream(meta)) {
                properties.loadFromXML(is);

                final Collection<Property> result = new ArrayList<>();
                final Enumeration<?> enumeration = properties.propertyNames();
                while (enumeration.hasMoreElements()) {
                    final Object element = enumeration.nextElement();
                    final Object value = properties.get(element);
                    final PropertyIdentifier identifier = createPropertyIdentifier(element.toString());
                    result.add(new StringProperty(identifier, value.toString()));
                }
                return result;
            } catch (final Exception e) {
                throw new WebDavException("can not load properties for " + path, e);
            }
        }
    }

    @Override
    public List<Path> list(final Path path) throws WebDavException {
        synchronized (monitor) {
            final File file = getFile(path, true);
            if (file.isFile()) {
                return Collections.emptyList();
            }

            final List<Path> result = new ArrayList<>();
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
    public void lock(final Path path, final Lock lock) throws WebDavException {
        synchronized (monitor) {
            final File lockFile = getMetaFile(path.append(LOCK_SUFFIX));
            try {
                FileUtils.write(lockFile, lock.getToken());
            } catch (final Exception e) {
                throw new WebDavException("can not write lock for " + path, e);
            }
        }
    }

    @Override
    public void setProperties(final Path path, final Collection<Property> properties) throws WebDavException {
        synchronized (monitor) {
            getFile(path, true); // ensure collection/item exists

            final File meta = getMetaFile(path.append(PROPERTIES_SUFFIX));
            if (meta.exists() && properties.isEmpty()) {
                delete(path, meta);
                return;
            }

            final Properties store = new Properties();
            for (final Property property : properties) {
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
    public void unlock(final Path path) throws WebDavException {
        synchronized (monitor) {
            final File lock = getMetaFile(path.append(LOCK_SUFFIX));
            if (!lock.delete()) {
                throw new WebDavException("can not remove lock for " + path);
            }
        }
    }
}
