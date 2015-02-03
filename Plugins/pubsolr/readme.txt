To build solr collection plugin run
	gradlew jar
Built pubsolr-x.x.x.jar can be found in build/libs sub-folder. Copy this file to libs folder of your collection in solr.

To build solr collection plugin including dependencies required for command-line-interface run
	gradlew cli
Built pubsolr-cli-x.x.x.jar can be found in build/libs sub-folder. Example of creating compounds.fsa using this jar:
	java -jar pubsolr-cli-x.x.x.jar -c -t compounds.txt -f compounds.fsa

To create eclipse project files run
	gradlew eclipse
	
To recreate eclipse project files run
	gradlew cleanEclipse eclipse
