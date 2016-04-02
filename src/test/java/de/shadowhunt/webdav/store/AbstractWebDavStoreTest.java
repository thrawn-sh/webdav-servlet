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
package de.shadowhunt.webdav.store;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.method.GetMethod;
import de.shadowhunt.webdav.store.WebDavEntity.Type;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public abstract class AbstractWebDavStoreTest {

    @Test
    public void createCollectionTest() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createCollection(path);
        Assert.assertTrue("must exist", store.exists(path));

        final WebDavEntity entity = store.getEntity(path);
        Assert.assertNotNull("must not be null", entity);
        Assert.assertEquals("type must match", Type.COLLECTION, entity.getType());
    }

    @Ignore // FIXME FileSystemStore
    @Test(expected = WebDavException.class)
    public void createCollectionTest_collection() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createCollection(path);
        Assert.assertTrue("must exist", store.exists(path));

        store.createCollection(path);
        Assert.fail("must not complete");
    }

    @Test(expected = WebDavException.class)
    public void createCollectionTest_item() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createItem(path, new ByteArrayInputStream("data".getBytes()));
        Assert.assertTrue("must exist", store.exists(path));

        store.createCollection(path);
        Assert.fail("must not complete");
    }

    @Test(expected = WebDavException.class)
    public void createCollectionTest_missing_parent() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection/collection");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createCollection(path);
        Assert.fail("must not complete");
    }

    @Test(expected = WebDavException.class)
    public void createCollectionTest_root() throws Exception {
        final WebDavStore store = getStore();

        store.createCollection(WebDavPath.ROOT);
        Assert.fail("must not complete");
    }

    @Test
    public void createItemTest() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        final String data = "data";
        store.createItem(path, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(path));

        final WebDavEntity entity = store.getEntity(path);
        Assert.assertNotNull("must not be null", entity);
        Assert.assertEquals("type must match", Type.ITEM, entity.getType());

        final InputStream content = store.getContent(path);
        Assert.assertNotNull("must not be null", content);

        Assert.assertEquals("content must match", data, IOUtils.toString(content));
    }

    @Test(expected = WebDavException.class)
    public void createItemTest_collection() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createCollection(path);
        Assert.assertTrue("must exist", store.exists(path));

        store.createItem(path, new ByteArrayInputStream("data".getBytes()));
        Assert.fail("must not complete");
    }

    @Test
    public void createItemTest_item() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createItem(path, new ByteArrayInputStream("first".getBytes()));
        Assert.assertTrue("must exist", store.exists(path));

        final String second = "second";
        store.createItem(path, new ByteArrayInputStream(second.getBytes()));

        final InputStream content = store.getContent(path);
        Assert.assertNotNull("must not be null", content);
        Assert.assertEquals("content must match", second, IOUtils.toString(content));
    }

    @Test(expected = WebDavException.class)
    public void createItemTest_missing_parent() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection/item.txt");
        Assert.assertFalse("must not exist", store.exists(path));
        store.createItem(path, new ByteArrayInputStream("data".getBytes()));
        Assert.fail("must not complete");
    }

    @Test(expected = WebDavException.class)
    public void createItemTest_root() throws Exception {
        final WebDavStore store = getStore();

        store.createItem(WebDavPath.ROOT, new ByteArrayInputStream("data".getBytes()));
        Assert.fail("must not complete");
    }

    @Test
    public void createLockBuilderTest() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertNotNull("must not be null", store.createLockBuilder());
    }

    @Test(expected = WebDavException.class)
    public void deleteTest_collection_children() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createCollection(path);
        Assert.assertTrue("must exist", store.exists(path));

        final WebDavPath child = WebDavPath.create("collection/item.txt");
        Assert.assertFalse("must not exist", store.exists(child));

        store.createItem(child, new ByteArrayInputStream("data".getBytes()));
        Assert.assertTrue("must exist", store.exists(child));

        store.delete(path);
        Assert.fail("must not complete");
    }

    @Test
    public void deleteTest_collection_empty() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createCollection(path);
        Assert.assertTrue("must exist", store.exists(path));

        store.delete(path);
        Assert.assertFalse("must not exist", store.exists(path));
    }

    @Test
    public void deleteTest_item() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createItem(path, new ByteArrayInputStream("data".getBytes()));
        Assert.assertTrue("must exist", store.exists(path));

        store.delete(path);
        Assert.assertFalse("must not exist", store.exists(path));
    }

    @Test(expected = WebDavException.class)
    public void deleteTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        store.delete(path);
        Assert.fail("must not complete");
    }

    @Test(expected = WebDavException.class)
    public void deleteTest_root() throws Exception {
        final WebDavStore store = getStore();

        store.delete(WebDavPath.ROOT);
        Assert.fail("must not complete");
    }

    @Test
    public void existsTest_collection() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createCollection(path);
        Assert.assertTrue("must exist", store.exists(path));
    }

    @Test
    public void existsTest_item() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createItem(path, new ByteArrayInputStream("data".getBytes()));
        Assert.assertTrue("must exist", store.exists(path));
    }

    @Test
    public void existsTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));
    }

    @Test
    public void existsTest_root() throws Exception {
        final WebDavStore store = getStore();
        Assert.assertTrue("must exist", store.exists(WebDavPath.ROOT));
    }

    @Test(expected = WebDavException.class)
    public void getContentTest_collection() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createCollection(path);
        Assert.assertTrue("must exist", store.exists(path));

        store.getContent(path);
        Assert.fail("must not complete");
    }

    @Test
    public void getContentTest_item() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        final String data = "data";
        store.createItem(path, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(path));

        final InputStream content = store.getContent(path);
        Assert.assertNotNull("must not be null", content);
        Assert.assertEquals("content must match", data, IOUtils.toString(content));
    }

    @Test(expected = WebDavException.class)
    public void getContentTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        store.getContent(path);
        Assert.fail("must not complete");
    }

    @Test
    public void getEntityTest_collection() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("collection");
        Assert.assertFalse("must not exist", store.exists(path));

        store.createCollection(path);
        Assert.assertTrue("must exist", store.exists(path));

        final WebDavEntity entity = store.getEntity(path);
        Assert.assertNotNull("must not be null", entity);
        Assert.assertEquals("type must match", Type.COLLECTION, entity.getType());
    }

    @Test
    public void getEntityTest_item() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        final String data = "data";
        store.createItem(path, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(path));

        final WebDavEntity entity = store.getEntity(path);
        Assert.assertNotNull("must not be null", entity);
        Assert.assertEquals("type must match", Type.ITEM, entity.getType());
    }

    @Test(expected = WebDavException.class)
    public void getEntityTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        final WebDavPath path = WebDavPath.create("item.txt");
        Assert.assertFalse("must not exist", store.exists(path));

        store.getEntity(path);
        Assert.fail("must not complete");
    }

    protected abstract WebDavStore getStore();

    @Test
    public void getSupportedLocksTest() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertNotNull("locks not be null", store.getSupportedLocks(WebDavPath.ROOT));
    }

    @Test
    public void grantAccessTest() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertNotNull("access not be null", store.grantAccess(new GetMethod(), WebDavPath.ROOT, Optional.empty()));
    }
}
