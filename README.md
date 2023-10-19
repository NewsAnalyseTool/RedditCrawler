# Reddit Crawler in Scala

Dieses Projekt ermöglicht das Abrufen von den beliebtesten Posts aus der Reddit Plattform mithilfe der Reddit API.
die Anzahl der ausgegebenen Menge kann in der main funktion bestimmt werden!

## Inhaltsverzeichnis

1. [Voraussetzungen](#voraussetzungen)
2. [Installation und Setup](#installation-und-setup)
3. [Ausführung des Programms](#ausführung-des-programms)
4. [Support](#support)

## Voraussetzungen

- **Scala**: Dieses Projekt wurde mit Scala 2.13 entwickelt.
- **Java**: Eine kompatible Java Virtual Machine ist erforderlich, z.B. Zulu-17.
- **sbt (Scala Build Tool)**: Zum Kompilieren und Ausführen des Programms.

## Installation und Setup

1. Klone das Repository auf deinen lokalen Computer.
2. Platziere deine `.env` Datei im Hauptverzeichnis des Projekts.
3. Befülle die `.env` Datei mit deinen Reddit-API-Zugangsdaten:

CLIENT_ID="DeinRedditClientID"
CLIENT_SECRET="DeinRedditClientSecret"
REDIRECT_URI="DeinRedirectURI"
USERNAME="DeinRedditUsername"
PASSWORD="DeinRedditPassword"

Registriere dich bei Reddit. Danach kannst du 
die client_id und das client_secret über diesen Link erhalten: 
[hier klicken](https://www.reddit.com/prefs/apps)
klicke danach auf "are you a developer? create an app..."


## Ausführung des Programms

1. Öffne ein Terminal im Hauptverzeichnis des Projekts.
2. Führe `sbt run` aus, um das Programm zu starten.

## Support

Bei Problemen oder Fragen kannst du mich über Discord kontaktieren.

---

