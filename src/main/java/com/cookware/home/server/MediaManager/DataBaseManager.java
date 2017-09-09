package com.cookware.home.server.MediaManager;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by Kody on 8/09/2017.
 */
public class DataBaseManager {
    private String fileName;
    private String url;
    private Logger log;


    public DataBaseManager(String mFileName){
        this.log = Logger.getLogger(this.getClass());
        this.fileName = mFileName;
        initialiseDataBase();
    }

    public void initialiseDataBase(){
        Connection conn = null;
        this.url = "jdbc:sqlite:data/" + fileName;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(this.url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
            }
        } catch ( Exception e ) {
            this.log.error("Database not successfully opened");
            System.exit(0);
        }
        this.log.info("Successfully opened database");
        try {
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE MEDIA(" +
                    "ID         BIGINT      NOT NULL, " +
                    "NAME       TEXT        NOT NULL, " +
                    "URL        TEXT        NOT NULL, " +
                    "QUALITY    SMALLINT    NOT NULL, " +
                    "STATE      TINYINT     NOT NULL, " +
                    "PRIORITY   TINYINT     NOT NULL, " +
                    "RELEASED   DATE        NOT NULL, " +
                    "ADDED      DATE        NOT NULL, " +
                    "DOWNLOADED DATE, " +
                    "PATH       TEXT, " +
                    "EPISODE    DECIMAL(4,2), " +
                    "PRIMARY KEY (ID)" +
                    ")";
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
        } catch ( Exception e ) {
            this.log.info("Database already initialised");
        }
        this.log.info("Successfully opened database");
    }

    public void addMedia(BigInteger id, String name, String url, int quality, int state, int priority, LocalDate released, LocalDate added) {
        Connection connection = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(this.url);
            connection.setAutoCommit(false);

            stmt = connection.createStatement();
            String sql = String.format("INSERT INTO MEDIA (ID,NAME,URL,QUALITY,STATE,PRIORITY,RELEASED, ADDED) " +
                    "VALUES (%d, '%s', '%s', %d, %d, %d, '%s', '%s');",id, name, url, quality, state, priority, java.sql.Date.valueOf(released), java.sql.Date.valueOf(added));
            log.info(String.format("SQL Query sent to database: %s",sql));
            stmt.executeUpdate(sql);

            stmt.close();
            connection.commit();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        log.info("Records created successfully");
    }
}
