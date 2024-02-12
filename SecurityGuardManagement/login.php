<?php
// Assuming your database connection details
$host = "localhost";
$username = "root";
$password = "";
$database = "fyp2";

// Establish database connection
$conn = mysqli_connect($host, $username, $password, $database);

// Check connection
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}

$jsonData = file_get_contents('php://input');

// Decode the JSON data
$data = json_decode($jsonData);

// Retrieve login credentials from the HTTP request
$guardId = isset($data->guard_id) ? $data->guard_id : null;
$password = isset($data->password) ? $data->password : null;

// Validate login credentials
$query = "SELECT * FROM guard WHERE guard_id='$guardId' AND password='$password'";
$result = mysqli_query($conn, $query);

if (mysqli_num_rows($result) > 0) {
    // Login successful
    echo "Login successful";
} else {
    // Login failed
    echo "Invalid credentials";
}

// Close the database connection
mysqli_close($conn);
?>
