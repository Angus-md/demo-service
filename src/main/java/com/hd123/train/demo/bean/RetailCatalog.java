/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service
 * 文件名： RetailCatalog.java
 * 模块说明：
 * 修改历史：
 * 2022年07月15日 - XX - 创建。
 */
package com.hd123.train.demo.bean;/**
 @author CMD
 @create 2022-07-15 15:12
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Chenmingda
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RetailCatalog {

    private String uuid;
    private String productUuid;
    private String beginDate;
    private String endDate;
    private double price;

}