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
import com.hd123.train.demo.controller.order.OrderFilter;
import com.hd123.train.demo.controller.order.OrderLine;
import com.hd123.train.demo.controller.product.SKU;
import com.hd123.train.demo.dao.product.SKUDao;
import com.hd123.train.demo.infrastructure.biz.BaseResponse;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
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

  /**
   * 根据 id 获取订单
   * @param uuid
   * @return order
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
  public BaseResponse saveOrder(Order order) {

      // 判断运送方式是否规范
      if (!EnumUtils.isValidEnum(Order.DELIVERTYPE.class, order.getDeliverType())){
        return BaseResponse.fail(503,"订单运送方式错误或未填写！");
      }

    // 判断需求的订单是否 合法
    List<OrderLine> descriptions = order.getDescriptions();
    for (OrderLine orderLine: descriptions) {
      //校验需求商品的id是否为空
      if (StringUtils.isBlank(orderLine.getSkuId()) || orderLine.getQty() == null){
        return BaseResponse.fail(503,"商品ID错误或未填写！");
      }
    }
//    获取 库存商品信息
    Map<String,BigDecimal> skuIdQty =  skuStockMap(descriptions);

    //进入判断
    for (OrderLine orderLine: descriptions) {
      //需求商品库存是否存在   商品库存是否为空
      if (!skuIdQty.containsKey(orderLine.getSkuId()) || skuIdQty.get(orderLine.getSkuId()) == null  ){
        return BaseResponse.fail(503,"库存无该商品信息！");
      }
      System.out.println("orderLine.getSkuId()=" + orderLine.getSkuId()  + "  skuIdQty.containsKey(orderLine.getSkuId()) " + skuIdQty.containsKey(orderLine.getSkuId()));
      // 判断库存是否足够
      if (orderLine.getQty().compareTo(skuIdQty.get(orderLine.getSkuId())) > 0){
        return BaseResponse.fail(503,"库存不足！");
      }
    }

  //  库存满足需求    开始创建订单
  //  判断传入的参数  是空 则新增   非空则获取原有id更新
    if(getOrderByBillNumber(order.getBillNumber()) != null){
//      更新
      String orderId = getOrderByBillNumber(order.getBillNumber()).getUuid();
      order.setUuid(orderId);
      jdbcTemplate.update(new UpdateBuilder()
          .table(POrder.TABLE_NAME)
          .setValues(POrder.toFieldValues(order))
          .build());
      updateOrderLine(descriptions,orderId);
    }else {
//      新增
      order.setUuid(UUID.randomUUID().toString());
      order.setState(Order.STATE.submited.toString());

      jdbcTemplate.update(new InsertBuilder()
          .table(POrder.TABLE_NAME)
          .addValues(POrder.toFieldValues(order))
          .build());

      updateOrderLine(descriptions,order.getUuid());
    }

    return BaseResponse.fail(200,"保存成功！");
  }

  private Map<String,BigDecimal> skuStockMap(List<OrderLine> descriptions) {
    List<String> skuIds = new ArrayList<>();
    for (OrderLine orderLine: descriptions) {
      skuIds.add(orderLine.getSkuId());
    }

    //存放库存map  id-qty
    List<SKU> skus = skuDao.querySkusById(skuIds);
    Map<String,BigDecimal> skuIdQty = new  HashMap<>();
    for (SKU sku:skus) {
      skuIdQty.put(sku.getId(),sku.getStockQty());
    }
    return  skuIdQty;
  }

  /**
   * 更新订单详细
   * @param orderLines
   * @param orderId
   */
  public void updateOrderLine(List<OrderLine> orderLines,String orderId){

    // 删除原有订单详细
    jdbcTemplate.update(new DeleteBuilder()
        .table(POrderLine.TABLE_NAME)
        .where(Predicates.equals(POrderLine.ORDERUUID,orderId))
        .build());

    //批量插入订单详细订单详细
    BatchUpdater batchUpdater = new BatchUpdater(jdbcTemplate);
    for (OrderLine orderLine: orderLines) {
      orderLine.setOrderUuid(orderId);
      InsertStatement insert = new InsertBuilder()
          .table(POrderLine.TABLE_NAME)
          .addValues(POrderLine.toFieldValues(orderLine))
          .build();
      batchUpdater.add(insert); // 添加语句
    }
    batchUpdater.update();  // 执行
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
  public BaseResponse checkOrder(Order order) {

//    订单的运送方式是否规范
    if (!EnumUtils.isValidEnum(Order.DELIVERTYPE.class, order.getDeliverType()))
      return BaseResponse.fail(503,"订单运送方式错误或未填写！");

    //获取所有订单详细对象列表
    List<OrderLine> descriptions = order.getDescriptions();
    Map<String, BigDecimal> newSkuIdStockMap = new HashMap<>();

    //    获取 库存商品信息
    Map<String,BigDecimal> oldSkuIdStockMap =  skuStockMap(descriptions);


//    所需要的商品 id 和  数量
    for (OrderLine orderLine: descriptions) {
//    校验需求商品的id是否为空
      if (StringUtils.isBlank(orderLine.getSkuId()) || orderLine.getQty() == null)
        return BaseResponse.fail(503,"商品信息错误或未填写！");
//    需求商品库存是否存在   商品库存是否为空
      if (!oldSkuIdStockMap.containsKey(orderLine.getSkuId())  ||  oldSkuIdStockMap.get(orderLine.getSkuId()) == null )
        return BaseResponse.fail(503,"库存商品不足-----------！");
      newSkuIdStockMap.put(skuDao.get(orderLine.getSkuId()).getId(),skuDao.get(orderLine.getSkuId()).getStockQty().subtract(orderLine.getQty()) );
    }

//    批量更新库存
    skuDao.batchUpdateSkuStock(newSkuIdStockMap);
//    更新状态
    orderStateUpdate(order.getBillNumber(),Order.STATE.audited);
    return BaseResponse.fail(503,"审核成功！");
  }

  /**
   *订单作废
   * @return
   */
  public BaseResponse invalidOrder(Order order) {

//    订单的运送方式是否规范
    if (!EnumUtils.isValidEnum(Order.DELIVERTYPE.class, order.getDeliverType()))
      return BaseResponse.fail(503,"订单运送方式错误或未填写！");

    //获取所有订单详细对象列表
    List<OrderLine> descriptions = order.getDescriptions();
    Map<String, BigDecimal> newSkuIdStockMap = new HashMap<>();

    //    获取 库存商品信息
    Map<String,BigDecimal> oldSkuIdStockMap =  skuStockMap(descriptions);


//    所需要的商品 id 和  数量
    for (OrderLine orderLine: descriptions) {
//    校验需求商品的id是否为空
      if (StringUtils.isBlank(orderLine.getSkuId()) || orderLine.getQty() == null)
        return BaseResponse.fail(503,"商品信息错误或未填写！");
//    需求商品库存是否存在   商品库存是否为空
      if (!oldSkuIdStockMap.containsKey(orderLine.getSkuId())  ||  oldSkuIdStockMap.get(orderLine.getSkuId()) == null )
        return BaseResponse.fail(503,"库存商品不足！");
      newSkuIdStockMap.put(skuDao.get(orderLine.getSkuId()).getId(),skuDao.get(orderLine.getSkuId()).getStockQty().subtract(orderLine.getQty()) );
    }

//    批量更新库存
    skuDao.batchUpdateSkuStock(newSkuIdStockMap);
//    更新状态
    orderStateUpdate(order.getBillNumber(),Order.STATE.aborted);
    return BaseResponse.fail(503,"审核成功！");
  }

  private boolean orderStateUpdate(String str,Order.STATE state) {
    jdbcTemplate.update( new UpdateBuilder()
        .table(POrder.TABLE_NAME)
        .setValue(POrder.STATE, state.toString())
        .where(Predicates.equals(POrder.BILLNUMBER,str))
        .build());
    return true;
  }


  /**
   * 分页查询
   * @param page
   * @param pageSize
   * @return
   */

//  借鉴sku query
  public QueryResult<Order> queryAll(int page, int pageSize) {
    SelectStatement select = new SelectBuilder()
        .select(POrder.COLUMNS)
        .from(POrder.TABLE_NAME)
        .orderBy(POrder.BILLNUMBER)
        .build();
    JdbcPagingQueryExecutor<Order> executor = new JdbcPagingQueryExecutor<Order>(jdbcTemplate, new Order.RowMapper() ); // (1)
    return executor.query(select, page, pageSize); // (2)
  }

  /**
   * 条件分页查询
   * @param filter
   * @return
   */
  public QueryResult<Order> query(OrderFilter filter) {
    SelectStatement select = new SelectBuilder()
        .select(POrder.COLUMNS).from(POrder.TABLE_NAME)
        .build();
    if (!StringUtils.isBlank(filter.getBillNumber())) {
      select.where(Predicates.equals(POrder.BILLNUMBER, filter.getBillNumber()));
    }


    JdbcPagingQueryExecutor executor = new JdbcPagingQueryExecutor(jdbcTemplate, POrder::mapRow);
    return executor.query(select, filter.getPage(), filter.getPageSize());
  }

}