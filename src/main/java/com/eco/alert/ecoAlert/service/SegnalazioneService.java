package com.eco.alert.ecoAlert.service;

import com.eco.alert.ecoAlert.dao.EnteDao;
import com.eco.alert.ecoAlert.dao.SegnalazioneDao;
import com.eco.alert.ecoAlert.dao.UtenteDao;
import com.eco.alert.ecoAlert.entity.*;
import com.eco.alert.ecoAlert.enums.StatoSegnalazione;
import com.eco.alert.ecoAlert.exception.*;
import com.ecoalert.model.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class SegnalazioneService {

    @Autowired
    private SegnalazioneDao segnalazioneDao;

    @Autowired
    private EnteDao enteDao;

    @Autowired
    private UtenteDao utenteDao;

    @Transactional
    public SegnalazioneOutput creaSegnalazione(Integer idUtente, SegnalazioneInput input) {
        log.info("Creazione segnalazione per utente con ID {}", idUtente);

        if (idUtente == null || input == null)
            throw new IdODatiMancantiException("ID utente o dati della segnalazione mancanti");

        if (!StringUtils.hasText(input.getTitolo()))
            throw new TitoloMancanteException("Titolo obbligatorio");

        if (!StringUtils.hasText(input.getDescrizione()))
            throw new DescrizioneMancanteException("Descrizione obbligatoria");

        if (input.getIdEnte() == null)
            throw new EnteNonTrovatoException("ID ente obbligatorio");

        UtenteEntity utente = utenteDao.findById(idUtente)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente con ID " + idUtente + " non trovato."));

        if (!(utente instanceof CittadinoEntity))
            throw new UtenteNonCittadinoException("Solo i cittadini possono creare segnalazioni");

        EnteEntity ente = enteDao.findById(input.getIdEnte())
                .orElseThrow(() -> new EnteNonTrovatoException("Ente non trovato"));

        SegnalazioneEntity segnalazione = new SegnalazioneEntity();
        segnalazione.setTitolo(input.getTitolo());
        segnalazione.setDescrizione(input.getDescrizione());
        segnalazione.setLatitudine(input.getLatitudine());
        segnalazione.setLongitudine(input.getLongitudine());
        segnalazione.setCittadino((CittadinoEntity) utente); // mapping corretto
        segnalazione.setEnte(ente);
        segnalazione.setStato(StatoSegnalazione.INSERITO);
        segnalazione.setDataSegnalazione(LocalDateTime.now());

        SegnalazioneEntity salvata = segnalazioneDao.save(segnalazione);
        log.info("Segnalazione {} creata in stato {}", salvata.getIdSegnalazione(), salvata.getStato());

        return toOutput(salvata);
    }

    public SegnalazioneOutput aggiornaStatoSegnalazione(
            Integer idEnte,
            Integer idSegnalazione,
            StatoSegnalazione nuovoStato,
            String nuovaDitta) {

        log.info("Aggiornamento segnalazione {} da parte dell'ente {}", idSegnalazione, idEnte);

        // Verifica esistenza segnalazione
        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() -> new SegnalazioneNonTrovataException("Segnalazione non trovata"));

        // Verifica esistenza ente
        EnteEntity ente = enteDao.findById(idEnte)
                .orElseThrow(() -> new EnteNonTrovatoException("Ente non trovato"));

        // Controllo autorizzazione: solo l'ente associato può aggiornare
        if (!segnalazione.getEnte().getId().equals(ente.getId())) {
            throw new EnteNonAutorizzatoException("Questo ente non può modificare la segnalazione");
        }

        if (nuovoStato != null) {
            segnalazione.setStato(nuovoStato);
        }

        if (nuovaDitta != null && !nuovaDitta.isBlank()) {
            segnalazione.setDitta(nuovaDitta);
        }

        if (nuovoStato == StatoSegnalazione.CHIUSO){
            segnalazione.setDataChiusura(LocalDateTime.now());
        }

        SegnalazioneEntity salvata = segnalazioneDao.save(segnalazione);
        return toOutput(salvata);
    }

    private List<SegnalazioneOutput> mapToOutputList(List<SegnalazioneEntity> entities) {
        return entities.stream().map(se -> {
            SegnalazioneOutput output = new SegnalazioneOutput();
            output.setId(se.getIdSegnalazione());
            output.setTitolo(se.getTitolo());
            output.setDescrizione(se.getDescrizione());
            output.setLatitudine(se.getLatitudine());
            output.setLongitudine(se.getLongitudine());
            output.setStato(StatoEnum.valueOf(se.getStato().name()));
            output.setIdUtente(se.getCittadino().getId());
            output.setIdEnte(se.getEnte().getId());
            output.setDitta(se.getDitta());
            ZoneOffset offset = ZoneOffset.ofHours(1); // se vuoi UTC+1
            output.setDataSegnalazione(se.getDataSegnalazione().atOffset(offset));
            output.setDataChiusura(se.getDataChiusura() != null ? se.getDataChiusura().atOffset(offset) : null);
            return output;
        }).toList();
    }

    // Mappa i commenti nella DTO
    public List<CommentoOutput> commentiOutputList(List<CommentoEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(commentoEntity -> {
                    CommentoOutput output = new CommentoOutput();
                    output.setId(commentoEntity.getIdCommento());
                    output.setDescrizione(commentoEntity.getDescrizione());
                    output.setIdUtente(commentoEntity.getUtente().getId());
                    ZoneOffset offset = ZoneOffset.ofHours(1); // UTC+1
                    output.setDataCommento(commentoEntity.getDataCommento().atOffset(offset));

                    if (commentoEntity.getUtente() instanceof CittadinoEntity cittadino){
                        output.setNome(cittadino.getNome());
                        output.setCognome(cittadino.getCognome());
                    }

                    if (commentoEntity.getUtente() instanceof EnteEntity ente){
                        output.setNomeEnte(ente.getNomeEnte());
                    }

                    return output;
                })
                .toList();
    }

    // Mappa i commenti nella DTO
    public List<AllegatoOutput> allegatiOutputList(List<AllegatoEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(allegatoEntity -> {
                    AllegatoOutput output = new AllegatoOutput();
                    output.setId(allegatoEntity.getId_allegato());
                    output.setNomeFile(allegatoEntity.getNomeFile());
                    return output;
                })
                .toList();
    }

    public SegnalazioneOutput toOutput(SegnalazioneEntity entity) {
        SegnalazioneOutput output = new SegnalazioneOutput();
        output.setId(entity.getIdSegnalazione());
        output.setTitolo(entity.getTitolo());
        output.setDescrizione(entity.getDescrizione());
        output.setLatitudine(entity.getLatitudine());
        output.setLongitudine(entity.getLongitudine());
        output.setStato(StatoEnum.valueOf(entity.getStato().name()));
        output.setIdUtente(entity.getCittadino().getId());
        output.setIdEnte(entity.getEnte().getId());
        output.setDitta(entity.getDitta());
        ZoneOffset offset = ZoneOffset.ofHours(1); // se vuoi UTC+1
        output.setDataSegnalazione(entity.getDataSegnalazione().atOffset(offset));
        output.setDataChiusura(entity.getDataChiusura() != null ? entity.getDataChiusura().atOffset(offset) : null);
        output.commenti(commentiOutputList(entity.getCommenti()));
        output.setAllegati(allegatiOutputList(entity.getAllegati()));
        return output;
    }

    public List<SegnalazioneOutput> getSegnalazioniByUserId(Integer id) {
        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato."));

        if (utente instanceof CittadinoEntity) {
            List<SegnalazioneEntity> segnalazioni = segnalazioneDao.findByCittadino_Id(id);
            return mapToOutputList(segnalazioni);
        }

        if (utente instanceof EnteEntity) {
            List<SegnalazioneEntity> segnalazioni = segnalazioneDao.findByEnte_Id(id);
            return mapToOutputList(segnalazioni);
        }

        throw new RuntimeException("Ruolo utente non valido");
    }

    public SegnalazioneOutput getSegnalazioneById(Integer idUtente, Integer idSegnalazione) {

        // Recupera la segnalazione
        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() -> new SegnalazioneNonTrovataException("Segnalazione non trovata"));

        // Recupera l'utente
        UtenteEntity utente = utenteDao.findById(idUtente)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato"));

        // Controlla i permessi
        if (utente instanceof CittadinoEntity) {
            if (!segnalazione.getCittadino().getId().equals(idUtente)) {
                throw new AccessoNonAutorizzatoException("Non puoi vedere questa segnalazione");
            }
        } else if (utente instanceof EnteEntity) {
            if (!segnalazione.getEnte().getId().equals(idUtente)) {
                throw new AccessoNonAutorizzatoException("Non puoi vedere questa segnalazione");
            }
        } else {
            throw new AccessoNonAutorizzatoException("Tipo utente non autorizzato");
        }

        return toOutput(segnalazione);
    }

    public void cancellaSegnalazione(Integer id, Integer idSegnalazione) throws OperazioneNonPermessaException {

        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente con ID : " + id + " non trovato."));
        if (!(utente instanceof CittadinoEntity)) {
            throw new UtenteNonCittadinoException("Solo i cittadini possono eliminare una segnalazione.");
        }

        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() -> new SegnalazioneNonTrovataException("Segnalazione con ID : " + idSegnalazione + " non trovata."));

        if (!Objects.equals(segnalazione.getCittadino().getId(), id)) {
            throw new OperazioneNonPermessaException("Non puoi eliminare una segnalazione che non ti appartiene.");

        }

        if (segnalazione.getStato() != StatoSegnalazione.INSERITO && segnalazione.getStato() != StatoSegnalazione.CHIUSO) {
            throw new StatoNonValidoException(
                    "Non puoi eliminare una segnalazione in stato " + segnalazione.getStato()
            );
        }
        segnalazioneDao.delete(segnalazione);
    }

    @Transactional
    public SegnalazioneOutput modificaSegnalazione(Integer id, Integer idSegnalazione, SegnalazioneInput input) {

        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() ->
                        new UtenteNonTrovatoException("Utente con ID " + id + " non trovato.")
                );

        if (!(utente instanceof CittadinoEntity)) {
            throw new OperazioneNonPermessaException("Solo i cittadini possono modificare una segnalazione.");
        }

        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() ->
                        new SegnalazioneNonTrovataException("Segnalazione con ID " + idSegnalazione + " non trovata.")
                );

        if (!Objects.equals(segnalazione.getCittadino().getId(), id)) {
            throw new OperazioneNonPermessaException("Non puoi modificare una segnalazione che non ti appartiene.");
        }

        if (segnalazione.getStato() == StatoSegnalazione.CHIUSO) {
            throw new StatoNonValidoException("Non puoi modificare una segnalazione chiusa.");
        }

        // Aggiornamento campi
        if (input.getTitolo() != null) segnalazione.setTitolo(input.getTitolo());
        if (input.getDescrizione() != null) segnalazione.setDescrizione(input.getDescrizione());
        if (input.getLatitudine() != null) segnalazione.setLatitudine(input.getLatitudine());
        if (input.getLongitudine() != null) segnalazione.setLongitudine(input.getLongitudine());

        if (input.getIdEnte() != null) {
            EnteEntity ente = enteDao.findById(input.getIdEnte())
                    .orElseThrow(() -> new EnteNonTrovatoException("Ente non trovato."));
            segnalazione.setEnte(ente);
        }

        SegnalazioneEntity salvata = segnalazioneDao.save(segnalazione);
        return toOutput(salvata);
    }
}
