<html>
<head>
<title>Nanopublication Search</title>
<link rel="stylesheet" href="plain.css" type="text/css" media="screen" title="Stylesheet" />
</head>
<body>
<form method="GET" action="database/api.php">

<h1>Nanopublication Search</h1>

<p>
URIs (one URI per line): <br/><textarea rows="10" cols="100" name="search-uri" /></textarea><br/>
<table>
<tr><td>Page:</td><td><input type="number" value="1" min="1" max="10000000" name="page"/></td></tr>
<tr><td>Begin timestamp:</td><td><input type="number" name="begin_timestamp"/></td></tr>
<tr><td>End timestamp:</td><td><input type="number" name="end_timestamp"/></td></tr>
<tr><td>Order:</td><td><input type="radio" name="order" value="0"/>unordered
  <input type="radio" name="order" value="1" checked/>descending by timestamp
  <input type="radio" name="order" value="2"/>ascending by timestamp</td></tr>
<tr><td>Graphs:</td><td><input type="checkbox" name="head" checked />Head
  <input type="checkbox" name="assertion" checked />Assertion
  <input type="checkbox" name="provenance" checked />Provenance
  <input type="checkbox" name="pubinfo" checked />Pubinfo</td></tr>
<tr><td>Return format:</td><td><input type="radio" name="format" value="text" required checked />text
  <input type="radio" name="format" value="json" required />JSON
  <input type="radio" name="format" value="link" required />link</td></tr>
</table>
</p>

<input type="submit" value="Search" />
</form>

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