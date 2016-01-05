<?php

class URIs {
	private $_conn; //stores the database connection
	
	public function __construct($conn){
		$this->_conn = $conn;

		require_once("preparedQuery.php");
	}

	public function getArtifactCodes($uri){
		$query = "SELECT artifactCode FROM uris WHERE uri = ?";
		$params = array($uri);
		$result = mysqli_prepared_query($this->_conn, $query, "s", $params);
		return json_encode($result);
	}
}
?>