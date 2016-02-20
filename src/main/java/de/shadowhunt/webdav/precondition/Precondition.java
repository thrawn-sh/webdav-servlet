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

import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.precondition.PreconditionParser.ConditionContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.ListContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.NoTagListContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.PreconditionContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.TagListContext;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;

public final class Precondition {

    private static class PreconditionValidatior extends PreconditionBaseListener {

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
            final ParserRuleContext parent = ctx.getParent();
            final WebDavPath path = paths.get(parent);
            if (path == null) {
                // tagList where the resource-tag does not belong to the store
                evaluatuion.put(ctx, false);
                return;
            }

            if (!store.exists(path)) {
                evaluatuion.put(ctx, false);
                return;
            }

            final boolean expectation = (ctx.NOT() == null);
            // TODO FIXME check condition

            evaluatuion.put(ctx, expectation);
        }

        @Override
        public void enterList(final ListContext ctx) {
            final ParserRuleContext parent = ctx.getParent();
            final WebDavPath path = paths.get(parent);
            paths.put(ctx, path);
        }

        @Override
        public void enterNoTagList(final NoTagListContext ctx) {
            paths.put(ctx, request.getPath());
        }

        @Override
        public void enterTagList(final TagListContext ctx) {
            final String resource = ctx.resourceTag().toString();
            final Optional<WebDavPath> path = request.toPath(resource);
            path.ifPresent(x -> paths.put(ctx, x));
        }

        @Override
        public void exitList(final ListContext ctx) {
            // for list to be true all child contexts *MUST* be true
            final int children = ctx.getChildCount();
            for (int c = 0; c < children; c++) {
                final ParseTree child = ctx.getChild(c);
                if (!getEvaluationResult(child)) {
                    evaluatuion.put(ctx, false);
                    return;
                }
            }
            evaluatuion.put(ctx, true);
        }

        @Override
        public void exitNoTagList(final NoTagListContext ctx) {
            // for noTagList to be true all child lists *MUST* be true
            final int children = ctx.getChildCount();
            for (int c = 0; c < children; c++) {
                final ParseTree child = ctx.getChild(c);
                if (!getEvaluationResult(child)) {
                    evaluatuion.put(ctx, false);
                    return;
                }
            }
            evaluatuion.put(ctx, true);
        }

        @Override
        public void exitTagList(final TagListContext ctx) {
            // for tagList to be true all child lists *MUST* be true
            final int children = ctx.getChildCount();
            for (int c = 0; c < children; c++) {
                final ParseTree child = ctx.getChild(c);
                if (!getEvaluationResult(child)) {
                    evaluatuion.put(ctx, false);
                    return;
                }
            }
            evaluatuion.put(ctx, true);
        }

        private boolean getEvaluationResult(final ParseTree node) {
            final Boolean result = evaluatuion.get(node);
            if (result == null) {
                return false;
            }
            return result;
        }
    }

    public static boolean verify(final WebDavStore store, final WebDavRequest request) {
        final String precondition = request.getOption("If", "");
        if (StringUtils.isEmpty(precondition)) {
            return true;
        }

        System.out.println("PRECOND:" + precondition);
        final CharStream stream = new ANTLRInputStream(precondition);
        final TokenSource lexer = new PreconditionLexer(stream);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final PreconditionParser parser = new PreconditionParser(tokens);

        final PreconditionContext context = parser.precondition();
        final ParseTreeWalker walker = new ParseTreeWalker();
        final PreconditionValidatior validator = new PreconditionValidatior(store, request);
        walker.walk(validator, context);

        // for precondition to be true at last one tagList or noTagList must be true
        final int children = context.getChildCount();
        for (int c = 0; c < children; c++) {
            final ParseTree child = context.getChild(c);
            if (validator.getEvaluationResult(child)) {
                return true;
            }
        }
        return false;
    }

    private Precondition() {
        // prevent instantiation
    }
}
