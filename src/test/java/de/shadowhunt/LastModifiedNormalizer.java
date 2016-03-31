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
package de.shadowhunt;

public class LastModifiedNormalizer implements ContentNormalizer {

    private static final String REGEX = "<D:getlastmodified>..., \\d{2} ... \\d{4} \\d{2}:\\d{2}:\\d{2} .\\d{4}</D:getlastmodified>";

    private static final String REPLACEMENT = "<D:getlastmodified>Thu, 01 Jan 1970 01:00:00 +0100</D:getlastmodified>";

    @Override
    public String normalize(final String content) {
        return content.replaceAll(REGEX, REPLACEMENT);
    }
}
