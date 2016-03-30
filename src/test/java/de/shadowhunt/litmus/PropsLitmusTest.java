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

import de.shadowhunt.CombinedNormalizer;
import de.shadowhunt.ContentNormalizer;
import de.shadowhunt.EtagNormalizer;
import de.shadowhunt.LastModifiedNormalizer;
import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are *NOT* independent
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PropsLitmusTest extends AbstractLitmusTest {

    private static final ContentNormalizer NORMALIZER = new CombinedNormalizer(new LastModifiedNormalizer(), new EtagNormalizer());

    private static final File ROOT = new File("src/test/resources/litmus/2-props/");

    protected static String concat(final String... input) {
        final StringBuilder sb = new StringBuilder();
        for (final String i : input) {
            sb.append(i);
        }
        return sb.toString();
    }

    @Test
    public void test_02_propfind_invalid() throws Exception {
        final TestResponse response = execute(new File(ROOT, "02-01.xml"));
        Assert.assertEquals("status must match", Status.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void test_03_propfind_invalid2() throws Exception {
        final TestResponse response = execute(new File(ROOT, "03-01.xml"));
        Assert.assertEquals("status must match", Status.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void test_04_propfind_d0() throws Exception {
        final TestResponse response = execute(new File(ROOT, "04-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname>litmus</D:displayname>", //
                "<D:getcontentlength>0</D:getcontentlength>", //
                "<D:getlastmodified>1970-01-01 01:00:00</D:getlastmodified>", //
                "<D:resourcetype>", //
                "<D:collection/>", //
                "</D:resourcetype>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:bar/>", //
                "<ns1:foo/>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 404 Not Found</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_05_propinit() throws Exception {
        final TestResponse delete_response = execute(new File(ROOT, "05-01.xml"));
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, delete_response.getStatus());

        final TestResponse put_response = execute(new File(ROOT, "05-02.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, put_response.getStatus());
    }

    @Test
    public void test_06_propset() throws Exception {
        final TestResponse response = execute(new File(ROOT, "06-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_07_propget() throws Exception {
        final TestResponse response = execute(new File(ROOT, "07-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:prop0>value0</ns1:prop0>", //
                "<ns1:prop1>value1</ns1:prop1>", //
                "<ns1:prop2>value2</ns1:prop2>", //
                "<ns1:prop3>value3</ns1:prop3>", //
                "<ns1:prop4>value4</ns1:prop4>", //
                "<ns1:prop5>value5</ns1:prop5>", //
                "<ns1:prop6>value6</ns1:prop6>", //
                "<ns1:prop7>value7</ns1:prop7>", //
                "<ns1:prop8>value8</ns1:prop8>", //
                "<ns1:prop9>value9</ns1:prop9>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_08_propextended() throws Exception {
        final TestResponse response = execute(new File(ROOT, "08-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<D:displayname>prop</D:displayname>", //
                "<D:getcontentlength>32</D:getcontentlength>", //
                "<D:getetag>0000000000</D:getetag>", //
                "<D:getlastmodified>1970-01-01 01:00:00</D:getlastmodified>", //
                "<D:supportedlock><D:lockentry><D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry></D:supportedlock>", //
                "<ns1:prop0>value0</ns1:prop0>", //
                "<ns1:prop1>value1</ns1:prop1>", //
                "<ns1:prop2>value2</ns1:prop2>", //
                "<ns1:prop3>value3</ns1:prop3>", //
                "<ns1:prop4>value4</ns1:prop4>", //
                "<ns1:prop5>value5</ns1:prop5>", //
                "<ns1:prop6>value6</ns1:prop6>", //
                "<ns1:prop7>value7</ns1:prop7>", //
                "<ns1:prop8>value8</ns1:prop8>", //
                "<ns1:prop9>value9</ns1:prop9>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_09_propmove() throws Exception {
        final TestResponse delete_response = execute(new File(ROOT, "09-01.xml"));
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, delete_response.getStatus());

        final TestResponse move_response = execute(new File(ROOT, "09-02.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, move_response.getStatus());
    }

    @Test
    public void test_10_propget() throws Exception {
        final TestResponse response = execute(new File(ROOT, "10-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
    }

    @Test
    public void test_11_propdeletes() throws Exception {
        final TestResponse response = execute(new File(ROOT, "11-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_12_propget() throws Exception {
        final TestResponse response = execute(new File(ROOT, "12-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop2</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:prop5>value5</ns1:prop5>", //
                "<ns1:prop6>value6</ns1:prop6>", //
                "<ns1:prop7>value7</ns1:prop7>", //
                "<ns1:prop8>value8</ns1:prop8>", //
                "<ns1:prop9>value9</ns1:prop9>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:prop0/>", //
                "<ns1:prop1/>", //
                "<ns1:prop2/>", //
                "<ns1:prop3/>", //
                "<ns1:prop4/>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 404 Not Found</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_13_propreplace() throws Exception {
        final TestResponse response = execute(new File(ROOT, "13-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_14_propget() throws Exception {
        final TestResponse response = execute(new File(ROOT, "14-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop2</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:prop5>newvalue5</ns1:prop5>", //
                "<ns1:prop6>newvalue6</ns1:prop6>", //
                "<ns1:prop7>newvalue7</ns1:prop7>", //
                "<ns1:prop8>newvalue8</ns1:prop8>", //
                "<ns1:prop9>newvalue9</ns1:prop9>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:prop0/>", //
                "<ns1:prop1/>", //
                "<ns1:prop2/>", //
                "<ns1:prop3/>", //
                "<ns1:prop4/>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 404 Not Found</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_15_propnullns() throws Exception {
        final TestResponse response = execute(new File(ROOT, "15-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_16_propget() throws Exception {
        final TestResponse response = execute(new File(ROOT, "16-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop2</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<nonamespace>randomvalue</nonamespace>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_17_prophighunicode() throws Exception {
        final TestResponse response = execute(new File(ROOT, "17-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_18_propget() throws Exception {
        final TestResponse response = execute(new File(ROOT, "18-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop2</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:high-unicode>\uD800\uDC00</ns1:high-unicode>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_19_propremoveset() throws Exception {
        final TestResponse response = execute(new File(ROOT, "19-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_20_propget() throws Exception {
        final TestResponse response = execute(new File(ROOT, "20-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop2</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:removeset>y</ns1:removeset>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_21_propsetremove() throws Exception {
        final TestResponse response = execute(new File(ROOT, "21-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_22_propget() throws Exception {
        final TestResponse response = execute(new File(ROOT, "22-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop2</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:removeset/>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 404 Not Found</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_23_propvalnspace() throws Exception {
        final TestResponse response = execute(new File(ROOT, "23-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_24_propwformed() throws Exception {
        final TestResponse response = execute(new File(ROOT, "24-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\" xmlns:ns1=\"http://example.com/neon/litmus/\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop2</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:valnspace>", //
                "</ns1:valnspace>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_25_propinit() throws Exception {
        final TestResponse delete_response = execute(new File(ROOT, "25-01.xml"));
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, delete_response.getStatus());

        final TestResponse put_response = execute(new File(ROOT, "25-02.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, put_response.getStatus());
    }

    @Test
    public void test_26_propmanyns() throws Exception {
        final TestResponse response = execute(new File(ROOT, "26-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_27_propget() throws Exception {
        final TestResponse response = execute(new File(ROOT, "27-01.xml"));
        Assert.assertEquals("status must match", Status.SC_MULTISTATUS, response.getStatus());
        Assert.assertEquals("contentType must match", "application/xml", response.getContentType());
        final String expected = concat("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
                "<D:multistatus xmlns:D=\"DAV:\"", //
                " xmlns:ns1=\"http://example.com/alpha\"", //
                " xmlns:ns2=\"http://example.com/beta\"", //
                " xmlns:ns3=\"http://example.com/delta\"", //
                " xmlns:ns4=\"http://example.com/epsilon\"", //
                " xmlns:ns5=\"http://example.com/eta\"", //
                " xmlns:ns6=\"http://example.com/gamma\"", //
                " xmlns:ns7=\"http://example.com/iota\"", //
                " xmlns:ns8=\"http://example.com/kappa\"", //
                " xmlns:ns9=\"http://example.com/theta\"", //
                " xmlns:ns10=\"http://example.com/zeta\">", //
                "<D:response>", //
                "<D:href>/webdav/litmus/prop</D:href>", //
                "<D:propstat>", //
                "<D:prop>", //
                "<ns1:somename>manynsvalue</ns1:somename>", //
                "<ns2:somename>manynsvalue</ns2:somename>", //
                "<ns3:somename>manynsvalue</ns3:somename>", //
                "<ns4:somename>manynsvalue</ns4:somename>", //
                "<ns5:somename>manynsvalue</ns5:somename>", //
                "<ns6:somename>manynsvalue</ns6:somename>", //
                "<ns7:somename>manynsvalue</ns7:somename>", //
                "<ns8:somename>manynsvalue</ns8:somename>", //
                "<ns9:somename>manynsvalue</ns9:somename>", //
                "<ns10:somename>manynsvalue</ns10:somename>", //
                "</D:prop>", //
                "<D:status>HTTP/1.1 200 OK</D:status>", //
                "</D:propstat>", //
                "</D:response>", //
                "</D:multistatus>", //
                "\r\n");
        Assert.assertEquals("content must match", expected, response.getContent(NORMALIZER));
    }

    @Test
    public void test_28_propcleanup() throws Exception {
        final TestResponse response = execute(new File(ROOT, "28-01.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, response.getStatus());
    }

}
