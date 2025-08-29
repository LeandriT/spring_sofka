package ec.com.softka.client_service.controller;

import ec.com.softka.client_service.dto.request.ClientPartialUpdate;
import ec.com.softka.client_service.dto.request.ClientRequest;
import ec.com.softka.client_service.dto.response.ClientResponse;
import ec.com.softka.client_service.dto.retention.OnCreate;
import ec.com.softka.client_service.dto.retention.OnUpdate;
import ec.com.softka.client_service.service.ClientService;
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

@RestController
@RequestMapping("/api/clients")
@Validated
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Operaciones CRUD para clientes")
public class ClientController {

    private final ClientService clientService;

    @Operation(
            summary = "Listar clientes",
            description = "Retorna todos los clientes",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation =
                                    ClientResponse.class))))
            }
    )
    @GetMapping
    public ResponseEntity<Page<ClientResponse>> index(Pageable pageable) {
        return ResponseEntity.ok(clientService.index(pageable));
    }

    @Operation(
            summary = "Obtener cliente por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                            content = @Content(schema = @Schema(implementation = ClientResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> show(
            @Parameter(description = "ID del cliente", example = "1") @PathVariable Integer id) {
        ClientResponse res = clientService.show(id);
        return ResponseEntity.ok(res);
    }

    @Operation(
            summary = "Crear nuevo cliente",
            requestBody = @RequestBody(required = true, description = "Datos para crear cliente",
                    content = @Content(schema = @Schema(implementation = ClientRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = ClientResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<ClientResponse> create(
            @Validated(OnCreate.class) @org.springframework.web.bind.annotation.RequestBody ClientRequest request) {
        ClientResponse created = clientService.create(request);
        // si el servicio retorna el id nuevo:
        URI location = URI.create("/api/clients/" + created.getDni()); // ajusta si usas Long id en el response
        return ResponseEntity.created(location).body(created);
    }

    @Operation(
            summary = "Actualizar cliente (reemplazo total)",
            requestBody = @RequestBody(required = true, description = "Datos para actualizar",
                    content = @Content(schema = @Schema(implementation = ClientRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizado",
                            content = @Content(schema = @Schema(implementation = ClientResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(
            @Parameter(description = "ID del cliente", example = "1") @PathVariable Integer id,
            @Validated(OnUpdate.class) @org.springframework.web.bind.annotation.RequestBody ClientRequest request) {
        ClientResponse updated = clientService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Actualización parcial del cliente (PATCH)",
            requestBody = @RequestBody(required = true, description = "Campos a modificar (parciales)",
                    content = @Content(schema = @Schema(implementation = ClientPartialUpdate.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Actualizado",
                            content = @Content(schema = @Schema(implementation = ClientResponse.class))),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            }
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponse> partialUpdate(
            @Parameter(description = "ID del cliente", example = "1") @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ClientPartialUpdate request) {
        ClientResponse updated = clientService.partialUpdate(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Eliminar cliente",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Eliminado"),
                    @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del cliente", example = "1") @PathVariable Integer id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}