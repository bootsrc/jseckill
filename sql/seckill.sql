/*
Navicat MariaDB Data Transfer

Source Server         : linux-cn-seckill
Source Server Version : 50560
Source Host           : localhost:3306
Source Database       : seckill

Target Server Type    : MariaDB
Target Server Version : 50560
File Encoding         : 65001

Date: 2019-01-22 16:06:53
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for seckill
-- ----------------------------
DROP TABLE IF EXISTS `seckill`;
CREATE TABLE `seckill` (
  `seckill_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  `name` varchar(120) CHARACTER SET utf8 NOT NULL COMMENT '商品名称',
  `number` int(11) NOT NULL COMMENT '库存数量',
  `start_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '秒杀开启时间',
  `end_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '秒杀结束时间',
  `create_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `version` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`seckill_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀库存表';

-- ----------------------------
-- Records of seckill
-- ----------------------------
INSERT INTO `seckill` VALUES ('1000', '商品0', '9937', '2019-01-15 11:00:00', '2019-10-25 00:00:00', '2019-01-13 21:28:31', '15');
INSERT INTO `seckill` VALUES ('1001', '商品1', '181', '2019-01-16 11:00:00', '2019-10-25 00:00:00', '2019-01-13 21:28:31', '7');
INSERT INTO `seckill` VALUES ('1002', '商品2', '293', '2019-01-17 11:00:00', '2019-10-25 00:00:00', '2019-01-13 21:28:31', '5');
INSERT INTO `seckill` VALUES ('1003', '商品3', '398', '2019-11-30 11:00:00', '2019-12-01 00:00:00', '2019-01-13 21:28:31', '0');
