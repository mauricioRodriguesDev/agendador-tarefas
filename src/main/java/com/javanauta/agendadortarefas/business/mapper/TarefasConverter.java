package com.javanauta.agendadortarefas.business.mapper;

import com.javanauta.agendadortarefas.business.dto.TarefasDTO;
import com.javanauta.agendadortarefas.infrastructure.entity.TarefasEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface TarefasConverter {

    TarefasEntity paraTarefasEntity(TarefasDTO tarefasDTO);

    TarefasDTO paraTarefasDTO(TarefasEntity tarefasEntity);
}
