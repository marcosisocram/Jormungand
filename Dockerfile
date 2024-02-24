FROM marcosisocram/graalvm-nik-maven:latest AS builder

LABEL authors="marcosisocram@gmail.com"

COPY pom.xml pom.xml
COPY src src

#necessario rodar 'mvn natice:compile -Pinstrumented' na maquina e rodar o app com uma boa quantidade de testes se quiser usar o arquivo default.iprof
COPY default.iprof default.iprof

RUN mvn native:compile -Pdocker-optimized


FROM scratch

WORKDIR /app
COPY --from=builder /opt/app/target/app /app/app
EXPOSE 8000

ENTRYPOINT ["/app/app"]