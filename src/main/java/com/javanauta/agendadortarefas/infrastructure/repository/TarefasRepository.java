package com.javanauta.agendadortarefas.infrastructure.repository;

import com.javanauta.agendadortarefas.infrastructure.entity.TarefasEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TarefasRepository extends MongoRepository <TarefasEntity, String> {
}
