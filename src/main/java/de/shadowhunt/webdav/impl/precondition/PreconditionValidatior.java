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

import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.impl.precondition.PreconditionParser.ConditionContext;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

public class PreconditionValidatior extends PreconditionBaseListener {

    private final WebDavPath defaultPath;

    private final ParseTreeProperty<Boolean> evaluatuion = new ParseTreeProperty<>();

    private final ParseTreeProperty<WebDavPath> paths = new ParseTreeProperty<>();

    private final WebDavStore store;

    public PreconditionValidatior(final WebDavStore store, final WebDavPath defaultPath) {
        this.store = store;
        this.defaultPath = defaultPath;
    }

    @Override
    public void enterCondition(final ConditionContext ctx) {
        final ParserRuleContext parent = ctx.getParent().getParent();
        final WebDavPath path = paths.get(parent);
        if (path == null) {
            // tagList where the resource-tag does not belong to the store
            evaluatuion.put(ctx, false);
            return;
        }

        final boolean expectation = (ctx.NOT() == null);
        final WebDavEntity entity = store.getEntity(path);
        if (entity == null) {
            evaluatuion.put(ctx, !expectation);
            return;
        }

        final TerminalNode stateToken = ctx.STATE_TOKEN();
        if (stateToken != null) {

            final String lock = stateToken.toString();

            return;
        }
    }
}
