<?php
require_once("database/connectDatabase.php");
require_once("database/indexesModel.php");

$indexesObj = new indexes($conn);
$indexesArray = $indexesObj->getIndexes();
?>

<html>
<head>
<title> List of indexes </title>
</head>
<body>
<table>
	<tr>
		<th>Dataset</th>
		<th>Indexes</th>
		<th>Content</th>
	</tr>
	<?php foreach ($indexesArray as $indexItem) {?>
	<tr>
		<td><?php echo htmlspecialchars($indexItem['title']); ?></td>
		<td><?php echo $indexItem['indexCount']; ?></td>
		<td><?php echo $indexItem['contentCount']; ?></td>
	</tr>
	<?php } ?>
</table>
</body>
</html>