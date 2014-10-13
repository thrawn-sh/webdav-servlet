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

/**
 * {@link Depth} defines the recursion level for the listing call {@link Store #list(Resource, Revision, Depth)}
 */
public enum Depth {

    /**
     * only list the resources itself, no sub-resources
     */
    EMPTY("0"),

    /**
     * only list all direct file sub-resources
     */
    FILES("1"),

    /**
     * only list all direct sub-resources (files and directories)
     */
    IMMEDIATE("1"),

    /**
     * recursively list all sub-resources (files and directories)
     */
    INFINITY("infinity");

    /**
     * recursion level
     */
    public final String value;

    private Depth(final String value) {
        this.value = value;
    }
}
