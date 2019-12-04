#!/usr/bin/env bash

while true;
    do rsync --archive --delete \
      --filter="- /WEB-INF/blobs" \
      --filter="- /WEB-INF/classes" \
      --filter="- /WEB-INF/groovy" \
      --filter="- /WEB-INF/lib" \
      --filter="- /WEB-INF/mail" \
      --filter="- /WEB-INF/pages" \
      --filter="- /WEB-INF/portofino-local.properties" \
      --filter="- /WEB-INF/portofino-model**" \
      --filter="- /WEB-INF/web.xml" \
      /application/ $CATALINA_HOME/webapps/ROOT/ \
      && sleep 1;
done
