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

package com.mds.group.purchase.user.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mds.group.purchase.configurer.WxServiceUtils;
import com.mds.group.purchase.core.AbstractService;
import com.mds.group.purchase.exception.ServiceException;
import com.mds.group.purchase.financial.service.FinancialDetailsService;
import com.mds.group.purchase.financial.service.GroupBrokerageService;
import com.mds.group.purchase.user.dao.GroupBalanceOrderMapper;
import com.mds.group.purchase.user.model.GroupBalanceOrder;
import com.mds.group.purchase.user.model.GroupLeader;
import com.mds.group.purchase.user.model.Wxuser;
import com.mds.group.purchase.user.service.GroupBalanceOrderService;
import com.mds.group.purchase.user.service.GroupLeaderService;
import com.mds.group.purchase.user.service.WxuserService;
import com.mds.group.purchase.user.vo.*;
import com.mds.group.purchase.utils.BeanMapper;
import com.mds.group.purchase.utils.GroupBalanceOrderUtil;
import com.mds.group.purchase.utils.ResultPage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Condition;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;



@Service
public class GroupBalanceOrderServiceImpl extends AbstractService<GroupBalanceOrder> implements GroupBalanceOrderService {

    @Resource
    private WxuserService wxuserService;
    @Resource
    private WxServiceUtils wxServiceUtils;
    @Resource
    private GroupLeaderService groupLeaderService;
    @Resource
    private GroupBalanceOrderUtil groupBalanceOrderUtil;
    @Resource
    private FinancialDetailsService financialDetailsService;
    @Resource
    private GroupBalanceOrderMapper tGroupBalanceOrderMapper;
    @Resource
    private GroupBrokerageService groupBrokerageService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int withdrawMoney(WithdrawMoneyVO withdrawMoneyVO) {
        int x = 0;
        GroupBalanceOrder balanceOrder = tGroupBalanceOrderMapper
                .selectByPrimaryKey(withdrawMoneyVO.getGroupbalanceOrderId());
        balanceOrder.setUpdateTime(DateUtil.date());
        Wxuser wxuser = wxuserService.findByGroupleaderId(balanceOrder.getGroupLeaderId());
        if (wxuser == null) {
            throw new ServiceException("用户不存在");
        }
        //同意提现
        if (withdrawMoneyVO.getOptionType().equals(1)) {
            if (balanceOrder.getApplyforState().equals(1)) {
                return 1;
            }
            balanceOrder.setApplyforState(1);
            GroupLeader groupLeader = groupLeaderService.findById(balanceOrder.getGroupLeaderId());
            groupLeader.setBrokerage(NumberUtil.sub(groupLeader.getBrokerage(), balanceOrder.getOutBalance()));
            if (groupLeader.getBrokerage().doubleValue() < 0) {
                throw new ServiceException("团长佣金不足");
            }
            //1-微信钱包
            if (balanceOrder.getOutType().equals(1)) {
                //发起付款请求
                wxServiceUtils.enterprisePayment(withdrawMoneyVO.getAppmodelId(), wxuser.getMiniOpenId(),
                        balanceOrder.getGroupBalanceOrderId().toString(), "",
                        balanceOrder.getOutBalance().setScale(2, BigDecimal.ROUND_HALF_UP).toString(), "团长余额提现",
                        StringUtils.substring(withdrawMoneyVO.getAppmodelId(), 9,
                                withdrawMoneyVO.getAppmodelId().length()));
                //生成佣金明细
                groupBrokerageService.save(balanceOrder);
                //提现成功后插入对账单记录
                financialDetailsService.save(balanceOrder, wxuser);
                groupLeaderService.update(groupLeader);
                x = tGroupBalanceOrderMapper.updateByPrimaryKeySelective(balanceOrder);
                //发送提现成功的模板消息
                groupBalanceOrderUtil.sendWithdrawSuccessMsg(balanceOrder, wxuser.getWxuserId());
            } else if (balanceOrder.getOutType().equals(2)) {
                //线下核销
                //生成佣金明细
                groupBrokerageService.save(balanceOrder);
                //提现成功后插入对账单记录
                financialDetailsService.save(balanceOrder, wxuser);
                groupLeaderService.update(groupLeader);
                x = tGroupBalanceOrderMapper.updateByPrimaryKeySelective(balanceOrder);
                //发送提现成功的模板消息
                groupBalanceOrderUtil.sendWithdrawSuccessMsg(balanceOrder, wxuser.getWxuserId());
            }

            //拒绝提现
        } else if (withdrawMoneyVO.getOptionType().equals(WithdrawMoneyVO.REFUSE)) {
            if (balanceOrder.getApplyforState().equals(WithdrawMoneyVO.REFUSE)) {
                return 1;
            }
            balanceOrder.setApplyforState(2);
            x = tGroupBalanceOrderMapper.updateByPrimaryKeySelective(balanceOrder);
            //发送提现失败的模板消息
            groupBalanceOrderUtil.sendWithdrawFailMsg(balanceOrder, wxuser.getWxuserId());
        } else {
            throw new ServiceException("非法操作");
        }
        return x;
    }


