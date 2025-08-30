package ec.com.sofka.account_service.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum AccountTypeEnum {

    SAVINGS("AHORROS"),
    CURRENT("CORRIENTE");

    private final String displayName;

    AccountTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Busca un AccountTypeEnum a partir de su nombre de visualización.
     *
     * @param displayName El nombre de visualización del tipo de cuenta.
     * @return El AccountTypeEnum correspondiente.
     * @throws IllegalArgumentException si el displayName no coincide con ningún enum.
     */
    public static AccountTypeEnum fromDisplayName(String displayName) {
        return Arrays.stream(AccountTypeEnum.values())
                .filter(type -> type.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró una constante de enum con el nombre: " + displayName));
    }
}
