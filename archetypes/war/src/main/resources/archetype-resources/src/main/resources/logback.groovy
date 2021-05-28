import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.jul.LevelChangePropagator
import org.hibernate.c3p0.internal.C3P0ConnectionProvider

import static ch.qos.logback.classic.Level.*

def defaultPattern = "%d %-5level %-40logger{40} %X{userId} %X{req.requestURI} %msg%n";
def appenders = [];

//java.util.logging integration (for better performance)
//See:
// http://www.slf4j.org/legacy.html#jul-to-slf4j
// http://logback.qos.ch/manual/configuration.html#LevelChangePropagator
// http://stackoverflow.com/questions/23263723/how-can-i-add-a-contextlistener-in-logback-groovy-configuration
def lcp = new LevelChangePropagator([ context: context, resetJUL: true ])
context.addListener(lcp)

appender("PORTOFINO-CONSOLE", ConsoleAppender) {
    addInfo("Adding console appender");
    encoder(PatternLayoutEncoder) {
        pattern = defaultPattern;
    }
}
appenders.add("PORTOFINO-CONSOLE");

root(INFO, appenders)
logger("org.hibernate", WARN)
logger(C3P0ConnectionProvider.name, INFO)

//Periodically reload this file when it changes
scan("30 seconds")
