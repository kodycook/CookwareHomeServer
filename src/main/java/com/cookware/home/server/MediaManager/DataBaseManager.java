package com.cookware.home.server.MediaManager;

import com.cookware.home.server.WebMediaServer.Media;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.rmi.server.ExportException;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Kody on 8/09/2017.
 */
public class DataBaseManager {
    private String fileName;
    private String url;
    private final Logger log = Logger.getLogger(DataBaseManager.class);


    public DataBaseManager(String mFileName) {
        this.fileName = mFileName;
    }

    public void initialiseDataBase() {
        Connection conn = null;
        this.url = "jdbc:sqlite:data/" + fileName;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(this.url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
            }
        } catch (Exception e) {
            this.log.error("Database not successfully opened");
            System.exit(0);
        }

        try {
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE MEDIA(" +
                    "ID         BIGINT      NOT NULL, " +
                    "NAME       TEXT        NOT NULL, " +
                    "TYPE       TINYINT     NOT NULL, " +
                    "URL        TEXT        NOT NULL, " +
                    "QUALITY    SMALLINT    NOT NULL, " +
                    "STATE      TINYINT     NOT NULL, " +
                    "PRIORITY   TINYINT     NOT NULL, " +
                    "RELEASED   DATE        NOT NULL, " +
                    "ADDED      DATE        NOT NULL, " +
                    "DOWNLOADED DATE, " +
                    "PATH       TEXT, " +
                    "PARENTID   BIGINT, " +
                    "EPISODE    DECIMAL(4,2), " +
                    "PRIMARY KEY (ID)" +
                    ")";
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
            this.log.info("Created new Database");
        } catch (Exception e) {
            this.log.info("Database already initialised");
        }
        this.log.info("Successfully opened database");
    }

    public boolean addMediaToDatabase(MediaInfo info) {
        final DownloadState state = DownloadState.PENDING;
        final LocalDate added = LocalDate.now();
        Connection connection = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(this.url);
            connection.setAutoCommit(false);

            stmt = connection.createStatement();
            String sql;
            if(info.TYPE.equals(MediaType.EPISODE)){
                sql = String.format("INSERT INTO MEDIA (ID,NAME,TYPE,URL,QUALITY,STATE,PRIORITY,RELEASED,ADDED, EPISODE, PARENTID) " +
                                "VALUES (%d, '%s', %d, '%s', %d, %d, %d, '%s', '%s', %f, %d);",
                        info.ID,
                        info.NAME,
                        info.TYPE.ordinal(),
                        info.URL,
                        info.QUALITY,
                        state.ordinal(),
                        info.PRIORITY,
                        java.sql.Date.valueOf(info.RELEASED),
                        java.sql.Date.valueOf(added),
                        info.EPISODE,
                        info.PARENTSHOWID);
            }
            else {
                sql = String.format("INSERT INTO MEDIA (ID,NAME,TYPE,URL,QUALITY,STATE,PRIORITY,RELEASED,ADDED) " +
                                "VALUES (%d, '%s', %d, '%s', %d, %d, %d, '%s', '%s');",
                        info.ID,
                        info.NAME,
                        info.TYPE.ordinal(),
                        info.URL,
                        info.QUALITY,
                        state.ordinal(),
                        info.PRIORITY,
                        java.sql.Date.valueOf(info.RELEASED),
                        java.sql.Date.valueOf(added));
            }
//            log.info(String.format("SQL Query sent to database: %s", sql));
            try {
                stmt.executeUpdate(sql);
            } catch (Exception e) {

                log.error(String.format("Hash collision while trying to write to Database between:\n%s\nAND\n%s",
                        info.toString(),
                        getMediaitem(info.ID).toString()));
                return false;
            }

            stmt.close();
            connection.commit();
            connection.close();
        } catch (Exception e) {
            log.error("Error updating Database");
            return false;
        }
        return true;
    }

    public List<MediaInfo> getDownloadQueue() {

        List<MediaInfo> mediaQueue = new ArrayList<>();
        MediaInfo currentMediaInfo;
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(this.url);
            c.setAutoCommit(false);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * " +
                    "FROM MEDIA " +
                    "WHERE STATE = 1 ;");


            while (rs.next()) {
                currentMediaInfo = new MediaInfo();
                currentMediaInfo.ID = new BigInteger(rs.getString("ID"));
                currentMediaInfo.NAME = rs.getString("NAME");
                currentMediaInfo.TYPE = MediaType.values()[rs.getInt("TYPE")];
                currentMediaInfo.STATE = DownloadState.values()[rs.getInt("STATE")];
                currentMediaInfo.URL = rs.getString("URL");
                currentMediaInfo.QUALITY = rs.getInt("QUALITY");
                currentMediaInfo.PRIORITY = rs.getInt("PRIORITY");
                currentMediaInfo.RELEASED = LocalDate.parse(rs.getString("RELEASED"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                if (currentMediaInfo.TYPE.equals(MediaType.EPISODE)){
                    currentMediaInfo.PARENTSHOWID = new BigInteger(rs.getString("PARENTID"));
                    currentMediaInfo.EPISODE = rs.getFloat("EPISODE");
                }

                mediaQueue.add(currentMediaInfo);
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            log.error(String.format("Cannot extract queued media from Database:\n%s", e));
        }
        return mediaQueue;
    }

    public MediaInfo getMediaitem(BigInteger mediaID){
        MediaInfo mediaInfo = null;
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(this.url);
            c.setAutoCommit(false);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT * " +
                    "FROM MEDIA " +
                    "WHERE ID = %d ;",
                    mediaID));


            while (rs.next()) {
                mediaInfo = new MediaInfo();
                mediaInfo.ID = new BigInteger(rs.getString("ID"));
                mediaInfo.NAME = rs.getString("NAME");
                mediaInfo.TYPE = MediaType.values()[rs.getInt("TYPE")];
                mediaInfo.STATE = DownloadState.values()[rs.getInt("STATE")];
                mediaInfo.URL = rs.getString("URL");
                mediaInfo.QUALITY = rs.getInt("QUALITY");
                mediaInfo.PRIORITY = rs.getInt("PRIORITY");
                mediaInfo.RELEASED = LocalDate.parse(rs.getString("RELEASED"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                if (mediaInfo.TYPE.equals(MediaType.EPISODE)){
                    mediaInfo.PARENTSHOWID = new BigInteger(rs.getString("PARENTID"));
                    mediaInfo.EPISODE = rs.getFloat("EPISODE");
                }
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            log.error(e);
        }
        return mediaInfo;
    }

    // TODO: Modify function to be able to update any attribute
    public void updateState(BigInteger mediaId, DownloadState downloadState){

        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(this.url);
            c.setAutoCommit(false);

            stmt = c.createStatement();
            String sql = String.format("UPDATE MEDIA SET STATE = %d where ID=%d;",downloadState.ordinal(), mediaId);
            stmt.executeUpdate(sql);

            c.commit();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            log.error(String.format("Failed to update database\n%s",e.getMessage()));
        }
    }
}
