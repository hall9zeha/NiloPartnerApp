# Nilo: Aplicación de ventas para android usando Firebase
 
Aplicación de ventas orientada al vendedor/administrador y al  cliente,  desarrollada en Android Studio usando el lenguaje kotlin. La finalidad de esta aplicación es la de poner en práctica todos los conocimientos adquiridos en el manejo del servicio de Firebase de google.

## Software :hammer_and_wrench:

* Android Studio  Bumblebee 2021.1.1

## Servicios  	:gear:

* [Firebase Firestore](https://firebase.google.com/docs/firestore/quickstart) -- Base de datos NoSQL alojada en la nube, en tiempo real.
* [Firebase Realtime Database](https://firebase.google.com/docs/database/android/start) -- Base de datos NoSQL alojada en la nube, en tiempo real.
* [Firebase Storage](https://firebase.google.com/docs/storage/android/start) -- Servicio de almacenamiento de objetos (fotos, videos).
* [Firebase Authentication](https://firebase.google.com/docs/auth?hl=es-419) -- Autenticación de usuarios con diversos proveedores.
* [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging?hl=es-419) -- Mensajería multiplataforma que te permite enviar mensajes de forma segura.

## Librerías :books:
 
* [FirebaseUI](https://github.com/firebase/FirebaseUI-Android) -- Librería recomendada por google para manejar el sdk de firestore  con mayor simplicidad y eficiencia.
* [Glide](https://github.com/bumptech/glide) -- Librería para el manejo de imágenes.
* [Volley](https://google.github.io/volley/) -- Librería HTTP para peticiones web. 

## Módulos Nilo Partner(vendedor) 	:iphone:
* Inicio y cierre  de sesión (google, facebook, teléfono, email)
* Agregar, actualizar, eliminar producto.
* Ver historial de pedidos.
* Chat de ayuda al cliente.
* Actualizar estado del pedido(aprovado, enviado, entregado) u orden de los clientes.
* Crear notificaciones de promociones y ofertas.

## Módulos Nilo Client(cliente) 	:iphone:
* Inicio y cierre de sesión (google, facebook, teléfono, email)
* Agregar, eliminar productos del carrito de compras.
* Pagar, confirmar pago.
* Ver historial de compras.
* Chat de ayuda al cliente.
* Visualizar en tiempo real el seguimiento de su pedido.
* Cambiar nombre y foto de su perfil de usuario. 

## Importante 

Para que la aplicación funcione correctamente al conectarla a su Cuenta de Firebase y activar la autenticación con google, debe proporcionar el código SHA-256 o SHA-1 generadas desde su IDE android studio indroduciendo en la terminal de Android Studio el comando: ```graddle signingReport```
y presionando ```ctrl``` ```+``` ```enter```.

También deberá agregar su propio archivo ```google-services.json``` generado en la configuración de su proyecto de firebase, dentro de la aplicación en android studio.

Para manejar las notificaciones desde un servidor externo pero usando el servicio de firebase cloud messaging, se ha proporcionado dos archivos PHP 
* [NiloRS](https://github.com/hall9zeha/NiloPartnerApp/blob/main/Extras/NiloRS.php)
* [Notification](https://github.com/hall9zeha/NiloPartnerApp/blob/main/Extras/Notification.php)
 
Estos archivos pueden ser utilizados y modificados a conveniencia, y cargados al servidor que utilice. Solo se debe agregar la llave de su proyecto de firebase.

## Capturas Nilo Partner :framed_picture:
<!--![alt text](https://github.com/hall9zeha/NiloPartnerApp/blob/main/Screenshots/Screenshot_20220301-113613~2.jpg?raw=true)-->
<img src="https://github.com/hall9zeha/NiloPartnerApp/blob/main/Screenshots/Screenshot_20220301-113736~2.jpg" alt="drawing" width="300"/>|
<img src="https://github.com/hall9zeha/NiloPartnerApp/blob/main/Screenshots/Screenshot_20220301-113748~2.jpg" alt="drawing" width="300"/>|
<img src="https://github.com/hall9zeha/NiloPartnerApp/blob/main/Screenshots/Screenshot_20220301-113813~2.jpg" alt="drawing" width="300"/>|
<img src="https://github.com/hall9zeha/NiloPartnerApp/blob/main/Screenshots/Screenshot_20220301-113831~2.jpg" alt="drawing" width="300"/>

## Capturas Nilo Client :framed_picture:

<img src="https://github.com/hall9zeha/NiloPartnerApp/blob/main/Screenshots/Screenshot_20220301-113613~2.jpg" alt="drawing" width="300"/>|
<img src="https://github.com/hall9zeha/NiloPartnerApp/blob/main/Screenshots/Screenshot_20220301-113623~2.jpg" alt="drawing" width="300"/>
