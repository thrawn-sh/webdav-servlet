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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import de.shadowhunt.servlet.webdav.Entity;

abstract class BasicResponse implements WebDavResponse {

    private static final String COLLECTION;

    static {
        { // non existing
            final Set<String> operations = new TreeSet<>();
            operations.add(OptionsMethod.METHOD);
            operations.add(MkColMethod.METHOD);
            operations.add(PutMethod.METHOD);
            NON_EXISTING = StringUtils.join(operations, ", ");
        }
        { // items
            final Set<String> operations = new TreeSet<>();
            operations.add(CopyMoveMethod.COPY_METHOD);
            operations.add(CopyMoveMethod.MOVE_METHOD);
            operations.add(DeleteMethod.METHOD);
            operations.add(GetMethod.METHOD);
            operations.add(HeadMethod.METHOD);
            operations.add(LockMethod.METHOD);
            operations.add(OptionsMethod.METHOD);
            operations.add(PropFindMethod.METHOD);
            operations.add(PropPatchMethod.METHOD);
            operations.add(PutMethod.METHOD);
            operations.add(UnlockMethod.METHOD);
            ITEM = StringUtils.join(operations, ", ");
        }
        { // collections
            final Set<String> operations = new TreeSet<>();
            operations.add(CopyMoveMethod.COPY_METHOD);
            operations.add(CopyMoveMethod.MOVE_METHOD);
            operations.add(DeleteMethod.METHOD);
            operations.add(GetMethod.METHOD);
            operations.add(HeadMethod.METHOD);
            operations.add(LockMethod.METHOD);
            // operations.add(MkColMethod.METHOD);
            operations.add(OptionsMethod.METHOD);
            operations.add(PropFindMethod.METHOD);
            operations.add(PropPatchMethod.METHOD);
            operations.add(UnlockMethod.METHOD);
            COLLECTION = StringUtils.join(operations, ", ");
        }
    }

    private static final String ITEM;

    private static final String NON_EXISTING;

    public static final WebDavResponse createBadRequest(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        };
    }

    public static final WebDavResponse createConflict(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_CONFLICT);
            }
        };
    }

    public static final WebDavResponse createCreated(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_CREATED);
            }
        };
    }

    public static final WebDavResponse createForbidden(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        };
    }

    public static final WebDavResponse createLocked(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(423);
            }
        };
    }

    public static final WebDavResponse createMessageNodeAllowed(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        };
    }

    public static WebDavResponse createMethodNotAllowed(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        };
    }

    public static final WebDavResponse createNoContent(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        };
    }

    public static final WebDavResponse createNotFound() {
        return new BasicResponse(null) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        };
    }

    public static WebDavResponse createOk(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_OK);
            }
        };
    }

    public static final WebDavResponse createPreconditionFailed(final Entity entity) {
        return new BasicResponse(entity) {

            @Override
            protected void write0(final HttpServletResponse response) throws ServletException, IOException {
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            }
        };
    }

    public static final WebDavResponse createUnsupportedMediaType(final Entity entity) {
        return new BasicResponse(entity) {

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

    protected BasicResponse(@CheckForNull final Entity entity) {
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
