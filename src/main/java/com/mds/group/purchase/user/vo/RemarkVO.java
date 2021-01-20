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

/**
 * The type Remark vo.
 *
 * @author pavawi
 */
@Data
public class RemarkVO {

    @ApiModelProperty(value = "团长ids|提现订单id:多个id用逗号分隔")
    @NotBlank
    private String ids;
    @ApiModelProperty(value = "备注内容")
    @NotBlank
    private String remark;
    @ApiModelProperty(value = "是否覆盖备注  0-不覆盖,1-覆盖")
    @NotNull
    private Integer coverType;
}