    @Override
    public ResultPage<List<WithdrawMoneyDetailsVO>> withdrawMoneyDetails(Integer pageNum, Integer pageSize,
                                                                         Integer searchType, String groupLeaderId,
                                                                         String appmodelId) {
        //0-全部记录 1-到账记录
        if (searchType.equals(0)) {
            PageHelper.startPage(pageNum, pageSize, "create_time desc");
        } else {
            PageHelper.startPage(pageNum, pageSize, "update_time desc");
        }
        List<GroupBalanceOrder> groupbalanceOrders = tGroupBalanceOrderMapper
                .selectWithdrawMoneyDetails(searchType, groupLeaderId, appmodelId);
        ResultPage<List<WithdrawMoneyDetailsVO>> resultPage = new ResultPage<>();
        resultPage.setTotle(new PageInfo<>(groupbalanceOrders).getTotal());
        List<WithdrawMoneyDetailsVO> withdrawMoneyDetailsVOS = BeanMapper
                .mapList(groupbalanceOrders, WithdrawMoneyDetailsVO.class);
        for (WithdrawMoneyDetailsVO withdrawMoneyDetailsVO : withdrawMoneyDetailsVOS) {
            withdrawMoneyDetailsVO.setCreateTimeValue(withdrawMoneyDetailsVO.getCreateTime().getTime());
        }
        //将团长信息封装进结果集
        //List<String> groupIds = groupbalanceOrders.stream().map(GroupbalanceOrder::getGroupLeaderId).collect
        // (Collectors.toList());
        //groupbalanceOrderUtil.packageResults(groupIds, withdrawMoneyDetailsVOS);
        resultPage.setList(withdrawMoneyDetailsVOS);
        return resultPage;
    }

    @Override
    public WithdrawMoneyDetailVO withdrawMoneyDetail(Long groupbalanceOrderId, String appmodelId) {
        GroupBalanceOrder balanceOrder = tGroupBalanceOrderMapper.selectByPrimaryKey(groupbalanceOrderId);
        WithdrawMoneyDetailVO detailVO = BeanMapper.map(balanceOrder, WithdrawMoneyDetailVO.class);
        //将团长信息封装进结果集
        groupBalanceOrderUtil.packageResult(balanceOrder.getGroupLeaderId(), detailVO);
        return detailVO;
    }

