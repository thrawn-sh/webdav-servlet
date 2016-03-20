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

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import de.shadowhunt.webdav.WebDavLock;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.precondition.PreconditionParser.ConditionContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.ExplicitResourceListContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.ImplicitResourceListContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.ListContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.LockContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.MatchContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.PreconditionContext;
import de.shadowhunt.webdav.precondition.PreconditionParser.ResourceContext;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.apache.commons.lang3.StringUtils;

public class LockTokenCollector extends AbstractAggregator<Map<WebDavPath, UUID>> {

    private final ParseTreeProperty<WebDavPath> paths = new ParseTreeProperty<>();

    private final WebDavRequest request;

    private final Map<WebDavPath, UUID> tokens = new TreeMap<>();

    LockTokenCollector(final WebDavRequest request) {
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

    @Override
    public void enterLock(final LockContext ctx) {
        propagatePath(ctx);
    }

    @Override
    public void enterMatch(final MatchContext ctx) {
        propagatePath(ctx);
    }

    @Override
    public void exitLock(final LockContext ctx) {
        final String token = ctx.LOCK().getText();
        if (!token.startsWith(WebDavLock.PREFIX)) {
            return;
        }

        final String plain = StringUtils.removeStart(token, WebDavLock.PREFIX);
        try {
            final WebDavPath path = paths.get(ctx);
            tokens.put(path, UUID.fromString(plain));
        } catch (final IllegalArgumentException e) {
            // ignore exception
        }
    }

    @Override
    Optional<Map<WebDavPath, UUID>> getResult(final PreconditionContext context) {
        return Optional.of(tokens);
    }

    private void propagatePath(final ParserRuleContext context) {
        final ParserRuleContext parent = context.getParent();
        final WebDavPath path = paths.get(parent);
        paths.put(context, path);
    }
}
