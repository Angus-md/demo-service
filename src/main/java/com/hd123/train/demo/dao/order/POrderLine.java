/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service 文件名： POrderLine.java 模块说明： 修改历史： 2022年07月18日 - XX - 创建。
 */
package
    com.hd123.train.demo.dao.order;/**
 @author CMD
 @create 2022-07-18 13:51
 */

import com.hd123.train.demo.controller.order.OrderLine;
import org.apache.commons.lang3.ObjectUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chenmingda
 * @since 1.0
 */
public abstract class POrderLine {

  public static final String TABLE_NAME = "orderline";

  public static final String UUID = "uuid";
  public static final String ORDERUUID = "orderUuid";
  public static final String LINENO = "lineNo";
  public static final String SKUID = "skuId";
  public static final String SKU_NAME = "sku_name";
  public static final String QTY = "qty";
  public static final String AMOUNT = "amount";

  public static final String[] COLUMNS = new String[]{
      UUID, ORDERUUID, LINENO, SKUID,SKU_NAME,QTY,AMOUNT
  };


  public static OrderLine mapRow(ResultSet rs, int rowNum) throws SQLException {
    OrderLine target = new OrderLine();
    target.setUuid(rs.getString(UUID));
    target.setOrderUuid(rs.getString(ORDERUUID));
    target.setLineNo(rs.getInt(LINENO));
    target.setSkuId(rs.getString(SKUID));
    target.setSku_name(rs.getString(SKU_NAME));
    target.setQty(rs.getBigDecimal(QTY));
    target.setAmount(rs.getBigDecimal(AMOUNT));
    return target;
  }

  public static Map<String, Object> toFieldValues(OrderLine entity) {
    Map<String, Object> fvm = new HashMap<>();
    fvm.put(UUID, ObjectUtils.defaultIfNull(entity.getUuid(), java.util.UUID.randomUUID().toString()));
    fvm.put(ORDERUUID, entity.getOrderUuid());
    fvm.put(LINENO, entity.getLineNo());
    fvm.put(SKUID, entity.getSkuId());
    fvm.put(SKU_NAME, entity.getSku_name());
    fvm.put(QTY, entity.getQty());
    fvm.put(AMOUNT, entity.getAmount());
    return fvm;
  }


}