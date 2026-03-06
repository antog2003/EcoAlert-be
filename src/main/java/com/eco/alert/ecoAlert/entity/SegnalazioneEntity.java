package com.eco.alert.ecoAlert.entity;

import com.eco.alert.ecoAlert.enums.StatoSegnalazione;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Rappresenta una segnalazione creata da un cittadino e gestita da un ente.
 */
@Data
@Entity
@Table(name = "segnalazione")
public class SegnalazioneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Segnalazione")
    private Integer idSegnalazione;

    @Column(name = "titolo", nullable = false)
    private String titolo;

    @Column(name = "descrizione", nullable = false)
    private String descrizione;

    @Column(name = "latitudine", nullable = false)
    private Double latitudine;

    @Column(name = "longitudine", nullable = false)
    private Double longitudine;

    @Column(name = "ditta")
    private String ditta;

    @Column(name = "data_segnalazione", nullable = false)
    private LocalDateTime dataSegnalazione;

    @Column(name = "data_chiusura")
    private LocalDateTime dataChiusura;

    /**
     * Stato corrente della segnalazione.
     * Salvato come stringa nel database.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false)
    private StatoSegnalazione stato = StatoSegnalazione.INSERITO;

    @ManyToOne
    @JoinColumn(name = "id_cittadino", nullable = false)
    private CittadinoEntity cittadino;

    @ManyToOne
    @JoinColumn(name = "id_ente", nullable = false)
    private EnteEntity ente;

    // se elimini una Segnalazione, spariscono anche i Commenti legati.
    // se elimini un Utente, si eliminano i suoi Commenti o Segnalazioni.
    @OneToMany(mappedBy = "segnalazione", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommentoEntity> commenti;

    @OneToMany(mappedBy = "segnalazione", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<AllegatoEntity> allegati;
}
