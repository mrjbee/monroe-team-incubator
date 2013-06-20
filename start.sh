cd projects/fserver
APP_PATH="target"
LIBS=$(echo $APP_PATH/dependency/*.jar | tr ' ' ':')
MAIN_JAR=$(echo $APP_PATH/main*.jar)
CLASSPATH=$MAIN_JAR:$LIBS
echo $CLASSPATH
java -cp $CLASSPATH org.monroe.team.jfeature.application.Main
