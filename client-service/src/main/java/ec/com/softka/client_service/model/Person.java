package ec.com.softka.client_service.model;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class Person extends Base {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String dni;
    @Column(nullable = false)
    private String gender;
    private int age;
    private String address;
    private String phone;
}
