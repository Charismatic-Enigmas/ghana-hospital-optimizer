package com.ghdsa.app;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class AcceptanceTest {

    @Test
    void runInteractiveFlowCreatesAndLoadsData() throws Exception {
        Path dbPath = Path.of("target/acceptance-db.db");
        Files.deleteIfExists(dbPath);

        String inputs = "1\n2\n3\n21\n"; // initialise, load, dispatch, exit
        InputStream origIn = System.in;
        PrintStream origOut = System.out;
        try (ByteArrayInputStream in = new ByteArrayInputStream(inputs.getBytes())) {
            System.setIn(in);
            // run main with our DB path
            Main.main(new String[]{dbPath.toString()});
        } finally {
            System.setIn(origIn);
            System.setOut(origOut);
        }

        // Verify the database file exists and contains data
        assertTrue(Files.exists(dbPath), "DB file should exist after running init/import");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());
             Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM locations");
            assertTrue(rs.next());
            int locCount = rs.getInt(1);
            assertTrue(locCount > 0, "locations should have been imported");

            rs = s.executeQuery("SELECT COUNT(*) FROM roads");
            assertTrue(rs.next());
            int roadCount = rs.getInt(1);
            assertTrue(roadCount > 0, "roads should have been imported");

            rs = s.executeQuery("SELECT COUNT(*) FROM service_requests");
            assertTrue(rs.next());
            int reqCount = rs.getInt(1);
            assertTrue(reqCount > 0, "service_requests should have been imported");
        }
    }
}
