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
package de.shadowhunt.webdav;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface WebDavStore {

    void createCollection(Path path) throws WebDavException;

    void createItem(Path path, InputStream content) throws WebDavException;

    Lock createLock() throws WebDavException;

    void delete(Path path) throws WebDavException;

    InputStream download(Path path) throws WebDavException;

    boolean exists(Path path) throws WebDavException;

    Entity getEntity(Path path) throws WebDavException;

    Collection<Property> getProperties(Path path) throws WebDavException;

    List<Path> list(Path path) throws WebDavException;

    void lock(Path path, Lock lock) throws WebDavException;

    void setProperties(Path path, Collection<Property> properties) throws WebDavException;

    void unlock(Path path) throws WebDavException;
}
