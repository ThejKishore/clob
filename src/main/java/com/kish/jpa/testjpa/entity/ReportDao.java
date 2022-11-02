package com.kish.jpa.testjpa.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.*;
import java.util.List;
import java.util.Scanner;

@Repository
public class ReportDao  {
    @Autowired
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    private void postConstruct() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public long save(Report report) {
        String sql = "insert into REPORT (NAME, CONTENT) values (?, ? )";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql.toString(),
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, report.getName());
                Reader reader = new StringReader(report.getContent());
                ps.setClob(2, reader);
                return ps;
            }
        }, holder);
        Number key = holder.getKey();
        if (key != null) {
            return key.longValue();
        }
        throw new RuntimeException("No generated primary key returned.");
    }

    public long update(long id,String content) {
        String sql = "update REPORT set content = ? where id = ?";
        KeyHolder holder = new GeneratedKeyHolder();
       return  jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                    throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setLong(2, id);
                Reader reader = new StringReader(content);
                ps.setClob(1, reader);
                return ps;
            }
        });
    }


    public Report load(long id) {
        List<Report> persons = jdbcTemplate.query("select * from REPORT where id =?",
                new Object[]{id}, (resultSet, i) -> {
                    return toReport(resultSet);
                });
        if (persons.size() == 1) {
            return persons.get(0);
        }
        throw new RuntimeException("No item found for id: " + id);
    }

    private Report toReport(ResultSet resultSet) throws SQLException {
        Report report = new Report();
        report.setId(resultSet.getLong("ID"));
        report.setName(resultSet.getString("NAME"));
        InputStream contentStream = resultSet.getClob("CONTENT")
                .getAsciiStream();
        String content =
                new Scanner(contentStream, "UTF-8").useDelimiter("\\A").next();
        report.setContent(content);
        return report;
    }
}