<html>
<head>
<title> Search for nanopublications </title>
</head>
<body>
<form method="GET" action="index.php">
	<input type="text" name="table" value="statements"  style="display:none;" />
	<input type="text" name="function" value="getNanopub" style="display:none;" />
	<h1> search for nanopubs </h1>
	<p>
		Hashcode:<input type="text" name="data[]" /><br />
		Object:<input type="text" name="data[]" /><br />
		Predicate:<input type="text" name="data[]" /><br />
		Subject:<input type="text" name="data[]" /><br />
	</p>
	<hr />
	<p>
		<input type="checkbox" name="head" /> Head<br />
		<input type="checkbox" name="assertion" /> Assertion<br />
		<input type="checkbox" name="provenance" /> Provenance<br />
		<input type="checkbox" name="pubinfo" /> Pubinfo<br />
	</p>

	<input type="submit" value="Search" />
</form>
</body>
</html>

<?php
require_once("_getnanopub.php");
?>