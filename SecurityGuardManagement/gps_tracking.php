<?php

// Replace these values with your actual database credentials
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

// Get raw JSON data from the request body
$jsonData = file_get_contents('php://input');

// Decode the JSON data
$data = json_decode($jsonData);

// Get data from the request (either POST or GET)
$guardId = isset($data->guardId) ? $data->guardId : null;
$latitude = isset($data->latitude) ? $data->latitude : null;
$longitude = isset($data->longitude) ? $data->longitude : null;


// Check if guardId exists in the table
$checkQuery = "SELECT * FROM gps_tracking WHERE guard_id = '$guardId'";
$result = $conn->query($checkQuery);

if ($result->num_rows > 0) {
    // guardId exists, perform an UPDATE
    $updateQuery = "UPDATE gps_tracking SET latitude='$latitude', longitude='$longitude', start_lat = COALESCE(start_lat, '$latitude'), start_long = COALESCE(start_long, '$longitude') WHERE guard_id = '$guardId'";
    
    if ($conn->query($updateQuery) === TRUE) {
        echo "Data updated successfully";
    } else {
        echo "Error updating record: " . $conn->error;
    }
} else {
    // guardId does not exist, perform an INSERT
    $insertQuery = "INSERT INTO gps_tracking (guard_id, latitude, longitude, start_lat, start_long) 
                VALUES ('$guardId', '$latitude', '$longitude', COALESCE(start_lat, '$latitude'), COALESCE(start_long, '$longitude'))";


    if ($conn->query($insertQuery) === TRUE) {
        echo "Data inserted successfully";
    } else {
        echo "Error inserting record: " . $conn->error;
    }
}

// Close the database connection
$conn->close();

?>
