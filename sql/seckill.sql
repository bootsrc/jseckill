/*
Navicat MySQL Data Transfer

Source Server         : localhost-mysql-seckill
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : seckill

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2019-01-14 17:47:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for seckill
-- ----------------------------
DROP TABLE IF EXISTS `seckill`;
CREATE TABLE `seckill` (
  `seckill_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  `name` varchar(120) NOT NULL COMMENT '商品名称',
  `number` int(11) NOT NULL COMMENT '库存数量',
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '秒杀开启时间',
  `end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '秒杀结束时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`seckill_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1004 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';

-- ----------------------------
-- Records of seckill
-- ----------------------------
INSERT INTO `seckill` VALUES ('1000', '1000元秒杀iphone6', '98', '2019-01-14 17:24:47', '2019-01-20 00:00:00', '2019-01-13 21:28:31');
INSERT INTO `seckill` VALUES ('1001', '500元秒杀ipad2', '200', '2019-01-13 00:00:00', '2019-01-20 00:00:00', '2019-01-13 21:28:31');
INSERT INTO `seckill` VALUES ('1002', '300元秒杀小米4', '300', '2019-01-13 00:00:00', '2019-01-20 00:00:00', '2019-01-13 21:28:31');
INSERT INTO `seckill` VALUES ('1003', '200元秒杀红米note', '400', '2019-01-13 00:00:00', '2019-01-20 00:00:00', '2019-01-13 21:28:31');
