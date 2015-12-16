<?php
/*
* 
*
*/
class Statements {
	private $_conn; //stores the database connection
	
	public function __construct($conn){
		$this->_conn = $conn;

		require_once("preparedQuery.php");
	}

	public function getByHashCode($hashCode){
		$query = "SELECT * FROM statements WHERE hashCode = ?";
		$result = mysqli_prepared_query($this->_conn, $query, "d", array($hashCode));
		return $result;
	}

	public function insert($artifactCode, $hashCode, $object, $predicate, $subject, $sectionID){
		$query = "INSERT INTO statements (artifactCode, hashCode, object, predicate, subject, sectionID) VALUES (?, ?, ?, ?, ?, ?)";
		$params = array($artifactCode, $hashCode, $object, $predicate, $subject, $sectionID);
		$result = mysqli_prepared_query($this->_conn, $query, "sdsssd", $params);
		return $result[0];
	}
}

/*
* nanopubURI, artifactCode, creationTime
* Creators, Authors
*
*/
?>