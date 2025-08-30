package ec.com.sofka.account_service.dto.movement.request;

import ec.com.sofka.account_service.dto.retentions.OnCreate;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovementPartialUpdateRequest {
    @NotNull(message = "date cannot be null", groups = {OnCreate.class})
    private LocalDateTime date;


}
