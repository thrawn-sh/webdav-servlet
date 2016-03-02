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
package de.shadowhunt.webdav.impl.method;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import de.shadowhunt.webdav.PropertyIdentifier;
import de.shadowhunt.webdav.WebDavEntity;
import de.shadowhunt.webdav.WebDavMethod;
import de.shadowhunt.webdav.WebDavPath;
import de.shadowhunt.webdav.WebDavProperty;
import de.shadowhunt.webdav.WebDavResponse.Status;
import de.shadowhunt.webdav.impl.StringWebDavProperty;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

//Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PropFindMethodTest extends AbstractWebDavMethodTest {

    @Test
    public void test00_missing() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(false);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_NOT_FOUND);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test01_infinity_not_supported() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn(PropFindMethod.INFINITY);
        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_FORBIDDEN);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test02_missing_request_body() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_BAD_REQUEST);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test03_no_properties() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(config.isAllowInfiniteDepthRequests()).thenReturn(true);

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getLock()).thenReturn(Optional.empty());
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"/>".getBytes()));
        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_BAD_REQUEST);
        Assert.assertNull("content must be null", response.getContent());
    }

    @Test
    public void test04_missing_selected_properties() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(config.isAllowInfiniteDepthRequests()).thenReturn(true);

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getLock()).thenReturn(Optional.empty());
        Mockito.when(entity.getName()).thenReturn("item.txt");
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><prop xmlns:t=\"test\"><t:foo/></prop></propfind>".getBytes()));
        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_MULTISTATUS);

        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:ns1=\"test\" xmlns:D=\"DAV:\">", //
                "<D:response>", //
                "<D:href>/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:foo/>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 404 Not Found</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", response.getContent(), expected);
    }

    @Test
    public void test04_selected_properties() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getLock()).thenReturn(Optional.empty());
        Mockito.when(entity.getName()).thenReturn("item.txt");
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><prop xmlns:t=\"test\"><t:foo/></prop></propfind>".getBytes()));
        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);
        Mockito.when(store.getProperties(path)).thenAnswer(new Answer<Collection<WebDavProperty>>() {

            @Override
            public Collection<WebDavProperty> answer(final InvocationOnMock invocation) throws Throwable {
                final List<WebDavProperty> properties = new ArrayList<>();
                properties.add(new StringWebDavProperty(new PropertyIdentifier("test", "bar"), "bar_content"));
                properties.add(new StringWebDavProperty(new PropertyIdentifier("test", "foo"), "foo_content"));
                properties.add(new StringWebDavProperty(new PropertyIdentifier("other", "foo"), "other_foo_content"));
                return properties;
            }
        });

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_MULTISTATUS);

        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:ns2=\"other\" xmlns:ns1=\"test\" xmlns:D=\"DAV:\">", //
                "<D:response>", //
                "<D:href>/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:foo>foo_content</ns1:foo>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", response.getContent(), expected);
    }

    @Test
    public void test05_all_properties() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getLock()).thenReturn(Optional.empty());
        Mockito.when(entity.getName()).thenReturn("item.txt");
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><allprop/></propfind>".getBytes()));
        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);
        Mockito.when(store.getProperties(path)).thenAnswer(new Answer<Collection<WebDavProperty>>() {

            @Override
            public Collection<WebDavProperty> answer(final InvocationOnMock invocation) throws Throwable {
                final List<WebDavProperty> properties = new ArrayList<>();
                properties.add(new StringWebDavProperty(new PropertyIdentifier("test", "bar"), "bar_content"));
                properties.add(new StringWebDavProperty(new PropertyIdentifier("test", "foo"), "foo_content"));
                properties.add(new StringWebDavProperty(new PropertyIdentifier("other", "foo"), "other_foo_content"));
                return properties;
            }
        });

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_MULTISTATUS);

        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:ns2=\"other\" xmlns:ns1=\"test\" xmlns:D=\"DAV:\">", //
                "<D:response>", //
                "<D:href>/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname>item.txt</D:displayname>", //
                "<D:getcontentlength>0</D:getcontentlength>", //
                "<D:getlastmodified>Thu Jan 01 01:00:00 CET 1970</D:getlastmodified>", //
                "<ns1:bar>bar_content</ns1:bar>", //
                "<ns1:foo>foo_content</ns1:foo>", //
                "<ns2:foo>other_foo_content</ns2:foo>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", response.getContent(), expected);
    }

    @Test
    public void test06_property_names() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        final WebDavPath path = WebDavPath.create("/item.txt");

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getLock()).thenReturn(Optional.empty());
        Mockito.when(entity.getName()).thenReturn("item.txt");
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><propname/></propfind>".getBytes()));
        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("0");
        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(path)).thenReturn(entity);
        Mockito.when(store.getProperties(path)).thenAnswer(new Answer<Collection<WebDavProperty>>() {

            @Override
            public Collection<WebDavProperty> answer(final InvocationOnMock invocation) throws Throwable {
                final List<WebDavProperty> properties = new ArrayList<>();
                properties.add(new StringWebDavProperty(new PropertyIdentifier("test", "bar"), "bar_content"));
                properties.add(new StringWebDavProperty(new PropertyIdentifier("test", "foo"), "foo_content"));
                properties.add(new StringWebDavProperty(new PropertyIdentifier("other", "foo"), "other_foo_content"));
                return properties;
            }
        });

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_MULTISTATUS);

        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:ns1=\"other\" xmlns:ns2=\"test\" xmlns:D=\"DAV:\">", //
                "<D:response>", //
                "<D:href>/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname/>", //
                "<D:getcontentlength/>", //
                "<D:getetag/>", //
                "<D:getlastmodified/>", //
                "<D:resourcetype/>", //
                "<D:supportedlock/>", //
                "<ns1:foo/>", //
                "<ns2:bar/>", //
                "<ns2:foo/>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", response.getContent(), expected);
    }

    @Test
    public void test07_all_collection() throws Exception {
        final WebDavMethod method = new PropFindMethod();

        final WebDavPath path = WebDavPath.create("/collection");
        final WebDavPath child = WebDavPath.create("/collection/item.txt");

        final WebDavEntity childEntity = Mockito.mock(WebDavEntity.class);

        Mockito.when(childEntity.getHash()).thenReturn(Optional.empty());
        Mockito.when(childEntity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(childEntity.getLock()).thenReturn(Optional.empty());
        Mockito.when(childEntity.getName()).thenReturn("item.txt");
        Mockito.when(childEntity.getType()).thenReturn(WebDavEntity.Type.ITEM);

        Mockito.when(entity.getHash()).thenReturn(Optional.empty());
        Mockito.when(entity.getLastModified()).thenReturn(new Date(0L));
        Mockito.when(entity.getLock()).thenReturn(Optional.empty());
        Mockito.when(entity.getName()).thenReturn("collection");
        Mockito.when(entity.getType()).thenReturn(WebDavEntity.Type.COLLECTION);

        Mockito.when(request.getInputStream()).thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><allprop/></propfind>".getBytes()));
        Mockito.when(request.getOption(Matchers.eq("Depth"), Matchers.anyString())).thenReturn("1");
        Mockito.when(request.getPath()).thenReturn(path);

        Mockito.when(store.exists(child)).thenReturn(true);
        Mockito.when(store.exists(path)).thenReturn(true);
        Mockito.when(store.getEntity(child)).thenReturn(childEntity);
        Mockito.when(store.getEntity(path)).thenReturn(entity);
        Mockito.when(store.getProperties(Mockito.any())).thenAnswer(new Answer<Collection<WebDavProperty>>() {

            @Override
            public Collection<WebDavProperty> answer(final InvocationOnMock invocation) throws Throwable {
                final List<WebDavProperty> properties = new ArrayList<>();
                properties.add(new StringWebDavProperty(new PropertyIdentifier("test", "bar"), "bar_content"));
                properties.add(new StringWebDavProperty(new PropertyIdentifier("test", "foo"), "foo_content"));
                properties.add(new StringWebDavProperty(new PropertyIdentifier("other", "foo"), "other_foo_content"));
                return properties;
            }
        });
        Mockito.when(store.list(path)).thenReturn(Arrays.asList(child));

        final Response response = execute(method);
        Assert.assertEquals("status must match", response.getStatus(), Status.SC_MULTISTATUS);

        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:ns2=\"other\" xmlns:ns1=\"test\" xmlns:D=\"DAV:\">", //
                "<D:response>", //
                "<D:href>/collection</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname>collection</D:displayname>", //
                "<D:getcontentlength>0</D:getcontentlength>", //
                "<D:getlastmodified>Thu Jan 01 01:00:00 CET 1970</D:getlastmodified>", //
                "<D:resourcetype>", //
                "<D:collection/>", //
                "</D:resourcetype>", //
                "<ns1:bar>bar_content</ns1:bar>", //
                "<ns1:foo>foo_content</ns1:foo>", //
                "<ns2:foo>other_foo_content</ns2:foo>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "<D:response>", //
                "<D:href>/collection/item.txt</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname>item.txt</D:displayname>", //
                "<D:getcontentlength>0</D:getcontentlength>", //
                "<D:getlastmodified>Thu Jan 01 01:00:00 CET 1970</D:getlastmodified>", //
                "<ns1:bar>bar_content</ns1:bar>", //
                "<ns1:foo>foo_content</ns1:foo>", //
                "<ns2:foo>other_foo_content</ns2:foo>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", response.getContent(), expected);
    }
}
