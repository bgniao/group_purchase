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

package com.mds.group.purchase.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.mds.group.purchase.configurer.GroupMallProperties;
import com.mds.group.purchase.constant.ActiviMqQueueName;
import com.mds.group.purchase.constant.RedisPrefix;
import com.mds.group.purchase.constant.TemplateMsgType;
import com.mds.group.purchase.jobhandler.ActiveDelaySendJobHandler;
import com.mds.group.purchase.user.model.GroupBalanceOrder;
import com.mds.group.purchase.user.model.GroupLeader;
import com.mds.group.purchase.user.service.GroupLeaderService;
import com.mds.group.purchase.user.vo.WithdrawMoneyDetailsVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Group balance order util.
 *
 * @author pavawi
 */
@Component
public class GroupBalanceOrderUtil {

    @Resource
    private GroupLeaderService groupLeaderService;
    @Resource
    private ActiveDelaySendJobHandler activeDelaySendJobHandler;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * Package results.
     *
     * @param groupIds                the group ids
     * @param withdrawMoneyDetailsVOS the withdraw money details vos
     */
    public void packageResults(List<String> groupIds, List<WithdrawMoneyDetailsVO> withdrawMoneyDetailsVOS) {
        if (CollectionUtil.isEmpty(groupIds)) {
            return;
        }
        List<GroupLeader> byGroupleaderIds = groupLeaderService.findByGroupleaderIds(groupIds);
        Map<String, GroupLeader> groupMap = byGroupleaderIds.stream()
                .collect(Collectors.toMap(GroupLeader::getGroupLeaderId, v -> v));
        withdrawMoneyDetailsVOS.forEach(o -> {
            GroupLeader groupLeader = groupMap.get(o.getGroupLeaderId());
            if (groupLeader != null) {
                o.setGroupName(groupLeader.getGroupName());
                o.setGroupPhone(groupLeader.getGroupPhone());
            }
        });
    }

    /**
     * Package result.
     *
     * @param groupId                the group id
     * @param withdrawMoneyDetailsVO the withdraw money details vo
     */
    public void packageResult(String groupId, WithdrawMoneyDetailsVO withdrawMoneyDetailsVO) {
        GroupLeader groupLeader = groupLeaderService.findById(groupId);
        if (groupLeader != null) {
            withdrawMoneyDetailsVO.setGroupName(groupLeader.getGroupName());
            withdrawMoneyDetailsVO.setGroupPhone(groupLeader.getGroupPhone());
        }
    }

    /**
     * Send withdraw success msg.
     *
     * @param balanceOrder the balance order
     * @param wxuserId     the wxuser id
     */
    public void sendWithdrawSuccessMsg(GroupBalanceOrder balanceOrder, Long wxuserId) {
        String key =
                GroupMallProperties.getRedisPrefix() + balanceOrder.getAppmodelId() + RedisPrefix.FROMID + wxuserId;
        List<String> formIds = (List<String>) redisTemplate.opsForValue().get(key);
        if (CollectionUtil.isNotEmpty(formIds)) {
            String formId = formIds.get(0);
            formIds.remove(0);
            redisTemplate.opsForValue().set(key, formIds);
            Map<String, Object> map = new HashMap<>(16);
            map.put("formId", formId);
            map.put("appmodelId", balanceOrder.getAppmodelId());
            //提现申请时间
            map.put("time", balanceOrder.getCreateTime());
            map.put("withdrawAccount", balanceOrder.getOutBalance());
            map.put("incomeAccount", balanceOrder.getOutBalance());
            map.put("wxuserId", wxuserId);
            map.put("type", TemplateMsgType.WITHDRAWSUCCESS);
            activeDelaySendJobHandler
                    .savaTask(JSON.toJSONString(map), ActiviMqQueueName.ORDER_MINIPROGRAM_TEMPLATE_MESSAGE, 0L,
                            balanceOrder.getAppmodelId(), false);
        }
    }


    /**
     * Send withdraw fail msg.
     *
     * @param balanceOrder the balance order
     * @param wxuserId     the wxuser id
     */
    public void sendWithdrawFailMsg(GroupBalanceOrder balanceOrder, Long wxuserId) {
        String key =
                GroupMallProperties.getRedisPrefix() + balanceOrder.getAppmodelId() + RedisPrefix.FROMID + wxuserId;
        List<String> formIds = (List<String>) redisTemplate.opsForValue().get(key);
        if (CollectionUtil.isNotEmpty(formIds)) {
            String formId = formIds.get(0);
            formIds.remove(0);
            redisTemplate.opsForValue().set(key, formIds);
            Map<String, Object> map = new HashMap<>(16);
            map.put("formId", formId);
            map.put("appmodelId", balanceOrder.getAppmodelId());
            //提现申请时间
            map.put("time", balanceOrder.getCreateTime());
            map.put("withdrawAccount", balanceOrder.getOutBalance());
            map.put("wxuserId", wxuserId);
            map.put("type", TemplateMsgType.WITHDRAWFAIL);
            activeDelaySendJobHandler
                    .savaTask(JSON.toJSONString(map), ActiviMqQueueName.ORDER_MINIPROGRAM_TEMPLATE_MESSAGE, 0L,
                            balanceOrder.getAppmodelId(), false);
        }
    }
}
