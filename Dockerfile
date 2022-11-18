FROM navikt/java:17-appdynamics
LABEL maintainer="Team Arbeidsforhold"

ARG JAR_PATH

ADD $JAR_PATH /app/app.jar
