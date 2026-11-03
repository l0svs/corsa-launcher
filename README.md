# Corsa Launcher

Écran d'accueil sur mesure pour l'autoradio Android de l'Opel Corsa D 2014.
La carte est à gauche, la voiture à droite et la barre de commandes en bas, avec un mode jour très contrasté pour le soleil et un mode nuit.

## Ce que fait la version actuelle

| Fonction | État |
|---|---|
| Carte qui se déplace au doigt, zoom à deux doigts, suivi de la voiture et bouton Recentrer | Disponible |
| Vitesse GPS | Disponible |
| Recherche de destination et favoris Domicile / Travail, guidage dans Waze ou Google Maps | Disponible |
| Musique en cours (titre, artiste, pochette, commandes) depuis n'importe quelle appli | Disponible, après autorisation |
| Grille des applis installées, avec leurs vraies icônes, et tuile CarPlay | Disponible |
| Mode jour, nuit ou automatique selon le lever et le coucher du soleil | Disponible |
| Volume, commande vocale, téléphone | Disponible, selon ce que l'autoradio permet |
| Conso, régime, température, batterie (boîtier OBD2) | Prochaine version |
| Portières (boîtier CANbus), pneus, pleins, caméra à la demande | Prochaines versions |

## Installer sur l'autoradio

1. Sur l'autoradio, ouvre le navigateur et va sur
   `https://github.com/l0svs/corsa-launcher/releases/latest/download/corsa-launcher.apk`
2. Ouvre le fichier téléchargé. Si Android bloque, autorise « Installer des applis inconnues » pour le navigateur.
3. Appuie sur le bouton Accueil et choisis **Corsa Launcher**, puis **Toujours**.
4. Au premier lancement, autorise la localisation. Pour la musique : Applis › Réglages › Autoriser l'accès à la musique.

Les mises à jour s'installent par-dessus, de la même façon.

Pour revenir à l'écran d'accueil d'origine : Applis › Réglages › Choisir l'écran d'accueil par défaut.

## Fabrication de l'APK

Chaque envoi sur `main` lance GitHub Actions, qui compile l'APK.
La release n'est publiée que si la clé de signature est configurée dans les secrets du dépôt
(`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`), pour que chaque version s'installe par-dessus la précédente.
Le journal de la dernière compilation est disponible dans la branche `ci-log`.

Carte : © OpenFreeMap, © OpenMapTiles, données © contributeurs OpenStreetMap.
