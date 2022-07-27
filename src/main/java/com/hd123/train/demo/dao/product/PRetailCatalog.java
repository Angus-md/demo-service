/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service
 * 文件名： PRetailcatalog.java
 * 模块说明：
 * 修改历史：
 * 2022年07月14日 - XX - 创建。
 */
package com.hd123.train.demo.dao.product;/**
 @author CMD
 @create 2022-07-14 22:52
 */

import com.hd123.rumba.commons.jdbc.entity.PStandardEntity;
import com.hd123.train.demo.bean.RetailCatalog;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Chenmingda
 * @since 1.0
 */
public class PRetailCatalog extends PStandardEntity {

    private static final long serialVersionUID = 4576794762076260192L;

    public static final String TABLE_NAME = "retailcatalog";
    public static String PRODUCTUUID = "productUuid";
    public static String BEGINDATE = "beginDate";
    public static String ENDDATE = "endDate";
    public static String PRICE = "price";


    public static Map<String, Object> forSaveNew(RetailCatalog retailCatalog) {
        Map<String, Object> fvm = new HashMap<>();
        fvm.put(UUID,retailCatalog.getUuid());
        fvm.put(PRODUCTUUID,retailCatalog.getProductUuid());
        fvm.put(BEGINDATE,retailCatalog.getBeginDate());
        fvm.put(ENDDATE,retailCatalog.getEndDate());
        fvm.put(PRICE,retailCatalog.getPrice());
        return fvm;
    }
}