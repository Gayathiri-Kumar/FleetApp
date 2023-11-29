<?php
include 'conn.php';

$emp_id = $_POST['name'];
$new_password = $_POST['new_password'];
$old_password = $_POST['old_password'];

$old_password_query = "SELECT password FROM drivers WHERE name = '$emp_id'";
$result = $conn->query($old_password_query);

if ($result) {

    if ($result->num_rows === 1) {
        $row = $result->fetch_assoc();

        if ($row['password'] == $old_password) {

            $update_query = "UPDATE drivers SET password ='$new_password' WHERE name = '$emp_id'";
            $update_result = $conn->query($update_query);

            if ($update_result) {
                echo json_encode(array("status" => "success"));
            } else {
                echo json_encode(array("status" => "failed", "message" => $conn->error()));
            }
        } else {
            echo json_encode(array("status" => "failed", "message" => "Old password is incorrect"));
        }
    } else {
        echo json_encode(array("status" => "failed", "message" => "Invalid employee id"));
    }
} else {
    echo json_encode(array("status" => "failed", "message" => $conn->error()));
}
?>
