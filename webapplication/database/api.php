<?php
/* DATABASE API V1.0
*	API should be able to process all database access
*
*	Dependencies:
*		- database/nanopubsModel.php
*
*	Usage: 
*		api.php
*			?table=<tablename>
*			&function=<functionname>
*			&data[]=<data1>
*			&data[]=<data2>
*			... etc
*/
require_once("connectDatabase.php");

//RETRIEVE GET VARIABLES
$table = $_GET['table']; //TABLENAME
$function = $_GET['function']; //FUNCTION OF THE MODEL
$data = $_GET['data']; //ARRAY CONSISTING OF ALL DATA


/* nanopubsModel connector */
if ($table == "nanopubs"){
	require_once("nanopubsModel.php");
	$nanopubs = new nanopubs($conn);

	switch ($function) {
		case "getNanopubs":
			$result = $nanopubs->getNanopubs();
			print_r($result);
			break;
		case "getNanopubsByID":
			$id = $data[0];
			$result = $nanopubs->getNanopubsByID($id);
			print_r($result);
			break;
		case "insertNanopub":
			$artifactCode = $data[0];
			$creationTime = $data[1];
			$result = $nanopubs->insertNanopub($artifactCode, $creationTime);
			print_r($result);
			break;
		default:
			break;
	}
}
if ($table == "statements"){
	require_once("statementsModel.php");
	$statementsObj = new statements($conn);

	switch ($function) {
		case "insert": {
			$artifactCode = $data[0];
			$hashCode = $data[1];
			$object = $data[2];
			$predicate = $data[3];
			$subject = $data[4];
			$sectionID = $data[5];
			$result = $statementsObj->insert($artifactCode, $hashCode, $object, $predicate, $subject, $sectionID);
			print_r($result);
			break;
		}
		case "getByHashCode": {
			$hashCode = $data[0];
			$result = $statementsObj->getByHashCode($hashCode);
			print_r($result);
		}
	}
}

?>