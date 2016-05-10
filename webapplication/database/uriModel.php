<?php

class URIs {
	public static $SECTION_HEAD = 1;
	public static $SECTION_ASSERTION = 2;
	public static $SECTION_PROVENANCE = 3;
	public static $SECTION_PUBINFO = 4;
	public static $PAGE_SIZE = 1000;

	private $_conn; // stores the database connection
	
	public function __construct($conn){
		$this->_conn = $conn;

		require_once("preparedQuery.php");
	}

	// RETURNS A LIST OF ARTIFACT CODES
	public function getArtifactCodes($uri, $head, $assertion, $provenance, $pubinfo, $page){
		// BUILDS A QUERY LIKE: SELECT artifactCode FROM uris WHERE uri = ? AND sectionID IN (x) LIMIT 1000
		$types = "";
		$query = "SELECT artifactCode FROM uris WHERE uri IN (";
		foreach ($uri as $searchuri){
			$query .= "?,";
			$types .= "s";
		}
		$query = rtrim($query, ',');
		$query .= ")";
		if ($head == "off" && $assertion == "off" && $provenance == "off" && $pubinfo == "off"){
			// ALL OFF
			
		}
		else {
			$query .= " AND sectionID IN (";
			if ($head == "on"){
				$query .= URIs::$SECTION_HEAD . ",";
			}
			if ($assertion == "on"){
				$query .= URIs::$SECTION_ASSERTION . ",";
			}
			if ($provenance == "on"){
				$query .= URIs::$SECTION_PROVENANCE . ",";
			}
			if ($pubinfo == "on"){
				$query .= URIs::$SECTION_PUBINFO . ",";
			}
			$query = rtrim($query, ',');
			$query .= ")";
		}

		$query .= " GROUP BY artifactCode";
		$query .= " HAVING COUNT(*) = " . count($uri);
		if ($page != 0){
			$query .= " LIMIT " . URIs::$PAGE_SIZE;
			$query .= " OFFSET " . ($page-1) * URIs::$PAGE_SIZE;
		}

		$params = $uri;
		$data = mysqli_prepared_query($this->_conn, $query, $types, $params);
		$result = array();

		foreach ($data as $item){
			$result[] = $item['artifactCode'];
		}
		return json_encode($result);
	}
}
?>