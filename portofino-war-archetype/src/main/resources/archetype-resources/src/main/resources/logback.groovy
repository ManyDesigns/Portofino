import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN

def defaultPattern = "%d [userId=%X{userId}] %logger{40} [%F:%L]%n%level: %msg%n";
def appenders = new ArrayList();

appender("PORTOFINO-CONSOLE", ConsoleAppender) {
    addInfo("Falling back to console appender");
    encoder(PatternLayoutEncoder) {
        pattern = defaultPattern;
    }
}
appenders.add("PORTOFINO-CONSOLE");

root(INFO, appenders)
logger("org.hibernate", WARN)
logger("org.hibernate.connection.C3P0ConnectionProvider", INFO)

