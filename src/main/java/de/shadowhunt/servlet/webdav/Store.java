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
package de.shadowhunt.servlet.webdav;

import java.io.InputStream;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface listing all available operations on a WebDav storage
 */
@ThreadSafe
public interface Store {

    /**
     * Create a folder with all necessary parents folders
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void createCollection(Path path) throws WebDavException;

    /**
     * Upload a new revision of the resource and set properties
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     * @param parents whether to create missing parents folders or not
     * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void createItem(Path path, InputStream content) throws WebDavException;

    /**
     * Delete the resource from the repository
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void delete(Path path) throws WebDavException;

    /**
     * Download the resource
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     *
     * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    InputStream download(Path path) throws WebDavException;

    /**
     * Check if the resource already exists in the latest revision of the repository
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     *
     * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code false}
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    boolean exists(Path path) throws WebDavException;

    /**
     * Retrieve information for the resource
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     *
     * @return {@link Entity} for the resource
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    Entity getEntity(Path path) throws WebDavException;

    /**
     * Retrieve information for the resource in the given revision and its child resources (depending on depth parameter)
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     * @param depth whether to retrieve only for the given resource, its children or only part of its children depending on the value of {@link Depth}
     *
     * @return {@link List} of {@link Entity} for the resource and its child resources (depending on depth parameter)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    List<Path> list(Path path) throws WebDavException;

    /**
     * Mark the expected revision of the resource as locked
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     * @param steal if the resource is locked by another user {@code true} will override the lock, otherwise the operation will fail
     *
     * @throws NullPointerException if resource is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void lock(Path path, boolean steal) throws WebDavException;

    /**
     * Remove the given properties form the resource
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     * @param properties {@link Property} to remove
     *
     * @throws IllegalArgumentException if properties contain {@code null} elements
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void propertiesDelete(Path path, Property... properties) throws WebDavException;

    /**
     * Set the given properties for the resource (new properties will be added, existing properties will be overridden)
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     * @param properties {@link Property} to createItem or override
     *
     * @throws IllegalArgumentException if properties contain {@code null} elements
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void propertiesSet(Path path, Property... properties) throws WebDavException;

    /**
     * Remove the lock on the expected revision of the resource
     *
     * @param path the {@link Path} of the resource (relative to the repository root)
     * @param force the user that created the lock must match the user who wants to delete it, unless force is {@code true}
     *
     * @throws NullPointerException if resource is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void unlock(Path path, boolean force) throws WebDavException;
}
