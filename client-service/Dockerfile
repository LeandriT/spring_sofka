# -- Etapa 1: Construcción de la aplicación --
# Usamos una imagen que tiene tanto Gradle como un JDK.
# El sufijo 'alpine' o 'slim' es opcional, pero ayuda a reducir el tamaño.
FROM gradle:8.10.2-jdk21 AS builder

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia los archivos del proyecto.
# La bandera --chown asegura que los archivos
# tengan los permisos correctos para el usuario 'gradle' por defecto.
COPY --chown=gradle:gradle . .

# Construye el proyecto Spring Boot y ejecuta todas las tareas
# incluyendo tests y JaCoCo.
RUN ./gradlew clean bootJar jacocoTestReport jacocoTestCoverageVerification --no-daemon

# -- Etapa 2: Creación de la imagen final para ejecutar la aplicación --
# Usa una imagen base mínima que solo tenga el JRE, sin herramientas de construcción.
FROM eclipse-temurin:21-jre-alpine

# Establece el directorio de trabajo final
WORKDIR /app

# Copia el JAR construido desde la etapa 'builder' a la etapa de ejecución.
# Usa un comodín (`*.jar`) para manejar nombres de archivos variables,
# y lo renombra a 'app.jar' para mayor consistencia.
COPY --from=builder /app/build/libs/*.jar app.jar

# Expone el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
# Usa el formato JSON array para el ENTRYPOINT para que se ejecute correctamente
# con los argumentos pasados.
ENTRYPOINT ["java", "-jar", "app.jar"]