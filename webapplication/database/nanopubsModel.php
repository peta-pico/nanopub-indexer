<?php
/*
* 
*
*/
class nanopubs {
	private $_conn; //stores the database connection
	
	public function __construct($conn){
		$this->_conn = $conn;

		require_once("preparedQuery.php");
	}

	public function getNanopubs(){
		$query = "SELECT * FROM nanopubs";
		$result = mysqli_prepared_query($this->_conn, $query);
		return $result;

	}
	
	public function getNanopubsByID(){
		$query = "SELECT * FROM nanopubs WHERE nanopubID = ?";
		$result = mysqli_prepared_query($this->_conn, $query);
		return $result;
	}
	
	public function getNanopubsByURI(){
		$query = "SELECT * FROM nanopubs WHERE nanopubURI = ?";
		$result = mysqli_prepared_query($this->_conn, $query);
		return $result;
	}
	
	public function getNanopubsByArtifactCode(){
		$query = "SELECT * FROM nanopubs WHERE artifactCode = ?";
		$result = mysqli_prepared_query($this->_conn, $query);
		return $result;
	}
	
	public function getNanopubsByCreationTime(){
		$query = "SELECT * FROM nanopubs WHERE creationTime = ?";
		$result = mysqli_prepared_query($this->_conn, $query);
		return $result;
	}
	
	public function insertNanopub($artifactCode, $creationTime){
		$query = "INSERT INTO nanopubs (artifactCode, creationTime) VALUES (?, ?)";		
		$result = mysqli_prepared_query($this->_conn, $query, "sd", array($artifactCode, $creationTime));
		return $result[0];
	}
}


/*
* nanopubURI, artifactCode, creationTime
* Creators, Authors
*
*/
?>