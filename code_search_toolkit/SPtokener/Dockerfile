FROM --platform=linux/amd64 openjdk:21-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y musl musl-dev git

RUN ln -s /usr/lib/x86_64-linux-musl/libc.so /lib/libc.musl-x86_64.so.1
# RUN ln -s /usr/lib/x86_64-linux-musl/libc.so /usr/lib/libc.musl-x86_64.so.1

RUN git clone https://github.com/SPtokener-toolkit/SPtokener.git

WORKDIR /app/SPtokener/java

RUN chmod +x build_run.sh

RUN mkdir -p /data/input

ENV BCB_MODE="--no-bcb"
ENV BETA=0.5
ENV THETA=0.35
ENV ETA=0.6

CMD bash ./build_run.sh /data/input $BCB_MODE $BETA $THETA $ETA
