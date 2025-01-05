FROM openjdk:17

# Set the working directory in the container
WORKDIR /app

#
#EXPOSE 8079
#ADD target/PlayerCompanion-Sicbo.jar PlayerCompanion-Sicbo.jar
#ENTRYPOINT ["java","-jar","PlayerCompanion-Sicbo.jar"]

EXPOSE 8070
ADD target/PlayerCompanion.jar PlayerCompanion.jar
ENTRYPOINT ["java","-jar","PlayerCompanion.jar"]