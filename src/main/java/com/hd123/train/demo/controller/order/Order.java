package com.hd123.train.demo.controller.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hd123.rumba.commons.jdbc.entity.PStandardEntity;
import com.hd123.train.demo.dao.order.POrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("订单")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {

  @JsonIgnore
  private String uuid;
  @ApiModelProperty("单号")
  private String billNumber;
  @ApiModelProperty("下单日期")
  private Date orderTime;
  @ApiModelProperty("购买人")
  private String buyer;
  @ApiModelProperty("状态")
  private String state;
  @ApiModelProperty("订单金额")
  private BigDecimal amount;
  @ApiModelProperty("方式")
  private String deliverType;
  @ApiModelProperty("订单说明")
  private String remark;
  @ApiModelProperty(value = "订单明细")
  private List<OrderLine> descriptions = new ArrayList<>();

  public enum STATE {
    submited,audited,aborted
  }
  public enum DELIVERTYPE {
    selfPick,courier
  }


  public static class RowMapper extends PStandardEntity.RowMapper<Order> {
    @Override
    public Order mapRow(ResultSet rs, int i) throws SQLException {
      Order order = new Order();
      order.setUuid(rs.getString(POrder.UUID));
      order.setBillNumber(rs.getString(POrder.BILLNUMBER));
      order.setOrderTime(rs.getDate(POrder.ORDERTIME));
      order.setBuyer(rs.getString(POrder.BUYER));
      order.setState(rs.getString(POrder.STATE));
      order.setAmount(rs.getBigDecimal(POrder.AMOUT));
      order.setDeliverType(rs.getString(POrder.DELIVERTYPE));
      order.setRemark(rs.getString(POrder.REMARK));
      return order;
    }
  }


}
