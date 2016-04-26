<?php
error_reporting(E_ALL & ~E_NOTICE);

$SUPPORTED_FORMATS = array("text", "json");
$RADIO_VALUES = array("on", "off");

// INIT
$format = "text";
$head = "off";
$assertion = "off";
$provenance = "off";
$pubinfo = "off";

require_once("connectDatabase.php");


// ==================== ERROR CHECKING ====================
// EMPTY REQUEST
if (!$_GET){
	error_return("Invalid request");
}

// INVALID URI
if (!$_GET['search-uri']){
	error_return("Invalid search-uri");
}

// CHECK FORMAT
if ($_GET['format']){
	$format = strtolower($_GET['format']);
}
if (!in_array($format, $SUPPORTED_FORMATS, true)){
	error_return("Invalid format");
}

// CHECK URI TYPES
if ($_GET['head']){
	$head = strtolower($_GET['head']);
}
if (!in_array($head, $RADIO_VALUES, true)){
	error_return("invalid head value");
}
if ($_GET['assertion']){
	$assertion = strtolower($_GET['assertion']);
}
if (!in_array($head, $RADIO_VALUES, true)){
	error_return("invalid assertion value");
}
if ($_GET['provenance']){
	$provenance = strtolower($_GET['provenance']);
}
if (!in_array($head, $RADIO_VALUES, true)){
	error_return("invalid provenance value");
}
if ($_GET['pubinfo']){
	$pubinfo = strtolower($_GET['pubinfo']);
}
if (!in_array($head, $RADIO_VALUES, true)){
	error_return("invalid pubinfo value");
}
// ==================== DONE ERROR CHECKING ====================


// START PROGRAM
require_once("uriModel.php");

// INIT
$uri = $_GET['search-uri'];
$uriObj = new URIs($conn);

// GET RESULTS
$result = $uriObj -> getArtifactCodes($uri, $head, $assertion, $provenance, $pubinfo);

// DISPlAY RESULTS
if ($format == "json"){
	returnJSON($result);
}
else if ($format == "text") {
	returnText($result);
}
else if ($format == "xml"){
	returnXML($result);
}
else {
	error_return("invalid format");
}

function returnJSON($data){
	print_r($data);
}

function returnText($data){
	$arraydata = json_decode($data, true);
	foreach ($arraydata as $item){
		echo $item . "<br/>";
	}	
}

function returnXML($data){
	returnJSON($data);
}

function error_return($msg){
	echo $msg;
	die();
}