/**
 * 版权所有(C)，XX有限公司，2022，所有权利保留。
 * <p>
 * 项目名： demo-service 文件名： OrderTest.java 模块说明： 修改历史： 2022年07月18日 - XX - 创建。
 */
package
    com.hd123.train.demo.test;/**
 @author CMD
 @create 2022-07-18 13:30
 */

import com.hd123.rumba.commons.biz.query.QueryResult;
import com.hd123.rumba.commons.jdbc.executor.BatchUpdater;
import com.hd123.rumba.commons.jdbc.sql.Predicates;
import com.hd123.rumba.commons.jdbc.sql.SelectBuilder;
import com.hd123.rumba.commons.jdbc.sql.SelectStatement;
import com.hd123.rumba.commons.jdbc.sql.UpdateBuilder;
import com.hd123.rumba.commons.jdbc.sql.UpdateStatement;
import com.hd123.train.demo.Application;
import com.hd123.train.demo.controller.order.Order;
import com.hd123.train.demo.controller.product.SKU;
import com.hd123.train.demo.dao.order.OrderDao;
import com.hd123.train.demo.dao.order.POrder;
import com.hd123.train.demo.dao.product.PSKU;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chenmingda
 * @since 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class OrderTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private OrderDao orderDao;

  @Test
  public void Test1(){

    String id = "123";

    SelectStatement select = new SelectBuilder()
        .select(POrder.COLUMNS)
        .from(POrder.TABLE_NAME)
        .build();

    List<Order> query = jdbcTemplate.query(select, POrder::mapRow);

    query.forEach(System.out::println);


  }

  @Test
  public void test2(){

//    2、遍历需求订单的详细     返回 空list 说明可以满足商品需求


    List<String > sk_ids = new ArrayList<>(); // 存放商品id
    sk_ids.add("111");sk_ids.add("222");

    SelectStatement sel = new SelectBuilder()
        .select(PSKU.COLUMNS)
        .from(PSKU.TABLE_NAME)
        .where(Predicates.in(null,PSKU.ID,sk_ids.toArray()))
        .build();

    List<SKU> sku_list = jdbcTemplate.query(sel, new BeanPropertyRowMapper<>(SKU.class));
    sku_list.forEach(System.out::println);


  }

  @Test
  public void Test3(){


    SelectStatement bii_num = new SelectBuilder()
        .select(POrder.UUID)
        .from(POrder.TABLE_NAME)
        .where(Predicates.equals(POrder.BILLNUMBER,"123"))
        .build();
    List<Order> query = jdbcTemplate.query(bii_num, new BeanPropertyRowMapper<>(Order.class));
    query.forEach(System.out::println);
  }


  @Test
  public void Test(){

//    需求数据
    ArrayList<String> quire_sku = new ArrayList<>();
    quire_sku.add("123");
    quire_sku.add("111");

    //库存数据
    SelectStatement sku_sel = new SelectBuilder()
        .select(PSKU.COLUMNS)
        .from(PSKU.TABLE_NAME)
        .where(Predicates.in(null,PSKU.ID,quire_sku.toArray()))
        .build();

    List<SKU> query = jdbcTemplate.query(sku_sel, new BeanPropertyRowMapper<>(SKU.class));
    query.forEach(System.out::println);

  }


  @Test
  public void test5(){

    QueryResult<Order> orderQueryResult = orderDao.queryAll(0, 10);

    List<Order> records = orderQueryResult.getRecords();

    for (Order o :records) {
      System.out.println(o.toString());
    }
  }


  @Test
  public void test6(){

    Map<String, BigDecimal> skuMap = new HashMap<>();
    BatchUpdater batchUpdater = new BatchUpdater(jdbcTemplate);

    skuMap.put("0011",new BigDecimal("100"));
    skuMap.put("0022",new BigDecimal("100"));


    for (Map.Entry<String, BigDecimal> sku: skuMap.entrySet() ) {
      UpdateStatement skuUpdate = new UpdateBuilder()
          .table(PSKU.TABLE_NAME)
          .setValue(PSKU.STOCK_QTY,sku.getValue())
          .where(Predicates.equals(PSKU.ID,sku.getKey()))
          .build();
      batchUpdater.add(skuUpdate);
    }
    batchUpdater.update();  // 执行



  }

}