FROM tomcat:9-jdk11-openjdk

RUN mkdir -p $CATALINA_HOME/webapps/ROOT/WEB-INF/
WORKDIR $CATALINA_HOME/webapps/ROOT

RUN echo 'JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"' > $CATALINA_HOME/bin/setenv.sh
RUN chmod +x $CATALINA_HOME/bin/setenv.sh

ADD 'https://jdbc.postgresql.org/download/postgresql-42.2.14.jar' $CATALINA_HOME/lib/

COPY target/demo-tt-*.war $CATALINA_HOME/webapps/ROOT.war

RUN jar xf ../ROOT.war; rm -f ../ROOT.war
