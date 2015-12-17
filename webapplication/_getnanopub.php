<?php 

if (!$_GET || $_GET['function']){
	die();
}

http_get("localhost/nanopubs/database/api.php?table=statements&function=getNanopub&data%5B%5D=");

?>