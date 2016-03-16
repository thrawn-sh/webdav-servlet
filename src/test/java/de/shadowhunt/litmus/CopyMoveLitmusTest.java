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
import java.util.Arrays;

import de.shadowhunt.TestResponse;
import de.shadowhunt.webdav.WebDavResponse.Status;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are *NOT* independent
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore // FIXME
public class CopyMoveLitmusTest extends AbstractLitmusTest {

    private static final File ROOT = new File("src/test/resources/litmus/1-copy_move/");

    @Test
    public void test_02_copy_init() throws Exception {
        for (final String test : Arrays.asList("02-01.xml", "02-02.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_CREATED, response.getStatus());
        }
    }

    @Test
    public void test_03_copy_simple() throws Exception {
        final TestResponse response = execute(new File(ROOT, "03-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, response.getStatus());
    }

    @Test
    public void test_04_copy_overwrite() throws Exception {
        final TestResponse copy_response = execute(new File(ROOT, "04-01.xml"));
        Assert.assertEquals("status must match", Status.SC_PRECONDITION_FAILED, copy_response.getStatus());

        for (final String test : Arrays.asList("04-02.xml", "04-03.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_NO_CONTENT, response.getStatus());
        }
    }

    @Test
    public void test_05_copy_nodestcoll() throws Exception {
        final TestResponse response = execute(new File(ROOT, "05-01.xml"));
        Assert.assertEquals("status must match", Status.SC_CONFLICT, response.getStatus());
    }

    @Test
    public void test_06_copy_cleanup() throws Exception {
        // delete existing files / collections
        for (final String test : Arrays.asList("06-01.xml", "06-02.xml", "06-03.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_NO_CONTENT, response.getStatus());
        }

        // delete non existing file / collection
        final TestResponse response = execute(new File(ROOT, "06-04.xml"));
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, response.getStatus());
    }

    @Test
    public void test_07_copy_coll() throws Exception {
        // create structure
        for (final String test : Arrays.asList("07-01.xml", "07-02.xml", "07-03.xml", "07-04.xml", "07-05.xml", "07-06.xml", "07-07.xml", "07-08.xml", "07-09.xml", "07-10.xml", "07-11.xml", "07-12.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_CREATED, response.getStatus());
        }

        // delete destinations
        for (final String test : Arrays.asList("07-13.xml", "07-14.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_NOT_FOUND, response.getStatus());
        }

        // copy
        for (final String test : Arrays.asList("07-15.xml", "07-16.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_CREATED, response.getStatus());
        }

        final TestResponse copy_on_exisiting_response = execute(new File(ROOT, "07-17.xml"));
        Assert.assertEquals("status must match", Status.SC_PRECONDITION_FAILED, copy_on_exisiting_response.getStatus());

        // cleanup
        for (final String test : Arrays.asList("07-18.xml", "07-19.xml", "07-20.xml", "07-21.xml", "07-22.xml", "07-23.xml", "07-24.xml", "07-25.xml", "07-26.xml", "07-27.xml", "07-28.xml", "07-29.xml", "07-30.xml", "07-31.xml",
                "07-32.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_NO_CONTENT, response.getStatus());
        }
    }

    @Test
    public void test_08_copy_shallow() throws Exception {
        // create structure
        for (final String test : Arrays.asList("08-01.xml", "08-02.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_CREATED, response.getStatus());
        }

        final TestResponse delete_desitination_response = execute(new File(ROOT, "08-03.xml"));
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, delete_desitination_response.getStatus());

        final TestResponse copy_response = execute(new File(ROOT, "08-04.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, copy_response.getStatus());

        final TestResponse delete_source_response = execute(new File(ROOT, "08-05.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, delete_source_response.getStatus());

        final TestResponse delete_non_exisiting_response = execute(new File(ROOT, "08-06.xml"));
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, delete_non_exisiting_response.getStatus());

        final TestResponse cleanup_response = execute(new File(ROOT, "08-07.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, cleanup_response.getStatus());
    }

    @Test
    public void test_09_move() throws Exception {
        // create structure
        for (final String test : Arrays.asList("09-01.xml", "09-02.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_CREATED, response.getStatus());
        }

        final TestResponse mkcol_response = execute(new File(ROOT, "09-03.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, mkcol_response.getStatus());

        final TestResponse move_new_response = execute(new File(ROOT, "09-04.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, move_new_response.getStatus());

        final TestResponse move_exisiting_response = execute(new File(ROOT, "09-05.xml"));
        Assert.assertEquals("status must match", Status.SC_PRECONDITION_FAILED, move_exisiting_response.getStatus());

        // move
        for (final String test : Arrays.asList("09-06.xml", "09-07.xml", "09-08.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_NO_CONTENT, response.getStatus());
        }
    }

    @Test
    public void test_10_move_coll() throws Exception {
        // create structure
        for (final String test : Arrays.asList("10-01.xml", "10-02.xml", "10-03.xml", "10-04.xml", "10-05.xml", "10-06.xml", "10-10.xml", "10-08.xml", "10-09.xml", "10-10.xml", "10-11.xml", "10-12.xml", "10-13.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_CREATED, response.getStatus());
        }

        final TestResponse copy1_response = execute(new File(ROOT, "10-14.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, copy1_response.getStatus());
        final TestResponse move1_response = execute(new File(ROOT, "10-15.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, move1_response.getStatus());
        final TestResponse move2_response = execute(new File(ROOT, "10-16.xml"));
        Assert.assertEquals("status must match", Status.SC_PRECONDITION_FAILED, move2_response.getStatus());
        final TestResponse move3_response = execute(new File(ROOT, "10-17.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, move3_response.getStatus());
        final TestResponse copy2_response = execute(new File(ROOT, "10-18.xml"));
        Assert.assertEquals("status must match", Status.SC_CREATED, copy2_response.getStatus());

        // delete
        for (final String test : Arrays.asList("10-19.xml", "10-20.xml", "10-21.xml", "10-22.xml", "10-23.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test, Status.SC_NO_CONTENT, response.getStatus());
        }

        final TestResponse delete_non_exisiting_response = execute(new File(ROOT, "10-24.xml"));
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, delete_non_exisiting_response.getStatus());

        for (final String test : Arrays.asList("10-25.xml", "10-26.xml", "10-27.xml", "10-28.xml", "10-29.xml")) {
            final TestResponse response = execute(new File(ROOT, test));
            Assert.assertEquals("status must match: " + test + test, Status.SC_NO_CONTENT, response.getStatus());
        }

        final TestResponse move_sparse_response = execute(new File(ROOT, "10-30.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, move_sparse_response.getStatus());
    }

    @Test
    public void test_11_move_cleanup() throws Exception {
        final TestResponse delete1_response = execute(new File(ROOT, "11-01.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, delete1_response.getStatus());

        final TestResponse delete2_response = execute(new File(ROOT, "11-02.xml"));
        Assert.assertEquals("status must match", Status.SC_NOT_FOUND, delete2_response.getStatus());

        final TestResponse delete3_response = execute(new File(ROOT, "11-03.xml"));
        Assert.assertEquals("status must match", Status.SC_NO_CONTENT, delete3_response.getStatus());
    }
}
