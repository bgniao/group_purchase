# 该镜像需要依赖的基础镜像
FROM openjdk:8-alpine

ENV TIME_ZONE=Asia/Shanghai 

RUN ln -snf /usr/share/zoneinfo/$TIME_ZONE /etc/localtime && echo $TIME_ZONE > /etc/timezone

# 将当前目录下的jar包复制到docker容器的/目录下
ADD ./target/*.jar app.jar
# 声明服务运行在8088端口
EXPOSE 8088
# 指定docker容器启动时运行jar包
ENTRYPOINT ["java", "-jar","/app.jar"]
# 指定维护者的名字
MAINTAINER pavawi
