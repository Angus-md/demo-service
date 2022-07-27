package com.hd123.train.demo.test;

import com.hd123.rumba.commons.jdbc.executor.BatchUpdater;
import com.hd123.rumba.commons.jdbc.sql.DeleteBuilder;
import com.hd123.rumba.commons.jdbc.sql.DeleteStatement;
import com.hd123.rumba.commons.jdbc.sql.InsertBuilder;
import com.hd123.rumba.commons.jdbc.sql.InsertStatement;
import com.hd123.rumba.commons.jdbc.sql.Predicates;
import com.hd123.rumba.commons.jdbc.sql.SelectBuilder;
import com.hd123.rumba.commons.jdbc.sql.SelectStatement;
import com.hd123.rumba.commons.jdbc.sql.UpdateBuilder;
import com.hd123.rumba.commons.jdbc.sql.UpdateStatement;
import com.hd123.train.demo.Application;
import com.hd123.train.demo.bean.Product;
import com.hd123.train.demo.bean.RetailCatalog;
import com.hd123.train.demo.dao.product.PProduct;
import com.hd123.train.demo.dao.product.PRetailCatalog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author cRazy
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DemoTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 案例测试
   */
  @Test
  public void test1() {
    InsertStatement insert = new InsertBuilder()
            .table("SR_SKU")
            .addValue("uuid", UUID.randomUUID().toString())
            .addValue("spuid", "0001")
            .addValue("id", "0001")
            .addValue("name", "Apple/苹果 iPhone 11")
            // .addValue("image", "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1575191341219&di=deccfc3eef04af955e53d9cf0243fbad&imgtype=0&src=http%3A%2F%2Fdingyue.nosdn.127.net%2FoFrbOsoSF3PLjusd8gA3Y1O0tg6erCdGopyuKM1Ef4T9h1523946901622.jpg")
            .addValue("image", "https://th.bing.com/th/id/OIP.S3QOsgnlzrpbpAdOXF4YMAHaEo?pid=ImgDet&rs=1")
            .addValue("marketPrice", new BigDecimal("1299"))
            .addValue("price", new BigDecimal("999"))
            .build();
    jdbcTemplate.update(insert);
  }

  /**
   * 练习1-1
   * 随机插入1000条记录
   * 随机插入0-50条关联的RETAILCATALOG表记录。
   *        每个商品的 RETAILCATALOG 的 BEGINDATE ~ ENDDATE 不能重叠。
   *        RETAILCATALOG 的PRICE 从100~999 不等。
   *        5% 的商品无R
   *        5% 的商品，初始StateETAILCATALOG=999
   */
  @Test
  public void practiceUnitTestOne(){

    //要插入的 商品记录数
    int productRecord = 20;
    // 设置 5%
    double productPercentNum = 0.05;
    //定义 销售表的记录数
    int logRecord = 0;
    //设置随机函数
    Random insertRandom = new Random(100);
//    设置销售目录的日期格式
    DateTimeFormatter logDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

//    商品列表
    List<Product> productAll = new ArrayList<>();
//    销售目录列表
    List<RetailCatalog> retailLogs = new ArrayList<>();

//    设置参数
    for (int i = 1; i <= productRecord ; i++) {
      Product product;
      if (i > productRecord * productPercentNum){
        product = new Product(
            UUID.randomUUID().toString(),
            RandomStringUtils.randomAlphanumeric(6),
            RandomStringUtils.randomAlphanumeric(6),
            insertRandom.nextInt(900) + 100,
            0);
         productAll.add(product);
//         每个商品的销售记录不一
        logRecord = insertRandom.nextInt(50);
        for (int j = 1; j <= logRecord ; j++) {
          retailLogs.add(new RetailCatalog(
              UUID.randomUUID().toString(),
              product.getUuid(),
              logDateTime.format(LocalDateTime.now()),
              logDateTime.format(LocalDateTime.now()),
              product.getSalePrice()
          ));
        }
      }else {
        productAll.add(new Product(
            UUID.randomUUID().toString(),
            RandomStringUtils.randomAlphanumeric(6),
            RandomStringUtils.randomAlphanumeric(6),
            insertRandom.nextInt(900) + 100,
            999));
      }
    }

//    将商品列表  批量插入
    BatchUpdater batchUpdater = new BatchUpdater(jdbcTemplate);
    for (Product product : productAll) {
      InsertStatement insert = new InsertBuilder() //
          .table(PProduct.TABLE_NAME) //
          .values(PProduct.forSaveNew(product)) //
          .build();
      batchUpdater.add(insert); // 添加语句
    }
    for (RetailCatalog retailCatalog : retailLogs) {
      InsertStatement insert = new InsertBuilder() //
          .table(PRetailCatalog.TABLE_NAME) //
          .values(PRetailCatalog.forSaveNew(retailCatalog)) //
          .build();
      batchUpdater.add(insert); // 添加语句
    }
    batchUpdater.update();  // 执行

  }

  /**
   * 练习2-1  UpdateBuilder 构建UpdateStatement ，修改指定商品的PRICE。
   */
  @Test
  public void UpdateTest1() {

    String uuid = "1366ecdd-4c4c-42ad-ab79-dd9ed4baca60";
    double up_price = 9999.9999;

    // 修改指定商品的PRICE。
    UpdateStatement update_PRICE = new UpdateBuilder().table(PProduct.TABLE_NAME)
            .setValue(PProduct.SALEPRICE, up_price)
            .where((Predicates.equals(PProduct.UUID, uuid)))
            .build();
    jdbcTemplate.update(update_PRICE);
    System.out.println("修改成功");
  }

  /**
   * 练习2-2 使用DeleteBuilder 构建 DeleteStatement ，删除指定的商品。
   */
  @Test
  public void UpdateTest2(){
    String uuid = "1366ecdd-4c4c-42ad-ab79-dd9ed4baca60";

    DeleteStatement delete_product = new DeleteBuilder()
            .table(PProduct.TABLE_NAME)
            .where((Predicates.equals(PProduct.UUID, uuid)))
            .build();
    jdbcTemplate.update(delete_product);
    System.out.println("删除成功");
  }

  /**
   * 练习2-3 UpdateBuilder 构建UpdateStatement ，将无 RETAILCATALOG  的商品，状态修改为999。
   */
  @Test
  public void UpdateTest3(){
//    UPDATE product
//    set product.state = '999'
//    WHERE NOT EXISTS(
//        SELECT 1
//        FROM retailcatalog
//        WHERE product.uuid = retailcatalog.productUuid
//
//    )
    UpdateStatement stateUpdate = new UpdateBuilder()
        .table(PProduct.TABLE_NAME)
        .setValue(PProduct.STATE,999)
        .where(Predicates.notExists(
            new SelectBuilder()
                .select("1")
                .from(PRetailCatalog.TABLE_NAME)
                .where(Predicates.equals(PProduct.TABLE_NAME,PProduct.UUID,PRetailCatalog.TABLE_NAME,PRetailCatalog.PRODUCTUUID))
            .build()

        ))
        .build();
    jdbcTemplate.update(stateUpdate);
  }

  /**
     * 练习3-1 使用SelectBuilder构建SelectStatement 查询 PRODUCT.PRICE > 100 的商品。
   */
  @Test
  public void selectTest1(){

    SelectStatement select = new SelectBuilder()
            .from(PProduct.TABLE_NAME)
            .where(Predicates.greater(PProduct.SALEPRICE,800))
            .build();
    List<Product> query = jdbcTemplate.query(select, new BeanPropertyRowMapper<>(Product.class));
    System.out.println("查询成功");
    query.forEach(System.out::println);
  }

  /**
   * 3-2 查询指定代码的商品，在指定日期的销售目录PRICE。
   */
  @Test
  public void selectTest(){
    //使用了左连接 查询
    // SELECT retailcatalog.PRICE
    // FROM retailcatalog LEFT JOIN product
    // on retailcatalog.PRODUCTUUID = product.uuid
    // where  product.`code` = 'v6FN3t' and retailcatalog.BEGINDATE = '2022-07-15 04:09:26'

    String code = "dqPHq9";

    String beginDate = "2022-07-26 02:37:10";
    String endDate = "2022-07-26 02:37:10";

    SelectStatement selectBuilder = new SelectBuilder()
            .select("log.uuid,log.PRICE")
            .distinct()
            .from(PRetailCatalog.TABLE_NAME,"log")
            .leftJoin(PProduct.TABLE_NAME,"p",
                    Predicates.equals("log",PRetailCatalog.PRODUCTUUID,"p",PProduct.UUID))
            .where(Predicates.equals("p",PProduct.CODE,code))
            .where(Predicates.greaterOrEquals("log",PRetailCatalog.BEGINDATE,beginDate))
            .where(Predicates.less ("log",PRetailCatalog.BEGINDATE,endDate))
            .build();

    List<RetailCatalog> query = jdbcTemplate.query(selectBuilder, new BeanPropertyRowMapper<>(RetailCatalog.class));
    System.out.println("----------");
    query.forEach(System.out::println);

    System.out.println("----------");

  }

  /**
   * 3-3 查询PRODUCT 中，存在20个及以上的 RETAILCATALOG的商品。
   */
  @Test
  public void selectTest3(){
//    sql语句
//
//    SELECT DISTINCT uuid,name,STATE,SALEPRICE
//    FROM product
//    WHERE EXISTS(
//        SELECT 1
//    FROM retailcatalog
//    GROUP BY retailcatalog.productUuid
//    HAVING COUNT(productUuid)>20 and product.uuid = productUuid
//)
    SelectStatement selectBuilder = new SelectBuilder()
            .select("uuid,name,STATE,SALEPRICE")
            .from(PProduct.TABLE_NAME)
            .where(Predicates.exists(
                    new SelectBuilder()
                            .select("1")
                            .from(PRetailCatalog.TABLE_NAME)
                            .groupBy(PRetailCatalog.PRODUCTUUID)
                            .having(
                                  Predicates.and(
                                      Predicates.greaterOrEquals("count("+ PRetailCatalog.PRODUCTUUID +")",20)),
                                      Predicates.equals(PProduct.TABLE_NAME, PProduct.UUID,PRetailCatalog.TABLE_NAME, PRetailCatalog.PRODUCTUUID)
                                  )
                            .build()
            ))
            .build();
    List<Product> query = jdbcTemplate.query(selectBuilder, new BeanPropertyRowMapper<>(Product.class));
    query.forEach(System.out::println);
  }


}
