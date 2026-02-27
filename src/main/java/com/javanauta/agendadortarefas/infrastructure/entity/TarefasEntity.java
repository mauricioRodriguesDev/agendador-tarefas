package com.javanauta.agendadortarefas.infrastructure.entity;


import com.javanauta.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("tarefa")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TarefasEntity {

    @Id
    private String id;

    private String nomeTarefa;
    private LocalDateTime dataCriacao;
    private String descricao;
    private String emailUsuario;
    private LocalDateTime dataEvento;
    private LocalDateTime dataAlteracao;
    private String Status;
    private StatusNotificacaoEnum statusNotificacaoEnum;


}
