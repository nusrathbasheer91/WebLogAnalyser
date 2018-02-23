package com.ef;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tool that parses web server access log file, loads it to MySQL table log_files,request_log and
 * adds any IP exceeding specified threshold to table blocked_ip with appropriate message.
 * 
 * @author Nuzz
 *
 */
public class Parser {

  /**
   * Initially loads all command line arguments and configuration properties. Then database
   * transactions are performed. If the log file has already been loaded, the file will not be read
   * into the table again. It will only find out the blocked ips with the new arguments and add.
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    LoadArguments arguments = LoadArguments.getInstance();
    MysqlTransactions transaction = new MysqlTransactions();
    try {
      arguments.load(args);
      if (!transaction.checkLogExists(arguments.getAccessLog())) {
        transaction.readLog(arguments.getAccessLog());
      }
      transaction.checkBlocked();
    } catch (IOException e) {
      e.printStackTrace();
      //throw new IOException();
    } catch (Exception e) {
      Logger lgr = Logger.getLogger(Parser.class.getName());
      lgr.log(Level.SEVERE, e.getMessage(), e);
      System.exit(-1);
    }
  }
}
