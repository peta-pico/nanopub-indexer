Nanopub Indexer
===============

_(under construction)_


### Requirements

Java 1.7 and Maven.


### Run Code

To run the preliminary code from the command line.

First compile with Maven:

    $ mvn package

Then you can use this script to run the code:

    $ scripts/Indexer.sh

You should get an output like this:

    ==========
    Server: http://np.inn.ac/
    
    Nanopub ID: http://example.org/nanopub-validator-example/RAPpJU5UOB4pa...
    Nanopub content:
    @prefix this: <http://example.org/nanopub-validator-example/RAPpJU5UOB...
    ...
	
### Run code (windows + eclipse)

First time run (to be able to import the package in eclipse):

	$ mvn eclipse:eclipse
	
Add the external JAR of the Nanopub JAVA library to the project in eclipse

Then you can edit the code. Make sure to use the command promt to compile the scripts before running them in eclipse.

To compile the script using the command promt, 2 global variables should be set: 

	$ set Path=<mvn root dir>\bin;%Path%
	
	$ set JAVA_HOME=<Java jdk root dir>

Next of one can run the mvn package command on the nanopub-indexer folder:

	$ mvn package
