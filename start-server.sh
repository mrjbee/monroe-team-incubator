cd distribution/target
rm -rf server
unzip -u distribution*.zip -d server
cd server
touch conf.properties
echo "felix.auto.deploy.dir=$(pwd)/bundles" >> conf.properties
echo "felix.auto.deploy.action=install, start" >> conf.properties
confFile=$(pwd)/conf.properties
confFileURI=FILE:${confFile}
echo "==== Using: ${confFile} ===="
echo "_______________________________________________"
cat $confFile
echo "_______________________________________________"
java -Dfelix.config.properties=$confFileURI -jar app-arhitect-osgi-core-0.1-SNAPSHOT-jar-with-dependencies.jar
