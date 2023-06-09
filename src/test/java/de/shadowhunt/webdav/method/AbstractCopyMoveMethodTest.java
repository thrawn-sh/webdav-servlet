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
package de.shadowhunt.webdav.method;

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavConstant.Depth;
import de.shadowhunt.webdav.WebDavConstant.Header;
import de.shadowhunt.webdav.WebDavConstant.Status;
import de.shadowhunt.webdav.WebDavPath;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Matchers;
import org.mockito.Mockito;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractCopyMoveMethodTest extends AbstractWebDavMethodTest {

    protected static final WebDavPath SOURCE_ITEM = WebDavPath.create("/source_item.txt");

    protected abstract WebDavMethod createMethod();

    @Before
    public void fillStore() {
        createItem(SOURCE_ITEM, "test", false);
    }

    @Test
    public void test00_missingSource() throws Exception {
        final WebDavMethod method = createMethod();

        Mockito.when(request.getPath()).thenReturn(NON_EXISTING);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.NOT_FOUND);
    }

    @Test
    public void test01_existingSourceItem_missingTargetItem() throws Exception {
        final WebDavMethod method = createMethod();

        Mockito.when(request.getHeader(Matchers.eq(Header.DEPTH), Matchers.anyString())).thenReturn(Depth.INFINITY.name);
        Mockito.when(request.getHeader(Matchers.eq(Header.DESTINATION), Matchers.anyString())).thenReturn("http://127.0.0.1/webdav/non_existing.txt");
        Mockito.when(request.getPath()).thenReturn(SOURCE_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.CREATED);
    }

    @Test
    public void test01_existingSourceItem_missingTargetParent() throws Exception {
        final WebDavMethod method = createMethod();

        Mockito.when(request.getHeader(Matchers.eq(Header.DEPTH), Matchers.anyString())).thenReturn(Depth.INFINITY.name);
        Mockito.when(request.getHeader(Matchers.eq(Header.DESTINATION), Matchers.anyString())).thenReturn("http://127.0.0.1/webdav/non_existing/item.txt");
        Mockito.when(request.getPath()).thenReturn(SOURCE_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.CONFLICT);
    }

    @Test
    public void test02_existingSourceItem_existingTargetItem_noOverride() throws Exception {
        final WebDavMethod method = createMethod();

        final WebDavPath target = WebDavPath.create("/target_item-02.txt");
        createItem(target, "test", false);

        Mockito.when(request.getHeader(Matchers.eq(Header.DEPTH), Matchers.anyString())).thenReturn(Depth.INFINITY.name);
        Mockito.when(request.getHeader(Matchers.eq(Header.DESTINATION), Matchers.anyString())).thenReturn("http://127.0.0.1/webdav/target_item-02.txt");
        Mockito.when(request.getHeader(Matchers.eq(Header.OVERRIDE), Matchers.anyString())).thenReturn("F");
        Mockito.when(request.getPath()).thenReturn(SOURCE_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.PRECONDITION_FAILED);
    }

    @Test
    public void test03_existingSourceItem_existingTargetItem_override() throws Exception {
        final WebDavMethod method = createMethod();

        final WebDavPath target = WebDavPath.create("/target_item-03.txt");
        createItem(target, "test", false);

        Mockito.when(request.getHeader(Matchers.eq(Header.DEPTH), Matchers.anyString())).thenReturn(Depth.INFINITY.name);
        Mockito.when(request.getHeader(Matchers.eq(Header.DESTINATION), Matchers.anyString())).thenReturn("http://127.0.0.1/webdav/target_item-03.txt");
        Mockito.when(request.getHeader(Matchers.eq(Header.OVERRIDE), Matchers.anyString())).thenReturn("T");
        Mockito.when(request.getPath()).thenReturn(SOURCE_ITEM);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.NO_CONTENT);
    }

    @Test
    public void test03_shallow() throws Exception {
        final WebDavPath srcRoot = createCollection(WebDavPath.create("shallow_source"), false).getPath();
        createItem(srcRoot.append("item.txt"), "test", false);
        createItem(srcRoot.append("child/item.txt"), "test", false);

        final WebDavMethod method = createMethod();

        final WebDavPath target = WebDavPath.create("/target_item-03.txt");
        createItem(target, "test", false);

        Mockito.when(request.getHeader(Matchers.eq(Header.DEPTH), Matchers.anyString())).thenReturn(Depth.MEMBERS.name);
        Mockito.when(request.getHeader(Matchers.eq(Header.DESTINATION), Matchers.anyString())).thenReturn("http://127.0.0.1/webdav/shallow_target");
        Mockito.when(request.getHeader(Matchers.eq(Header.OVERRIDE), Matchers.anyString())).thenReturn("T");
        Mockito.when(request.getPath()).thenReturn(srcRoot);

        final TestResponse response = execute(method);
        assertNoContent(response, Status.CREATED);
    }
}
