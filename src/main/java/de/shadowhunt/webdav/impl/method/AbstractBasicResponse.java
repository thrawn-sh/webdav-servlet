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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.webdav.Entity;
import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavMethod.Method;

import org.apache.commons.lang3.StringUtils;

abstract class AbstractBasicResponse implements WebDavResponse {

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

    public static final WebDavResponse createBadRequest(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        };
    }

    public static final WebDavResponse createConflict(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_CONFLICT);
            }
        };
    }

    public static final WebDavResponse createCreated(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_CREATED);
            }
        };
    }

    public static final WebDavResponse createForbidden(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        };
    }

    public static final WebDavResponse createLocked(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(423);
            }
        };
    }

    public static final WebDavResponse createMessageNodeAllowed(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        };
    }

    public static WebDavResponse createMethodNotAllowed(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        };
    }

    public static final WebDavResponse createNoContent(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        };
    }

    public static final WebDavResponse createNotFound() {
        return new AbstractBasicResponse(null) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        };
    }

    public static WebDavResponse createOk(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_OK);
            }
        };
    }

    public static final WebDavResponse createPreconditionFailed(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            }
        };
    }

    public static final WebDavResponse createUnsupportedMediaType(@Nullable final Entity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            }
        };
    }

    protected static final String getAllowedMethods(@CheckForNull final Entity entity) {
        final WebDavConfig config = WebDavConfig.getInstance();
        if (entity == null) {
            return config.isReadOnly() ? NON_EXISTING_READ_ONLY : NON_EXISTING;
        }
        if (entity.getType() == Entity.Type.COLLECTION) {
            return config.isReadOnly() ?  COLLECTION_READ_ONLY : COLLECTION;
        }
        return config.isReadOnly() ?  ITEM_READ_ONLY : ITEM;
    }

    protected final Entity entity;

    protected AbstractBasicResponse(@Nullable final Entity entity) {
        this.entity = entity;
    }

    @Override
    public final void write(final HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Allow", getAllowedMethods(entity));
        response.addHeader("DAV", "1,2");
        response.addHeader("MS-Author-Via", "DAV"); // MS required header
        if (entity != null) {
            if (entity.getType() == Entity.Type.ITEM) {
                Optional<String> hash = entity.getHash();
                if (hash.isPresent()) {
                    response.addHeader("ETag", hash.get());
                }
            }
            response.addDateHeader("Last-Modified", entity.getLastModified().getTime());
        }
        response.setCharacterEncoding("UTF-8");
        write0(response);
    }

    protected abstract void write0(final HttpServletResponse response) throws ServletException, IOException;
}
