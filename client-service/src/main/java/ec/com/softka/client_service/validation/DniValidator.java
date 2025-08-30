package ec.com.softka.client_service.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DniValidator implements ConstraintValidator<ValidDni, String> {

    @Override
    public boolean isValid(String dni, ConstraintValidatorContext context) {

        if (dni == null || !dni.matches("\\d{10}")) {
            setCustomMessage(context, dni);
            return false;
        }


        int provinceCode = Integer.parseInt(dni.substring(0, 2));
        if (provinceCode < 1 || provinceCode > 24) {
            setCustomMessage(context, dni);
            return false;
        }


        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int num = dni.charAt(i) - '0';
            if (i % 2 == 0) {
                num *= 2;
                if (num > 9) {
                    num -= 9;
                }
            }
            suma += num;
        }
        int verificador = (10 - (suma % 10)) % 10;
        boolean ok = verificador == (dni.charAt(9) - '0');

        if (!ok) {
            setCustomMessage(context, dni);
        }
        return ok;
    }

    private void setCustomMessage(ConstraintValidatorContext context, String dni) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                String.format("client with dni %s is invalid", dni)
        ).addConstraintViolation();
    }
}