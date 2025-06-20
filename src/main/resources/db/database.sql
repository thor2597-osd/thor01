-- 收藏表格
CREATE TABLE `tb_collection` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏的商品的主键Id',
                                 `user_id` bigint NOT NULL COMMENT '收藏商品的用户id',
                                 `product_detail_id` bigint NOT NULL COMMENT '收藏的商品详情id'
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 评论表
CREATE TABLE `tb_comment` (
                              `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                              `ref_id` varchar(32) NOT NULL,
                              `user_id` bigint NOT NULL,
                              `content` varchar(1000) NOT NULL,
                              `parent_id` bigint DEFAULT NULL,
                              `gmt_created` datetime NOT NULL,
                              `gmt_modified` datetime NOT NULL,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 发布的商品表
CREATE TABLE `tb_listing` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '发布商品的主键id',
                              `user_id` bigint NOT NULL COMMENT '用户Id',
                              `product_detail_id` bigint NOT NULL COMMENT '商品详情Id',
                              `status` tinyint NOT NULL DEFAULT '0' COMMENT '销售状态，0代表未卖出，1代表已卖出',
                              `brand` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品所属类型',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品大类
CREATE TABLE `tb_product` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品主键Id',
                              `brand` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品分类的名称',
                              `product_detail_number` bigint NOT NULL COMMENT '里面的商品详情数量'
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品详情
CREATE TABLE `tb_product_detail` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品详情主键Id',
                                     `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品详情名称',
                                     `price` decimal(10,2) NOT NULL COMMENT '商品详情价格',
                                     `product_detail_number` bigint NOT NULL COMMENT '商品详情库存',
                                     `product_id` bigint NOT NULL COMMENT '所属商品大类的Id',
                                     `author_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布商品的用户Id',
                                     `photo` varchar(2555) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品详情图片或视频',
                                     `collection` bigint NOT NULL COMMENT '该商品详情被收藏数',
                                     `description` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品描述',
                                     `comment_number` bigint NOT NULL COMMENT '商品下面的评论数量',
                                     `gmt_created` datetime NOT NULL COMMENT '商品的发布时间',
                                     `gmt_modified` datetime NOT NULL COMMENT '商品的修改时间',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 购买记录
CREATE TABLE `tb_purchase` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购买记录的主键id',
                               `user_id` bigint NOT NULL COMMENT '购买记录的用户id',
                               `product_detail_id` bigint NOT NULL COMMENT '购买记录的关联商品id'
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户表
CREATE TABLE `tb_user` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户的主键',
                           `nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户的昵称',
                           `collection` bigint NOT NULL COMMENT '收藏的商品数量',
                           `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户账号',
                           `pass_word` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户的密码',
                           `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户电话号码',
                           `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
                           `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户头像',
                           `purchase_quantity` bigint NOT NULL COMMENT '订单数量',
                           `view_count` bigint NOT NULL COMMENT '浏览数量',
                           `listing_count` bigint NOT NULL COMMENT '发布商品的数量',
                           `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户的收货地址'
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 浏览
CREATE TABLE `tb_view` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浏览记录的主键Id',
                           `user_id` bigint NOT NULL COMMENT '用户主键Id',
                           `product_detail_id` bigint NOT NULL COMMENT '商品详情主键Id'
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单表
CREATE TABLE `tb_pay_order` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `order_no` varchar(64) NOT NULL COMMENT '商户订单号',
                                `amount` double(10,2) NOT NULL COMMENT '订单金额',
                                `qr_code` varchar(512) DEFAULT NULL COMMENT '支付二维码链接',
                                `status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态：0-待支付 1-支付成功 2-支付失败',
                                `user_id` bigint NOT NULL COMMENT '购买商品的用户id',
                                `trade_no` varchar(64) DEFAULT NULL COMMENT '支付宝交易号',
                                `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
                                `create_time` datetime NOT NULL COMMENT '创建时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付订单表';

--订单项表
CREATE TABLE `tb_order_items` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键Id',
                                  `order_id` bigint NOT NULL COMMENT '所依附的大订单id',
                                  `product_detail_id` bigint NOT NULL COMMENT '商品Id',
                                  `quantity` int NOT NULL COMMENT '购买的商品数量',
                                  `total_price` double NOT NULL COMMENT '小计，商品总价格',
                                  PRIMARY KEY (`id`),
                                  KEY `order_id` (`order_id`),
                                  CONSTRAINT `tb_order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `tb_pay_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;