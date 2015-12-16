<?php
require_once("database/connectDatabase.php");
require_once("database/nanopubsModel.php");
$val = rand(0, 100000000);
echo $val . "<br />";	

//+++++++++ NANOPUBS MODEL ++++++++++++++
$nanopubs = new nanopubs($conn);

//TEST INSERT VIA MODEL
//$result = $nanopubs->insertNanopub($val, $val, $val, $val, $val, $val, $val);
//print_r($result);

//TEST INSERT VIA API
echo 
	'<a href="database/api.php?table=nanopubs
		&function=insertNanopub&data[]='.$val.'&data[]='.$val.'&data[]='.$val.'&data[]='.$val.'&data[]='.$val.'&data[]='.$val.'&data[]='.$val.'">
			link
	</a>';

//TEST SELECT

?>