    @Override
    public int withdrawMoneyRemark(RemarkVO userRemarkVO) {
        List<GroupBalanceOrder> groupbalanceOrders = tGroupBalanceOrderMapper.selectByIds(userRemarkVO.getIds());
        AtomicInteger i = new AtomicInteger(0);
        if (userRemarkVO.getCoverType() == 0) {
            //不覆盖原有备注,过滤出非空备注的用户
            List<GroupBalanceOrder> collect = groupbalanceOrders.stream()
                    .filter(obj -> !StringUtils.isNotBlank(obj.getRemark())).collect(Collectors.toList());
            collect.forEach(obj -> {
                obj.setRemark(userRemarkVO.getRemark());
                i.set(tGroupBalanceOrderMapper.updateByPrimaryKeySelective(obj));
            });
        }
        if (userRemarkVO.getCoverType() == 1) {
            groupbalanceOrders.forEach(obj -> {
                obj.setRemark(userRemarkVO.getRemark());
                i.set(tGroupBalanceOrderMapper.updateByPrimaryKeySelective(obj));
            });
        }
        return i.get();
    }

    @Override
    public List<FinanceManagerVO> findanceManager(Integer pageNum, Integer pageSize, Integer searchType,
                                                  String groupbalanceOrderId, String groupName, String createTime,
                                                  String updateTime, String appmodelId,
                                                  HttpServletResponse response) {
        Map<String, Object> paramMap = new HashMap<>(8);
        paramMap.put("appmodelId", appmodelId);
        paramMap.put("searchType", searchType);
        if (StringUtils.isNotBlank(groupName)) {
            String name = "%";
            for (char c : groupName.toCharArray()) {
                name = name.concat(String.valueOf(c)).concat("%");
            }
            paramMap.put("groupName", name);
        }
        if (StringUtils.isNotBlank(groupbalanceOrderId)) {
            paramMap.put("groupbalanceOrderId", groupbalanceOrderId);
        }
        if (StringUtils.isNotBlank(createTime)) {
            String[] split = createTime.split(",");
            paramMap.put("startCreateTime", split[0]);
            if (StringUtils.isNotBlank(split[1])) {
                paramMap.put("endCreateTime", split[1]);
            }
        }
        if (StringUtils.isNotBlank(updateTime)) {
            String[] split = updateTime.split(",");
            paramMap.put("startUpdateTime", split[0]);
            if (StringUtils.isNotBlank(split[1])) {
                paramMap.put("endUpdateTime", split[1]);
            }
        }
        //0-待审核  1-已到账  2-已关闭
        if (searchType.equals(0) || searchType.equals(FinanceManagerVO.AUDITREFUSE)) {
            PageHelper.startPage(pageNum, pageSize, "create_time desc");
        } else if (searchType.equals(1)) {
            PageHelper.startPage(pageNum, pageSize, "update_time desc");
        }

        List<FinanceManagerVO> financeManagerVOS = tGroupBalanceOrderMapper.selectFinanceManager(paramMap);
        if (financeManagerVOS == null || financeManagerVOS.size() == 0) {
            return new ArrayList<>();
        }
        getGroupInfo(financeManagerVOS);
        return financeManagerVOS;
    }

    @Override
    public List<GroupBalanceOrder> findByGroupLeaderId(String groupLeaderId) {
        return tGroupBalanceOrderMapper.selectByGroupLeaderId(groupLeaderId);
    }

    @Override
    public void financeManagerExport(List<Long> groupbalanceOrderIds, HttpServletResponse response) {
        String ids = groupbalanceOrderIds.stream().map(Object::toString).collect(Collectors.joining(","));
        List<GroupBalanceOrder> groupbalanceOrders = tGroupBalanceOrderMapper.selectByIds(ids);
        if (groupbalanceOrders == null || groupbalanceOrders.size() == 0) {
            throw new ServiceException("数据为空");
        }
        List<FinanceManagerVO> financeManagerVOS = BeanMapper.mapList(groupbalanceOrders, FinanceManagerVO.class);
        getGroupInfo(financeManagerVOS);
        exportXXL(response, financeManagerVOS);
    }

