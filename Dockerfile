FROM openjdk:11.0.11-slim

WORKDIR /home/ichat

COPY libs/ ./libs/
COPY plugins/ ./plugins/
RUN mkdir -p ./plugins/TestPlugin
COPY start.sh .

CMD ["sh", "start.sh"]