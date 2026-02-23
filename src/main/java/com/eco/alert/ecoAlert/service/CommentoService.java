package com.eco.alert.ecoAlert.service;

import com.eco.alert.ecoAlert.dao.CommentoDao;
import com.eco.alert.ecoAlert.dao.SegnalazioneDao;
import com.eco.alert.ecoAlert.dao.UtenteDao;
import com.eco.alert.ecoAlert.entity.*;
import com.eco.alert.ecoAlert.exception.*;
import com.ecoalert.model.CommentoInput;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Log4j2
@Service
public class CommentoService {

    @Autowired
    private CommentoDao commentoDao;

    @Autowired
    private UtenteDao utenteDao;

    @Autowired
    private SegnalazioneDao segnalazioneDao;

    @Transactional
    public CommentoEntity creaCommento(Integer idUtente, Integer idSegnalazione, CommentoInput input) {

        UtenteEntity utente = utenteDao.findById(idUtente)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato con ID: " + idUtente));

        SegnalazioneEntity segnalazione = segnalazioneDao.findById(idSegnalazione)
                .orElseThrow(() -> new SegnalazioneNonTrovataException("Segnalazione non trovata con ID: " + idSegnalazione));

        if (utente instanceof CittadinoEntity) {
            if (!segnalazione.getCittadino().getId().equals(idUtente)) {
                throw new OperazioneNonPermessaException("Il cittadino non può commentare questa segnalazione");
            }
        } else if (utente instanceof EnteEntity) {
            if (!segnalazione.getEnte().getId().equals(idUtente)) {
                throw new OperazioneNonPermessaException("L'ente non può commentare questa segnalazione");
            }
        }

        CommentoEntity commento = new CommentoEntity();
        commento.setDescrizione(input.getDescrizione());
        commento.setUtente(utente);
        commento.setSegnalazione(segnalazione);
        commento.setDataCommento(LocalDateTime.now());

        return commentoDao.save(commento);
    }

    public void cancellaCommento(Integer idUtente, Integer idSegnalazione, Integer idCommento) {
        CommentoEntity commento = commentoDao.findById(idCommento)
                .orElseThrow(() -> new CommentoNonTrovatoException("Commento non trovato"));

        if (!commento.getUtente().getId().equals(idUtente)) {
            throw new OperazioneNonPermessaException("Non puoi eliminare questo commento");
        }

        if (!commento.getSegnalazione().getIdSegnalazione().equals(idSegnalazione)) {
            throw new OperazioneNonPermessaException("Commento non associato alla segnalazione indicata");
        }

        commentoDao.delete(commento);
    }
}
