import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.*
import ch.qos.logback.core.FileAppender

//Create an appender that writes to logs/portofino.log in Tomcat
def catalinaBase = System.getProperty("catalina.base");
def defaultPattern = "%d{HH:mm:ss.SSS} [userId=%X{userId}] %logger{40} [%F:%L]%n%level: %msg%n";
def appenders = new ArrayList();

if(catalinaBase != null) {
    appender("PORTOFINO-TOMCAT", FileAppender) {
        def outputFile = "${catalinaBase}/logs/portofino.log";
        addInfo("Tomcat detected, using file appender: ${outputFile}");
        file = outputFile;
        encoder(PatternLayoutEncoder) {
            pattern = defaultPattern;
        }
    }
    appenders.add("PORTOFINO-TOMCAT");
}

appender("PORTOFINO-CONSOLE", ConsoleAppender) {
    addInfo("Falling back to console appender");
    encoder(PatternLayoutEncoder) {
        pattern = defaultPattern;
    }
}
appenders.add("PORTOFINO-CONSOLE");

//Custom log levels
logger("org.hibernate", WARN)
logger("org.hibernate.connection.C3P0ConnectionProvider", INFO)

//Default log level
root(INFO, appenders)