    @Override
    public void deleteGroupbalanceOrder(List<Long> groupbalanceOrderIds) {
        Condition condition = new Condition(GroupBalanceOrder.class);
        condition.createCriteria().andIn("groupbalanceOrderId", groupbalanceOrderIds)
                .andIn("applyforState", Arrays.asList(0, 1));
        if (tGroupBalanceOrderMapper.selectCountByCondition(condition) > 0) {
            throw new ServiceException("不可删除待审核或已到账的记录");
        }
        String ids = groupbalanceOrderIds.stream().map(Object::toString).collect(Collectors.joining(","));
        tGroupBalanceOrderMapper.deleteByIds(ids);
    }

    @Override
    public BigDecimal countCumulativeCashWithdrawal(String groupLeaderId) {
        return tGroupBalanceOrderMapper.countCumulativeCashWithdrawal(groupLeaderId);
    }

    /**
     * 分装团长信息
     *
     * @param financeManagerVOS
     */
    private void getGroupInfo(List<FinanceManagerVO> financeManagerVOS) {
        List<String> groupIds = financeManagerVOS.stream().map(GroupBalanceOrder::getGroupLeaderId)
                .collect(Collectors.toList());
        Map<String, GroupLeader> groupLeaderMap = groupLeaderService.findByGroupleaderIds(groupIds).stream()
                .collect(Collectors.toMap(GroupLeader::getGroupLeaderId, v -> v));
        financeManagerVOS.forEach(obj -> {
            GroupLeader groupLeader = groupLeaderMap.get(obj.getGroupLeaderId());
            if (groupLeader != null) {
                obj.setGroupName(groupLeader.getGroupName());
                obj.setPhone(groupLeader.getGroupPhone());
            }
        });
    }

    private void exportXXL(HttpServletResponse response, List<FinanceManagerVO> financeManagerVOS) {
        ExcelWriter writer = ExcelUtil.getBigWriter();
        writer.addHeaderAlias("groupbalanceOrderId", "交易订单");
        writer.setColumnWidth(0, 20);
        writer.addHeaderAlias("groupName", "姓名");
        writer.addHeaderAlias("phone", "手机号");
        writer.setColumnWidth(2, 15);
        writer.addHeaderAlias("outbalance", "金额");
        writer.addHeaderAlias("createTime", "提交时间");
        writer.setColumnWidth(4, 20);
        writer.addHeaderAlias("outType", "提现方式");
        writer.addHeaderAlias("remark", "备注");
        writer.setColumnWidth(7, 15);
        writer.addHeaderAlias("applyforState", "状态");
        ArrayList<Map<String, Object>> rows = new ArrayList<>();
        for (FinanceManagerVO financeManagerVO : financeManagerVOS) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("groupbalanceOrderId", financeManagerVO.getGroupBalanceOrderId().toString());
            map.put("groupName", financeManagerVO.getGroupName());
            map.put("phone", financeManagerVO.getPhone());
            map.put("outbalance", financeManagerVO.getOutBalance().toString());
            map.put("createTime", DateUtil.format(financeManagerVO.getCreateTime(), " yyyy-MM-dd HH:mm"));
            if (financeManagerVO.getOutType().equals(1)) {
                map.put("outType", "微信钱包");
            }
            if (financeManagerVO.getOutType().equals(2)) {
                map.put("outType", "线下核销");
            }
            map.put("remark", financeManagerVO.getRemark());
            if (financeManagerVO.getApplyforState().equals(0)) {
                map.put("applyforState", "待审核");
            }
            if (financeManagerVO.getApplyforState().equals(1)) {
                map.put("applyforState", "通过审核");
            }
            if (financeManagerVO.getApplyforState().equals(2)) {
                map.put("applyforState", "拒绝");
            }
            rows.add(map);
        }
        try {
            // 一次性写出内容，使用默认样式
            writer.write(rows);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String fileName = DateUtil.currentSeconds() + ".xlsx";
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            writer.flush(response.getOutputStream());
        } catch (IOException e) {
            // 关闭writer，释放内存
            throw new ServiceException("文件下载失败");
        } finally {
            writer.close();
        }
    }


}
