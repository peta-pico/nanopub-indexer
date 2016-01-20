<html>
<head>
<title> Search for nanopublications </title>
</head>
<body>
<form method="GET" action="database/api.php">
	<h1> search for nanopubs </h1>
	<p>
		URI:<input type="text" name="search-uri" /><br />
		<hr />
		<input type="checkbox" name="head" checked />Head<br />
		<input type="checkbox" name="assertion" checked />Assertion<br />
		<input type="checkbox" name="provenance" checked />Provenance<br />
		<input type="checkbox" name="pubinfo" checked />Pubinfo<br />
		<hr />
		<input type="radio" name="format" value="text" required checked />text<br />
		<input type="radio" name="format" value="json" required />JSON<br />
		<input type="radio" name="format" value="xml" required disabled />XML<br />
	</p>

	<input type="submit" value="Search" />
</form>
</body>
</html>

<?php
//require_once("_getnanopub.php");
?>