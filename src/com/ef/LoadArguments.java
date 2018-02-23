package com.ef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This is a singleton class, used to load command line arguments and configuration file parameters.
 * Default value for configFile is used. Options supported are startDate in format
 * "yyyy-MM-dd.HH:mm:ss", duration is HOURLY or DAILY, accesLog is path to the logFile to be parsed,
 * threshold is the limit on the maximum requests by an ip in a particular time window, configFile
 * is path to the configuration file.
 * 
 * In the configuration file, one can set the username, password and ip to the mysql server they
 * want to connect to.
 * 
 * @author Nuzz
 *
 */
public class LoadArguments {

  public static final LoadArguments arguments = new LoadArguments();
  private String startDate;
  private Duration duration;
  private String accessLog;
  private int threshold = 0;
  private String configFile = "conf/parser.conf";
  private String username;
  private String password;
  private String ip;

  private LoadArguments() {};

  /**
   * @return singleton instance of this class
   */
  public static LoadArguments getInstance() {
    return arguments;
  }

  /**
   * function can be called to initialize the loading of parameters
   * 
   * @param args is Command Line Arguments
   * @throws IOException
   */
  public void load(String[] args) throws IOException {
    parseCommand(args);
    parseConfig(configFile);
  }

  /**
   * 
   * @param value is the argument input
   * @return boolean date is of valid argument date format
   */
  boolean validateDate(String value) {
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");
    try {
      LocalDate.parse(value, format);
      return true;
    } catch (DateTimeException e) {
      return false;
    }
  }

  /**
   * This function parses the command line arguments and loads accordingly. Command Line Arguments
   * accepted are : --startDate,--duration,--threshold,--accesslog,--config
   * 
   * Saves startDate in the format in the logfile "yyyy-MM-dd HH:mm:ss.SSS"
   * 
   * @param args is Command Line Arguments
   * @throws IllegalArgumentException if argument provided does not match constraints
   */
  void parseCommand(String[] args) {
    for (String arg : args) {
      String[] tokens = arg.split("=", 2);
      if(tokens[0].startsWith("--")) {
        String key = tokens[0].trim();
        String value = tokens[1].trim();

        if (key.equals("--startDate")) {
          if (validateDate(value)) {
            startDate = value.replaceAll("\\.", " ").trim();
          } else {
            throw new IllegalArgumentException(
                "Invalid value entered for StartDate. Format : yyyy-MM-dd.HH:mm:ss");
          }

        } else if (key.equals("--duration")) {
          try {
            duration = Duration.valueOf(value.toUpperCase());
          } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid value entered for Duration. Options => hourly or daily");
          }
        } else if (key.equals("--threshold")) {
          try {
            threshold = Integer.parseInt(value);
          } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid value entered for Threshold. Only integers allowed");
          }
        } else if (key.equals("--accesslog")) {
          accessLog = value;
        } else if (key.equals("--config")) {
          configFile = value;
        }
      }
      
    }
  }

  /**
   * Loads username, password, ip required to connect to MySQL database
   * 
   * @param path of the config file
   * @throws IOException if config file was not readable
   */
  private void parseConfig(String path) throws IOException {
    File file = new File(path);
    System.out.println(file.getAbsolutePath());
    try {
      BufferedReader reader = new BufferedReader(new FileReader(path));
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        String[] tokens = line.split(":", 2);
        String key = tokens[0].trim();
        String value = tokens[1].trim();

        if (key.equals("user")) {
          username = value;
        } else if (key.equals("password")) {
          password = value;
        } else if (key.equals("ip")) {
          ip = value;
        }
      }
      reader.close();

    } catch (IOException e) {
      throw new IOException("Unable to open file : configFile.");
    }
  }

  /**
   * 
   * @return path of accessLog
   */
  public String getAccessLog() {
    return accessLog;
  }

  /**
   * 
   * @return startDate constraint
   */
  public String getStartDate() {
    return startDate;
  }

  /**
   * 
   * @return duration constraint in terms of hours. Daily = 24 hours.
   */
  public int getDurationInHours() {
    if (duration == Duration.DAILY) {
      return 24;
    } else
      return 1;
  }

  /**
   * 
   * @return duration constraint
   */
  public Duration getDuration() {
    return duration;
  }

  /**
   * 
   * @return threshold constraint
   */
  public int getThreshold() {
    return threshold;
  }

  /**
   * 
   * @return username to connect to MySQL
   */
  public String getUsername() {
    return username;
  }

  /**
   * 
   * @return password to connect to MySQL
   */
  public String getPassword() {
    return password;
  }

  /**
   * 
   * @return Ip to connect to MySQL
   */
  public String getIp() {
    return ip;
  }
}
