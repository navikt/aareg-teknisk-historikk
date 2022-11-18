FROM navikt/java:17-appdynamics
LABEL maintainer="Team Arbeidsforhold"

ADD target/app.jar /app/app.jar