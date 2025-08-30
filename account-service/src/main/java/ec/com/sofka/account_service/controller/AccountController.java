package ec.com.sofka.account_service.controller;

import ec.com.sofka.account_service.dto.account.request.AccountPartialUpdateRequest;
import ec.com.sofka.account_service.dto.account.request.AccountRequest;
import ec.com.sofka.account_service.dto.account.response.AccountResponse;
import ec.com.sofka.account_service.dto.retentions.OnCreate;
import ec.com.sofka.account_service.dto.retentions.OnUpdate;
import ec.com.sofka.account_service.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Accounts", description = "Operaciones CRUD para cuentas bancarias")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "Listar cuentas",
            description = "Retorna todas las cuentas paginadas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation =
                                    AccountResponse.class))))
            }
    )
    @GetMapping
    public ResponseEntity<Page<AccountResponse>> index(Pageable pageable) {
        return ResponseEntity.ok(accountService.index(pageable));
    }

    @Operation(
            summary = "Obtener cuenta por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cuenta encontrada",
                            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> show(
            @Parameter(description = "ID de la cuenta", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(accountService.show(id));
    }

    @Operation(
            summary = "Crear nueva cuenta",
            requestBody = @RequestBody(required = true, description = "Datos para crear cuenta",
                    content = @Content(schema = @Schema(implementation = AccountRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creada",
                            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @Validated(OnCreate.class) @org.springframework.web.bind.annotation.RequestBody AccountRequest request) {
        AccountResponse created = accountService.create(request);
        URI location = URI.create("/api/accounts/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(
            summary = "Actualizar cuenta (reemplazo total)",
            requestBody = @RequestBody(required = true, description = "Datos para actualizar",
                    content = @Content(schema = @Schema(implementation = AccountRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizada",
                            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
            @Parameter(description = "ID de la cuenta", example = "1") @PathVariable Long id,
            @Validated(OnUpdate.class) @org.springframework.web.bind.annotation.RequestBody AccountRequest request) {
        return ResponseEntity.ok(accountService.update(id, request));
    }

    @Operation(
            summary = "Actualización parcial de la cuenta (PATCH)",
            requestBody = @RequestBody(required = true, description = "Campos a modificar (parciales)",
                    content = @Content(schema = @Schema(implementation = AccountPartialUpdateRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizada",
                            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
            }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponse> partialUpdate(
            @Parameter(description = "ID de la cuenta", example = "1") @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody AccountPartialUpdateRequest patch) {
        return ResponseEntity.ok(accountService.partialUpdate(id, patch));
    }

    @Operation(
            summary = "Eliminar cuenta",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Eliminada"),
                    @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la cuenta", example = "1") @PathVariable Long id
    ) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}