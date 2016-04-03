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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.shadowhunt.webdav.WebDavException;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.method.GetMethod;
import de.shadowhunt.webdav.property.PropertyIdentifier;
import de.shadowhunt.webdav.property.StringWebDavProperty;
import de.shadowhunt.webdav.property.WebDavProperty;
import de.shadowhunt.webdav.store.WebDavEntity.Type;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractWebDavStoreTest {

    private static final WebDavPath COLLECTION = WebDavPath.create("collection");

    private static final WebDavPath COLLECTION_CHILD = WebDavPath.create("collection/collection");

    private static final WebDavPath ITEM = WebDavPath.create("item.txt");

    private static final WebDavPath ITEM_CHILD = WebDavPath.create("collection/item.txt");

    @Test
    public void createCollectionTest() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        final WebDavEntity entity = store.getEntity(COLLECTION);
        Assert.assertNotNull("entity must not be null", entity);
        Assert.assertEquals("type must match", Type.COLLECTION, entity.getType());
    }

    @Test(expected = WebDavException.class)
    public void createCollectionTest_collection() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.fail("must not complete");
    }

    @Test(expected = WebDavException.class)
    public void createCollectionTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.createItem(ITEM, new ByteArrayInputStream("data".getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        store.createCollection(ITEM);
        Assert.fail("must not complete");
    }

    @Test(expected = WebDavException.class)
    public void createCollectionTest_missing_parent() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION_CHILD));

        store.createCollection(COLLECTION_CHILD);
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

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final WebDavEntity entity = store.getEntity(ITEM);
        Assert.assertNotNull("entity must not be null", entity);
        Assert.assertEquals("type must match", Type.ITEM, entity.getType());

        final InputStream content = store.getContent(ITEM);
        Assert.assertNotNull("content must not be null", content);

        Assert.assertEquals("content must match", data, IOUtils.toString(content));
    }

    @Test(expected = WebDavException.class)
    public void createItemTest_collection() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        store.createItem(COLLECTION, new ByteArrayInputStream("data".getBytes()));
        Assert.fail("must not complete");
    }

    @Test
    public void createItemTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.createItem(ITEM, new ByteArrayInputStream("first".getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final String second = "second";
        store.createItem(ITEM, new ByteArrayInputStream(second.getBytes()));

        final InputStream content = store.getContent(ITEM);
        Assert.assertNotNull("content must not be null", content);
        Assert.assertEquals("content must match", second, IOUtils.toString(content));
    }

    @Test
    public void createItemTest_locked() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.createItem(ITEM, new ByteArrayInputStream("first".getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final WebDavLockBuilder builder = store.createLockBuilder();
        builder.setRoot(ITEM);
        final WebDavLock lock = builder.build();

        store.lock(ITEM, lock);

        final String second = "second";
        store.createItem(ITEM, new ByteArrayInputStream(second.getBytes()));

        final InputStream content = store.getContent(ITEM);
        Assert.assertNotNull("content must not be null", content);
        Assert.assertEquals("content must match", second, IOUtils.toString(content));

        final WebDavEntity entity = store.lock(ITEM, lock);
        Assert.assertEquals("lock must match", Optional.of(lock), entity.getLock());
    }

    @Test(expected = WebDavException.class)
    public void createItemTest_missing_parent() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM_CHILD));
        store.createItem(ITEM_CHILD, new ByteArrayInputStream("data".getBytes()));
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

        Assert.assertNotNull("builder must not be null", store.createLockBuilder());
    }

    @Test(expected = WebDavException.class)
    public void deleteTest_collection_children() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        final WebDavPath child = ITEM_CHILD;
        Assert.assertFalse("must not exist", store.exists(child));

        store.createItem(child, new ByteArrayInputStream("data".getBytes()));
        Assert.assertTrue("must exist", store.exists(child));

        store.delete(COLLECTION);
        Assert.fail("must not complete");
    }

    @Test
    public void deleteTest_collection_empty() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        store.delete(COLLECTION);
        Assert.assertFalse("must not exist", store.exists(COLLECTION));
    }

    @Test
    public void deleteTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.createItem(ITEM, new ByteArrayInputStream("data".getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        store.delete(ITEM);
        Assert.assertFalse("must not exist", store.exists(ITEM));
    }

    @Test(expected = WebDavException.class)
    public void deleteTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.delete(ITEM);
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

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));
    }

    @Test
    public void existsTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.createItem(ITEM, new ByteArrayInputStream("data".getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));
    }

    @Test
    public void existsTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));
    }

    @Test
    public void existsTest_root() throws Exception {
        final WebDavStore store = getStore();
        Assert.assertTrue("must exist", store.exists(WebDavPath.ROOT));
    }

    @Test(expected = WebDavException.class)
    public void getContentTest_collection() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        store.getContent(COLLECTION);
        Assert.fail("must not complete");
    }

    @Test
    public void getContentTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final InputStream content = store.getContent(ITEM);
        Assert.assertNotNull("content must not be null", content);
        Assert.assertEquals("content must match", data, IOUtils.toString(content));
    }

    @Test(expected = WebDavException.class)
    public void getContentTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.getContent(ITEM);
        Assert.fail("must not complete");
    }

    @Test
    public void getEntityTest_collection() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        final WebDavEntity entity = store.getEntity(COLLECTION);
        Assert.assertNotNull("enitiy must not be null", entity);
        Assert.assertEquals("type must match", Type.COLLECTION, entity.getType());
    }

    @Test
    public void getEntityTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final WebDavEntity entity = store.getEntity(ITEM);
        Assert.assertNotNull("entity must not be null", entity);
        Assert.assertEquals("type must match", Type.ITEM, entity.getType());
    }

    @Test(expected = WebDavException.class)
    public void getEntityTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.getEntity(ITEM);
        Assert.fail("must not complete");
    }

    @Test
    public void getPropertiesTest_collection() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        final Collection<WebDavProperty> properties = store.getProperties(COLLECTION);
        Assert.assertNotNull("properties must not be null", properties);
    }

    @Test
    public void getPropertiesTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final Collection<WebDavProperty> properties = store.getProperties(ITEM);
        Assert.assertNotNull("properties must not be null", properties);
    }

    @Test(expected = WebDavException.class)
    public void getPropertiesTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.getProperties(ITEM);
        Assert.fail("must not complete");
    }

    protected abstract WebDavStore getStore();

    @Test
    public void getSupportedLocksTest() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertNotNull("locks must not be null", store.getSupportedLocks(WebDavPath.ROOT));
    }

    @Test
    public void grantAccessTest() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertNotNull("access must not be null", store.grantAccess(new GetMethod(), WebDavPath.ROOT, Optional.empty()));
    }

    @Test
    public void listTest_collection() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        final List<WebDavPath> list = store.list(COLLECTION);
        Assert.assertNotNull("list must not be null", list);
        Assert.assertTrue("list must be empty", list.isEmpty());
    }

    @Test
    public void listTest_collection_children() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));
        store.createCollection(ITEM_CHILD);
        Assert.assertTrue("must exist", store.exists(ITEM_CHILD));
        store.createCollection(COLLECTION_CHILD);
        Assert.assertTrue("must exist", store.exists(COLLECTION_CHILD));

        final List<WebDavPath> list = store.list(COLLECTION);
        Assert.assertNotNull("list must not be null", list);
        Assert.assertEquals("list size must match", 2, list.size());
        Assert.assertTrue("list must contain item", list.contains(ITEM_CHILD));
        Assert.assertTrue("list must contain collection", list.contains(COLLECTION_CHILD));
    }

    @Test
    public void listTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final List<WebDavPath> list = store.list(ITEM);
        Assert.assertNotNull("list must not be null", list);
        Assert.assertTrue("list must be empty", list.isEmpty());
    }

    @Test(expected = WebDavException.class)
    public void listTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.list(ITEM);
        Assert.fail("must not complete");
    }

    @Test
    public void lockTest_collection() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        final WebDavLockBuilder builder = store.createLockBuilder();
        builder.setRoot(COLLECTION);
        final WebDavLock lock = builder.build();

        final WebDavEntity entity = store.lock(COLLECTION, lock);
        Assert.assertEquals("lock must match", Optional.of(lock), entity.getLock());
    }

    @Test
    public void lockTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final WebDavLockBuilder builder = store.createLockBuilder();
        builder.setRoot(ITEM);
        final WebDavLock lock = builder.build();

        final WebDavEntity entity = store.lock(ITEM, lock);
        Assert.assertEquals("lock must match", Optional.of(lock), entity.getLock());
    }

    @Test(expected = WebDavException.class)
    public void lockTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final WebDavLockBuilder builder = store.createLockBuilder();
        builder.setRoot(ITEM);
        final WebDavLock lock = builder.build();

        store.lock(ITEM, lock);
        Assert.fail("must not complete");
    }

    @Test
    public void setPropertiesTest_collection() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        final StringWebDavProperty foo = new StringWebDavProperty(new PropertyIdentifier("foo", "bar"), "foo:bar");
        final StringWebDavProperty bar = new StringWebDavProperty(new PropertyIdentifier("bar", "foo"), "bar:foo");
        final List<WebDavProperty> properties = Arrays.asList(foo, bar);

        store.setProperties(COLLECTION, properties);
        final Collection<WebDavProperty> actual = store.getProperties(COLLECTION);
        Assert.assertEquals("properties size must match", 2, actual.size());
        Assert.assertTrue("properties must contain item", actual.contains(foo));
        Assert.assertTrue("properties must contain collection", actual.contains(bar));
    }

    @Test
    public void setPropertiesTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final StringWebDavProperty foo = new StringWebDavProperty(new PropertyIdentifier("foo", "bar"), "foo:bar");
        final StringWebDavProperty bar = new StringWebDavProperty(new PropertyIdentifier("bar", "foo"), "bar:foo");
        final List<WebDavProperty> properties = Arrays.asList(foo, bar);

        store.setProperties(ITEM, properties);
        final Collection<WebDavProperty> actual = store.getProperties(ITEM);
        Assert.assertEquals("properties size must match", 2, actual.size());
        Assert.assertTrue("properties must contain item", actual.contains(foo));
        Assert.assertTrue("properties must contain collection", actual.contains(bar));
    }

    @Test(expected = WebDavException.class)
    public void setPropertiesTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.setProperties(ITEM, Collections.emptyList());
        Assert.fail("must not complete");
    }

    @Test
    public void setPropertiesTest_override() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final StringWebDavProperty foo = new StringWebDavProperty(new PropertyIdentifier("foo", "bar"), "foo:bar");
        final StringWebDavProperty bar = new StringWebDavProperty(new PropertyIdentifier("bar", "foo"), "bar:foo");
        final List<WebDavProperty> properties = Arrays.asList(foo, bar);

        store.setProperties(ITEM, properties);

        final StringWebDavProperty abc = new StringWebDavProperty(new PropertyIdentifier("abc", "def"), "abc:def");
        final List<WebDavProperty> propertiesOverride = Arrays.asList(abc);

        store.setProperties(ITEM, propertiesOverride);
        final Collection<WebDavProperty> actual = store.getProperties(ITEM);
        Assert.assertEquals("properties size must match", 1, actual.size());
        Assert.assertTrue("properties must contain item", actual.contains(abc));
    }

    @Test
    public void unlockTest_collection() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        final WebDavLockBuilder builder = store.createLockBuilder();
        builder.setRoot(COLLECTION);
        final WebDavLock lock = builder.build();

        store.lock(COLLECTION, lock);
        store.unlock(COLLECTION);

        final WebDavEntity entity = store.getEntity(COLLECTION);
        Assert.assertEquals("lock must match", Optional.empty(), entity.getLock());
    }

    @Test(expected = WebDavException.class)
    public void unlockTest_collection_not_locked() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(COLLECTION));

        store.createCollection(COLLECTION);
        Assert.assertTrue("must exist", store.exists(COLLECTION));

        store.unlock(COLLECTION);
        Assert.fail("must not complete");
    }

    @Test
    public void unlockTest_item() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        final WebDavLockBuilder builder = store.createLockBuilder();
        builder.setRoot(ITEM);
        final WebDavLock lock = builder.build();

        store.lock(ITEM, lock);
        store.unlock(ITEM);

        final WebDavEntity entity = store.getEntity(ITEM);
        Assert.assertEquals("lock must match", Optional.empty(), entity.getLock());
    }

    @Test(expected = WebDavException.class)
    public void unlockTest_item_not_locked() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        final String data = "data";
        store.createItem(ITEM, new ByteArrayInputStream(data.getBytes()));
        Assert.assertTrue("must exist", store.exists(ITEM));

        store.unlock(ITEM);
        Assert.fail("must not complete");
    }

    @Test(expected = WebDavException.class)
    public void unlockTest_non_existing() throws Exception {
        final WebDavStore store = getStore();

        Assert.assertFalse("must not exist", store.exists(ITEM));

        store.unlock(ITEM);
        Assert.fail("must not complete");
    }
}
