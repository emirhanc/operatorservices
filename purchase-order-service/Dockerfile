FROM bellsoft/liberica-runtime-container:jdk-17.0.7-glibc as builder

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

COPY ./src ./src

RUN ./mvnw clean install


FROM bellsoft/liberica-runtime-container:jre-17.0.7-glibc

WORKDIR /app

COPY --from=builder /app/target/*.jar /app/*.jar

ENTRYPOINT ["java", "-jar", "/app/*.jar" ]

