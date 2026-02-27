package com.javanauta.agendadortarefas.business.service;

import com.javanauta.agendadortarefas.business.dto.TarefasDTO;
import com.javanauta.agendadortarefas.business.mapper.TarefaUpdateConverter;
import com.javanauta.agendadortarefas.business.mapper.TarefasConverter;
import com.javanauta.agendadortarefas.infrastructure.entity.TarefasEntity;
import com.javanauta.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import com.javanauta.agendadortarefas.infrastructure.exceptions.ResourceNotFundException;
import com.javanauta.agendadortarefas.infrastructure.repository.TarefasRepository;
import com.javanauta.agendadortarefas.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefasService {

    private final TarefasRepository tarefasRepository;
    private final TarefasConverter tarefasConverter;
    private final JwtUtil jwtUtil;
    private final TarefaUpdateConverter tarefaUpdateConverter;


    public TarefasDTO gravarTarefa(String token, TarefasDTO dto) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        dto.setDataCriacao(LocalDateTime.now());
        dto.setStatusNotificacaoEnum(StatusNotificacaoEnum.PENDENTE);
        dto.setEmailUsuario(email);
        TarefasEntity entity = tarefasConverter.paraTarefasEntity(dto);

        return tarefasConverter.paraTarefasDTO(tarefasRepository.save(entity));
    }

    public List<TarefasDTO> buscarTarefasAgendadasPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {

        return tarefasConverter.paraListTarefasDTO(
                tarefasRepository.findByDataEventoBetween(dataInicial, dataFinal));

    }

    public List<TarefasDTO> buscarTarefasPorEmail(String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        return tarefasConverter.paraListTarefasDTO(
                tarefasRepository.findByEmailUsuario(email));

    }

    public void deletaTarefaPorId(String id) {
        try {
            tarefasRepository.deleteById(id);
        } catch (ResourceNotFundException e) {

            throw new ResourceNotFundException("Tarefa não encontrada, id inexistente " + id,
                    e.getCause());
        }
    }

    public TarefasDTO alteraStatusTarefas(StatusNotificacaoEnum status, String id) {
        try {
            TarefasEntity entity = tarefasRepository.findById(id).
                    orElseThrow(() -> new ResourceNotFundException("Tarefa não encontrada, id inexistente " + id));
            entity.setStatusNotificacaoEnum(status);
            entity.setDataAlteracao(LocalDateTime.now());

            return tarefasConverter.paraTarefasDTO(tarefasRepository.save(entity));

        } catch (ResourceNotFundException e) {
            throw new ResourceNotFundException("Erro ao alterar status da tarefa" + e.getCause());
        }
    }

    public TarefasDTO updateTarefas(TarefasDTO dto, String id) {
        try {
            TarefasEntity entity = tarefasRepository.findById(id).
                    orElseThrow(() -> new ResourceNotFundException("Tarefa não encontrada, id inexistente " + id));
            tarefaUpdateConverter.updateTarefas(dto, entity);
            tarefasConverter.paraTarefasDTO(tarefasRepository.save(entity));
            entity.setDataAlteracao(LocalDateTime.now());

            return tarefasConverter.paraTarefasDTO(tarefasRepository.save(entity));

        } catch (ResourceNotFundException e) {
            throw new ResourceNotFundException("Erro ao alterar status da tarefa" + e.getCause());
        }
    }


}


