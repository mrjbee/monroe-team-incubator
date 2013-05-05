cd distribution/target
rm -rf server
unzip -u distribution*.zip -d server
cd server
java -jar app-arhitect-osgi-core-0.1-SNAPSHOT-jar-with-dependencies.jar -b ./bundles
