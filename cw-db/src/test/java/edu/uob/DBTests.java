package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

// PLEASE READ:
// The tests in this file will fail by default for a template skeleton, your job is to pass them
// and maybe write some more, read up on how to write tests at
// https://junit.org/junit5/docs/current/user-guide/#writing-tests
final class DBTests {

  private DBServer server;

  // we make a new server for every @Test (i.e. this method runs before every @Test test case)
  @BeforeEach
  void setup(@TempDir File dbDir) {
    // Notice the @TempDir annotation, this instructs JUnit to create a new temp directory somewhere
    // and proceeds to *delete* that directory when the test finishes.
    // You can read the specifics of this at
    // https://junit.org/junit5/docs/5.4.2/api/org/junit/jupiter/api/io/TempDir.html

    // If you want to inspect the content of the directory during/after a test run for debugging,
    // simply replace `dbDir` here with your own File instance that points to somewhere you know.
    // IMPORTANT: If you do this, make sure you rerun the tests using `dbDir` again to make sure it
    // still works and keep it that way for the submission.

    server = new DBServer(dbDir);
  }

  // Here's a basic test for spawning a new server and sending an invalid command,
  // the spec dictates that the server respond with something that starts with `[ERROR]`
  @Test
  void testInvalidCommandIsAnError() throws IOException {
    assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("CREATE").startsWith("[ERROR]"));
  }

  // Add more unit tests or integration tests here.
  // Unit tests would test individual methods or classes whereas integration tests are geared
  // towards a specific usecase (i.e. creating a table and inserting rows and asserting whether the
  // rows are actually inserted)

  @Test
  void testCreateUse() throws IOException {
    assertTrue(server.handleCommand("CREATE DATABASE a").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("CREATE DATABASE").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("CREATE DATABASE a;").startsWith("[OK]"));

    assertTrue(server.handleCommand("CREATE TABLE").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("CREATE TABLE tb1;").startsWith("[ERROR]"));

    assertTrue(server.handleCommand("USE b;").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("USE a;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE tb1;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE tb1;").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("CREATE TABLE tb2 (att1,att2,att3);").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE tb3 (att1 , &att2,att3);").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("CREATE TABLE tb3 (att1 , att2,att3);").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE tb4 (att1, att1);").startsWith("[ERROR]"));

    assertTrue(server.handleCommand("CREATE DATABASE b;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE b;").startsWith("[OK]"));
  }

  @Test
  void testDrop() throws IOException {
    assertTrue(server.handleCommand("CREATE DATABASE a;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE DATABASE b;").startsWith("[OK]"));
    assertTrue(server.handleCommand("DROP DATABASE").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("DROP DATABASE b").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("DROP DATABASE b;").startsWith("[OK]"));

    assertTrue(server.handleCommand("USE a;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE tb1;").startsWith("[OK]"));
    assertTrue(server.handleCommand("DROP TABLE").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("DROP TABLE tb1").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("DROP TABLE tb1;").startsWith("[OK]"));
  }

  @Test
  void testAlter() throws IOException {
    assertTrue(server.handleCommand("CREATE DATABASE a;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE a;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE tb1 (att1);").startsWith("[OK]"));
    assertTrue(server.handleCommand("ALTER TABLE tb1;").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("ALTER TABLE tb1 ADD att1;").startsWith("[ERROR]"));
    assertTrue(server.handleCommand("ALTER TABLE tb1 DROP att1;").startsWith("[OK]"));
    assertTrue(server.handleCommand("ALTER TABLE tb1 ADD att1;").startsWith("[OK]"));
  }

  @Test
  void testInsert() throws IOException{
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Steve', 65, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Dave', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Bob', 35, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Clive', 20, FALSE);").startsWith("[OK]"));
  }

  @Test
  void testSelectDelete() throws IOException {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Steve', 65, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Dave', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Bob', 35, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Clive', 20, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks;").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE name != 'Dave';").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE name == 'Clive';").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE pass==TRUE;").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE name LIKE 've';").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT id FROM marks WHERE pass == FALSE;").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT name FROM marks WHERE mark>60;").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);").startsWith("[OK]"));
    assertTrue(server.handleCommand("SELECT * FROM marks WHERE (pass == FALSE) OR (mark > 35);").startsWith("[OK]"));

    assertTrue(server.handleCommand("DELETE FROM marks WHERE name == 'Dave';").startsWith("[OK]"));

  }

  @Test
  void testJoin() throws IOException {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Steve', 65, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Dave', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Bob', 35, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Clive', 20, FALSE);").startsWith("[OK]"));

    assertTrue(server.handleCommand("CREATE TABLE coursework (task, grade);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('OXO', 3);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('DB', 1);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('OXO', 4);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO coursework VALUES ('STAG', 2);").startsWith("[OK]"));

    assertTrue(server.handleCommand("JOIN coursework AND marks ON grade AND id;").startsWith("[OK]"));
  }

  @Test
  void testUpdate() throws IOException {
    assertTrue(server.handleCommand("CREATE DATABASE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("USE markbook;").startsWith("[OK]"));
    assertTrue(server.handleCommand("CREATE TABLE marks (name, mark, pass);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Steve', 65, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Dave', 55, TRUE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Bob', 35, FALSE);").startsWith("[OK]"));
    assertTrue(server.handleCommand("INSERT INTO marks VALUES ('Clive', 20, FALSE);").startsWith("[OK]"));

    assertTrue(server.handleCommand("UPDATE marks SET mark = 38 WHERE name == 'Clive';").startsWith("[OK]"));

  }

}
