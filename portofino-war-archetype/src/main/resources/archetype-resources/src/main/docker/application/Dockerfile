FROM tomcat:8.5.42-jdk8-slim
ENV CATALINA_HOME /usr/local/tomcat
RUN mkdir -p $CATALINA_HOME/webapps/ROOT/WEB-INF/
COPY maven/lib/* $CATALINA_HOME/lib/
COPY maven/ROOT.war $CATALINA_HOME/webapps/ROOT.war
WORKDIR $CATALINA_HOME/webapps/ROOT
RUN jar xf ../ROOT.war
RUN rm -f ../ROOT.war
