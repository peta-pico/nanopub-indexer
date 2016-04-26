<html>
<head>
<title> Search for nanopublications </title>
</head>
<body>
<form method="GET" action="database/api.php">
	<h1> search for nanopubs </h1>
	<p>
		URI:<input type="text" name="search-uri" /><br />
		
		<input type="checkbox" name="head" checked />Head<br />
		<input type="checkbox" name="assertion" checked />Assertion<br />
		<input type="checkbox" name="provenance" checked />Provenance<br />
		<input type="checkbox" name="pubinfo" checked />Pubinfo<br />
		
		<input type="radio" name="format" value="text" required checked />text<br />
		<input type="radio" name="format" value="json" required />JSON<br />
	</p>

	<input type="submit" value="Search" />
</form>	
	<hr />
<form method="GET" action="application.php">
	<h1> Advanced search </h2>
	<p>URI list: (seperate with a newline)</p>
	<textarea rows="10" cols="50" name="search-uri"></textarea><br/>
	<input type="submit" value="Search" />
</form>
</body>
</html>

<?php
//require_once("_getnanopub.php");
?>