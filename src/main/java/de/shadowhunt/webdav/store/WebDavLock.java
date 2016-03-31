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

import javax.annotation.concurrent.Immutable;

import de.shadowhunt.webdav.WebDavConstant.Depth;
import de.shadowhunt.webdav.WebDavPath;

@Immutable
public interface WebDavLock {

    enum LockScope {
        EXCLUSIVE, SHARED;
    }

    enum LockType {
        READ, WRITE;
    }

    int INFINITY = Integer.MAX_VALUE;

    String PREFIX = "urn:uuid:";

    Depth getDepth();

    String getOwner();

    WebDavPath getRoot();

    LockScope getScope();

    int getTimeoutInSeconds();

    UUID getToken();

    LockType getType();
}
