###################
# STAGE 1: builder
###################
FROM node:18-bullseye as builder

WORKDIR /home/node

RUN apt-get update && apt-get upgrade -y && apt-get install openjdk-11-jdk curl git -y \
    && curl -O https://download.clojure.org/install/linux-install-1.11.1.1262.sh \
    && chmod +x linux-install-1.11.1.1262.sh \
    && ./linux-install-1.11.1.1262.sh

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
