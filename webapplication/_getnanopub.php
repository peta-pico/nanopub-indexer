<?php 

if (!$_GET || !$_GET['function'] || !$_GET['data']){
	die();
}

$sections = ['HEAD', 'ASSERTION', 'PROVENANCE', 'PUBINFO'];
$peerlist = ['http://np.inn.ac/', 'http://nanopubs.stanford.edu/nanopub-server/', 'http://rdf.disgenet.org/nanopub-server/', 'http://ristretto.med.yale.edu:8080/nanopub-server/'];

$hashCode = $_GET['data'][0];
$object = urlencode($_GET['data'][1]);
$predicate = urlencode($_GET['data'][2]);
$subject = urlencode($_GET['data'][3]);
$head = !empty($_GET['head']) ? $_GET['head'] : "";
$assertion = !empty($_GET['assertion']) ? $_GET['assertion'] : "";
$provenance = !empty($_GET['provenance']) ? $_GET['provenance'] : "";
$pubinfo = !empty($_GET['pubinfo']) ? $_GET['pubinfo'] : "";

$url = "http://localhost/nanopubs/database/api.php?table=statements&function=getNanopub&data%5B%5D="
	.$hashCode."&data%5B%5D="
	.$object."&data%5B%5D="
	.$predicate."&data%5B%5D="
	.$subject."&data%5B%5D="
	.$head."&data%5B%5D="
	.$assertion."&data%5B%5D="
	.$provenance."&data%5B%5D="
	.$pubinfo;

$result = file_get_contents($url);

echo "<hr />";
echo "hashCode: " . $hashCode . "<br/ >";
echo "Object: " . $object . "<br/ >";
echo "predicate: " . $predicate . "<br/ >";
echo "subject: " . $subject . "<br/ >";

$result = json_decode($result, true);
foreach ($result as $item){
	echo "<h3>" . $item['artifactCode'] . "</h3>";
	echo $sections[$item['sectionID']-1];
	echo "<p>
			object: " . $item['object'] . "<br />
			predicate: " . $item['predicate'] . "<br />
			subject: " . $item['subject'] . "<br />
			hashCode: " . $item['hashCode'] . "<br />
		</p>";

	for ($i = 0; $i < count($peerlist); $i ++){
		echo " <a href='" . $peerlist[$i] ."". $item['artifactCode'] . "'>Server " . ($i+1) . "</a> ";	
	}
	
}
?>