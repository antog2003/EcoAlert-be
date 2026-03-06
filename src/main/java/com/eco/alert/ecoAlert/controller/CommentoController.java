package com.eco.alert.ecoAlert.controller;

import com.eco.alert.ecoAlert.entity.CommentoEntity;
import com.eco.alert.ecoAlert.service.CommentoService;
import com.ecoalert.api.CommentiApi;
import com.ecoalert.model.CommentoInput;
import com.ecoalert.model.CommentoOutput;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class CommentoController implements CommentiApi {

    @Autowired
    private CommentoService commentoService;

    @Override
    public ResponseEntity<CommentoOutput> createCommento(
            Integer id,
            Integer idSegnalazione,
            @RequestBody CommentoInput commentoInput
    ) {
        // Service ritorna l'entità
        CommentoEntity entity = commentoService.creaCommento(id, idSegnalazione, commentoInput);

        // Mapping manuale a CommentoOutput
        CommentoOutput out = new CommentoOutput();
        out.setId(entity.getIdCommento());
        out.setDescrizione(entity.getDescrizione());
        out.setIdUtente(entity.getUtente().getId());

        return ResponseEntity.status(201).body(out);
    }

    @Override
    public ResponseEntity<Void> deleteCommento(
            Integer id,
            Integer idSegnalazione,
            Integer idCommento
    ) {
        commentoService.cancellaCommento(id, idSegnalazione, idCommento);
        return ResponseEntity.noContent().build();
    }
}
