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

package com.mds.group.purchase.user.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * The type Withdraw money apply vo.
 *
 * @author pavawi
 */
@Data
public class WithdrawMoneyApplyVO {

    @ApiModelProperty(value = "团长id")
    @NotBlank
    private String groupLeaderId;

    @ApiModelProperty(value = "formId", hidden = true)
    private String formId;

    @ApiModelProperty(value = "申请提现金额")
    @NotNull
    private BigDecimal withdrawMoney;

    @ApiModelProperty(value = "提现方式 1-微信钱包  2-线下核销")
    @NotNull
    private Integer optionType;
    @ApiModelProperty(hidden = true)
    private String appmodelId;

    /**
     * The interface Option type.
     *
     * @author pavawi
     */
    public interface OptionType {
        // 提现方式
        /**
         * 微信钱包
         */
        int WX = 1;
        /**
         * 线下核销
         */
        int OFFLINE = 2;
    }

}
