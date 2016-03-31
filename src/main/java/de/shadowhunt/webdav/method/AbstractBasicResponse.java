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
package de.shadowhunt.webdav.method;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavResponse;
import de.shadowhunt.webdav.WebDavResponseWriter;
import de.shadowhunt.webdav.method.WebDavMethod.Method;
import de.shadowhunt.webdav.store.WebDavEntity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

abstract class AbstractBasicResponse implements WebDavResponseWriter {

    private static final String COLLECTION;

    private static final String COLLECTION_READ_ONLY;

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static final String DEFAULT_ENCODING = DEFAULT_CHARSET.name();

    private static final FastDateFormat HTTP_DATE_FORMATTER = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss zzz");

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

    public static final WebDavResponseWriter createBadRequest(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_BAD_REQUEST);
            }
        };
    }

    public static final WebDavResponseWriter createConflict(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_CONFLICT);
            }
        };
    }

    public static final WebDavResponseWriter createCreated(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_CREATED);
            }
        };
    }

    public static final WebDavResponseWriter createForbidden(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_FORBIDDEN);
            }
        };
    }

    public static final WebDavResponseWriter createLocked(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_LOCKED);
            }
        };
    }

    public static final WebDavResponseWriter createMessageNodeAllowed(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_METHOD_NOT_ALLOWED);
            }
        };
    }

    public static WebDavResponseWriter createMethodNotAllowed(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_METHOD_NOT_ALLOWED);
            }
        };
    }

    public static final WebDavResponseWriter createNoContent(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_NO_CONTENT);
            }
        };
    }

    public static final WebDavResponseWriter createNotFound() {
        return new AbstractBasicResponse(null) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_NOT_FOUND);
            }
        };
    }

    public static WebDavResponseWriter createOk(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_OK);
            }
        };
    }

    public static final WebDavResponseWriter createPreconditionFailed(@Nullable final WebDavEntity entity) {
        return new AbstractBasicResponse(entity) {

            @Override
            protected void write0(final WebDavResponse response) throws IOException {
                response.setStatus(WebDavResponse.Status.SC_PRECONDITION_FAILED);
            }
        };
    }

    public static final WebDavResponseWriter createUnsupportedMediaType(@Nullable final WebDavEntity entity) {
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

        response.addHeader(WebDavResponse.ALLOW_HEADER, getAllowedMethods(entity, config));
        response.addHeader(WebDavResponse.DAV_HEADER, "1,2");
        response.addHeader(WebDavResponse.MS_AUTHOR_HEADER, "DAV"); // MS required header
        if (entity != null) {
            final Optional<String> etag = entity.getEtag();
            etag.ifPresent(x -> response.addHeader(WebDavResponse.ETAG_HEADER, x));

            response.addHeader(WebDavResponse.LAST_MODIFIED_HEADER, HTTP_DATE_FORMATTER.format(entity.getLastModified()));
        }
        write0(response);
    }

    protected abstract void write0(final WebDavResponse response) throws IOException;

}
