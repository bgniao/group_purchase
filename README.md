# 社区团购1.0


## [关于我们](https://www.bgniao.cn)
我们是一家专业从事开发小程序社区团购的公司。本项目是我们公司之前的1.0版本，在实际使用中我们遇到了很多痛点，其中包括多版本运行，服务横向扩展困难等问题，为此我们在1.0的基础上重构代码，总结问题。推出了基于Spring Cloud的1.2版本，于2020年4月份上线第一个正式版本，并不断迭代更新，获得了客户一致好评。

## Spring Boot版本
### 项目介绍
`group_purchase`是一套社区团购系统，包括小程序端及后台管理系统，基于SpringBoot+MyBatis实现，采用Docker
容器化部署。包含营销活动、文章管理、财务管理、商品管理、订单管理、购物车、店铺装修、接龙订单、微信群机器人、店铺设置、用户管理等功能。如需前后端全部源码，请联系微信：18058505737


### 快速开始
- 拉取代码

```shell script
git clone https://github.com/bgniao/group_purchase.git
```

- 进入目录

```shell script
cd group_purchase
```

- 修改密钥

```shell script 
文件 com/mds/group/purchase/utils/GeoCodeUtil.java
字段 private static final String KEY = "xxx";

文件 com/mds/group/purchase/utils/KdniaoTrackQueryAPI.java
字段  private String EBusinessID = "xxx";    
字段  private String AppKey = "xxx";         

文件 docker-compose.yml
字段 group-purchase.environment.PARAMS
```

- 编译源码

```shell script
mvn clean package -Dmaven.test.skip=true
```

- 启动docker-compose

```shell script
docker-compose up -d
```

- 打开swagger页面

[http://localhost:8089/api/swagger-ui.html](http://localhost:8089/api/swagger-ui.html)

### 项目演示
小程序：
管理后台地址：

### 组织结构
```
.
├── activity -- 营销活动
├── article -- 文章管理
├── configurer -- 通用设置
├── constant -- 全局变量
├── core -- 基础核心
├── exception -- 自定义异常
├── financial -- 财务管理
├── goods -- 商品管理
├── jobhandler -- 定时任务
├── listener -- mq监听
├── logistics -- 物流管理
├── order -- 订单管理
├── shop -- 商铺管理
├── solitaire -- 接龙管理
├── system -- 系统设置
├── user -- 用户管理
├── utils -- 工具类
├── websocket -- ws支持
└── wechatbot -- 微信机器人

```

### 技术选型


| 技术                 | 说明                | 官网                                           |
| -------------------- | ------------------- | ---------------------------------------------- |
| SpringBoot           | 容器+MVC框架        | https://spring.io/projects/spring-boot         |
| MyBatis              | ORM框架             | http://www.mybatis.org/mybatis-3/zh/index.html |
| Activemq             | 消息队列            | http://activemq.apache.org/ |
| Redis                | 分布式缓存          | https://redis.io/                              |
| MongoDB              | NoSql数据库         | https://www.mongodb.com                        |
| Fastjson             | JSON解析        |https://github.com/alibaba/   |
| XXL-JOB             | 分布式任务调度平台 |https://www.xuxueli.com/xxl-job/ |
| WxJava             | 微信开发 Java SDK       | https://github.com/Wechat-Group/WxJava        |
| Druid                | 数据库连接池        | https://github.com/alibaba/druid               |
| Lombok               | 简化对象封装工具    | https://github.com/rzwitserloot/lombok         |
| Hutool               | Java工具类库        | https://github.com/looly/hutool                |
| tk.mybatis           | MyBatis 工具 | https://mybatis.io/ |
| Swagger-UI           | 文档生成工具        | https://github.com/swagger-api/swagger-ui      |
| Hibernator-Validator | 验证框架            | http://hibernate.org/validator                 |

## Spring Cloud版本

### 亮点功能
- 独立提货点、提货点合并
- 首页自定义DIY装修、分类页装修
- 会员+返利模式
- 商品订单自动分拣
- 一分钟快速创建店铺并提交审核
- 商城和社区团购并存模式
- 第三方低费率支付

### 功能架构

![FunctionList](https://bgniao.gitee.io/group_purchase/images/FunctionList.jpeg)

### 系统架构

![SystemArchitecture](https://bgniao.gitee.io/group_purchase/images/SystemArchitecture.jpeg)

### 技术选型

| 技术                   | 说明                 | 官网                                                 |
| ---------------------- | -------------------- | ---------------------------------------------------- |
| Spring Cloud           | 微服务框架           | https://spring.io/projects/spring-cloud              |
| Spring Cloud Alibaba   | 微服务框架           | https://github.com/alibaba/spring-cloud-alibaba      |
| Spring Boot            | 容器+MVC框架         | https://spring.io/projects/spring-boot               |
| MyBatis-Plus       | 数据层代码生成       | http://www.mybatis.org/generator/index.html          |
| Swagger                | 文档生产工具         | https://swagger.io/     |
| Elasticsearch          | 搜索引擎             | https://github.com/elastic/elasticsearch             |
| RabbitMq               | 消息队列             | https://www.rabbitmq.com/                            |
| Redis                  | 分布式缓存           | https://redis.io/                                    |
| Druid                  | 数据库连接池         | https://github.com/alibaba/druid                     |
| OSS                    | 对象存储             | https://github.com/aliyun/aliyun-oss-java-sdk        |
| JWT                    | JWT登录支持          | https://github.com/jwtk/jjwt                         |
| XXL-JOB             | 分布式任务调度平台 |https://www.xuxueli.com/xxl-job/ |
| Lombok                 | 简化对象封装工具     | https://github.com/rzwitserloot/lombok               |
| Jenkins                | 自动化部署工具       | https://github.com/jenkinsci/jenkins                 |
| Docker                 | 应用容器引擎         | https://www.docker.com/                              |


### 平台效果

#### 后台应用

![Applications](https://bgniao.gitee.io/group_purchase/images/Applications.png)
![Wallboard](https://bgniao.gitee.io/group_purchase/images/Wallboard.png)

#### 会员

![Member](https://bgniao.gitee.io/group_purchase/images/Member.png)

#### 储值

![Stored](https://bgniao.gitee.io/group_purchase/images/Stored.png)

#### 订单

![Order](https://bgniao.gitee.io/group_purchase/images/Order.png)

#### 打印

![Print](https://bgniao.gitee.io/group_purchase/images/Print.png)

#### 积分

![Integral](https://bgniao.gitee.io/group_purchase/images/Integral.png)

#### 财务

![Financial](https://bgniao.gitee.io/group_purchase/images/Financial.png)

## 许可证

Apache License 2.0


