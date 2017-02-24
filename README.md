Nanopub Indexer
===============

_(under construction)_


### Requirements and Dependencies

Java 1.7, Maven, MySQL database, PHP, Apache.

Database layout described below

### Run Code at the nanopubs server

To run the preliminary code from the command line
Make sure a mysql database 'nanopubs' is running

Make sure the latest version is present (if not pull from git server)

First compile with Maven:

    $ mvn package

Then you can use this script to run the code:

    $ scripts/Indexer.sh <dbusername> <dbpassword>

You should get an output like this:

    ==========
	Server: http://np.inn.ac/

	Info:
	 1000 peerpagesize
	 9042233 peernanopubno
	 4818155244572270824 peerjid

	Db:
	 7786000 dbnanopubno
	 4818155244572270824 dbjid
	==========

	Starting from: 7786000
	...
	..

	
To run the deamon use:

	$ scripts/Indexer.sh <dbusername> <dbpassword> > output.txt &


### Run under Windows with Eclipse

First time run (to be able to import the package in eclipse):

	$ mvn eclipse:eclipse
	
Add the external JAR of the Nanopub JAVA library to the project in eclipse

Then you can edit the code. Make sure to use the command promt to compile the scripts before running them in eclipse.

To compile the script using the command promt, 2 global variables should be set: 

	$ set Path=<mvn root dir>\bin;%Path%
	
	$ set JAVA_HOME=<Java jdk root dir>

Next of one can run the mvn package command on the nanopub-indexer folder:

	$ mvn package

And simply run the indexer in eclipse


### Search for nanopubs

Host the webapplication and make sure to add a
'connectDatabase.php' with that makes a valid connection to the MySQL database mentioned below.
using a MySQLi object that is stored in '$conn'

e.g.

	<?php
	$servername = "localhost";
	$username = "root";
	$password = "examplePassword";
	$dbname = "nanopubs";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);

	// Check connection
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
	} 
	?>

http://petapico.d2s.labs.vu.nl/api/


### Database layout

	mysql> show tables;
	+--------------------+
	| Tables_in_nanopubs |
	+--------------------+
	| indexes            |
	| nanopubs           |
	| servers            |
	| type_sections      |
	| uris               |
	+--------------------+

	mysql> desc indexes;
	+--------------+--------------+------+-----+---------+-------+
	| Field        | Type         | Null | Key | Default | Extra |
	+--------------+--------------+------+-----+---------+-------+
	| artifactCode | varchar(128) | NO   | PRI | NULL    |       |
	| title        | varchar(512) | NO   |     | NULL    |       |
	| children     | int(11)      | NO   |     | NULL    |       |
	+--------------+--------------+------+-----+---------+-------+

	mysql> desc nanopubs;
	+--------------+--------------+------+-----+---------+-------+
	| Field        | Type         | Null | Key | Default | Extra |
	+--------------+--------------+------+-----+---------+-------+
	| artifactCode | varchar(255) | NO   | PRI | NULL    |       |
	| timestamp    | int(16)      | NO   |     | NULL    |       |
	| finished     | tinyint(4)   | NO   |     | 0       |       |
	+--------------+--------------+------+-----+---------+-------+

	mysql> desc servers;
	+---------------+--------------+------+-----+---------+----------------+
	| Field         | Type         | Null | Key | Default | Extra          |
	+---------------+--------------+------+-----+---------+----------------+
	| id            | int(11)      | NO   | PRI | NULL    | auto_increment |
	| serverName    | varchar(255) | NO   |     | NULL    |                |
	| nextNanopubNo | bigint(32)   | NO   |     | 0       |                |
	| journalId     | bigint(32)   | NO   |     | 0       |                |
	+---------------+--------------+------+-----+---------+----------------+

	mysql> desc type_sections;
	+---------+-------------+------+-----+---------+----------------+
	| Field   | Type        | Null | Key | Default | Extra          |
	+---------+-------------+------+-----+---------+----------------+
	| id      | tinyint(4)  | NO   | PRI | NULL    | auto_increment |
	| section | varchar(64) | NO   |     | NULL    |                |
	+---------+-------------+------+-----+---------+----------------+

	mysql> select * from type_sections;
	+----+------------+
	| id | section    |
	+----+------------+
	|  1 | head       |
	|  2 | assertion  |
	|  3 | provenance |
	|  4 | pubinfo    |
	+----+------------+

	mysql> desc uris;
	+--------------+--------------+------+-----+---------+-------+
	| Field        | Type         | Null | Key | Default | Extra |
	+--------------+--------------+------+-----+---------+-------+
	| URI          | varchar(767) | NO   | PRI | NULL    |       |
	| artifactCode | varchar(128) | NO   | PRI | NULL    |       |
	| sectionID    | tinyint(4)   | NO   | PRI | NULL    |       |
	+--------------+--------------+------+-----+---------+-------+