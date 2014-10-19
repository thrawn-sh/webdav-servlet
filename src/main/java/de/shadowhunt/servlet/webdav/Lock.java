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

import javax.annotation.concurrent.Immutable;

@Immutable
public final class Lock {

    public static enum Scope {
        EXCLUSIVE, SHARED;
    }

    private final String owner;

    private final Scope scope;

    private final String token;

    public Lock(final String token, final Scope scope, final String owner) {
        this.owner = owner;
        this.token = token;
        this.scope = scope;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Lock)) {
            return false;
        }

        final Lock lock = (Lock) o;

        if (!token.equals(lock.token)) {
            return false;
        }

        return true;
    }

    public String getOwner() {
        return owner;
    }

    public Scope getScope() {
        return scope;
    }

    public String getToken() {
        return token;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Lock{");
        sb.append("owner=").append(owner);
        sb.append(", token=").append(token);
        sb.append(", scope=").append(scope);
        sb.append('}');
        return sb.toString();
    }
}
