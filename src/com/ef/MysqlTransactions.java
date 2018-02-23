package com.ef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides the necessary MySQL transactions required by the tool to do its processing :
 * Checking if current log file is already updated to MySQL, Loading Log File, Checking for ips that
 * need to be blocked
 * 
 * @author Nuzz
 *
 */
public class MysqlTransactions {
  LoadArguments arguments = LoadArguments.getInstance();
  private String connectionString;

  /**
   * This function searches check if the current accessLog file's name has been previously loaded or
   * not in MySQL by checking the web_log_data.log_files table
   * 
   * @param accessLog : path of accessLog
   * @return true if same name accessLog is already loaded into mySQL
   * @throws SQLException if there is a database access error
   * @throws ClassNotFoundException 
   */
  boolean checkLogExists(String accessLog) throws SQLException, ClassNotFoundException {
    connectionString = "jdbc:mysql://" + arguments.getIp() + "?user=" + arguments.getUsername()
        + "&password=" + arguments.getPassword();
    File logFile = new File(arguments.getAccessLog());
    String accessLogName = logFile.getName();
    Connection connect = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;

    try {
      Class.forName("com.mysql.jdbc.Driver");
      connect = DriverManager.getConnection(connectionString);
      preparedStatement =
          connect.prepareStatement("select * from web_log_data.log_files where log_file_name=?");
      preparedStatement.setString(1, accessLogName);
      resultSet = preparedStatement.executeQuery();

      if (resultSet.last()) {
        if (resultSet.getRow() == 1) {
          connect.close();
          return true;
        }
      }
    } catch (SQLException e) {
      Logger lgr = Logger.getLogger(MysqlTransactions.class.getName());
      lgr.log(Level.SEVERE, e.getMessage(), e);
      System.exit(-1);
    }

    connect.close();
    return false;
  }

  /**
   * This function creates new entries according to schema. Adds accessLog name to log_files and
   * adds the requests in that log file to request_log.
   * 
   * @param path of accessLog file
   * @throws IOException if accessLog was unreadable
   */
  void readLog(String path) throws IOException {

    connectionString = "jdbc:mysql://" + arguments.getIp() + "?user=" + arguments.getUsername()
        + "&password=" + arguments.getPassword();
    int logId;
    File logFile = new File(arguments.getAccessLog());
    String accessLogName = logFile.getName();
    Connection connect = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    String line;
    BufferedReader buffer = new BufferedReader(new FileReader(path));

    try {
      connect = DriverManager.getConnection(connectionString);
      preparedStatement =
          connect.prepareStatement("Insert into web_log_data.log_files values(default,?)");
      preparedStatement.setString(1, accessLogName);
      preparedStatement.executeUpdate();
      connect.close();

      connect = DriverManager.getConnection(connectionString);
      preparedStatement = connect
          .prepareStatement("Select log_file_id from web_log_data.log_files where log_file_name=?");
      preparedStatement.setString(1, accessLogName);
      resultSet = preparedStatement.executeQuery();
      resultSet.next();
      logId = resultSet.getInt(1);
      connect.close();

      connect = DriverManager.getConnection(connectionString);
      preparedStatement =
          connect.prepareStatement("Insert into web_log_data.request_log values(?,?,?,?,?,?) ");
      while ((line = buffer.readLine()) != null) {
        String[] tokens = line.split("\\|");
        if (tokens.length == 5) {
          preparedStatement.setInt(1, logId);
          preparedStatement.setString(2, tokens[0]);
          preparedStatement.setString(3, tokens[1]);
          preparedStatement.setString(4, tokens[2]);
          preparedStatement.setString(5, tokens[3]);
          preparedStatement.setString(6, tokens[4]);
          preparedStatement.addBatch();
        }
      }
      preparedStatement.executeBatch();
      connect.close();
      buffer.close();
    } catch (FileNotFoundException e) {
      throw new FileNotFoundException("Unable to open file : accessLog.");
    } catch (SQLException e) {
      Logger lgr = Logger.getLogger(MysqlTransactions.class.getName());
      lgr.log(Level.SEVERE, e.getMessage(), e);
      System.exit(-1);
    }
  }

  /**
   * This function queries the request_log table to get Ips that need to be blocked based on current
   * restraints and adds that to the bolcked_ips table. Duplicate rows comprising of log_id, ip,
   * constraint threshold, datetime constraint and duration will overwrite the existing.
   * 
   * Catches any SQLException caused by database access error.
   */
  void checkBlocked() {
    connectionString = "jdbc:mysql://" + arguments.getIp() + "?user=" + arguments.getUsername()
        + "&password=" + arguments.getPassword();
    int logId;
    File logFile = new File(arguments.getAccessLog());
    String accessLogName = logFile.getName();
    Connection connect = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;

    try {
      connect = DriverManager.getConnection(connectionString);
      preparedStatement = connect
          .prepareStatement("Select log_file_id from web_log_data.log_files where log_file_name=?");
      preparedStatement.setString(1, accessLogName);
      resultSet = preparedStatement.executeQuery();
      resultSet.next();
      logId = resultSet.getInt(1);
      connect.close();

      connect = DriverManager.getConnection(connectionString);
      preparedStatement = connect.prepareStatement(
          "select ip, count from ( select log_file_id, ip, datetime, count(*) as count from "
              + "request_log where log_file_id = ? and datetime between ? and "
              + "DATE_ADD(?, INTERVAL ? HOUR) group by ip)toblock where count>= ?;");
      preparedStatement.setInt(1, logId);
      preparedStatement.setString(2, arguments.getStartDate());
      preparedStatement.setString(3, arguments.getStartDate());
      preparedStatement.setInt(4, arguments.getDurationInHours());
      preparedStatement.setInt(5, arguments.getThreshold());
      resultSet = preparedStatement.executeQuery();

      preparedStatement =
          connect.prepareStatement("Replace into web_log_data.blocked_ips values(?,?,?,?,?,?) ");
      while (resultSet.next()) {
        preparedStatement.setInt(1, logId);
        preparedStatement.setString(2, resultSet.getString(1));
        preparedStatement.setInt(3, arguments.getThreshold());
        preparedStatement.setInt(4, resultSet.getInt(2));
        preparedStatement.setString(5, arguments.getStartDate());
        String message =
            resultSet.getString(1) + " was blocked because it exceed " + arguments.getDuration()
                + " threshold of " + arguments.getThreshold() + " at " + arguments.getStartDate()+" with number of requests = " + resultSet.getInt(2);
        System.out.println(message);
        preparedStatement.setString(6, message);
        preparedStatement.addBatch();
      }
      preparedStatement.executeBatch();
      connect.close();

    } catch (SQLException e) {
      Logger lgr = Logger.getLogger(MysqlTransactions.class.getName());
      lgr.log(Level.SEVERE, e.getMessage(), e);
      System.exit(-1);
    }

  }

}
