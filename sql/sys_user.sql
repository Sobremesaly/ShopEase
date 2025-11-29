CREATE TABLE `sys_user` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `username` varchar(50) NOT NULL COMMENT '用户名（登录用）',
                            `password` varchar(100) NOT NULL COMMENT '加密后的密码',
                            `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `status` tinyint(1) DEFAULT 1 COMMENT '状态：1正常，0禁用',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`)  -- 用户名唯一，避免重复注册
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 插入测试数据（密码是 123456，加密后存储）
INSERT INTO `sys_user` (`username`, `password`, `nickname`)
VALUES ('admin', '$10$f4FnDGxmzayoRqBmR0CAT.rm3OsQksuVzXMhZcVRKGg7H8yZUmop6', '管理员');
