/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service 文件名： POrder.java 模块说明： 修改历史： 2022年07月18日 - XX - 创建。
 */
package
    com.hd123.train.demo.dao.order;/**
 @author CMD
 @create 2022-07-18 10:49
 */

import com.hd123.train.demo.controller.order.Order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chenmingda
 * @since 1.0
 */
public  class POrder {

  public static final String TABLE_NAME = "sr_order";
  public static final String UUID = "uuid";
  public static final String BILLNUMBER = "billNumber";
  public static final String ORDERTIME = "orderTime";
  public static final String BUYER = "buyer";
  public static final String STATE = "state";
  public static final String AMOUT = "amount";
  public static final String DELIVERTYPE = "deliverType";
  public static final String REMARK = "remark";
  public static final String LINES = "line";

  public static final String[] COLUMNS = new String[]{
      UUID, BILLNUMBER, ORDERTIME, BUYER, STATE, AMOUT, REMARK, DELIVERTYPE, REMARK, LINES
  };



  public static Order mapRow(ResultSet rs, int rowNum) throws SQLException {
    Order target = new Order();
    target.setUuid(rs.getString(UUID));
    target.setBillNumber(rs.getString(BILLNUMBER));
    target.setOrderTime(rs.getDate(ORDERTIME));
    target.setBuyer(rs.getString(BUYER));
    target.setState(rs.getString(STATE));
    target.setAmount(rs.getBigDecimal(AMOUT));
    target.setDeliverType(rs.getString(DELIVERTYPE));
    target.setRemark(rs.getString(REMARK));
    return target;
  }


  public static Map<String, Object> toFieldValues(Order entity) {
    Map<String, Object> fvm = new HashMap<>();
    fvm.put(UUID, entity.getUuid());
    fvm.put(BILLNUMBER, entity.getBillNumber());
    fvm.put(ORDERTIME, entity.getOrderTime());
    fvm.put(BUYER, entity.getBuyer());
    fvm.put(STATE, entity.getState());
    fvm.put(AMOUT, entity.getAmount());
    fvm.put(DELIVERTYPE, entity.getDeliverType());
    fvm.put(REMARK, entity.getRemark());
    return fvm;
  }



}