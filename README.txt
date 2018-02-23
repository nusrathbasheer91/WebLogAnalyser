To run the jar file : 
Jar "parser.jar" should be on same level as lib,src,data,conf folder.

Use this command : java -cp .:/lib/mysql-connector-java-5.1.6.jar -jar "parser.jar" :com.ef.Parser --accesslog=data/access.log --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100

Modify conf/parser.conf with updated values of mysql connection.

src folder contains source code of the tool
testsrc folder contains unit testing code of the tool
The SQL schema is in schema.sql.
The SQL questions with answers are in sqlAnswers.sql