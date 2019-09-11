FROM tomcat:8.5.42-jdk8-slim
ENV CATALINA_HOME /usr/local/tomcat
ADD maven/*.jar $CATALINA_HOME/lib/
ADD maven/ROOT.war $CATALINA_HOME/webapps/ROOT.war
ADD maven/portofino-local.properties $CATALINA_HOME/webapps/ROOT/WEB-INF/
WORKDIR $CATALINA_HOME/webapps/ROOT
RUN jar xf ../ROOT.war
RUN rm -f ../ROOT.war
