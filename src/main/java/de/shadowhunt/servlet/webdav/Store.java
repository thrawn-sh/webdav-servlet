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
     * Upload a new revision of the resource and set properties
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param parents whether to create missing parents folders or not
     * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void add(Resource resource, InputStream content) throws WebDavException;

    /**
     * Recursively copy a resource in the given revision
     *
     * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
     * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
     * @param parents whether to create missing parents folders or not
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void copy(Resource srcResource, Resource targetResource) throws WebDavException;

    /**
     * Delete the resource from the repository
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void delete(Resource resource) throws WebDavException;

    /**
     * Download the resource
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     *
     * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    InputStream download(Resource resource) throws WebDavException;

    /**
     * Check if the resource already exists in the latest revision of the repository
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     *
     * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code false}
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    boolean exists(Resource resource) throws WebDavException;

    /**
     * Retrieve information for the resource
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     *
     * @return {@link Entity} for the resource
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    Entity info(Resource resource) throws WebDavException;

    /**
     * Retrieve information for the resource in the given revision and its child resources (depending on depth parameter)
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param depth whether to retrieve only for the given resource, its children or only part of its children depending on the value of {@link Depth}
     *
     * @return {@link List} of {@link Entity} for the resource and its child resources (depending on depth parameter)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    List<Resource> list(Resource resource) throws WebDavException;

    /**
     * Mark the expected revision of the resource as locked
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param steal if the resource is locked by another user {@code true} will override the lock, otherwise the operation will fail
     *
     * @throws NullPointerException if resource is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void lock(Resource resource, boolean steal) throws WebDavException;

    /**
     * Create a folder with all necessary parents folders
     *
     * @param resource the {@link de.shadowhunt.servlet.webdav.Resource} of the resource (relative to the repository root)
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void mkdir(Resource resource) throws WebDavException;

    /**
     * Recursively move a resource (latest revision)
     *
     * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
     * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
     * @param parents whether to create missing parents folders or not
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void move(Resource srcResource, Resource targetResource) throws WebDavException;

    /**
     * Remove the given properties form the resource
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param properties {@link Property} to remove
     *
     * @throws IllegalArgumentException if properties contain {@code null} elements
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void propertiesDelete(Resource resource, Property... properties) throws WebDavException;

    /**
     * Set the given properties for the resource (new properties will be added, existing properties will be overridden)
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param properties {@link Property} to add or override
     *
     * @throws IllegalArgumentException if properties contain {@code null} elements
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void propertiesSet(Resource resource, Property... properties) throws WebDavException;

    /**
     * Remove the lock on the expected revision of the resource
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param force the user that created the lock must match the user who wants to delete it, unless force is {@code true}
     *
     * @throws NullPointerException if resource is {@code null}
     * @throws WebDavException if an error occurs while operating on the repository
     */
    void unlock(Resource resource, boolean force) throws WebDavException;
}
