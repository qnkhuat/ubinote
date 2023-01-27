###################
# STAGE 1: builder
###################
FROM node:16-slim as builder

WORKDIR /home/node/app

# install java 11
RUN apt-get update && apt-get install -y git curl openjdk-11-jdk \
      && curl -O https://download.clojure.org/install/linux-install-1.11.1.1208.sh \
      && chmod +x linux-install-1.11.1.1208.sh \
      && ./linux-install-1.11.1.1208.sh

COPY . .
RUN clojure -T:build uberjar

# ###################
# # STAGE 2: runner
# ###################
FROM eclipse-temurin:11-jre-alpine as runner

ENV FC_LANG en-US LC_CTYPE en_US.UTF-8

# dependencies
# install google-chrome
# Installs latest Chromium (100) package.
RUN apk add --no-cache \
        chromium \
        nss \
        freetype \
        harfbuzz \
        ca-certificates \
        ttf-freefont \
        nodejs \
        npm

RUN npm install -g "single-file-cli"

COPY --from=builder /home/node/app/target/ubinote.jar /app/

CMD ["java", "-jar", "/app/ubinote.jar"]
