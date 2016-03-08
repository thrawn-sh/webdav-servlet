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
package de.shadowhunt.webdav;

import org.junit.Assert;
import org.junit.Test;

public class WebDavPathTest {

    @Test
    public void compareWebDavPaths() {
        final WebDavPath path = WebDavPath.create("/a");
        final WebDavPath same = WebDavPath.create("/a");
        Assert.assertEquals("WebDavPath compareTo same: 0", 0, path.compareTo(same));
        Assert.assertEquals("same compareTo WebDavPath: 0", 0, same.compareTo(path));

        final WebDavPath other = WebDavPath.create("/b");
        Assert.assertTrue("WebDavPath is smaller than other", (path.compareTo(other) < 0));
        Assert.assertTrue("other is bigger than WebDavPath", (other.compareTo(path) > 0));
    }

    @Test
    public void createWebDavPath() {
        final WebDavPath expected = WebDavPath.create("/a/b/c/d.txt");
        Assert.assertEquals(expected, WebDavPath.create("/a/b/c/d.txt"));
        Assert.assertEquals(expected, WebDavPath.create("a/b/c/d.txt"));
        Assert.assertEquals(expected, WebDavPath.create("//a/b/c/d.txt"));
        Assert.assertEquals(expected, WebDavPath.create("a//b/c//d.txt"));
        Assert.assertEquals(expected, WebDavPath.create("/a/b/c/d.txt/"));
        Assert.assertEquals(expected, WebDavPath.create("/a/b/./c/d.txt"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWebDavPath_withParentDirectory() throws Exception {
        WebDavPath.create("/a/b/../c/d.txt");
        Assert.fail("path with parent directory must not complete");
    }

    @Test
    public void createRootWebDavPath() {
        Assert.assertEquals("/ is ROOT", WebDavPath.ROOT, WebDavPath.create(WebDavPath.SEPARATOR));
        Assert.assertEquals("empty is ROOT", WebDavPath.ROOT, WebDavPath.create(""));
        Assert.assertEquals("null is ROOT", WebDavPath.ROOT, WebDavPath.create(null));
    }

    @Test
    public void equalsWebDavPath() {
        final WebDavPath path = WebDavPath.create("/a");
        Assert.assertEquals("WebDavPath equals WebDavPath", path, path);

        final WebDavPath same = WebDavPath.create("/a");

        Assert.assertNotSame("WebDavPath and same are different object", path, same);
        Assert.assertEquals("WebDavPath equals same", path, same);
        Assert.assertEquals("same equals WebDavPath", same, path);

        final WebDavPath other = WebDavPath.create("/b");
        Assert.assertNotEquals("WebDavPath doesn't equal other", path, other);
        Assert.assertNotEquals("same doesn't equal other", same, other);
    }

    @Test
    public void getParent() {
        final WebDavPath child = WebDavPath.create("/a/b/c/d.txt");
        Assert.assertEquals(WebDavPath.create("/a/b/c"), child.getParent());
        Assert.assertEquals(WebDavPath.ROOT, WebDavPath.ROOT.getParent());
    }

    @Test
    public void getValue() {
        final String expected = "/a/b/c/d.txt";
        final WebDavPath path = WebDavPath.create(expected);
        Assert.assertEquals("WebDavPath value must match", expected, path.getValue());
    }

    @Test
    public void hashCodeWebDavPath() {
        final WebDavPath path = WebDavPath.create("/a");
        Assert.assertEquals("WebDavPath has same hashCode as WebDavPath", path.hashCode(), path.hashCode());

        final WebDavPath same = WebDavPath.create("/a");

        Assert.assertEquals("WebDavPath and same have same hashCode", path.hashCode(), same.hashCode());

        final WebDavPath other = WebDavPath.create("/b");
        Assert.assertNotEquals("WebDavPath and other don't have same hashCode", path.hashCode(), other.hashCode());
    }
}
