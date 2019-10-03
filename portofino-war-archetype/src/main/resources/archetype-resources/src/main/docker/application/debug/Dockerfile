FROM ${project.artifactId}:${project.version}
#Link to database
COPY portofino-local.properties $CATALINA_HOME/webapps/ROOT/WEB-INF/
#For remote debugging (remember to open the port)
#Use "*:<port>" for Java 9+
ENV JPDA_ADDRESS="8000"
ENV JPDA_TRANSPORT="dt_socket"
ENTRYPOINT ["catalina.sh", "jpda", "run"]
