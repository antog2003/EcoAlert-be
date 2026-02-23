package com.eco.alert.ecoAlert.enums;

/**
 * Enumerazione che rappresenta gli stati possibili di una segnalazione.
 */
public enum StatoSegnalazione {
    INSERITO,     // Stato iniziale al momento della creazione.
    RICEVUTO,    // L'ente ha preso in carico la segnalazione
    SOSPESO,      // Segnalazione temporaneamente sospesa
    CHIUSO;       // Segnalazione conclusa

    /**
     * Restituisce lo stato in formato leggibile (opzionale, utile per log o JSON)
     */
    @Override
    public String toString() {
        return name();
    }
}

