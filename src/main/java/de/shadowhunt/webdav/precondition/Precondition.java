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

import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.precondition.PreconditionParser.ConditionContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.EtagContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.ExplicitResourceListContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.ImplicitResourceListContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.ListContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.LockContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.MatchContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.PreconditionContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.ResourceContext;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Precondition {

    private static class PreconditionValidatior extends PreconditionBaseListener {

        private static final UUID UUID_ZERO = new UUID(0L, 0L);

        private final ParseTreeProperty<Boolean> evaluatuion = new ParseTreeProperty<>();

        private final ParseTreeProperty<WebDavPath> paths = new ParseTreeProperty<>();

        private final WebDavRequest request;

        private final WebDavStore store;

        PreconditionValidatior(final WebDavStore store, final WebDavRequest request) {
            this.store = store;
            this.request = request;
        }

        @Override
        public void enterCondition(final ConditionContext ctx) {
            propagatePath(ctx);
        }

        @Override
        public void enterExplicitResourceList(final ExplicitResourceListContext ctx) {
            final ResourceContext resourceContext = ctx.resource();
            final String resource = resourceContext.URL().getText();
            final Optional<WebDavPath> path = request.toPath(resource);
            path.ifPresent(x -> paths.put(ctx, x));
        }

        @Override
        public void enterImplicitResourceList(final ImplicitResourceListContext ctx) {
            final WebDavPath path = request.getPath();
            paths.put(ctx, path);
        }

        @Override
        public void enterList(final ListContext ctx) {
            propagatePath(ctx);
        }

        private void evaluateChildren(final ParseTree node) {
            // for node to be true all child nodes *MUST* be true
            final int children = node.getChildCount();
            for (int c = 0; c < children; c++) {
                final ParseTree child = node.getChild(c);
                if (child instanceof ListContext) {
                    if (!evaluatuion.get(child)) {
                        evaluatuion.put(node, false);
                        return;
                    }
                }
            }
            evaluatuion.put(node, true);
        }

        @Override
        public void exitCondition(final ConditionContext ctx) {
            final MatchContext match = ctx.match();
            final boolean result = evaluatuion.get(match);
            if (ctx.NOT() == null) {
                evaluatuion.put(ctx, result);
            } else {
                evaluatuion.put(ctx, !result);
            }
        }

        @Override
        public void exitExplicitResourceList(final ExplicitResourceListContext ctx) {
            evaluateChildren(ctx);
        }

        @Override
        public void exitImplicitResourceList(final ImplicitResourceListContext ctx) {
            evaluateChildren(ctx);
        }

        @Override
        public void exitList(final ListContext ctx) {
            boolean result = true;
            for (final ConditionContext context : ctx.condition()) {
                final boolean conditionResult = evaluatuion.get(context);
                result &= conditionResult;
            }
            evaluatuion.put(ctx, result);
        }

        @Override
        public void exitMatch(final MatchContext ctx) {
            final ParserRuleContext parent = ctx.getParent();
            final WebDavPath path = paths.get(parent);
            if (path == null) {
                // explicitResourceList where the resource does not belong to the store
                evaluatuion.put(ctx, false);
                return;
            }

            if (!store.exists(path)) {
                evaluatuion.put(ctx, false);
                return;
            }

            final WebDavEntity entity = store.getEntity(path);

            final LockContext lockContext = ctx.lock();
            if (lockContext != null) {
                final UUID lockToken = getLockToken(lockContext.LOCK().getText());
                if (UUID_ZERO.equals(lockToken)) {
                    evaluatuion.put(ctx, false);
                    return;
                }

                final Optional<WebDavLock> lock = entity.getLock();
                lock.ifPresent(x -> evaluatuion.put(ctx, x.getToken().equals(lockToken)));
                return;
            }

            final EtagContext etagContext = ctx.etag();
            if (etagContext != null) {
                final String etagToken = etagContext.ETAG().getText();
                final Optional<String> etag = entity.getEtag();
                etag.ifPresent(x -> evaluatuion.put(ctx, etagToken.equals(x)));
                return;
            }

            // fallback
            evaluatuion.put(ctx, false);
        }

        private UUID getLockToken(final String token) {
            if (!token.startsWith(WebDavLock.PREFIX)) {
                return UUID_ZERO;
            }

            final String plain = StringUtils.removeStart(token, WebDavLock.PREFIX);
            try {
                return UUID.fromString(plain);
            } catch (final IllegalArgumentException e) {
                return UUID_ZERO;
            }
        }

        private void propagatePath(final ParserRuleContext context) {
            final ParserRuleContext parent = context.getParent();
            final WebDavPath path = paths.get(parent);
            paths.put(context, path);
        }
    }

    private static class ThrowingErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException e) throws ParseCancellationException {
            throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

    private static final ANTLRErrorListener ERROR_LISTENER = new ThrowingErrorListener();

    private static final Logger LOGGER = LoggerFactory.getLogger(Precondition.class);

    public static final String PRECONDITION_HEADER = "If";

    public static boolean verify(final WebDavStore store, final WebDavRequest request) {
        final String precondition = request.getHeader(PRECONDITION_HEADER, "");
        if (StringUtils.isEmpty(precondition)) {
            return true;
        }

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
            final PreconditionValidatior validator = new PreconditionValidatior(store, request);
            walker.walk(validator, context);

            // for precondition to be true at last one explicitResourceList or implicitResourceList must be true
            final int children = context.getChildCount();
            for (int c = 0; c < children; c++) {
                final ParseTree child = context.getChild(c);
                if (validator.evaluatuion.get(child)) {
                    return true;
                }
            }
            return false;
        } catch (final ParseCancellationException e) {
            LOGGER.warn("could not parse precondition '" + precondition + "'", e);
            return false;
        }
    }

    private Precondition() {
        // prevent instantiation
    }
}
