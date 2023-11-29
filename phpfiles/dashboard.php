<?php
if(isset($_POST['user'])) {
    require_once "conn.php"; 
    $user = $_POST['user'];
    
    // Use a prepared statement to fetch data
    $stmt = $conn->prepare("SELECT user, model, startkm, endkm, 
        start_time, end_time ,
        place FROM pro WHERE user = ? ORDER BY id DESC");
    $stmt->bind_param("s", $user);
    $stmt->execute();
    $result = $stmt->get_result();

    // Initialize an empty array to store the results
    $data = array();

    // Check if there are any results
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
        
        $row['end_time'] = date('d M Y h:i A' ,strtotime($row['end_time']));
            $data['data'][] = $row;
        }
        $data['status'] = 'success';
    } else {
        $data['status'] = 'noData';
    }

    $stmt->close();
    $conn->close();

    // Ensure no output before setting the header
    ob_clean();

    header('Content-Type: application/json; charset=UTF-8');
    echo json_encode($data);
}
?>
