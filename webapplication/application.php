<?php

if (!$_GET){
	printerror();
}

if (!$_GET['search-uri']){
	printerror();
}

$searchuri = trim($_GET['search-uri']);
$searchuriarray = explode("\n", $searchuri);

$basepath = "http://" . $_SERVER['HTTP_HOST'] . pathinfo($_SERVER["PHP_SELF"])['dirname'];

$uri = $searchuriarray[0];
$result = json_decode(trim(file_get_contents($basepath . '/database/api.php?format=json&search-uri=' . $uri)), true);
foreach ($searchuriarray as $uri){
	$url = $basepath . '/database/api.php?format=json&search-uri=' . trim($uri);
	$tempresult = json_decode(trim(file_get_contents($url)), true);
	$result = array_intersect($result, $tempresult);
}

printoutput($result);

function printoutput($obj){
	$obj = json_encode($obj);
	print_r($obj);
}

function errormessage(){
	$obj = array();
	$obj['error'] = true;

	return $obj;
}

function printerror(){
	$error =  errormessage();
	printoutput($error);
	die ();
}
?>