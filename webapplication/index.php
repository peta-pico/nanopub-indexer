<html>
<head>
<title>Nanopublication Search</title>
</head>
<body>
<form method="GET" action="database/api.php">
	<h1>Nanopublication Search</h1>
	<p>
		URI: (separate multiple URIs with a newline) <br/><textarea rows="10" cols="100" name="search-uri" /></textarea><br />
		Page: (0 for no pages) <input type="number" value="1" min="0" max="10000000" name="page"/><br/><br/>
		<input type="number" name="begin_timestamp"/> Begin timestamp <br/>
		<input type="number" name="end_timestamp"/> End timestamp <br/>
		<input type="radio" name="order" value="0"/>unordered<br/>
		<input type="radio" name="order" value="1" checked/>descending order by timestamp<br/>
		<input type="radio" name="order" value="2"/>ascending order by timestamp
		<br/><br/>
		
		<input type="checkbox" name="head" checked />Head<br />
		<input type="checkbox" name="assertion" checked />Assertion<br />
		<input type="checkbox" name="provenance" checked />Provenance<br />
		<input type="checkbox" name="pubinfo" checked />Pubinfo<br />
		
		<input type="radio" name="format" value="text" required checked />text<br />
		<input type="radio" name="format" value="json" required />JSON<br />
		<input type="radio" name="format" value="link" required />link<br />


		
	</p>

	<input type="submit" value="Search" />
</form>	
<hr />
<h3><a href="indexes.php">Indexes</a></h3>


<!--
<form method="GET" action="application.php">
	<h1> Advanced search </h2>
	<p>URI list: (seperate with a newline)</p>
	<textarea rows="10" cols="50" name="search-uri"></textarea><br/>
	<input type="submit" value="Search" />
</form>
-->
</body>
</html>

<?php
//require_once("_getnanopub.php");
?>