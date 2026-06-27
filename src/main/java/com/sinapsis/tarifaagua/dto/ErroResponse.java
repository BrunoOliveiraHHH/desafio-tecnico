package com.sinapsis.tarifaagua.dto;

import java.time.Instant;
import java.util.List;

public record ErroResponse(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes
) {
}
