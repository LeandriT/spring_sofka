package ec.com.softka.client_service.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientResponse {
    private int id;
    private String name;
    private String dni;
    private String gender;
    private int age;
    private String address;
    private String phone;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}