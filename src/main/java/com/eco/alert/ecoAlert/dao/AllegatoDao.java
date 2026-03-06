package com.eco.alert.ecoAlert.dao;

import com.eco.alert.ecoAlert.entity.AllegatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllegatoDao extends JpaRepository<AllegatoEntity, Integer> {

    List<AllegatoEntity> findBySegnalazioneIdSegnalazione(Integer idSegnalazione);
}
