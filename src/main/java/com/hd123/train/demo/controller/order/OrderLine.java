/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service 文件名： OrderDescription.java 模块说明： 修改历史： 2022年07月18日 - XX - 创建。
 */
package
    com.hd123.train.demo.controller.order;/**
 @author CMD
 @create 2022-07-18 10:47
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Chenmingda
 * @since 1.0
 */

@Data
@ApiModel("订单明细")
@ToString
public class OrderLine {

  @JsonIgnore
  private String uuid;

  @ApiModelProperty("订单uuid")
  private String orderUuid;

  @ApiModelProperty("行号")
  private Integer lineNo	;

  @ApiModelProperty("商品id")
  private String skuId;

  @ApiModelProperty("商品名")
  private String sku_name;

  @ApiModelProperty("数量")
  private BigDecimal qty;

  @ApiModelProperty("金额")
  private BigDecimal amount;

}
