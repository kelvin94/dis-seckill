-- 创建数据库
-- CREATE DATABASE seckill DEFAULT CHARACTER SET utf8;
-- DELIMITER  ;;;

DROP TABLE IF EXISTS  seckill_swag ;
DROP TABLE IF EXISTS  seckill_order ;

-- 创建秒杀商品表
CREATE TABLE seckill_swag(
                             seckill_swag_id  smallint NOT NULL,
                             title  varchar (1000) DEFAULT NULL,
                             price  decimal (10,2) DEFAULT NULL,
                             seckill_price  decimal (10,2) DEFAULT NULL,
                             stock_count  bigint DEFAULT NULL,
                             start_time  timestamp NOT NULL DEFAULT '1970-02-01 00:00:01',
                             end_time  timestamp NOT NULL DEFAULT '1970-02-01 00:00:01',
                             create_time  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY ( seckill_swag_id )
);
CREATE  INDEX idx_start_time ON seckill_swag (start_time);
CREATE  INDEX idx_end_time ON seckill_swag (end_time);

COMMENT ON TABLE seckill_swag IS '秒杀商品表';
comment on column seckill_swag.seckill_swag_id is '商品ID';
comment on column seckill_swag.title is '商品标题';
comment on column seckill_swag.price is '商品原价格';
comment on column seckill_swag.seckill_price is ' 商品秒杀价格';
comment on column seckill_swag.stock_count is '剩余库存数量';
comment on column seckill_swag.start_time is '秒杀开始时间';
comment on column seckill_swag.end_time is '秒杀结束时间';
comment on column seckill_swag.create_time is '创建时间';


-- 创建秒杀订单表
CREATE TABLE  seckill_order (
                                seckill_swag_id  bigint NOT NULL  ,
                                total  decimal (10, 2) DEFAULT NULL   ,
                                user_phone  bigint NOT NULL   ,
                                create_time  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                state  smallint NOT NULL DEFAULT -1,
                                PRIMARY KEY ( seckill_swag_id ,  user_phone ) /*联合主键，保证一个用户只能秒杀一件商品*/
);
comment on table seckill_order is '秒杀订单表';
comment on column seckill_order.seckill_swag_id is '对应的秒杀商品ID';
comment on column seckill_order.total is '支付金额';
comment on column seckill_order.user_phone is '用户手机号';
comment on column seckill_order.state is ' 状态：-1无效 0成功 1已付款';
comment on column seckill_order.user_phone is 'user_phone';

CREATE OR REPLACE FUNCTION upd_timestamp() RETURNS TRIGGER  AS $$
BEGIN
    NEW.create_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql^;

CREATE TRIGGER trigger_upd_timestamp
    BEFORE UPDATE
    ON seckill_order
    FOR EACH ROW
EXECUTE PROCEDURE upd_timestamp()^;

------------- ---------------------------------------------------------------------------
----          NO LONGER USE THE SCHEMA BELOW
------------- ---------------------------------------------------------------------------
-- ----------------------------
-- Table structure for sk_swag
-- ----------------------------
DROP TABLE IF EXISTS sk_swag;
-- CREATE TABLE sk_swag (
--                          id  smallint NOT NULL,
--                          swag_name  varchar(30) DEFAULT NULL,
--                          swag_title  varchar(64) DEFAULT NULL,
--                          swag_detail  text,
--                          swag_price  decimal(10,2) DEFAULT NULL,
--                          swag_stock  int DEFAULT '0',
--                          PRIMARY KEY ( id )
-- );
--
-- comment on column sk_swag.id is '商品ID';
-- comment on column sk_swag.swag_name is '商品名称';
-- comment on column sk_swag.swag_title is '商品标题';
-- comment on column sk_swag.swag_detail is '商品详情';
-- comment on column sk_swag.swag_price is '商品price';
-- comment on column sk_swag.swag_stock is '商品库存，-1表示没有限制';


-- ----------------------------
-- Table structure for sk_swag_seckill
-- ----------------------------
DROP TABLE IF EXISTS  sk_swag_seckill ;
-- CREATE TABLE  sk_swag_seckill  (
--                                    id  smallint NOT NULL,
--                                    swag_id  bigint DEFAULT NULL,
--                                    seckill_price  decimal(10,2) DEFAULT '0.00',
--                                    stock_count  int DEFAULT NULL ,
--                                    start_date  date DEFAULT NULL ,
--                                    end_date  date DEFAULT NULL ,
--                                    version  int DEFAULT NULL ,
--                                    PRIMARY KEY ( id )
-- );
-- comment on column sk_swag_seckill.id is '秒杀商品id';
-- comment on column sk_swag_seckill.swag_id is '商品id';
-- comment on column sk_swag_seckill.seckill_price is '秒杀价';
-- comment on column sk_swag_seckill.stock_count is '库存数量';
-- comment on column sk_swag_seckill.start_date is '秒杀开始时间';
-- comment on column sk_swag_seckill.start_date is '秒杀结束时间';
-- comment on column sk_swag_seckill.start_date is '并发版本控制';





-- ----------------------------
-- Table structure for sk_order
-- ----------------------------
DROP TABLE IF EXISTS  sk_order ;
-- CREATE TABLE  sk_order  (
--                             id  smallint NOT NULL,
--                             user_id  int DEFAULT NULL,
--                             order_id  int DEFAULT NULL,
--                             swag_id  smallint DEFAULT NULL,
--                             PRIMARY KEY ( id )
-- );
--
-- create unique index u_userid_swagid on sk_order(user_id, swag_id);
--
--
-- -- ----------------------------
-- -- Table structure for sk_order_info
-- -- ----------------------------
-- DROP TABLE IF EXISTS  sk_order_info ;
-- CREATE TABLE  sk_order_info  (
--                                  id  smallint NOT NULL,
--                                  user_id  int DEFAULT NULL,
--                                  swag_id  int DEFAULT NULL,
--                                  swag_name  varchar(30) DEFAULT NULL,
--                                  swag_count  int DEFAULT NULL,
--                                  swag_price  decimal(10,2) DEFAULT NULL,
--                                  order_channel  smallint DEFAULT NULL  ,
--                                  status  int DEFAULT NULL  ,
--                                  create_date  date DEFAULT NULL,
--                                  pay_date  date DEFAULT NULL,
--                                  PRIMARY KEY ( id )
-- );
-- comment on column sk_order_info.order_channel is '订单渠道，1在线，2android，3ios';
-- comment on column sk_order_info.order_channel is '订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退款，5已完成';
