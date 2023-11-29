<?php
if(isset($_POST['user']) && isset($_POST['vehicle'])){
    require_once "conn.php"; // Include your database connection here

    $user = $_POST['user'];
    $vehicle = $_POST['vehicle'];
    $stmt = $conn->prepare("SELECT * FROM vehicle_details WHERE vehicle = ?  LIMIT 1");
    $stmt->bind_param("s", $vehicle);
    $stmt->execute();
    $result1 = $stmt->get_result();

    if($result1 -> num_rows == 1) {
        $sqlCheck = "SELECT startkm FROM pro WHERE user = ? AND model = ? AND in_use = 1";
        $stmtCheck = $conn->prepare($sqlCheck);
        $stmtCheck->bind_param("ss", $user, $vehicle);   
        $stmtCheck->execute();
        $result = $stmtCheck->get_result();
        
        if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $start = $row['startkm'];
         echo json_encode(array("status" => "end", "user" => "$user", "model" => "$vehicle","startkm"=>"$start" ));    
        } else {
            $maxSQL = "SELECT MAX(endkm) FROM pro WHERE model = ?";
            $stmtMax = $conn->prepare($maxSQL);
            $stmtMax->bind_param("s", $vehicle);
            $stmtMax->execute();
            $maxResult = $stmtMax->get_result();
            $maxRow = $maxResult->fetch_assoc();
            $maxendKm = $maxRow['MAX(endkm)'];
      if($maxendKm == ""){
    $maxendKm =0;
      }
            echo json_encode(array("status" => "start", "user" => "$user", "model" => "$vehicle","max_end_km" => "$maxendKm" ));   
            $stmtMax->close();
        }
        $stmtCheck->close(); 
         
        }
        else {
            echo json_encode(array("status" => "vehiclenotfound", "message" => "Vehicle not found"));
        }
    $stmt->close();
}

?>
