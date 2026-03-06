package com.eco.alert.ecoAlert.service;

import com.eco.alert.ecoAlert.dao.CittadinoDao;
import com.eco.alert.ecoAlert.dao.EnteDao;
import com.eco.alert.ecoAlert.entity.CittadinoEntity;
import com.eco.alert.ecoAlert.entity.EnteEntity;
import com.eco.alert.ecoAlert.entity.UtenteEntity;
import com.eco.alert.ecoAlert.dao.UtenteDao;
import com.eco.alert.ecoAlert.enums.StatoSegnalazione;
import com.eco.alert.ecoAlert.exception.EmailDuplicataException;
import com.eco.alert.ecoAlert.exception.LoginException;
import com.eco.alert.ecoAlert.exception.OperazioneNonPermessaException;
import com.eco.alert.ecoAlert.exception.UtenteNonTrovatoException;
import com.ecoalert.model.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class UserService {

    @Autowired
    private UtenteDao utenteDao;

    @Autowired
    private CittadinoDao cittadinoDao;

    @Autowired
    private EnteDao enteDao;

    public UtenteOutput creaUtente(UtenteInput input) {

        log.info("Creazione nuovo utente...");

        if (input.getEmail() == null || input.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email obbligatoria.");
        }

        if (input.getPassword() == null || input.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password non valida.");
        }

        UtenteEntity utenteConStessaMail = utenteDao.findByEmail(input.getEmail());
        if (utenteConStessaMail != null) {
            throw new EmailDuplicataException();
        }

        String ruolo = input.getRuolo();

        if ("cittadino".equalsIgnoreCase(ruolo)) {
            return creaCittadino(input);
        }

        if ("ente".equalsIgnoreCase(ruolo)) {
            return creaEnte(input);
        }

        throw new IllegalArgumentException("Ruolo non valido.");
    }

    private UtenteOutput creaCittadino(UtenteInput input) {

        if (input.getNome() == null || input.getCognome() == null) {
            throw new IllegalArgumentException("Nome e cognome obbligatori.");
        }

        // ❗ Impediamo che un cittadino inserisca un nome da ente
        if (input.getNome().toLowerCase().contains("comune di")) {
            throw new OperazioneNonPermessaException(
                    "Un cittadino non può registrarsi come ente."
            );
        }

        CittadinoEntity cittadino = new CittadinoEntity();
        cittadino.setEmail(input.getEmail());
        cittadino.setPassword(input.getPassword());
        cittadino.setNome(input.getNome());
        cittadino.setCognome(input.getCognome());
        cittadino.setNazione(input.getNazione());
        cittadino.setNumeroTelefono(input.getNumeroTelefono());
        cittadino.setCodiceFiscale(input.getCodiceFiscale());

        CittadinoEntity saved = cittadinoDao.save(cittadino);

        UtenteOutput output = new UtenteOutput();
        output.setId(saved.getId());
        output.setEmail(saved.getEmail());
        output.setRuolo("cittadino");

        return output;
    }

    private UtenteOutput creaEnte(UtenteInput input) {

        if (input.getNome() == null || input.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome ente obbligatorio.");
        }

        String nomeEnte = input.getNome().trim();

        // ✅ Deve iniziare con "Comune di"
        if (!nomeEnte.toLowerCase().startsWith("comune di ")) {
            throw new OperazioneNonPermessaException(
                    "Il nome ente deve iniziare con 'Comune di ...'"
            );
        }

        // ✅ Email istituzionale obbligatoria
        if (!isEmailIstituzionale(input.getEmail())) {
            throw new OperazioneNonPermessaException(
                    "Registrazione ente consentita solo con email istituzionale."
            );
        }

        EnteEntity ente = new EnteEntity();
        ente.setEmail(input.getEmail());
        ente.setPassword(input.getPassword());
        ente.setNomeEnte(nomeEnte);
        ente.setNazioneEnte(input.getNazione());
        ente.setCittaEnte(input.getCitta());

        EnteEntity saved = enteDao.save(ente);

        UtenteOutput output = new UtenteOutput();
        output.setId(saved.getId());
        output.setEmail(saved.getEmail());
        output.setRuolo("ente");

        return output;
    }

    private boolean isEmailIstituzionale(String email) {

        String lower = email.toLowerCase();

        return lower.endsWith(".gov.it")
                || lower.contains("@comune.")
                || lower.endsWith(".it");
    }

    public LoginOutput login (LoginInput loginInput) {
        log.info("Login Utente...");

        UtenteEntity utente = utenteDao.findByEmail(loginInput.getEmail());
        if(utente == null){
            throw new LoginException("Email non presente nel database.");
        }

        if(!utente.getPassword().equals(loginInput.getPassword())){
            throw new LoginException("Password Errata.");
        }

        String ruolo = (utente instanceof CittadinoEntity) ? "cittadino" : "ente";
        LoginOutput output = new LoginOutput();
        output.setRuolo(ruolo);
        output.setUserId(utente.getId());
        return output;
    }

    public UtenteDettaglioOutput getUserById(Integer id) {
        log.info("Recupero utente con ID {}", id);

        // Cerca l'utente nella tabella base
        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente con ID " + id + " non trovato."));

        UtenteDettaglioOutput output = new UtenteDettaglioOutput();
        output.setId(utente.getId());
        output.setEmail(utente.getEmail());

        // Controlla il tipo effettivo dell’utente
        if (utente instanceof CittadinoEntity cittadino) {
            output.setRuolo("cittadino");
            output.setNome(cittadino.getNome());
            output.setCognome(cittadino.getCognome());
            output.setNazione(cittadino.getNazione());
            output.setNumeroTelefono(cittadino.getNumeroTelefono());
            output.setCodiceFiscale(cittadino.getCodiceFiscale());
        } else if (utente instanceof EnteEntity ente) {
            output.setRuolo("ente");
            output.setNomeEnte(ente.getNomeEnte());
            output.setNazione(ente.getNazioneEnte());
        }

        return output;
    }

    public List<EnteOutput> getAllEnti() {
        List<EnteEntity> enti = enteDao.findAll();
        List<EnteOutput> result = new ArrayList<>();

        for (EnteEntity ente : enti) {
            EnteOutput enteOutput = new EnteOutput();
            enteOutput.setId(ente.getId());
            enteOutput.setNomeEnte(ente.getNomeEnte());
            enteOutput.setCitta(ente.getCittaEnte());
            enteOutput.setNazione(ente.getNazioneEnte());
            enteOutput.setEmail(ente.getEmail());
            result.add(enteOutput);
        }

        return result;
    }

    public void deleteUser(Integer id) {
        UtenteEntity utente = utenteDao.findById(id)
                .orElseThrow(() -> new UtenteNonTrovatoException("Utente non trovato."));

        if (utente instanceof CittadinoEntity cittadino) {

            boolean hasSegnalazioniAttive = cittadino.getSegnalazioni().stream()
                    .anyMatch(s -> s.getStato() != StatoSegnalazione.CHIUSO);

            if (hasSegnalazioniAttive) {
                throw new OperazioneNonPermessaException(
                        "Impossibile eliminare utente con segnalazioni attive"
                );
            }

            cittadinoDao.delete(cittadino);
            return;
        }

        if (utente instanceof EnteEntity ente) {

            boolean hasSegnalazioniAttive = ente.getSegnalazioniGestite().stream()
                    .anyMatch(s -> s.getStato() != StatoSegnalazione.CHIUSO);

            if (hasSegnalazioniAttive) {
                throw new OperazioneNonPermessaException(
                        "Impossibile eliminare ente con segnalazioni attive"
                );
            }

            enteDao.delete(ente);
            return;
        }

    }
}
