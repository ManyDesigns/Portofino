FROM tomcat:9-jdk11-openjdk-slim
ENV CATALINA_HOME /usr/local/tomcat
RUN mkdir -p $CATALINA_HOME/webapps/ROOT/WEB-INF/
ADD maven/*.jar $CATALINA_HOME/lib/
ADD maven/ROOT.war $CATALINA_HOME/webapps/ROOT.war
WORKDIR $CATALINA_HOME/webapps/ROOT
RUN jar xf ../ROOT.war
RUN rm -f ../ROOT.war
