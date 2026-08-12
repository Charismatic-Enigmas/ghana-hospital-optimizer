package com.ghdsa.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ghdsa.model.Location;
import com.ghdsa.model.Resource;
import com.ghdsa.model.Road;
import com.ghdsa.model.ServiceRequest;


public class DatabaseManager implements AutoCloseable {
    private final Connection connection;

    public DatabaseManager(String jdbcUrl) throws SQLException {
        this.connection = DriverManager.getConnection(jdbcUrl);
    }

   
    public void initSchema(String schemaSqlPath) throws IOException, SQLException {
        String sql;
        try (InputStream in = openResourceOrFile(schemaSqlPath)) {
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
       
        String[] statements = sql.replaceAll("--[^\n]*\n", "\n").split(";");
        try (Statement stmt = connection.createStatement()) {
            for (String raw : statements) {
                String s = raw.trim();
                if (!s.isEmpty()) stmt.execute(s);
            }
        }
    }

    private InputStream openResourceOrFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        if (Files.exists(filePath)) {
            return Files.newInputStream(filePath);
        }
        InputStream resourceStream = DatabaseManager.class.getClassLoader().getResourceAsStream(path.replace(File.separatorChar, '/'));
        if (resourceStream != null) {
            return resourceStream;
        }
        throw new FileNotFoundException(path);
    }

    public void importLocationsCsv(String csvPath) throws IOException, SQLException {
        String sql = "INSERT INTO locations (location_id, name, facility, area, type, latitude, longitude) VALUES (?,?,?,?,?,?,?)";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(openResourceOrFile(csvPath), StandardCharsets.UTF_8));
             PreparedStatement ps = connection.prepareStatement(sql)) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] c = splitCsvLine(line);
                ps.setInt(1, Integer.parseInt(c[0]));
                ps.setString(2, c[1]);
                ps.setString(3, c[2]);
                ps.setString(4, c[3]);
                ps.setString(5, c[4]);
                ps.setDouble(6, Double.parseDouble(c[5]));
                ps.setDouble(7, Double.parseDouble(c[6]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void importRoadsCsv(String csvPath) throws IOException, SQLException {
        String sql = "INSERT INTO roads (road_id, from_location_id, to_location_id, distance_km, travel_time_min, road_condition_weight) VALUES (?,?,?,?,?,?)";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(openResourceOrFile(csvPath), StandardCharsets.UTF_8));
             PreparedStatement ps = connection.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] c = splitCsvLine(line);
                ps.setInt(1, Integer.parseInt(c[0]));
                ps.setInt(2, Integer.parseInt(c[1]));
                ps.setInt(3, Integer.parseInt(c[2]));
                ps.setDouble(4, Double.parseDouble(c[3]));
                ps.setDouble(5, Double.parseDouble(c[4]));
                ps.setDouble(6, Double.parseDouble(c[5]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void importServiceRequestsCsv(String csvPath) throws IOException, SQLException {
        String sql = "INSERT INTO service_requests (request_id, source_location, dest_location, category, urgency, time_submitted, deadline, status) VALUES (?,?,?,?,?,?,?,?)";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(openResourceOrFile(csvPath), StandardCharsets.UTF_8));
             PreparedStatement ps = connection.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] c = splitCsvLine(line);
                ps.setInt(1, Integer.parseInt(c[0]));
                ps.setInt(2, Integer.parseInt(c[1]));
                ps.setInt(3, Integer.parseInt(c[2]));
                ps.setString(4, c[3]);
                ps.setString(5, c[4]);
                ps.setString(6, c[5]);
                ps.setString(7, c[6]);
                ps.setString(8, c[7]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void importResourcesCsv(String csvPath) throws IOException, SQLException {
        String sql = "INSERT INTO resources (resource_id, type, home_location, capacity, availability_status) VALUES (?,?,?,?,?)";
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath));
             PreparedStatement ps = connection.prepareStatement(sql)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] c = splitCsvLine(line);
                ps.setInt(1, Integer.parseInt(c[0]));
                ps.setString(2, c[1]);
                ps.setInt(3, Integer.parseInt(c[2]));
                ps.setInt(4, Integer.parseInt(c[3]));
                ps.setString(5, c[4]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Location> loadLocations() throws SQLException {
        List<Location> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM locations")) {
            while (rs.next()) {
                result.add(new Location(rs.getInt("location_id"), rs.getString("name"),
                        rs.getString("facility"), rs.getString("area"), rs.getString("type"),
                        rs.getDouble("latitude"), rs.getDouble("longitude")));
            }
        }
        return result;
    }

    public List<Road> loadRoads() throws SQLException {
        List<Road> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM roads")) {
            while (rs.next()) {
                result.add(new Road(rs.getInt("road_id"), rs.getInt("from_location_id"),
                        rs.getInt("to_location_id"), rs.getDouble("distance_km"),
                        rs.getDouble("travel_time_min"), rs.getDouble("road_condition_weight")));
            }
        }
        return result;
    }

    public List<ServiceRequest> loadServiceRequests() throws SQLException {
        List<ServiceRequest> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM service_requests")) {
            while (rs.next()) {
                result.add(new ServiceRequest(rs.getInt("request_id"), rs.getInt("source_location"),
                        rs.getInt("dest_location"), rs.getString("category"), rs.getString("urgency"),
                        LocalDateTime.parse(rs.getString("time_submitted").replace(" ", "T")),
                        LocalDateTime.parse(rs.getString("deadline").replace(" ", "T")),
                        rs.getString("status")));
            }
        }
        return result;
    }

    public List<Resource> loadResources() throws SQLException {
        List<Resource> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM resources")) {
            while (rs.next()) {
                result.add(new Resource(rs.getInt("resource_id"), rs.getString("type"),
                        rs.getInt("home_location"), rs.getInt("capacity"), rs.getString("availability_status")));
            }
        }
        return result;
    }

    public void insertAlgorithmRun(String algorithmName, int inputSize, long timeNs, double memoryKb) throws SQLException {
        String sql = "INSERT INTO algorithm_runs (algorithm_name, input_size, time_ns, memory_kb, date_run) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, algorithmName);
            ps.setInt(2, inputSize);
            ps.setLong(3, timeNs);
            ps.setDouble(4, memoryKb);
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    public void insertAuditEvent(String eventType, String entityType, int entityId, String detail) throws SQLException {
        String sql = "INSERT INTO audit_events (event_type, entity_type, entity_id, detail, event_time) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, eventType);
            ps.setString(2, entityType);
            ps.setInt(3, entityId);
            ps.setString(4, detail);
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

   
    private String[] splitCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
    
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++; 
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                cols.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        cols.add(cur.toString());
        for (int i = 0; i < cols.size(); i++) {
            String s = cols.get(i).trim();
            if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
                s = s.substring(1, s.length() - 1);
            }
            cols.set(i, s);
        }
        return cols.toArray(new String[0]);
    }

    @Override
    public void close() throws SQLException { connection.close(); }

   
    public boolean tableExists(String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
