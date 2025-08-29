package ec.com.softka.client_service.dto.request;

import ec.com.softka.client_service.dto.retention.OnCreate;
import ec.com.softka.client_service.dto.retention.OnUpdate;
import ec.com.softka.client_service.validation.ValidDni;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientRequest {

    @NotBlank(message = "name is required", groups = {OnCreate.class})
    private String name;

    @NotBlank(message = "dni is required", groups = {OnCreate.class})
    @ValidDni(message = "client with dni {validatedValue} is invalid", groups = {OnCreate.class})
    private String dni;

    @NotBlank(message = "gender is required", groups = {OnCreate.class})
    private String gender;

    @NotNull(message = "age is required", groups = {OnCreate.class})
    @Min(value = 0, message = "age must be >= 0", groups = {OnCreate.class})
    private Integer age;

    @NotBlank(message = "address is required", groups = {OnCreate.class})
    private String address;

    @NotBlank(message = "phone is required", groups = {OnCreate.class})
    private String phone;

    // Si en update NO quieres exigir password, quítalo de OnUpdate
    @NotBlank(message = "password is required", groups = {OnCreate.class})
    private String password;

    @NotNull(message = "active is required", groups = {OnCreate.class, OnUpdate.class})
    private Boolean active;
}
