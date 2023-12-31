FROM openjdk:17-oracle

COPY /workspace/server /server
WORKDIR /server

CMD ["java", "-jar", "spigot-1.17.1.jar",  "--universe", "runtime_saves", "-nogui"]
