FROM ${project.artifactId}:${project.version}
#Link to database
COPY portofino-local.properties $CATALINA_HOME/webapps/ROOT/WEB-INF/
#For remote debugging (remember to open the port)
#Use "*:<port>" for Java 9+
ENV JPDA_ADDRESS="8000"
ENV JPDA_TRANSPORT="dt_socket"
ENV PORTOFINO_APPLICATION_DIRECTORY=/application/WEB-INF
#Job to periodically copy JSP's and other files from the source to the application
ADD copy-application-files.sh /
RUN apt-get update && apt-get -y install rsync
RUN chmod +x /copy-application-files.sh
ENTRYPOINT /copy-application-files.sh & catalina.sh jpda run
