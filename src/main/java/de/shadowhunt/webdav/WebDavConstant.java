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

import java.util.Arrays;
import java.util.Comparator;

public final class WebDavConstant {

    public enum Depth {

        INFINITY("infinity", Integer.MAX_VALUE), //
        MEMBERS("1", 1), //
        SELF("0", 0);

        private static final Comparator<Depth> VALUE_COMPARATOR = new Comparator<Depth>() {

            @Override
            public int compare(final Depth d1, final Depth d2) {
                return Integer.compare(d1.value, d2.value);
            }
        };

        public static Depth parse(final String depth, final Depth... allowed) {
            if (Depth.INFINITY.name.equalsIgnoreCase(depth)) {
                return Depth.INFINITY;
            }

            final int value = Integer.parseInt(depth);
            Arrays.sort(allowed, VALUE_COMPARATOR);
            for (final Depth d : allowed) {
                if (value <= d.value) {
                    return d;
                }
            }
            return Depth.INFINITY;
        }

        public final String name;

        public final int value;

        Depth(final String name, final int value) {
            this.name = name;
            this.value = value;
        }
    }

    public enum Header {

        ALLOW("Allow"), //
        CACHE_CONTROL("Cache-Control"), //
        DAV("DAV"), //
        DEPTH("Depth"), //
        DESTINATION("Destination"), //
        ETAG("ETag"), //
        LAST_MODIFIED("Last-Modified"), //
        LOCK_TOKEN("Lock-Token"), //
        MS_AUTHOR("MS-Author-Via"), //
        OVERRIDE("Overwrite"), //
        PRECONDITION("If"), //
        TIMEOUT("Timeout");

        public final String value;

        Header(final String value) {
            this.value = value;
        }
    }

    public enum Status {

        BAD_REQUEST(400), //
        CONFLICT(409), //
        CREATED(201), //
        FORBIDDEN(403), //
        INTERNAL_SERVER_ERROR(500), //
        LOCKED(423), //
        METHOD_NOT_ALLOWED(405), //
        MULTI_STATUS(207), //
        NO_CONTENT(204), //
        NOT_FOUND(404), //
        NOT_IMPLEMENTED(501), //
        OK(200), //
        PRECONDITION_FAILED(412), //
        UNAUTHORIZED(401), //
        UNSUPPORTED_MEDIA_TYPE(415);

        public final int value;

        Status(final int value) {
            this.value = value;
        }
    }
}
