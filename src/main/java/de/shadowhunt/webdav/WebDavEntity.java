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

import java.util.Date;
import java.util.Optional;

import javax.annotation.concurrent.Immutable;

@Immutable
public interface WebDavEntity extends Comparable<WebDavEntity> {

    enum Type {
        COLLECTION(1), ITEM(2);

        public final int priority;

        Type(final int priority) {
            this.priority = priority;
        }
    }

    @Override
    boolean equals(Object o);

    Optional<String> getHash();

    Date getLastModified();

    Optional<WebDavLock> getLock();

    String getName();

    WebDavPath getPath();

    long getSize();

    Type getType();

    @Override
    int hashCode();

    boolean isLocked();

    @Override
    String toString();

}
