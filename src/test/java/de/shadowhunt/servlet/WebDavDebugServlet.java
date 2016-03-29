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

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.webdav.WebDavRequest;

import org.apache.commons.io.FileUtils;

public class WebDavDebugServlet extends WebDavServlet {

    private static final long serialVersionUID = 1L;

    private File dumpLocation = FileUtils.getTempDirectory();

    @Override
    protected WebDavRequest createWebDavRequestWrapper(final HttpServletRequest request) throws IOException {
        return RequestDebugHelper.generateAndDump(request, config, dumpLocation);
    }
}
