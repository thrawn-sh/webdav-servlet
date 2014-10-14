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
import java.util.Date;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import de.shadowhunt.servlet.webdav.Depth;
import de.shadowhunt.servlet.webdav.Entity;
import de.shadowhunt.servlet.webdav.Property;
import de.shadowhunt.servlet.webdav.Resource;
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

    @Override
    public void add(final Resource resource, final InputStream content) throws WebDavException {
        final File file = getFile(resource, false);
        OutputStream os = null;
        try {
            os = FileUtils.openOutputStream(file);
            IOUtils.copy(content, os);
        } catch (IOException e) {
            throw new WebDavException("can not write to resource " + resource, e);
        } finally {
            IOUtils.closeQuietly(content);
            IOUtils.closeQuietly(os);
        }
    }

    private Date calculateLastModified(final File file, final Resource resource) {
        try {
            return new Date(file.lastModified());
        } catch (final Exception e) {
            throw new WebDavException("can not calculate last modified date for " + resource, e);
        }
    }

    @CheckForNull
    private String calculateMd5(final File file, final Resource resource) {
        if (file.isDirectory()) {
            return null;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return DigestUtils.md5Hex(fis);
        } catch (Exception e) {
            throw new WebDavException("can not calculate md5 hash for " + resource, e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    private long calculateSize(final File file, final Resource resource) {
        if (file.isDirectory()) {
            return 0L;
        }

        try {
            return file.length();
        } catch (Exception e) {
            throw new WebDavException("can not calculate size for " + resource, e);
        }
    }

    @Override
    public void copy(final Resource srcResource, final Resource targetResource) throws WebDavException {
        final File source = getFile(srcResource, true);
        final File target = getFile(targetResource, false);
        try {
            FileUtils.copyFile(source, target);
        } catch (final IOException e) {
            throw new WebDavException("can not copy " + srcResource + " to " + targetResource, e);
        }
    }

    @Override
    public void delete(final Resource resource) throws WebDavException {
        final File file = getFile(resource, true);
        if (file.isFile()) {
            if (!file.delete()) {
                throw new WebDavException("can not delete " + resource);
            }
        } else {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                throw new WebDavException("can not delete " + resource, e);
            }
        }
    }

    private Entity.Type determineType(final File file) {
        if (file.isDirectory()) {
            return Entity.Type.COLLECTION;
        } else {
            return Entity.Type.FILE;
        }
    }

    @Override
    public InputStream download(final Resource resource) throws WebDavException {
        final File file = getFile(resource, true);
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            throw new WebDavException("can not retrieve content", e);
        }
    }

    @Override
    public boolean exists(final Resource resource) throws WebDavException {
        final File file = getFile(resource, false);
        return file.exists();
    }

    private File getFile(final Resource resource, final boolean mustExist) throws WebDavException {
        final File file = new File(resourceRoot, resource.getValue());
        if (mustExist && !file.exists()) {
            throw new WebDavException(HttpServletResponse.SC_NOT_FOUND, "can not locate resource: " + resource);
        }
        return file;
    }

    private File getMetaFile(final Resource resource) throws WebDavException {
        return new File(metaRoot, resource.getValue());
    }

    @Override
    public Entity info(final Resource resource) throws WebDavException {
        final File file = getFile(resource, true);

        final Entity entity = new Entity();
        entity.setResource(resource);
        entity.setMd5(calculateMd5(file, resource));
        entity.setSize(calculateSize(file, resource));
        entity.setType(determineType(file));

        entity.setLastModified(calculateLastModified(file, resource));

        return entity;
    }

    @Override
    public List<Entity> list(final Resource resource, final Depth depth) throws WebDavException {
        final File file = getFile(resource, true);
        final List<Entity> entities = new ArrayList<>();
        for (final String child : file.list()) {
            entities.add(info(resource.append(Resource.create(child))));
        }
        return entities;
    }

    @Override
    public void lock(final Resource resource, final boolean steal) throws WebDavException {

    }

    @Override
    public void mkdir(final Resource resource) throws WebDavException {
        final File file = getFile(resource, false);
        if (!file.mkdirs()) {
            throw new WebDavException("can not create folder " + resource);
        }
    }

    @Override
    public void move(final Resource srcResource, final Resource targetResource, final boolean parents) throws WebDavException {
        final File source = getFile(srcResource, true);
        final File target = getFile(targetResource, false);
        try {
            FileUtils.moveFile(source, target);
        } catch (final IOException e) {
            throw new WebDavException("can not move " + srcResource + " to " + targetResource, e);
        }
    }

    @Override
    public void propertiesDelete(final Resource resource, final Property... properties) throws WebDavException {

    }

    @Override
    public void propertiesSet(final Resource resource, final Property... properties) throws WebDavException {

    }

    @Override
    public void unlock(final Resource resource, final boolean force) throws WebDavException {

    }
}
