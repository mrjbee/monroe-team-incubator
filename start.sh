cd projects/fserver
pwd
APP_PATH="target"
echo "$APP_PATH"
LIBS=$(echo $APP_PATH/dependency/*.jar | tr ' ' ':')
echo "$LIBS"
java -cp $LIBS org.monroe.team.jfeature.application.Main
