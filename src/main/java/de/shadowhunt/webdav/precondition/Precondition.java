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
package de.shadowhunt.webdav.precondition;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.precondition.PreconditionParser.PreconditionContext;
import de.shadowhunt.webdav.store.WebDavStore;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Precondition {

    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) throws ParseCancellationException {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

    private static final ANTLRErrorListener ERROR_LISTENER = new ThrowingErrorListener();

    private static final Logger LOGGER = LoggerFactory.getLogger(Precondition.class);

    public static final String PRECONDITION_HEADER = "If";

    private static <R> Optional<R> evaluate(final String precondition, final AbstractAggregator<R> aggregator) {
        try {
            final CharStream stream = new ANTLRInputStream(precondition);
            final PreconditionLexer lexer = new PreconditionLexer(stream);
            lexer.removeErrorListeners();
            lexer.addErrorListener(ERROR_LISTENER);

            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final PreconditionParser parser = new PreconditionParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(ERROR_LISTENER);

            final PreconditionContext context = parser.precondition();
            final ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(aggregator, context);

            return aggregator.getResult(context);
        } catch (final ParseCancellationException e) {
            LOGGER.warn("could not parse precondition '" + precondition + "'", e);
            return Optional.empty();
        }
    }

    public static Map<WebDavPath, UUID> getTokens(final WebDavRequest request) {
        final String precondition = request.getHeader(PRECONDITION_HEADER, "");
        if (StringUtils.isEmpty(precondition)) {
            return Collections.emptyMap();
        }

        final Optional<Map<WebDavPath, UUID>> valid = evaluate(precondition, new LockTokenCollector(request));
        return valid.orElse(Collections.emptyMap());
    }

    public static boolean verify(final WebDavStore store, final WebDavRequest request) {
        final String precondition = request.getHeader(PRECONDITION_HEADER, "");
        if (StringUtils.isEmpty(precondition)) {
            return true;
        }

        final Optional<Boolean> valid = evaluate(precondition, new PreconditionValidator(store, request));
        return valid.orElse(Boolean.FALSE);
    }

    private Precondition() {
        // prevent instantiation
    }
}
