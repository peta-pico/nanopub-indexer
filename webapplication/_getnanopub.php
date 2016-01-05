<?php 

if (!$_GET){
	error_return("No request");
}

if (!$_GET['uri']){
	error_return("Wrong request");
}

$uri = urlencode($_GET['uri']);

$sections = ['HEAD', 'ASSERTION', 'PROVENANCE', 'PUBINFO'];
$peerlist = ['http://np.inn.ac/', 'http://nanopubs.stanford.edu/nanopub-server/', 'http://rdf.disgenet.org/nanopub-server/', 'http://ristretto.med.yale.edu:8080/nanopub-server/'];


$url = "http://localhost/nanopubs/database/api.php?uri=" . $uri;

$result = file_get_contents($url);
$result = json_decode($result, true);

foreach ($result as $item){
	echo $item['artifactCode'] . "<br />";
}


function error_return($msg){
	echo $msg;
	die();
}