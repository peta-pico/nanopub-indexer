<?php
error_reporting(0);

require_once("connectDatabase.php");

if (!$_GET){
	error_return("Invalid request");
}

if (!$_GET['search-uri']){
	error_return("Invalid request");
}

if ($_GET['format']){
	$format = $_GET['format'];
}
else {
	$format = "list";
}

$uri = $_GET['search-uri'];


//GOT THROUGH ALL ERROR CHECKS
require_once("uriModel.php");
$uriObj = new URIs($conn);

$result = $uriObj -> getArtifactCodes($uri);

if ($format == "JSON" || $format == "json"){
	print_r($result);
}

else {
	foreach (json_decode($result, true) as $item){
		echo $item['artifactCode'] . "<br/>";
	}
}

function error_return($msg){
	echo $msg;
	die();
}