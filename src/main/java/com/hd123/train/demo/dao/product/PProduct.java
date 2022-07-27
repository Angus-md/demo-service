/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service
 * 文件名： PProduct.java
 * 模块说明：
 * 修改历史：
 * 2022年07月14日 - XX - 创建。
 */
package com.hd123.train.demo.dao.product;/**
 @author CMD
 @create 2022-07-14 21:30
 */

import com.hd123.rumba.commons.jdbc.entity.PStandardEntity;
import com.hd123.train.demo.bean.Product;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Chenmingda
 * @since 1.0
 */
public class PProduct extends PStandardEntity {

    private static final long serialVersionUID = -7704832880838749302L;
    // 表名与字段名常量
    public static final String TABLE_NAME = "product";
    public static String NAME = "name";
    public static String CODE = "code";
    public static String SALEPRICE = "salePrice";
    public static String STATE = "state";

    public static final String[] COLUMNS = new String[]{
        UUID, NAME, CODE, SALEPRICE,STATE
    };


    public static Map<String, Object> forSaveNew(Product product) {
        Map<String, Object> fvm = new HashMap<>();
        fvm.put(UUID,product.getUuid());
        fvm.put(NAME,product.getName());
        fvm.put(CODE,product.getCode());
        fvm.put(SALEPRICE,product.getSalePrice());
        fvm.put(STATE,product.getState());
        return fvm;
    }
}