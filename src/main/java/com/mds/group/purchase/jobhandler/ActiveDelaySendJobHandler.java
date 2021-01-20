/*
 * Copyright Ningbo Qishan Technology Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mds.group.purchase.jobhandler;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mds.group.purchase.configurer.ActiveMqClient;
import com.mds.group.purchase.configurer.GroupMallProperties;
import com.mds.group.purchase.constant.RedisPrefix;
import com.mds.group.purchase.constant.TimeType;
import com.mds.group.purchase.jobhandler.to.ActiveMqTaskTO;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * The type Active delay send job handler.
 *
 * @author whh
 */
@Component
@EnableConfigurationProperties({ActiveMQProperties.class})
public class ActiveDelaySendJobHandler {
    private Logger logger = LoggerFactory.getLogger(ActiveDelaySendJobHandler.class);


    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ActiveMqClient activeMqClient;
    @Resource
    private ActiveMQProperties activeMQProperties;

    /**
     * Sava task.
     *
     * @param data        发送的数据
     * @param queueName   对垒名
     * @param milliSecond 时间
     * @param appmodelId  商品标示
     * @param isCoverage  是否覆盖原发送数据
     */
    public void savaTask(@NotNull String data, @NotNull String queueName, @NotNull Long milliSecond,
                         @NotNull String appmodelId, @NotNull Boolean isCoverage) {
        logger.info(StrUtil.format("保存发送任务：消息内容{},队列名称{},延时时长{},appmodel:{},isCoverage：{}", data, queueName, milliSecond, appmodelId, isCoverage));

        //如果10分钟之内的直接发送
        //指定队列生成令牌值存至redis
        String key = null;
        switch (queueName) {
            case "GroupMall_start_activity_v1":
            case "GroupMall_end_activity_v1":
            case "GroupMall_ready_activity_v1":
                key = GroupMallProperties.getRedisPrefix().concat(appmodelId).concat(RedisPrefix.ACTIVITY_TOKEN);
                break;
            default:
                break;
        }
        if (milliSecond <= TimeType.TENMINUTES) {
            if (isCoverage) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", data);
                jsonObject.put("activeMqTaskTOId", null);
                jsonObject.put("appmodelId", appmodelId);
                isGenerateToken(jsonObject, key);
                logger.info(StrUtil.format("发送延时消息并覆盖原数据：消息内容{},队列名称{},延时时长{}", jsonObject.toJSONString(), queueName, milliSecond));
                activeMqClient.delaySend(jsonObject.toJSONString(), queueName, milliSecond);
            } else {
                logger.info(StrUtil.format("发送延时消息：消息内容{},队列名称{},延时时长{}", data, queueName, milliSecond));

                activeMqClient.delaySend(data, queueName, milliSecond);
            }
            return;
        }
        ActiveMqTaskTO activeMqTaskTO = new ActiveMqTaskTO();
        activeMqTaskTO.setCreateTime(DateUtil.date());
        if (isCoverage) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", data);
            jsonObject.put("appmodelId", appmodelId);
            isGenerateToken(jsonObject, key);
            activeMqTaskTO.setJsonData(jsonObject.toJSONString());
        } else {
            activeMqTaskTO.setJsonData(data);
        }
        long end = activeMqTaskTO.getCreateTime().getTime() + milliSecond;
        Date endTime = DateUtil.date(end);
        activeMqTaskTO.setEndTime(endTime);
        activeMqTaskTO.setState(Boolean.FALSE);
        activeMqTaskTO.setQueueName(queueName);
        activeMqTaskTO.setSendState(false);
        activeMqTaskTO.setSendSum(0);
        activeMqTaskTO.setAppmodelId(appmodelId);
        activeMqTaskTO.setActiveBrokerUrl(activeMQProperties.getBrokerUrl());
        logger.info(StrUtil.format("消息保存至mongo：消息内容{}", JSONUtil.toJsonStr(activeMqTaskTO)));
        mongoTemplate.save(activeMqTaskTO);
    }

    /**
     * 是否生成令牌
     * @param key redis key
     */
    private void isGenerateToken(JSONObject jsonObject, String key) {
        if (StringUtils.isNotBlank(key)) {
            int randNum = RandomUtil.randomInt(10000000, 99999999);
            jsonObject.put("token", randNum);
            String hashKey = String.valueOf(HashUtil.bkdrHash(key.concat(":").concat(jsonObject.getString("id"))));
            redisTemplate.opsForHash().put(key, hashKey, String.valueOf(randNum));
        }
    }

    /**
     * 延时发送消息确认队列是否已执行
     *
     * @param activeMqTaskTOId the active mq task to id
     */
    public void updateState(String activeMqTaskTOId) {
        if (activeMqTaskTOId == null || activeMqTaskTOId.length() == 0) {
            return;
        }
        int findSum = 0;
        while (true) {
            try {
                if (findSum == 5) {
                    XxlJobLogger.log("mongodb 超时连接已达5次任务执行失败!!! Method ------> updateState)");
                }
                Query query = new Query();
                query.addCriteria(Criteria.where("id").is(activeMqTaskTOId));
                mongoTemplate.updateFirst(query, Update.update("state", Boolean.TRUE), ActiveMqTaskTO.class);
                break;
            } catch (Exception e) {
                findSum++;
                XxlJobLogger.log("mongodb查询超时,第" + findSum + "次, Method ------> updateState");
            }
        }
    }

    /**
     * 发送到10分钟内需要处理的延迟队列
     */
    @XxlJob("ActiveDelaySendJobHandler")
    public ReturnT<String> execute(String param) {
        Date currentDate = DateUtil.date();
        Date date = DateUtil.offset(currentDate, DateField.MINUTE, 10);
        Query query = new Query();
        //查询mongdb里面队列执行时间小于当前时间（当前时间加10分钟）并且状态是未消费的队列消息
        query.addCriteria(Criteria.where("endTime").lt(date).and("state").is(false));
        List<ActiveMqTaskTO> activeMqTaskTOS;
        int findSum = 0;
        while (true) {
            try {
                if (findSum == 5) {
                    XxlJobLogger.log("mongodb 超时连接已达5次任务执行失败!!! Method ------> execute");
                    return ReturnT.FAIL;

                }
                activeMqTaskTOS = mongoTemplate.find(query, ActiveMqTaskTO.class);
                break;
            } catch (Exception e) {
                findSum++;
                XxlJobLogger.log("mongodb查询超时,第" + findSum + "次,Method ------> execute");
            }
        }
        try {
            int i = 0;
            for (ActiveMqTaskTO activeMqTaskTO : activeMqTaskTOS) {
                toJson(activeMqTaskTO);
                if (activeMqTaskTO.getSendSum().equals(0)) {
                    activeMqTaskTO.setSendState(true);
                    activeMqTaskTO.setSendSum(1);
                } else {
                    activeMqTaskTO.setSendSum(activeMqTaskTO.getSendSum() + 1);
                }
                //发送超出10次视为无效
                if (activeMqTaskTO.getSendSum() > 10) {
                    updateState(activeMqTaskTO.getId());
                    continue;
                }
                i++;
                long endtime = activeMqTaskTO.getEndTime().getTime() - System.currentTimeMillis();
                if (activeMqTaskTO.getLastSendTime() != null) {
                    Date last = DateUtil.offset(currentDate, DateField.MINUTE, -10);
                    if (DateUtil.isIn(currentDate, activeMqTaskTO.getLastSendTime(), last)) {
                        continue;
                    }
                }
                activeMqTaskTO.setLastSendTime(currentDate);
                activeMqClient.delaySend(activeMqTaskTO.getJsonData(), activeMqTaskTO.getQueueName(), endtime);
                mongoTemplate.save(activeMqTaskTO);
            }
            if (i != activeMqTaskTOS.size()) {
                if (activeMqTaskTOS.size() - i > 0) {
                    XxlJobLogger.log("部分消息为发送成功");
                }
                if (i == 0) {
                    XxlJobLogger.log("未发送任何消息");
                }
                return ReturnT.FAIL;
            }
            XxlJobLogger.log("消息发送成功");
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void toJson(ActiveMqTaskTO activeMqTaskTO) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(activeMqTaskTO.getJsonData());
            jsonObject.put("activeMqTaskTOId", activeMqTaskTO.getId());
            activeMqTaskTO.setJsonData(jsonObject.toJSONString());
        } catch (JSONException e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", activeMqTaskTO.getJsonData());
            jsonObject.put("activeMqTaskTOId", activeMqTaskTO.getId());
            activeMqTaskTO.setJsonData(jsonObject.toJSONString());
        }
    }

}
