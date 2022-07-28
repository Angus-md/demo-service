/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service 文件名： OrderFilter.java 模块说明： 修改历史： 2022年07月28日 - XX - 创建。
 */
package
    com.hd123.train.demo.controller.order;/**
 @author CMD
 @create 2022-07-28 9:07
 */

import com.hd123.train.demo.infrastructure.biz.Filter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @author Chenmingda
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OrderFilter extends Filter {

  private String billNumber; //根据 billNumber  查询
  private List<String> idIn;
  private String idNameLike;

}