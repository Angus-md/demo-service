/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service 文件名： OrderDao.java 模块说明： 修改历史： 2022年07月18日 - XX - 创建。
 */
package
    com.hd123.train.demo.dao.order;/**
 @author CMD
 @create 2022-07-18 13:30
 */

import com.hd123.rumba.commons.biz.query.QueryResult;
import com.hd123.rumba.commons.jdbc.executor.BatchUpdater;
import com.hd123.rumba.commons.jdbc.executor.JdbcPagingQueryExecutor;
import com.hd123.rumba.commons.jdbc.sql.DeleteBuilder;
import com.hd123.rumba.commons.jdbc.sql.InsertBuilder;
import com.hd123.rumba.commons.jdbc.sql.InsertStatement;
import com.hd123.rumba.commons.jdbc.sql.Predicates;
import com.hd123.rumba.commons.jdbc.sql.SelectBuilder;
import com.hd123.rumba.commons.jdbc.sql.SelectStatement;
import com.hd123.rumba.commons.jdbc.sql.UpdateBuilder;
import com.hd123.train.demo.controller.order.Order;
import com.hd123.train.demo.controller.order.OrderLine;
import com.hd123.train.demo.dao.product.SKUDao;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Chenmingda
 * @since 1.0
 */
@Repository
public class OrderDao {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private SKUDao skuDao;


  public  enum deliverTypeRequire {
    selfPick,courier
  }

  /**
   * 根据 id 获取订单
   * @param uuid
   * @return
   */
  public Order getOrderByUuid(String uuid){

//    查找指定id
    SelectStatement select =  new SelectBuilder()
        .select(POrder.COLUMNS)
        .from(POrder.TABLE_NAME)
        .where(Predicates.equals(POrder.UUID,uuid))
        .build();

//    获取订单记录进行封装后  获取订单详细
    List<Order> list = jdbcTemplate.query(select, POrder::mapRow);
    if (list.isEmpty()) {return null;}
    Order order = list.get(0);

//    完善订单信息
    SelectStatement sel  = new SelectBuilder()
        .select(POrderLine.COLUMNS)
        .from(POrderLine.TABLE_NAME)
        .where(Predicates.equals(POrderLine.ORDERUUID,uuid))
        .build();

    order.setDescriptions(jdbcTemplate.query(sel, POrderLine::mapRow));
    return order;
  }

  /**
   * 根据 billNumber 获取订单
   * @param billNumber
   * @return
   */
  public Order getOrderByBillNumber(String billNumber){

//    查找指定billNumber
    SelectStatement select =  new SelectBuilder()
        .select(POrder.COLUMNS)
        .from(POrder.TABLE_NAME)
        .where(Predicates.equals(POrder.BILLNUMBER,billNumber))
        .build();

//    获取订单记录进行封装后  获取订单详细
    List<Order> list = jdbcTemplate.query(select, POrder::mapRow);
    if (list.isEmpty()) {return null;}
    Order order = list.get(0);

//    完善订单信息
    SelectStatement sel  = new SelectBuilder()
        .select(POrderLine.COLUMNS)
        .from(POrderLine.TABLE_NAME)
        .where(Predicates.equals(POrderLine.ORDERUUID,order.getUuid()))
        .build();
    order.setDescriptions(jdbcTemplate.query(sel, POrderLine::mapRow));

    return order;
  }



  /**
   * 自动产生订单号，需要判断商品剩余库存是否足够。保存后的订单状态是已提交
   * @param order
   */
  public boolean saveOrder(Order order) {

//    如果是修改则判断状态是否是已经提交
    if(isNotEmptyByBillNumber(order.getBillNumber()))
      if (!"submited".equals(order.getState()))
        return false;
//    订单的运送方式是否规范
      if (!EnumUtils.isValidEnum(deliverTypeRequire.class, order.getDeliverType()))
        return false;

    // 1、获取 订单对象里 每个订单详细对象
    List<OrderLine> descriptions = order.getDescriptions();

//    所需要的商品 id 和  数量
    for (OrderLine orderLine: descriptions) {
//    校验需求商品的id是否为空
      if (StringUtils.isBlank(orderLine.getSkuId()) || orderLine.getQty() == null)
        return false;
//    需求商品库存是否存在   商品库存是否为空
      if (skuDao.get(orderLine.getSkuId()) == null || skuDao.get(orderLine.getSkuId()).getStockQty() == null )
        return false;
//      判断库存是否足够
      if (orderLine.getQty().compareTo(skuDao.get(orderLine.getSkuId()).getStockQty()) > 0){
        return false;
      }
    }

//  库存满足需求开始创建订单
//  判断传入的参数  是空 则新增   非空则获取原有id更新
    BatchUpdater batchUpdater = new BatchUpdater(jdbcTemplate);

    if(isNotEmptyByBillNumber(order.getBillNumber())){
      String orderId = getOrderByBillNumber(order.getBillNumber()).getUuid();
//      更新
      jdbcTemplate.update(new UpdateBuilder()
          .table(POrder.TABLE_NAME)
          .setValue(POrder.UUID,orderId)
          .setValue(POrder.BILLNUMBER,order.getBillNumber())
          .setValue(POrder.ORDERTIME,order.getOrderTime())
          .setValue(POrder.BUYER,order.getBuyer())
          .setValue(POrder.STATE,order.getState())
          .setValue(POrder.AMOUT,order.getAmount())
          .setValue(POrder.DELIVERTYPE,order.getDeliverType())
          .setValue(POrder.REMARK,order.getRemark())
          .where(Predicates.equals(POrder.BILLNUMBER,order.getBillNumber()))
          .build());

      jdbcTemplate.update(new DeleteBuilder()
          .table(POrderLine.TABLE_NAME)
          .where(Predicates.equals(POrderLine.ORDERUUID,orderId))
          .build());
      //      更新订单详细
      for (OrderLine orderLine: descriptions) {
        orderLine.setOrderUuid(orderId);
        InsertStatement insert = new InsertBuilder()
            .table(POrderLine.TABLE_NAME)
            .addValues(POrderLine.toFieldValues(orderLine))
            .build();
        batchUpdater.add(insert); // 添加语句
      }

    }else {
//      新增
      Order target = new Order(
          UUID.randomUUID().toString(),
          order.getBillNumber(),
          order.getOrderTime(),
          order.getBuyer(),
          "submited",
          order.getAmount(),
          order.getDeliverType(),
          order.getRemark(),
          order.getDescriptions());

      jdbcTemplate.update(new InsertBuilder()
          .table(POrder.TABLE_NAME)
          .addValues(POrder.toFieldValues(target))
          .build());

//      增加订单详细
      for (OrderLine orderLine: descriptions) {
        orderLine.setOrderUuid(target.getUuid());
        InsertStatement insert = new InsertBuilder()
            .table(POrderLine.TABLE_NAME)
            .addValues(POrderLine.toFieldValues(orderLine))
            .build();
        batchUpdater.add(insert); // 添加语句
      }
    }
    batchUpdater.update();  // 执行
    return true;
  }

//  返回布尔类型
  public boolean isNotEmptyByBillNumber(String billNumber){
    SelectStatement sel = new SelectBuilder()
        .select(POrder.UUID,POrder.BILLNUMBER)
        .from(POrder.TABLE_NAME)
        .where(Predicates.equals(POrder.BILLNUMBER, billNumber))
        .build();
    List<Order> query = jdbcTemplate.query(sel, new BeanPropertyRowMapper<>(Order.class));
    return !query.isEmpty();  // 空 true  返回  false    |   存在  false 返回true
  }

