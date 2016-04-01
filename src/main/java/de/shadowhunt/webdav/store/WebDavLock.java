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
package de.shadowhunt.webdav.store;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.Immutable;

import de.shadowhunt.webdav.WebDavConstant.Depth;
import de.shadowhunt.webdav.WebDavPath;

import org.apache.commons.lang3.StringUtils;

@Immutable
public interface WebDavLock {

    enum LockScope {
        EXCLUSIVE, SHARED;
    }

    enum LockType {
        READ, WRITE;
    }

    class Timeout {

        public static final Timeout INFINITE = new Timeout(-1);

        public static final String INFINITE_STRING = "Infinite";

        public static final String PREFIX = "Seconds-";

        public static Timeout parse(final String value) {
            if (StringUtils.startsWithIgnoreCase(value, INFINITE_STRING)) {
                return INFINITE;
            }

            final String digits = StringUtils.removeStartIgnoreCase(value, PREFIX);

            try {
                final int seconds = Integer.parseInt(digits);
                return new Timeout(seconds);
            } catch (final NumberFormatException e) {
                return INFINITE;
            }
        }

        private final TimeUnit unit = TimeUnit.SECONDS;

        private final int value;

        Timeout(final int value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Timeout other = (Timeout) obj;
            if (unit != other.unit) {
                return false;
            }
            if (value != other.value) {
                return false;
            }
            return true;
        }

        public TimeUnit getUnit() {
            return unit;
        }

        public int getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((unit == null) ? 0 : unit.hashCode());
            result = prime * result + value;
            return result;
        }

        @Override
        public String toString() {
            if (value < 0) {
                return INFINITE_STRING;
            }
            return PREFIX + value;
        }
    }

    String PREFIX = "urn:uuid:";

    Depth getDepth();

    String getOwner();

    WebDavPath getRoot();

    LockScope getScope();

    Timeout getTimeout();

    UUID getToken();

    LockType getType();
}
