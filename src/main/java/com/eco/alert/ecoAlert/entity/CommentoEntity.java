package com.eco.alert.ecoAlert.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Commento associato ad una segnalazione.
 */
@Data
@Entity
@Table(name = "commenti")
public class CommentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Commento")
    private Integer idCommento;

    @Column(name = "descrizione", nullable = false)
    private String descrizione;

    @Column(name = "data_commento", nullable = false)
    private LocalDateTime dataCommento;

    /**
     * Utente che ha scritto il commento.
     * Utente può essere cittadino o ente.
     */
    @ManyToOne
    @JoinColumn(name="id")
    private UtenteEntity utente;

    /**
     * Segnalazione a cui appartiene il commento.
     */
    @ManyToOne
    @JoinColumn(name="id_Segnalazione")
    private SegnalazioneEntity segnalazione;
}
