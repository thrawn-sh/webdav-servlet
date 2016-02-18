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
package de.shadowhunt.webdav.impl.precondition;

import javax.servlet.http.HttpServletRequest;

import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavStore;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;

public class Precondition {

    public void verify(final WebDavStore store, final WebDavPath path, final HttpServletRequest request) {
        final String precondition = request.getHeader("If");
        if (StringUtils.isEmpty(precondition)) {
            return;
        }

        final CharStream stream = new ANTLRInputStream(precondition);
        final TokenSource lexer = new PreconditionLexer(stream);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final PreconditionParser parser = new PreconditionParser(tokens);

        final ParseTree tree = parser.precondition();
        final ParseTreeWalker walker = new ParseTreeWalker();
        final ParseTreeListener listener = new PreconditionValidatior(store, path);
        walker.walk(listener, tree);
    }
}
