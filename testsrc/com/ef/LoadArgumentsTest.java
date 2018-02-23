package com.ef;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.junit.Test;

public class LoadArgumentsTest {
  LoadArguments arguments = LoadArguments.getInstance();
  
  @Test
  public void testValidateDate() {
    assertTrue(arguments.validateDate("2017-01-01.13:00:00"));
    assertFalse(arguments.validateDate("2017-01-01.13:00.00"));
  }
  
  @Test
  public void testParseCommandsPass() {
    String[] args = {"--accesslog=data/access.log","--startDate=2017-01-01.13:00:00","--duration=hourly","--threshold=100"};
    arguments.parseCommand(args);
    assertEquals("2017-01-01 13:00:00",arguments.getStartDate());
    assertEquals(Duration.HOURLY,arguments.getDuration());
    assertEquals(1, arguments.getDurationInHours());
    assertEquals(100,arguments.getThreshold());
    assertEquals("data/access.log",arguments.getAccessLog());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testParseCommandsFail() {
    String[] args = {"--accesslog=data/access.log","--startDate=qwer-01-01.13:00:00","--duration=hourly","--threshold=hello"};
    arguments.parseCommand(args);
  }
  
  @Test
  public void testParseConfig() throws IOException {
    String[] args = {"--accesslog=data/access.log","--startDate=2017-01-01.13:00:00","--duration=hourly","--threshold=100"};
    arguments.load(args);
    assertEquals("developer1",arguments.getUsername());
    assertNotEquals("000000",arguments.getPassword());
    assertEquals("localhost/web_log_data",arguments.getIp());
  }

}
