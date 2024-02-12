<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "fyp2";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$jsonData = file_get_contents("php://input");

// Decode the JSON data
$data = json_decode($jsonData);

// Get the guardId, datetime, and action from the decoded JSON data
$guardId = isset($data->guardId) ? $data->guardId : null;
$datetime = isset($data->datetime) ? $data->datetime : null;
$action = isset($data->action) ? $data->action : null;

// Check if the row exists for the given guardId
$sql_check = "SELECT * FROM gps_tracking WHERE guard_id = '$guardId'";

$result_check = $conn->query($sql_check);

if ($result_check !== false && $result_check->num_rows > 0) {
    // Row exists, perform the update
    if ($action === 'start') {
        $sql = "UPDATE gps_tracking SET start_time = COALESCE(start_time, '$datetime') WHERE guard_id = '$guardId'";
    } elseif ($action === 'stop') {
        $sql = "UPDATE gps_tracking SET stop_time = COALESCE(stop_time, '$datetime') WHERE guard_id = '$guardId'";
    }

    if ($conn->query($sql) === TRUE) {
        echo "Data inserted/updated successfully";
    } else {
        echo "Error: " . $sql . "<br>" . $conn->error;
    }
} else {
    echo "Error: No existing record for guardId";
}

$conn->close();
?>
