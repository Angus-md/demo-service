/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service 文件名： OrderController.java 模块说明： 修改历史： 2022年07月18日 - XX - 创建。
 */
package
    com.hd123.train.demo.controller.order;/**
 @author CMD
 @create 2022-07-18 14:10
 */

import com.hd123.rumba.commons.biz.query.QueryResult;
import com.hd123.train.demo.dao.order.OrderDao;
import com.hd123.train.demo.infrastructure.biz.BaseResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chenmingda
 * @since 1.0
 */
@RestController
@RequestMapping(value = "/v1/demo/order", produces = "application/json;charset=utf-8")
public class OrderController {

  @Autowired
  private OrderDao orderDao;

  /**
   * 查询指定订单
   * @param id
   * @return
   */
  @ApiOperation(value = "获取订单")
  @GetMapping(path = "/get/{id}")
  public BaseResponse getOrderByUuid(
      @ApiParam(value = "订单ID", required = true) @PathVariable("id") String id) {

    if (id == null){
      return BaseResponse.fail(503,"id值为空");
    }

    Order order = orderDao.getOrderByUuid(id);

    if (order == null){
      return BaseResponse.fail(503,"订单号错误或不存在！");
    }
    return BaseResponse.success(order);
  }

  /**
   * 新建订单
   * @param order
   * @return
   */
  @ApiOperation(value = "新建订单")
  @PostMapping(path = "/create")
  public BaseResponse createOrder(
      @ApiParam(value = "订单数据" , required = true)@RequestBody Order order ){

    //新建订单， 订单号需要不存在
    if (orderDao.isNotEmptyByBillNumber(order.getBillNumber())){
      return BaseResponse.fail(503,"订单号存在");
    }
    //执行新建

    return orderDao.saveOrder(order);
  }

  /**
   *
   * @param order
   * @return
   */
  @ApiOperation(value = "保存订单")
  @PostMapping(path = "/update")
  public BaseResponse update(
      @ApiParam(value = "订单数据",required = true)@RequestBody Order order){

    //判断 订单号是否存在   保存订单需要存在
    if (!orderDao.isNotEmptyByBillNumber(order.getBillNumber())){
      return BaseResponse.fail(503,"订单号错误或不存在！");
    }

    if (!Order.STATE.submited.toString().equals(order.getState()))
      return BaseResponse.fail(503,"订单未提交！");

    //是否修改成功

    return  orderDao.saveOrder(order);
  }


  @ApiOperation(value = "审核订单")
  @PostMapping(path = "/check")
  public BaseResponse checkOrder(
      @ApiParam(value = "订单数据" ,required = true)@RequestBody Order order){

    //判断 订单号是否存在   保存订单需要存在
    if (!orderDao.isNotEmptyByBillNumber(order.getBillNumber())){
      return BaseResponse.fail(503,"订单号错误或不存在！");
    }

//    订单状态必须已提交
    if (!Order.STATE.submited.toString().equals(order.getState())){
      return BaseResponse.fail(503,"订单未提交！");
    }

    return  orderDao.checkOrder(order);
  }

  /**
   * 审核订单  和  作废订单均需  判断订单状态
   * @param order
   * @return
   */
  @ApiOperation(value = "作废订单")
  @PostMapping(path = "/removeOrder")
  public BaseResponse removeOrder(
      @ApiParam(value = "订单数据",required = true)@RequestBody Order order){

    //判断 订单号是否存在   保存订单需要存在
    if (!orderDao.isNotEmptyByBillNumber(order.getBillNumber())){
      return BaseResponse.fail(503,"订单号错误或不存在！");
    }

//    订单状态必须已审核
      if (!Order.STATE.audited.toString().equals(order.getState())){
      return BaseResponse.fail(503,"订单未审核！");
    }

    return orderDao.invalidOrder(order);
  }

  @ApiOperation(value = "分页查询")
  @GetMapping(path = "/query/{page}")
  public BaseResponse pageQuery(
      @ApiParam(value = "查询页数",required = true)@PathVariable("page") Integer page){

    QueryResult<Order> orderQueryResult = orderDao.queryAll(page, 2);

    return BaseResponse.success(orderQueryResult);
  }


}