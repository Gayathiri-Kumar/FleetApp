<?php

include 'conn.php';
$name = $_POST['name'];
$emp_id= $_POST['emp_id'];
$password = $_POST['password'];
$sql_check = "SELECT * FROM drivers WHERE emp_id = ?";
$stmt = $conn->prepare($sql_check);
$stmt->bind_param("s", $emp_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    echo "Employee Id already exists";
} else {
    $sql_insert = "INSERT INTO drivers (emp_id, name, password) VALUES (?, ?, ?)";
    $stmt = $conn->prepare($sql_insert);
    $stmt->bind_param("sss", $emp_id, $name, $password);

    if ($stmt->execute()) {
        echo "Registration successful";
    } else {
        echo "Error: " . $stmt->error;
    }
}

// Close the database connection
$conn->close();

?>