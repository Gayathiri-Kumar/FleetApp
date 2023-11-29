<?php
require_once "conn.php";

// Use a prepared statement to fetch data
$stmt = $conn->prepare("SELECT vehicle FROM vehicle_details");

if (!$stmt) {
    // Handle error if the statement preparation fails
    $data['status'] = 'error';
    $data['message'] = $conn->error;
} else {
    $stmt->execute();
    $result = $stmt->get_result();

    // Initialize an empty array to store the results
    $data = array();

    // Check if there are any results
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            // Append each row to the $data array
            $data['data'][] = $row;
        }
        $data['status'] = 'success';
    } else {
        $data['status'] = 'noData';
    }

    $stmt->close();
}

$conn->close();

header('Content-Type: application/json; charset=UTF-8');
echo json_encode($data);
?>
