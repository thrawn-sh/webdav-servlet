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
package de.shadowhunt.servlet;

import java.util.Optional;

import de.shadowhunt.webdav.WebDavConfig;

class HttpServletConfig implements WebDavConfig {

    private volatile boolean allowInfiniteDepthRequests = false;

    private volatile String cssForCollectionListings = null;

    private volatile boolean readOnly = true;

    private volatile boolean showCollectionListings = false;

    @Override
    public Optional<String> getCssForCollectionListings() {
        return Optional.ofNullable(cssForCollectionListings);
    }

    @Override
    public boolean isAllowInfiniteDepthRequests() {
        return allowInfiniteDepthRequests;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean isShowCollectionListings() {
        return showCollectionListings;
    }

    public void setAllowInfiniteDepthRequests(final boolean allowInfiniteDepthRequests) {
        this.allowInfiniteDepthRequests = allowInfiniteDepthRequests;
    }

    public void setCssForCollectionListings(final String cssForCollectionListings) {
        this.cssForCollectionListings = cssForCollectionListings;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setShowCollectionListings(final boolean showCollectionListings) {
        this.showCollectionListings = showCollectionListings;
    }
}
