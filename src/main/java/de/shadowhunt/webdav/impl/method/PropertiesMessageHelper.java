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
package de.shadowhunt.webdav.impl.method;

import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class PropertiesMessageHelper {

    private static final ErrorHandler QUIET_ERROR_HANDLER = new ErrorHandler() {

        @Override
        public void error(final SAXParseException exception) throws SAXException {
            // do nothing
        }

        @Override
        public void fatalError(final SAXParseException exception) throws SAXException {
            // do nothing
        }

        @Override
        public void warning(final SAXParseException exception) throws SAXException {
            // do nothing
        }
    };

    @CheckForNull
    public static Document parse(final InputStream inputStream) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            final DocumentBuilder db = factory.newDocumentBuilder();
            db.setErrorHandler(QUIET_ERROR_HANDLER);
            return db.parse(inputStream);
        } catch (final Exception e) {
            return null;
        }
    }

    private PropertiesMessageHelper() {
        // prevent instantiation
    }
}
