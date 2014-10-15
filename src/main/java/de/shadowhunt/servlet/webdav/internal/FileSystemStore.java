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
package de.shadowhunt.servlet.webdav.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Path;
import de.shadowhunt.servlet.webdav.Property;
import de.shadowhunt.servlet.webdav.Store;
import de.shadowhunt.servlet.webdav.WebDavException;

public class FileSystemStore implements Store {

    private final File metaRoot;

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

    @CheckForNull
    private String calculateMd5(final File file, final Path path) {
        if (file.isDirectory()) {
            return null;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return DigestUtils.md5Hex(fis);
        } catch (Exception e) {
            throw new WebDavException("can not calculate md5 hash for " + path, e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    private long calculateSize(final File file, final Path path) {
        if (file.isDirectory()) {
            return 0L;
        }

        try {
            return file.length();
        } catch (Exception e) {
            throw new WebDavException("can not calculate size for " + path, e);
        }
    }

    @Override
    public void createCollection(final Path path) throws WebDavException {
        final File file = getFile(path, false);
        if (!file.mkdir()) {
            throw new WebDavException("can not create folder " + path);
        }
    }

    @Override
    public void createItem(final Path path, final InputStream content) throws WebDavException {
        final File file = getFile(path, false);
        OutputStream os = null;
        try {
            os = FileUtils.openOutputStream(file);
            IOUtils.copy(content, os);
        } catch (IOException e) {
            throw new WebDavException("can not write to resource " + path, e);
        } finally {
            IOUtils.closeQuietly(content);
            IOUtils.closeQuietly(os);
        }
    }

    @Override
    public void delete(final Path path) throws WebDavException {
        final File file = getFile(path, true);
        if (file.isFile()) {
            if (!file.delete()) {
                throw new WebDavException("can not delete " + path);
            }
        } else {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                throw new WebDavException("can not delete " + path, e);
            }
        }
    }

    @Override
    public InputStream download(final Path path) throws WebDavException {
        final File file = getFile(path, true);
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            throw new WebDavException("can not retrieve content", e);
        }
    }

    @Override
    public boolean exists(final Path path) throws WebDavException {
        final File file = getFile(path, false);
        return file.exists();
    }

    @Override
    public Entity getEntity(final Path path) throws WebDavException {
        final File file = getFile(path, true);

        if (file.isFile()) {
            return Entity.createItem(path, calculateMd5(file, path), calculateSize(file, path));
        }
        return Entity.createCollection(path);
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
    public List<Path> list(final Path path) throws WebDavException {
        final File file = getFile(path, true);
        if (file.isFile()) {
            return Collections.emptyList();
        }

        final List<Path> children = new ArrayList<>();
        for (final String child : file.list()) {
            children.add(path.getChild(child));
        }
        return children;
    }

    @Override
    public void lock(final Path path, final boolean steal) throws WebDavException {

    }

    @Override
    public void propertiesDelete(final Path path, final Property... properties) throws WebDavException {

    }

    @Override
    public void propertiesSet(final Path path, final Property... properties) throws WebDavException {

    }

    @Override
    public void unlock(final Path path, final boolean force) throws WebDavException {

    }
}
