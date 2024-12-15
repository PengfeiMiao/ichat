FROM openjdk:11.0.11-slim

# 作者信息
MAINTAINER pengfei.miao pengfei.miao@thoughtworks.com

WORKDIR /home/ichat

COPY libs/ ./libs/
COPY plugins/ ./plugins/
RUN mkdir -p ./plugins/TestPlugin
COPY start.sh .

CMD ["sh", "start.sh"]