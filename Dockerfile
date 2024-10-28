FROM ghcr.io/navikt/baseimages/temurin:21
LABEL maintainer="Team Arbeidsforhold"

ARG JAR_PATH

ADD $JAR_PATH /app/app.jar