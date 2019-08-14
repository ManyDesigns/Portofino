FROM tomcat:9.0

EXPOSE 8080
RUN rm -fr /usr/local/tomcat/webapps/ROOT
COPY target/*.war /usr/local/tomcat/webapps/ROOT.war