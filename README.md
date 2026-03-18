# EcoAlert - Backend

Backend del progetto EcoAlert, un sistema informativo web e mobile per la segnalazione e la gestione di problematiche ambientali (rifiuti, vandalismo, inquinamento).

Il backend espone API REST per la gestione degli utenti e delle segnalazioni, ed è progettato per integrarsi con un’applicazione mobile sviluppata in Flutter.

---

## Descrizione

EcoAlert consente ai cittadini di segnalare problematiche ambientali in modo strutturato, permettendo la gestione e il monitoraggio delle segnalazioni da parte del sistema.

Il backend gestisce:

* autenticazione e autorizzazione degli utenti
* gestione delle segnalazioni
* persistenza dei dati su database relazionale
* esposizione di API documentate tramite OpenAPI

---

## Tecnologie utilizzate

* Java 17
* Spring Boot 3
* Spring Security (autenticazione JWT)
* Spring Data JPA / Hibernate
* MySQL
* OpenAPI 3.0

---

## Struttura del progetto

```
EcoAlert-be/
├── src/
│   ├── main/java/        # codice sorgente backend
│   ├── main/resources/   # configurazione (application.yml)
│   └── test/             # test JUnit
├── api/
│   └── EcoAlert.yaml     # definizione OpenAPI
├── pom.xml               # configurazione Maven
└── README.md
```

---

## Installazione ed esecuzione

### Clonare il repository

```bash
git clone https://github.com/Antonio1373/EcoAlert-be.git
cd EcoAlert-be
```

### Configurazione del database

Configurare il database MySQL modificando il file `application.yml` con i propri parametri di connessione (host, username, password, nome database).

### Avvio del server

```bash
mvn spring-boot:run
```

Il server sarà disponibile all’indirizzo:
http://localhost:8080

---

## API principali

| Metodo | Endpoint           | Descrizione            |
| ------ | ------------------ | ---------------------- |
| POST   | /api/auth/login    | Autenticazione utente  |
| POST   | /api/auth/register | Registrazione utente   |
| GET    | /api/user/{id}     | Recupero dati utente   |
| POST   | /api/segnalazioni  | Creazione segnalazione |

---

## Database

Lo schema del database è disponibile nel file:

```
database/ecoalert_schema.sql
```

---

## Documentazione API

La documentazione OpenAPI è disponibile in:

```
api/EcoAlert.yaml
```

---

## Integrazione

Questo backend è progettato per funzionare con il frontend Flutter disponibile al seguente repository:
https://github.com/Antonio1373/EcoAlert-fe

---

## Autore

Antonio Granato
Laureando in Informatica

---

## Licenza

Progetto sviluppato a scopo didattico.
