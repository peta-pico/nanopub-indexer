<?php

require_once("connectDatabase.php");

if (!$_GET){
	error_return("No request");
}

if (!$_GET['uri']){
	error_return("Wrong request");
}

$uri = $_GET['uri'];

//GOT THROUGH ALL ERROR CHECKS
require_once("uriModel.php");
$uriObj = new URIs($conn);

$result = $uriObj -> getArtifactCodes($uri);
print_r($result);


function error_return($msg){
	echo $msg;
	die();
}