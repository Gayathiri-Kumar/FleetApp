<?php
if(isset($_POST['emp_id']) && isset($_POST['password'])){
    require_once "conn.php"; // Include your database connection here

    $emp_id = $_POST['emp_id'];
    $password = $_POST['password'];

    $stmtAdmin = $conn->prepare("SELECT * FROM admin WHERE username = ? LIMIT 1");
    $stmtAdmin->bind_param("s", $emp_id);
    $stmtAdmin->execute();
    $resultAdmin = $stmtAdmin->get_result();

    $stmtDriver = $conn->prepare("SELECT * FROM drivers WHERE emp_id = ? LIMIT 1");
    $stmtDriver->bind_param("s", $emp_id);
    $stmtDriver->execute();
    $resultDriver = $stmtDriver->get_result();

    if ($resultAdmin->num_rows == 1) {
        // User found in admin table
        $adminData = $resultAdmin->fetch_assoc();

        if ($adminData['password'] == $password) {
            // Password is correct
            $user_name = $adminData['name']; // Assuming the name column is named 'name'
            echo json_encode(array("status" => "admin", "name" => $user_name));
        } else {
            // Incorrect password for admin
            echo json_encode(array("status" => "incorrect_password", "message" => "Incorrect Admin Password"));
        }
    } elseif ($resultDriver->num_rows == 1) {
        // User found in drivers table
        $driverData = $resultDriver->fetch_assoc();

        if ($driverData['password'] == $password) {
            // Password is correct
            $user_name = $driverData['name']; // Assuming the name column is named 'name'
            echo json_encode(array("status" => "success", "name" => $user_name));
        } else {
            // Incorrect password for driver
            echo json_encode(array("status" => "incorrect_password", "message" => "Incorrect Driver Password"));
        }
    } else {
        // User not registered in both admin and drivers tables
        echo json_encode(array("status" => "notregistered", "message" => "User Not Registered"));
    }

    $stmtAdmin->close();
    $stmtDriver->close();
    $conn->close();
} else {
    echo json_encode(array("status" => "invalid_data", "message" => "Invalid data"));
}
?>
