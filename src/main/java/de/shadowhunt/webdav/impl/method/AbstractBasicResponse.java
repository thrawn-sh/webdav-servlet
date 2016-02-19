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
package de.shadowhunt.webdav.impl.method;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavMethod.Method;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavResponseFoo;

import org.apache.commons.lang3.StringUtils;

abstract class AbstractBasicResponse implements WebDavResponseFoo {

    private static final String COLLECTION;

    private static final String COLLECTION_READ_ONLY;

    private static final String ITEM;

    private static final String ITEM_READ_ONLY;

    private static final String NON_EXISTING;

    private static final String NON_EXISTING_READ_ONLY;

    static {
        final Set<Method> nonExistingOperations = new TreeSet<>();
        nonExistingOperations.add(Method.OPTIONS);
        NON_EXISTING_READ_ONLY = StringUtils.join(nonExistingOperations, ", ");
        nonExistingOperations.add(Method.MKCOL);
        nonExistingOperations.add(Method.PUT);
        NON_EXISTING = StringUtils.join(nonExistingOperations, ", ");

        final Set<Method> itemOperations = new TreeSet<>();
        itemOperations.add(Method.GET);
        itemOperations.add(Method.HEAD);
        itemOperations.add(Method.OPTIONS);
        itemOperations.add(Method.PROPFIND);
        ITEM_READ_ONLY = StringUtils.join(itemOperations, ", ");
        itemOperations.add(Method.COPY);
        itemOperations.add(Method.DELETE);
        itemOperations.add(Method.LOCK);
        itemOperations.add(Method.MOVE);
        itemOperations.add(Method.PROPPATCH);
        itemOperations.add(Method.PUT);
        itemOperations.add(Method.UNLOCK);
        ITEM = StringUtils.join(itemOperations, ", ");

        final Set<Method> collectionOperations = new TreeSet<>();
        collectionOperations.add(Method.GET);
        collectionOperations.add(Method.HEAD);
        collectionOperations.add(Method.OPTIONS);
        collectionOperations.add(Method.PROPFIND);
        COLLECTION_READ_ONLY = StringUtils.join(collectionOperations, ", ");
        collectionOperations.add(Method.COPY);
        collectionOperations.add(Method.DELETE);
        collectionOperations.add(Method.LOCK);
        // collectionOperations.add(Method.MKCOL);
        collectionOperations.add(Method.MOVE);
        collectionOperations.add(Method.PROPPATCH);
        collectionOperations.add(Method.UNLOCK);
        COLLECTION = StringUtils.join(collectionOperations, ", ");
    }

    public static final WebDavResponseFoo createBadRequest(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_BAD_REQUEST);
            }
        };
    }

    public static final WebDavResponseFoo createConflict(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_CONFLICT);
            }
        };
    }

    public static final WebDavResponseFoo createCreated(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_CREATED);
            }
        };
    }

    public static final WebDavResponseFoo createForbidden(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_FORBIDDEN);
            }
        };
    }

    public static final WebDavResponseFoo createLocked(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_LOCKED);
            }
        };
    }

    public static final WebDavResponseFoo createMessageNodeAllowed(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_METHOD_NOT_ALLOWED);
            }
        };
    }

    public static WebDavResponseFoo createMethodNotAllowed(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_METHOD_NOT_ALLOWED);
            }
        };
    }

    public static final WebDavResponseFoo createNoContent(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_NO_CONTENT);
            }
        };
    }

    public static final WebDavResponseFoo createNotFound() {
        return new AbstractBasicResponse(null) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_NOT_FOUND);
            }
        };
    }

    public static WebDavResponseFoo createOk(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_OK);
            }
        };
    }

    public static final WebDavResponseFoo createPreconditionFailed(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_PRECONDITION_FAILED);
            }
        };
    }

    public static final WebDavResponseFoo createUnsupportedMediaType(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_UNSUPPORTED_MEDIA_TYPE);
            }
        };
    }

    protected static final String getAllowedMethods(@CheckForNull final WebDavEntity entity, final WebDavConfig config) {
        if (entity == null) {
            return config.isReadOnly() ? NON_EXISTING_READ_ONLY : NON_EXISTING;
        }
        if (entity.getType() == WebDavEntity.Type.COLLECTION) {
            return config.isReadOnly() ? COLLECTION_READ_ONLY : COLLECTION;
        }
        return config.isReadOnly() ? ITEM_READ_ONLY : ITEM;
    }

    protected final WebDavEntity entity;

    protected AbstractBasicResponse(@Nullable final WebDavEntity entity) {
        this.entity = entity;
    }

    @Override
    public final void write(final WebDavResponse response) throws IOException {
        final WebDavRequest request = response.getRequest();
        final WebDavConfig config = request.getConfig();

        response.addHeader("Allow", getAllowedMethods(entity, config));
        response.addHeader("DAV", "1,2");
        response.addHeader("MS-Author-Via", "DAV"); // MS required header
        if (entity != null) {
            final Optional<String> hash = entity.getHash();
            hash.ifPresent(x -> response.addHeader("ETag", x));

            // response.addDateHeader("Last-Modified", entity.getLastModified().getTime()); FIXME
        }
        // response.setCharacterEncoding("UTF-8"); FIXME
        write0(response);
    }

    protected abstract void write0(final WebDavResponse response) throws IOException;

}
