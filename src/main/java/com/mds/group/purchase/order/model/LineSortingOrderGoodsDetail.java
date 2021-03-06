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

package com.mds.group.purchase.order.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The type Line sorting order goods detail.
 *
 * @author pavawi
 */
@Table(name = "t_line_sorting_order_goods_detail")
@Data
public class LineSortingOrderGoodsDetail {


    @Id
    @Column(name = "line_sorting_order_goods_detail_id")
    @ApiModelProperty(value = "线路分拣单详情")
    private Integer lineSortingOrderGoodsDetailId;


    @Column(name = "line_sorting_order_goods_id")
    @ApiModelProperty(value = "线路分拣单id")
    private Integer lineSortingOrderGoodsId;

    @Column(name = "community_id")
    @ApiModelProperty(value = "小区id")
    private Long communityId;

    @ApiModelProperty(value = "小区名")
    @Column(name = "community_name")
    private String communityName;

    @ApiModelProperty(value = "数量")
    @Column(name = "goods_number")
    private Integer goodsNumber;
}