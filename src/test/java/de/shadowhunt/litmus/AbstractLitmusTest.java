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
package de.shadowhunt.litmus;

import java.io.File;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavConfig;
import de.shadowhunt.webdav.WebDavDispatcher;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavRequest;
import de.shadowhunt.webdav.WebDavStore;
import de.shadowhunt.webdav.impl.store.FileSystemStore;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class AbstractLitmusTest {

    private static final WebDavDispatcher DISPATCHER = WebDavDispatcher.getInstance();

    private static File root;

    private static WebDavStore store;

    @AfterClass
    public static void destroyStore() {
        FileUtils.deleteQuietly(root);
    }

    @BeforeClass
    public static void initStore() {
        root = new File(new File(FileUtils.getTempDirectory(), "webdav-servlet-test"), UUID.randomUUID().toString());
        store = new FileSystemStore(root);

        final WebDavPath litmusRoot = WebDavPath.create("/litmus");
        store.createCollection(litmusRoot);
    }

    @Mock
    protected WebDavConfig config;

    private WebDavRequest create(final File request) throws Exception {
        final JAXBContext context = JAXBContext.newInstance(XmlRequest.class);

        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final XmlRequest webdavRequest = (XmlRequest) unmarshaller.unmarshal(request);
        webdavRequest.setConfig(config);

        return webdavRequest;
    }

    protected TestResponse execute(final File request) throws Exception {
        final WebDavRequest webDavRequest = create(request);
        final TestResponse response = new TestResponse(webDavRequest);

        DISPATCHER.service(store, webDavRequest, response);

        return response;
    }

    @Before
    public void initMock() {
        MockitoAnnotations.initMocks(this);
    }
}
