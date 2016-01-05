<html>
<head>
<title> Search for nanopublications </title>
</head>
<body>
<form method="GET" action="index.php">
	<h1> search for nanopubs </h1>
	<p>
		URI:<input type="text" name="uri" /><br />
	</p>

	<input type="submit" value="Search" />
</form>
</body>
</html>

<?php
require_once("_getnanopub.php");
?>