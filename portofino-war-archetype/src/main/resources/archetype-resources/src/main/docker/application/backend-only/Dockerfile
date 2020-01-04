FROM ${project.artifactId}:${project.version}
COPY web.xml $CATALINA_HOME/webapps/ROOT/WEB-INF/
RUN rm -rf $CATALINA_HOME/webapps/ROOT/assets
RUN rm -rf $CATALINA_HOME/webapps/ROOT/pages
RUN rm -rf $CATALINA_HOME/webapps/ROOT/*.css
RUN rm -rf $CATALINA_HOME/webapps/ROOT/*.eot
RUN rm -rf $CATALINA_HOME/webapps/ROOT/*.html
RUN rm -rf $CATALINA_HOME/webapps/ROOT/*.js
RUN rm -rf $CATALINA_HOME/webapps/ROOT/*.ttf
RUN rm -rf $CATALINA_HOME/webapps/ROOT/*.woff2
RUN rm -rf $CATALINA_HOME/webapps/ROOT/*.woff2
