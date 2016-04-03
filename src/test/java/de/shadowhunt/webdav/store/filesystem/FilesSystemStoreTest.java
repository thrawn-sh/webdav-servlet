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
package de.shadowhunt.webdav.store.filesystem;

import java.io.File;
import java.util.UUID;

import de.shadowhunt.webdav.store.AbstractWebDavStoreTest;
import de.shadowhunt.webdav.store.WebDavStore;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

public class FilesSystemStoreTest extends AbstractWebDavStoreTest {

    private File root;

    @After
    public void after() {
        FileUtils.deleteQuietly(root);
    }

    @Before
    public void before() {
        root = new File(FileUtils.getTempDirectory(), "webdav-servlet-test_" + UUID.randomUUID());
    }

    @Override
    protected WebDavStore getStore() {
        return new FileSystemStore(root, true);
    }

}
