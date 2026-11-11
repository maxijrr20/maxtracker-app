# ---------------------------------------------------------
# MaxTracker API — imagen de produccion (Render, Docker)
# ---------------------------------------------------------

# Etapa 1: compilar el JAR con Maven Wrapper y JDK 21
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primero solo el pom y el wrapper: las dependencias quedan en cache mientras no cambie el pom
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src/ src/
# Los tests ya corren en GitHub Actions (Backend CI); aqui solo se empaqueta
RUN ./mvnw -B -ntp package -DskipTests \
    && cp target/*.jar app.jar

# Etapa 2: imagen liviana solo con el JRE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios
RUN useradd --system --uid 1001 maxtracker
USER maxtracker

COPY --from=build /app/app.jar app.jar

# El plan gratuito de Render tiene 512 MB: la JVM usa como maximo el 75 % de la memoria del contenedor
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

# Render define PORT; application.properties lo lee (por defecto 8080)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
