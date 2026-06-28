ARG BUILD_HOME=/marathon

FROM gradle:jdk25 as build-image

ARG BUILD_HOME
ENV APP_HOME=$BUILD_HOME
WORKDIR $APP_HOME

COPY --chown=gradle:gradle build.gradle.kts $APP_HOME/
COPY --chown=gradle:gradle settings.gradle.kts $APP_HOME/
COPY --chown=gradle:gradle src $APP_HOME/src
COPY --chown=gradle:gradle worlds $APP_HOME/worlds

RUN gradle --no-daemon build

FROM eclipse-temurin:25-jre

ARG BUILD_HOME
ENV APP_HOME=$BUILD_HOME
COPY --from=build-image $APP_HOME/build/libs/*.jar minestom-server.jar

ENTRYPOINT exec java -jar minestom-server.jar