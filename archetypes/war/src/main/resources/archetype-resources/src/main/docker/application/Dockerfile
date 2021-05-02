FROM tomcat:9-jdk11-openjdk
ENV CATALINA_HOME=/usr/local/tomcat
ENV PORTOFINO_APPLICATION_DIRECTORY=$CATALINA_HOME/webapps/ROOT/WEB-INF/
RUN mkdir -p $CATALINA_HOME/webapps/ROOT/WEB-INF/
COPY maven/lib/* $CATALINA_HOME/lib/
COPY maven/ROOT.war $CATALINA_HOME/webapps/ROOT.war
WORKDIR $CATALINA_HOME/webapps/ROOT
RUN jar xf ../ROOT.war
RUN rm -f ../ROOT.war
