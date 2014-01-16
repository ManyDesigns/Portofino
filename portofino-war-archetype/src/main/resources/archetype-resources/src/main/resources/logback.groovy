import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.*

def defaultPattern = "%d %-5level %-40logger{40} %msg%n";
def appenders = [];

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
scan("30 seconds")
