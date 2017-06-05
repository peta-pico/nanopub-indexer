<?php
error_reporting(E_ALL & ~E_NOTICE);

$SUPPORTED_FORMATS = array("text", "json", "html");
$RADIO_VALUES = array("on", "off");

// INIT
$format = "text";
$head = "off";
$assertion = "off";
$provenance = "off";
$pubinfo = "off";
$begin_timestamp = NULL;
$end_timestamp = NULL;
$order = 0;
$debug = false;

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

// PAGE
$page = 1;
if ($_GET['page']){
	if ($_GET['page'] > 0){
		$page = $_GET['page'];
	}
	else {
		error_return("invalid page value");
	}
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

// TIMESTAMPS
if ($_GET['begin_timestamp']){
	$begin_timestamp = $_GET['begin_timestamp'];
}
if ($_GET['end_timestamp']){
	$end_timestamp = $_GET['end_timestamp'];
}
if ($_GET['order']){
	$o = $_GET['order'];
	if ($o == 0 || $o == 1 || $o == 2) {
		$order = $o;
	}
}

if ($_GET['debug']){
	$debug = true;
}

// ==================== DONE ERROR CHECKING ====================


// START PROGRAM
require_once("uriModel.php");

// INIT
$uri = trim($_GET['search-uri']);
$uriArray = array_map('trim', explode("\n", $uri));

$uriObj = new URIs($conn);

// GET RESULTS
$result = $uriObj -> getArtifactCodes($uriArray, $head, $assertion, $provenance, $pubinfo, $page, $begin_timestamp, $end_timestamp, $order, $debug);

// DISPlAY RESULTS
if ($format == "json"){
	returnJSON($result);
}
else if ($format == "text") {
	returnText($result);
}
else if ($format == "html") {
	returnHtml($result);
}
else {
	error_return("invalid format");
}

function returnJSON($data){
	header('Content-Type: application/json');
	print_r($data);
}

function returnText($data){
	header('Content-Type: text/plain');
	$arraydata = json_decode($data, true);
	foreach ($arraydata as $item){
		echo $item . PHP_EOL;
	}
}

function returnHtml($data){
	header('Content-Type: text/html');
	echo "<!DOCTYPE html>";
	echo "<html><head><title>Results</title></head><body><ul>";
	$arraydata = json_decode($data, true);
	foreach ($arraydata as $item){
		echo "<li><a href=" . $item . ">" . $item . "</a></li>";
	}
	echo "</ul></body></html>";
}

function error_return($msg){
	echo $msg;
	die();
}