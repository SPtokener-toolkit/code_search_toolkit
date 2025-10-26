FROM ubuntu:20.04

RUN apt-get update 
RUN apt-get install -y git python3-pip

WORKDIR /app

RUN git clone https://github.com/SPtokener-toolkit/code_search_toolkit.git

WORKDIR /app/code_search_toolkit/CCSTokener-fork

RUN python3 -m pip install -r requirements.txt

ENV QUERY_FILE="snippet.java"
ENV BETA=0.6
ENV THETA=0.4
ENV ETA=0.55

CMD bash ./ccstokener_runner.sh /data/dataset $BETA $THETA $ETA --query_file "/data/dataset/${QUERY_FILE}" ./results

