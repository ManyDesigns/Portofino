import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender

import static ch.qos.logback.classic.Level.*

def catalinaBase = System.getProperty("catalina.base");
def defaultPattern = "%d{HH:mm:ss.SSS} [userId=%X{userId}] %logger{40} [%F:%L]%n%level: %msg%n";
def appenders = [];

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
    addInfo("Adding console appender");
    encoder(PatternLayoutEncoder) {
        pattern = defaultPattern;
    }
}
appenders.add("PORTOFINO-CONSOLE");

root(INFO, appenders)
logger("org.hibernate", WARN)
logger("org.hibernate.connection.C3P0ConnectionProvider", INFO)

//Periodically reload the file if it changed
scan("1 minute");