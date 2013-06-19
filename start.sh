APP_PATH="application/target"
echo "$APP_PATH"
LIBS=$(echo $APP_PATH/dependency/*.jar | tr ' ' ':')
echo "$LIBS"
java -cp $APP_PATH/application-0.1-SNAPSHOT.jar:$LIBS org.monroe.team.jfeature.application.Main