  /**
   * 审核订单
   * @param order
   * @return
   */
  public boolean checkOrder(Order order) {

//    订单的运送方式是否规范
    if (!EnumUtils.isValidEnum(deliverTypeRequire.class, order.getDeliverType()))
      return false;

    //获取所有订单详细对象列表
    List<OrderLine> descriptions = order.getDescriptions();
    Map<String, BigDecimal> skuMap = new HashMap<>();

//    所需要的商品 id 和  数量
    for (OrderLine orderLine: descriptions) {
//    校验需求商品的id是否为空
      if (StringUtils.isBlank(orderLine.getSkuId()) || orderLine.getQty() == null)
        return false;
//    需求商品库存是否存在   商品库存是否为空
      if (skuDao.get(orderLine.getSkuId()) == null || skuDao.get(orderLine.getSkuId()).getStockQty() == null )
        return false;
      skuMap.put(skuDao.get(orderLine.getSkuId()).getId(),skuDao.get(orderLine.getSkuId()).getStockQty().subtract(orderLine.getQty()) );
    }

    if(!skuDao.batchUpdateSkuStock(skuMap))
      return false;

    jdbcTemplate.update( new UpdateBuilder()
        .table(POrder.TABLE_NAME)
        .setValue(POrder.STATE, "audited")
        .where(Predicates.equals(POrder.BILLNUMBER, order.getBillNumber()))
        .build());

    return true;
  }

  /**
   *订单作废
   * @return
   */
  public boolean invalidOrder(Order order) {

//    订单的运送方式是否规范
    if (!EnumUtils.isValidEnum(deliverTypeRequire.class, order.getDeliverType()))
      return false;

    //获取所有订单详细对象列表
    List<OrderLine> descriptions = order.getDescriptions();
    Map<String, BigDecimal> skuMap = new HashMap<>();

//    所需要的商品 id 和  数量
    for (OrderLine orderLine: descriptions) {
//    校验需求商品的id是否为空
      if (StringUtils.isBlank(orderLine.getSkuId()) || orderLine.getQty() == null)
        return false;
//    需求商品库存是否存在   商品库存是否为空
      if (skuDao.get(orderLine.getSkuId()) == null || skuDao.get(orderLine.getSkuId()).getStockQty() == null )
        return false;
      skuMap.put(skuDao.get(orderLine.getSkuId()).getId(),skuDao.get(orderLine.getSkuId()).getStockQty().add(orderLine.getQty()) );
    }

    if(!skuDao.batchUpdateSkuStock(skuMap))
      return false;

    jdbcTemplate.update( new UpdateBuilder()
        .table(POrder.TABLE_NAME)
        .setValue(POrder.STATE, "aborted")
        .where(Predicates.equals(POrder.BILLNUMBER, order.getBillNumber()))
        .build());
    return true;
  }

  /**
   * 分页查询
   * @param page
   * @param pageSize
   * @return
   */
  public QueryResult<Order> queryAll(int page, int pageSize) {
    SelectStatement select = new SelectBuilder()
        .select(POrder.COLUMNS)
        .from(POrder.TABLE_NAME)
        .orderBy(POrder.BILLNUMBER)
        .build();
    JdbcPagingQueryExecutor<Order> executor = new JdbcPagingQueryExecutor<Order>(jdbcTemplate, new Order.RowMapper() ); // (1)
    return executor.query(select, page, pageSize); // (2)
  }

}