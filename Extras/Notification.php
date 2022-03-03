<?php
class Notification{
//*****************************************************************************************************
//*************************************   class to send push notifications
//*****************************************************************************************************
	function sendNotificationByTokens($title, $message, $tokens, $orderId, $status){
		$path_to_firebase_cm = 'https://fcm.googleapis.com/fcm/send'; 
		
		$fields = array(
            'registration_ids' => $tokens,
			'data' => array('title' => $title , 'body' => $message,
			'icon'=> 'ic_stat_name',
			'sound'=> 'default',
			'color'=> '#F44336',
			'tag'=>'nilo_notify_tag',
			'image'=>'https://miro.medium.com/max/724/1*JbDo7U0l62vYMfm1WxnA1g.png',
			'click_action'=> 'OPEN_ORDER',
			'action_intent'=> '1',
			'id'=> $orderId,
			'status'=> $status),
			'time_to_live' => 604800
			);

        $headers = array('Authorization:key=[your key of proyect in firebase/delete brackets]',
            'Content-Type:application/json'
        );
		
		$ch = curl_init();
		
		curl_setopt($ch, CURLOPT_URL, $path_to_firebase_cm);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_IPRESOLVE, CURL_IPRESOLVE_V4 );
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
		
        $result = curl_exec($ch);

        if(!$result) {
          $response["success"]=100;
          $response["Error"]='Error: "' . curl_error($ch) . '" - Code: ' . curl_errno($ch);
        } else {
          $response["success"]=3;
          $response["StatusCode"]= curl_getinfo($ch, CURLINFO_HTTP_CODE);
          $response["message"]='Notificación enviada correctamente.';
          $response["Response HTTP Body"]= " - " .$result ." -";
        }

		curl_close($ch);

        return ($response);
	}
	

		function sendNotificationByTopics($title, $message, $topic, $photoUrl){
		$path_to_firebase_cm = 'https://fcm.googleapis.com/fcm/send'; 
		
		$fields = array(
            'to' => "/topics/$topic",
			'notification' => array(
			'title' => $title ,
			'body' => $message,
			'icon'=> 'ic_stat_name',
			'sound'=> 'default',
			'color'=> '#F44336',
			'tag'=>'nilo_notify_tag',
			'image' => $photoUrl,
			'click_action'=> 'OPEN_ORDER'),
			'time_to_live' => 604800
			
			);

        $headers = array('Authorization:key=[your key of proyect in firebase/delete brackets]',
            'Content-Type:application/json'
        );
		
		$ch = curl_init();
		
		curl_setopt($ch, CURLOPT_URL, $path_to_firebase_cm);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_IPRESOLVE, CURL_IPRESOLVE_V4 );
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
		
        $result = curl_exec($ch);

        if(!$result) {
          $response["success"]=100;
          $response["Error"]='Error: "' . curl_error($ch) . '" - Code: ' . curl_errno($ch);
        } else {
          $response["success"]=3;
          $response["StatusCode"]= curl_getinfo($ch, CURLINFO_HTTP_CODE);
          $response["message"]='Notificación enviada correctamente.';
          $response["Response HTTP Body"]= " - " .$result ." -";
        }

		curl_close($ch);

        return ($response);
	}
}
?>
