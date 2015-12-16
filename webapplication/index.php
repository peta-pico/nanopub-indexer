<html>
<head>
<title> Search for nanopublications </title>
</head>
<body>
<form method="GET" action="database/api.php">
	Table:
	<input type="text" name="table" value="statements"  /><br/>
	Function:
	<input type="text" name="function" value="getByHashCode"  />
	<br />
	<hr />
	Hashcode:<input type="text" name="data[]" /><br />
	<hr />
	Search in 
	<p>
	Head <input type="checkbox" name="head" /><br />
	Assertion <input type="checkbox" name="assertion" /><br />
	Provenance <input type="checkbox" name="provenance" /><br />
	Pubinfo <input type="checkbox" name="pubinfo" /><br />
	
	</p>

	<input type="submit" value="Search" />
</form>
</body>
</html>