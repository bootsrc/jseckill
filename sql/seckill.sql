/*
Navicat MySQL Data Transfer

Source Server         : localhost-mysql-seckill
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : seckill

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2019-02-24 21:50:58
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for seckill
-- ----------------------------
DROP TABLE IF EXISTS `seckill`;
CREATE TABLE `seckill` (
  `seckill_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  `name` varchar(120) CHARACTER SET utf8 NOT NULL COMMENT '商品名称',
  `inventory` int(11) NOT NULL COMMENT '库存数量',
  `start_time` datetime NOT NULL COMMENT '秒杀开启时间',
  `end_time` datetime NOT NULL COMMENT '秒杀结束时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `version` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`seckill_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1004 DEFAULT CHARSET=utf8mb4 COMMENT='秒杀库存表';

-- ----------------------------
-- Records of seckill
-- ----------------------------
INSERT INTO `seckill` VALUES ('1000', '商品0', '119990', '2019-01-17 14:29:10', '2019-12-20 00:00:00', '2019-01-13 21:28:31', '11');
INSERT INTO `seckill` VALUES ('1001', '商品1', '188', '2019-01-17 14:29:12', '2019-12-20 00:00:00', '2019-01-13 21:28:31', '12');
INSERT INTO `seckill` VALUES ('1002', '商品2', '289', '2019-01-17 14:29:18', '2019-12-20 00:00:00', '2019-01-13 21:28:31', '11');
INSERT INTO `seckill` VALUES ('1003', '商品3', '394', '2019-01-17 14:29:22', '2019-12-20 00:00:00', '2019-01-13 21:28:31', '6');
