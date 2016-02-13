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
package de.shadowhunt.servlet.methods;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import de.shadowhunt.servlet.webdav.Entity;

import org.apache.commons.lang3.StringUtils;

abstract class AbstractBasicResponse implements WebDavResponse {

    private static final String COLLECTION;

    private static final String ITEM;

    private static final String NON_EXISTING;

    static {
        final Set<String> nonExistingOperations = new TreeSet<>();
        nonExistingOperations.add(OptionsMethod.METHOD);
        nonExistingOperations.add(MkColMethod.METHOD);
        nonExistingOperations.add(PutMethod.METHOD);
        NON_EXISTING = StringUtils.join(nonExistingOperations, ", ");
        
        final Set<String> itemOperations = new TreeSet<>();
        itemOperations.add(CopyMoveMethod.COPY_METHOD);
        itemOperations.add(CopyMoveMethod.MOVE_METHOD);
        itemOperations.add(DeleteMethod.METHOD);
        itemOperations.add(GetMethod.METHOD);
        itemOperations.add(HeadMethod.METHOD);
        itemOperations.add(LockMethod.METHOD);
        itemOperations.add(OptionsMethod.METHOD);
        itemOperations.add(PropFindMethod.METHOD);
        itemOperations.add(PropPatchMethod.METHOD);
        itemOperations.add(PutMethod.METHOD);
        itemOperations.add(UnlockMethod.METHOD);
        ITEM = StringUtils.join(itemOperations, ", ");
        
        final Set<String> collectionOperations = new TreeSet<>();
        collectionOperations.add(CopyMoveMethod.COPY_METHOD);
        collectionOperations.add(CopyMoveMethod.MOVE_METHOD);
        collectionOperations.add(DeleteMethod.METHOD);
        collectionOperations.add(GetMethod.METHOD);
        collectionOperations.add(HeadMethod.METHOD);
        collectionOperations.add(LockMethod.METHOD);
        // operations.add(MkColMethod.METHOD);
        collectionOperations.add(OptionsMethod.METHOD);
        collectionOperations.add(PropFindMethod.METHOD);
        collectionOperations.add(PropPatchMethod.METHOD);
        collectionOperations.add(UnlockMethod.METHOD);
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
        if (entity == null) {
            return NON_EXISTING;
        }
        if (entity.getType() == Entity.Type.COLLECTION) {
            return COLLECTION;
        }
        return ITEM;
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
                response.addHeader("ETag", entity.getHash());
            }
            response.addDateHeader("Last-Modified", entity.getLastModified().getTime());
        }
        response.setCharacterEncoding("UTF-8");
        write0(response);
    }

    protected abstract void write0(final HttpServletResponse response) throws ServletException, IOException;
}
