#基础镜像使用jdk1.8
FROM openjdk:8-jre

#作者
MAINTAINER NL

# VOLUME 指定临时文件目录为/tmp，在主机/var/lib/docker目录下创建了一个临时文件并链接到容器的/tmp
VOLUME /tmp
ARG JAR_FILE=target/internal-admin.jar
# 将jar包添加到容器中并更名
COPY ${JAR_FILE} /app.jar

# 运行jar包
RUN bash -c 'touch /app.jar'

# 为了缩短 Tomcat 启动时间，添加一个系统属性指向 “/dev/./urandom” 作为 Entropy Source
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

#ENTRYPOINT ["nohup","java","-jar","/data/cat/internal-admin.jar","&"]

#暴露8085端口
EXPOSE 8080