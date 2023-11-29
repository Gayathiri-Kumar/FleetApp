<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    require_once "conn.php"; 
    $model = $_POST['model'];
    $month = $_POST['month'];
    
    $stmt = $conn->prepare("SELECT user, SUM(Tkm) as Tkm FROM `pro` WHERE  MONTHNAME(end_time)=? AND  model = ? GROUP BY user ORDER BY Tkm ASC");

    $stmt->bind_param("ss", $month, $model);
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
    $data['model'] = $model;
    $data['month'] = $month;

    $stmt->close();
    $conn->close();

    header('Content-Type: application/json; charset=UTF-8');
    echo json_encode($data);
}