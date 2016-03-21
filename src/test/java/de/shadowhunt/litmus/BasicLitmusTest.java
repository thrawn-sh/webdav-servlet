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

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are *NOT* independent
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicLitmusTest extends AbstractLitmusTest {

    private static final File ROOT = new File("src/test/resources/litmus/0-basic/");

    @Test
    public void test_02_options() throws Exception {
        final TestResponse response = execute(new File(ROOT, "02-01.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    public void test_03_put_get() throws Exception {
        final TestResponse put_response = execute(new File(ROOT, "03-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, put_response.getStatus());

        final TestResponse get_response = execute(new File(ROOT, "03-02.xml"));
        Assert.assertEquals("status must match", Status.SC_OK, get_response.getStatus());
        Assert.assertEquals("content must match", "This is\na test file.\nfor litmus\ntesting.\n", get_response.getContent());
    }

    @Test
    public void test_04_put_get_utf8_segment() throws Exception {
        final TestResponse put_response = execute(new File(ROOT, "04-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, put_response.getStatus());

        final TestResponse get_response = execute(new File(ROOT, "04-02.xml"));
        Assert.assertEquals("status must match", Status.SC_OK, get_response.getStatus());
        Assert.assertEquals("content must match", "This is\na test file.\nfor litmus\ntesting.\n", get_response.getContent());
    }

    @Test
    public void test_05_put_no_parent() throws Exception {
        final TestResponse response = execute(new File(ROOT, "05-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CONFLICT, response.getStatus());
    }

    @Test
    public void test_06_mkcol_over_plain() throws Exception {
        final TestResponse response = execute(new File(ROOT, "06-01.xml"));
        Assert.assertEquals("status must match", Status.SC_METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    public void test_07_delete() throws Exception {
        final TestResponse response = execute(new File(ROOT, "07-01.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    public void test_08_delete_null() throws Exception {
        final TestResponse response = execute(new File(ROOT, "08-01.xml"));
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, response.getStatus());
    }

    @Test
    public void test_10_mkcol() throws Exception {
        final TestResponse response = execute(new File(ROOT, "10-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_11_mkcol_again() throws Exception {
        final TestResponse response = execute(new File(ROOT, "11-01.xml"));
        Assert.assertEquals("status must match", Status.SC_METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    public void test_12_delete_coll() throws Exception {
        final TestResponse response = execute(new File(ROOT, "12-01.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    public void test_13_mkcol_no_parent() throws Exception {
        final TestResponse response = execute(new File(ROOT, "13-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CONFLICT, response.getStatus());
    }

    @Test
    public void test_14_mkcol_with_body() throws Exception {
        final TestResponse response = execute(new File(ROOT, "14-01.xml"));
        Assert.assertEquals("status must match", Status.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
    }

}
