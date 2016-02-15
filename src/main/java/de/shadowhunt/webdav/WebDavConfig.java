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

import java.io.Serializable;

public final class WebDavConfig implements Serializable {

    private static final WebDavConfig INSTANCE = new WebDavConfig();

    private static final long serialVersionUID = 1L;

    public static WebDavConfig getInstance() {
        return INSTANCE;
    }

    private volatile boolean allowInfiniteDepthRequests = false;

    private volatile boolean readOnly = true;

    private volatile boolean showCollectionListings = false;

    private WebDavConfig() {
        // prevent instantiation
    }

    public boolean isAllowInfiniteDepthRequests() {
        return allowInfiniteDepthRequests;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isShowCollectionListings() {
        return showCollectionListings;
    }

    public void setAllowInfiniteDepthRequests(final boolean allowInfiniteDepthRequests) {
        this.allowInfiniteDepthRequests = allowInfiniteDepthRequests;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setShowCollectionListings(final boolean showCollectionListings) {
        this.showCollectionListings = showCollectionListings;
    }
}
