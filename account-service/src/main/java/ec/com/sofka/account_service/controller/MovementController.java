package ec.com.sofka.account_service.controller;


import ec.com.sofka.account_service.dto.movement.request.MovementPartialUpdateRequest;
import ec.com.sofka.account_service.dto.movement.request.MovementRequest;
import ec.com.sofka.account_service.dto.movement.response.MovementResponse;
import ec.com.sofka.account_service.dto.retentions.OnCreate;
import ec.com.sofka.account_service.dto.retentions.OnUpdate;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import ec.com.sofka.account_service.service.MovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Movements", description = "Operaciones CRUD para movimientos de cuenta")
@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class MovementController {

    private final MovementService movementService;


    @Operation(summary = "Listar movimientos", description = "Retorna todos los movimientos paginados",
            responses = @ApiResponse(responseCode = "200",
                    content = @Content(array =
                    @ArraySchema(schema = @Schema(implementation = MovementResponse.class)))))
    @GetMapping
    public ResponseEntity<Page<MovementResponse>> index(Pageable pageable) {
        return ResponseEntity.ok(movementService.index(pageable));
    }

    @Operation(summary = "Listar movimientos por cuenta",
            responses = @ApiResponse(responseCode = "200",
                    content = @Content(array =
                    @ArraySchema(schema = @Schema(implementation = MovementResponse.class)))))
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<MovementResponse>> byAccount(
            @Parameter(description = "ID de la cuenta", example = "1") @PathVariable Long accountId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(movementService.byAccount(accountId, pageable));
    }

    @Operation(summary = "Obtener movimiento por ID",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation =
                            MovementResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<MovementResponse> show(
            @Parameter(description = "ID del movimiento", example = "10") @PathVariable Long id
    ) {
        return ResponseEntity.ok(movementService.show(id));
    }

    @Operation(summary = "Crear movimiento",
            requestBody = @RequestBody(required = true, description = "Datos del movimiento",
                    content = @Content(schema = @Schema(implementation = MovementRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = MovementResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
            })
    @PostMapping
    public ResponseEntity<MovementResponse> create(
            @Validated(OnCreate.class) @org.springframework.web.bind.annotation.RequestBody MovementRequest request) {

        MovementResponse created = movementService.create(request);
        URI location = URI.create("/api/movements/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Depósito en cuenta",
            responses = @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(schema = @Schema(implementation = MovementResponse.class))))
    @PostMapping("/account/{accountId}/deposit")
    public ResponseEntity<MovementResponse> deposit(
            @PathVariable Long accountId,
            @RequestParam BigDecimal amount) {

        MovementRequest req = new MovementRequest();
        req.setAccountId(accountId);
        req.setAmount(amount);
        req.setMovementType(MovementTypeEnum.DEPOSIT);
        MovementResponse created = movementService.create(req);
        URI location = URI.create("/api/movements/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Retiro en cuenta",
            responses = @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(schema = @Schema(implementation = MovementResponse.class))))
    @PostMapping("/account/{accountId}/withdraw")
    public ResponseEntity<MovementResponse> withdraw(
            @PathVariable Long accountId,
            @RequestParam BigDecimal amount) {

        MovementRequest req = new MovementRequest();
        req.setAccountId(accountId);
        req.setAmount(amount);
        req.setMovementType(MovementTypeEnum.WITHDRAWAL);
        MovementResponse created = movementService.create(req);
        URI location = URI.create("/api/movements/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Actualizar movimiento (reemplazo total)",
            requestBody = @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = MovementRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizado",
                            content = @Content(schema = @Schema(implementation = MovementResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            })
    @PutMapping("/{id}")
    public ResponseEntity<MovementResponse> update(
            @Parameter(description = "ID del movimiento", example = "10") @PathVariable Long id,
            @Validated(OnUpdate.class) @org.springframework.web.bind.annotation.RequestBody MovementRequest request) {

        return ResponseEntity.ok(movementService.update(id, request));
    }

    @Operation(summary = "Actualización parcial del movimiento (PATCH)",
            requestBody = @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = MovementPartialUpdateRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizado",
                            content = @Content(schema = @Schema(implementation = MovementResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            })
    @PatchMapping("/{id}")
    public ResponseEntity<MovementResponse> partialUpdate(
            @Parameter(description = "ID del movimiento", example = "10") @PathVariable Long id,
            @Validated(OnCreate.class) @org.springframework.web.bind.annotation.RequestBody
            MovementPartialUpdateRequest patch) {
        return ResponseEntity.ok(movementService.partialUpdate(id, patch));
    }


    @Operation(summary = "Eliminar movimiento",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Eliminado"),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        movementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}