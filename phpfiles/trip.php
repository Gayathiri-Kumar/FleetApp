<?php
include "conn.php";

if (isset($_POST['startkm']) && isset($_POST['model']) && isset($_POST['user'])) {
    $startkm = $_POST['startkm'];
    $model = $_POST['model'];
    $user = $_POST['user'];

    // Get the original name of the uploaded image
    $image_name = $_FILES['startimg']['name'];

    $target_dir = "uploads/";

    // Get the file extension
    $file_extension = pathinfo($image_name, PATHINFO_EXTENSION);

    // Generate a unique image name with the original extension
    $image_name_with_extension = uniqid() . '.' . $file_extension;

    $target_file = $target_dir . $image_name_with_extension;

    // Move the uploaded file to the target directory
    if (move_uploaded_file($_FILES['startimg']['tmp_name'], $target_file)) {
        // Use prepared statement to insert data into the database
        $sql = "INSERT INTO pro (user, model, startkm, startimg, start_time, in_use) VALUES (?, ?, ?, ?, NOW(), 1)";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ssss", $user, $model, $startkm, $image_name_with_extension);

        if ($stmt->execute()) {
            echo json_encode(array("status" => "success"));  
        } else {
            echo json_encode(array("status" => "failed"));  
        }

        $stmt->close();
    } else {
        echo "Error uploading file";
    }
}

if (isset($_POST['endkm']) && isset($_POST['place']) && isset($_POST['user']) && isset($_POST['passengers'])) {
    $endkm = $_POST['endkm'];
    $place = $_POST['place'];
    $model = $_POST['model'];
    $user = $_POST['user'];
    $passengers = $_POST['passengers'];
    // Get the original name of the uploaded image
    $image_name = $_FILES['endimg']['name'];

    $target_dir = "uploads/";

    // Get the file extension
    $file_extension = pathinfo($image_name, PATHINFO_EXTENSION);

    // Generate a unique image name with the original extension
    $image_name_with_extension = uniqid() . '.' . $file_extension;

    $target_file = $target_dir . $image_name_with_extension;

    // Move the uploaded file to the target directory
    if (move_uploaded_file($_FILES['endimg']['tmp_name'], $target_file)) {
     
        $sql = "UPDATE pro SET `place`=?, `endkm` = ?, `endimg` = ?, `in_use` = 2, end_time = NOW(), `passengers` = ? WHERE `user`= ? AND `model` = ? AND `in_use` = 1";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ssssss", $place, $endkm, $image_name_with_extension, $passengers, $user, $model);
     
        if ($stmt->execute()) {
            echo json_encode(array("status" => "success"));  
        } else {
            echo json_encode(array("status" => "failed", "message" => "failed"));  
        }

        $stmt->close();
    } else {
        echo "Error uploading file";
    }
} 
?>
