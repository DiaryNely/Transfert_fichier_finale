# Transfert_fichier_finale
Projet de Transfert de Données en Java
Ce projet est une implémentation simple d'un système de transfert de données entre un serveur et un client en Java. Il repose sur des sockets pour établir la communication entre les deux parties.

Fonctionnalités principales
Serveur :

Écoute les connexions des clients.
Reçoit et traite les données envoyées par le client.
Envoie une réponse ou confirmation au client, si nécessaire.
Client :

Se connecte au serveur.
Envoie des données spécifiques.
Affiche la réponse ou confirmation reçue du serveur.

ServerIntermediate.java : Contient la logique serveur pour écouter les connexions et traiter les données.
Client.java : Contient la logique client pour se connecter au serveur et envoyer les données.

Instructions pour exécuter le projet
-compile le projet en entier : javac -d . *.java

Lancer le serveur :
Compile et exécute la classe Adftp.java en premier.
Assure-toi que le serveur écoute sur un port valide (par exemple, 5000).

Lancer le client :
Compile et exécute la classe AdtpClient.java.
Assure-toi d'utiliser la même adresse IP et le même port que le serveur.

Interaction :

Saisis les données à envoyer depuis le client et observe la réponse du serveur.
