<?php
	header("Access-Control-Allow-Origin: *");
	header("Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept");

	$data = json_decode(file_get_contents('php://input'), true);

	require('Notification.php');
    $topic=$data["topic"];
	$method = $data["method"];
	$title = $data["title"];
	$message = $data["message"];
	$tokens = explode(",", $data["tokens"]);
	$photoUrl = $data["image"];
	$orderId = $data["id"];
	$status = $data["status"];
	
	//Código de pruebas hardcodeado
//	$topic="topic_offers";
//	$method = "sendNotificationByTopics";
//	$title = "From Server";
//	$message = "Custom Message: 
//¿Qué es Lorem Ipsum?
//Lorem Ipsum es simplemente el texto de relleno de las imprentas y archivos de texto. Lorem Ipsum ha sido el texto de relleno estándar de las industrias desde el año 1500, cuando un impresor (N. del T. persona que se dedica a la imprenta) desconocido usó una galería de textos y los mezcló de tal manera que logró hacer un libro de textos especimen.";
//	$tokens = explode(",", "[your token of device/delete brackets]");
	
	$length = count($tokens);
	
/*	for($i=0; $i< $length; $i++){
		echo $tokens[$i];
		echo "<br>";
		echo "<br>";
	}
	*/
	
	//***********************************************************************************************************
	//*****   AQUI INICIA LA DEFINICIÓN DE FUNCIONES QUE A SU VEZ ACCEDERAN A LOS MÉTODOS DEL OBJETO NOTIFICATION
	//***********************************************************************************************************
	
	function sendNotification($title, $message, $tokens, $orderId, $status){
		$notification = new Notification();
		$response=$notification->sendNotificationByTokens($title, $message, $tokens,$orderId, $status);
		
		return $response;
	}
		function sendNotificationByTopics($title, $message, $topic, $image){
		$notification = new Notification();
		$response=$notification->sendNotificationByTopics($title, $message, $topic, $image);
		
		return $response;
	}
	//************************************************************************************************************
	//*****   SWICH UTILIZADO PARA FILTRAR Y MANDAR A LLAMAR EL MÉTODO CORRESPONDIENTE
	//************************************************************************************************************
	switch ($method) {
		case "sendNotification":{
			$response=sendNotification($title, $message, $tokens, $orderId, $status);
			break;
		}
		case "sendNotificationByTopics":{
		    $response=sendNotificationByTopics($title, $message, $topic, $photoUrl);
		    break;
		}
			
		default:{
			$response["success"]=104;
			$response["message"]='El método indicado no se encuentra registrado';
		}
	}
	
	echo json_encode ($response)
?>
