# Web Log Parser

Simple Java tool that parses web server access log file, loads the log to MySQL and checks if a given IP makes more than a certain number of requests for the given duration. 

The tool takes "startDate", "duration" and "threshold" as command line arguments. "startDate" is of "yyyy-MM-dd.HH:mm:ss" format, "duration" can take only "hourly", "daily" as inputs and "threshold" can be an integer.

How the tool works:

    java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100
	
	The tool will find any IPs that made more than 100 requests starting from 2017-01-01.13:00:00 to 2017-01-01.14:00:00 (one hour) and print them to console AND also load them to another MySQL table with comments on why it's blocked.

	java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.13:00:00 --duration=daily --threshold=250

	The tool will find any IPs that made more than 250 requests starting from 2017-01-01.13:00:00 to 2017-01-02.13:00:00 (24 hours) and print them to console AND also load them to another MySQL table with comments on why it's blocked.
	
Run from command line
	
    java -cp "parser.jar" com.ef.Parser --accesslog=/path/to/file --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100