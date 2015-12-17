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

	public function get($hashCode, $object, $predicate, $subject, $head = "", $assertion = "", $provenance = "", $pubinfo = ""){
		$query = "SELECT * FROM statements WHERE 1 = 1";
		$types = "";
		$params = array();

		if (!empty($hashCode)){
			$query .= " AND hashCode = ?";
			$types .= "s";
			$params[] = $hashCode;
		}
		if (!empty($object)){
			$query .= " AND object = ?";
			$types .= "s";
			$params[] = $object;
		}
		if (!empty($predicate)){
			$query .= " AND predicate = ?";
			$types .= "s";
			$params[] = $predicate;
		}
		if (!empty($subject)){
			$query .= " AND subject = ?";
			$types .= "s";
			$params[] = $subject;
		}

		if (!empty($head) || !empty($assertion) || !empty($provenance) || !empty($pubinfo)){
			$query .= " AND sectionID IN (";
			if (!empty($head))
				$query .= "1,";
			if (!empty($assertion))
				$query .= "2,";
			if (!empty($provenance))
				$query .= "3,";
			if (!empty($pubinfo))
				$query .= "4,";
			$query = rtrim($query, ',');
			$query .= ")";
		}

		$result = mysqli_prepared_query($this->_conn, $query, $types, $params);
		return json_encode($result);
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

?>