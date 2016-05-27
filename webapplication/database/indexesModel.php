<?php

class indexes {

	private $_conn; // stores the database connection
	
	public function __construct($conn){
		$this->_conn = $conn;

		require_once("preparedQuery.php");
	}

	public function getIndexes(){
		$query = "SELECT title, COUNT( title ) AS indexCount, SUM( children ) AS contentCount
					FROM  indexes
					GROUP BY title";
		$result = mysqli_prepared_query($this->_conn, $query);
		return $result;
	}
}

?>