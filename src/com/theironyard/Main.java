package com.theironyard;

import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS events (id IDENTITY, description VARCHAR, start_date TIMESTAMP)");
    }

    public static void insertEvent(Connection conn, String description, LocalDateTime startDate) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO events VALUES (NULL, ?, ?)");
        stmt.setString(1, description);
        stmt.setTimestamp(2, Timestamp.valueOf(startDate));
        stmt.execute();
    }

    public static ArrayList<Event> selectEvents(Connection conn) throws SQLException {
        ArrayList<Event> events = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM events");
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            Event event = new Event();
            event.id = results.getInt("id");
            event.description = results.getString("description");
            event.startDate = results.getTimestamp("start_date").toLocalDateTime();
            events.add(event);
        }
        return events;
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.get(
                "/",
                (request, response) -> {
                    HashMap m = new HashMap();
                    //preset today's time and date
                    m.put("now", LocalDateTime.now());
                    m.put("events", selectEvents(conn));
                    return new ModelAndView(m, "events.html");
                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/create-event",
                ((request, response) -> {
                    String description = request.queryParams("description");
                    String startDateStr = request.queryParams("startDate");

                    try {
                        LocalDateTime startDate = LocalDateTime.parse(startDateStr);
                        insertEvent(conn, description, startDate);
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );

    }
}
