FROM ${project.artifactId}:${project.version}
#Link to database
COPY portofino-local.properties $CATALINA_HOME/webapps/ROOT/WEB-INF/
#For remote debugging (remember to open the port)
ENV JPDA_ADDRESS="*:8000"
ENV JPDA_TRANSPORT="dt_socket"
ENV PORTOFINO_APPLICATION_DIRECTORY=/application
#These are mounted as volumes
RUN rm -rf $CATALINA_HOME/webapps/ROOT/assets
RUN rm -rf $CATALINA_HOME/webapps/ROOT/pages
ENTRYPOINT ["catalina.sh", "jpda", "run"]
