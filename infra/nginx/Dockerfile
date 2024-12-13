# FROM命令定义构建镜像的基础镜像，必须是Dockerfile的首个命令
FROM nginx:1.23

# 作者信息
MAINTAINER pengfei.miao pengfei.miao@thoughtworks.com

WORKDIR /usr/local/src

# 将本地文件添加到容器中
#ADD client /usr/share/nginx/html
ADD nginx.conf /etc/nginx/nginx.conf