-- ======================================================
-- 龙胜惠民通 · 演示数据脚本（iCAN比赛专用）
-- 版本：v2.0.9 升级补丁（兼容 MySQL 5.7）
-- 说明：修复 points_flow / user / points_rule 表缺失字段
--       并插入 30 户居民档案演示数据
-- 执行：mysql -u root -p village_demo_db < resident_data_demo.sql
-- ======================================================

USE village_demo_db;

-- ======================================================
-- 【v2.0.9 数据库结构升级补丁】2026-08-30
-- 兼容 MySQL 5.7（不使用 ADD COLUMN IF NOT EXISTS）
-- 使用存储过程 + INFORMATION_SCHEMA 判断字段是否存在
-- ======================================================

-- 1. 修复 points_flow 表（缺失 batch_id, batch_name, apply_id）
DROP PROCEDURE IF EXISTS upgrade_points_flow;
DELIMITER $$
CREATE PROCEDURE upgrade_points_flow()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'points_flow' 
                   AND COLUMN_NAME = 'batch_id') THEN
        ALTER TABLE points_flow ADD COLUMN `batch_id` VARCHAR(64) NULL COMMENT '关联检查批次ID（雪花ID）' AFTER `tenant_id`;
    END IF;
    
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'points_flow' 
                   AND COLUMN_NAME = 'batch_name') THEN
        ALTER TABLE points_flow ADD COLUMN `batch_name` VARCHAR(100) NULL COMMENT '关联检查批次名称' AFTER `batch_id`;
    END IF;
    
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'points_flow' 
                   AND COLUMN_NAME = 'apply_id') THEN
        ALTER TABLE points_flow ADD COLUMN `apply_id` VARCHAR(64) NULL COMMENT '关联积分申请/评分记录ID（雪花ID）' AFTER `batch_name`;
    END IF;
END$$
DELIMITER ;
CALL upgrade_points_flow();
DROP PROCEDURE upgrade_points_flow;

-- 2. 修复 user 表（v2.0 双轨制积分字段）
DROP PROCEDURE IF EXISTS upgrade_user;
DELIMITER $$
CREATE PROCEDURE upgrade_user()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'user' 
                   AND COLUMN_NAME = 'total_earned_points') THEN
        ALTER TABLE `user` ADD COLUMN `total_earned_points` INT DEFAULT 0 COMMENT '总获得积分（只增不减）' AFTER `resident_profile_id`;
    END IF;
    
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'user' 
                   AND COLUMN_NAME = 'available_points') THEN
        ALTER TABLE `user` ADD COLUMN `available_points` INT DEFAULT 0 COMMENT '当前可用积分（兑换时扣减）' AFTER `total_earned_points`;
    END IF;
END$$
DELIMITER ;
CALL upgrade_user();
DROP PROCEDURE upgrade_user;

-- 3. 修复 points_rule 表（v2.0 行为类型与月度上限）
DROP PROCEDURE IF EXISTS upgrade_points_rule;
DELIMITER $$
CREATE PROCEDURE upgrade_points_rule()
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'points_rule' 
                   AND COLUMN_NAME = 'behavior_type') THEN
        ALTER TABLE `points_rule` ADD COLUMN `behavior_type` VARCHAR(20) DEFAULT 'daily' COMMENT 'daily/important/activity' AFTER `sort_order`;
    END IF;
    
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'points_rule' 
                   AND COLUMN_NAME = 'max_times_per_month') THEN
        ALTER TABLE `points_rule` ADD COLUMN `max_times_per_month` INT DEFAULT 0 COMMENT '每月上限（0=不限）' AFTER `behavior_type`;
    END IF;
END$$
DELIMITER ;
CALL upgrade_points_rule();
DROP PROCEDURE upgrade_points_rule;

-- ======================================================
-- 清空已有居民档案数据（仅清空 resident_profile 表）
-- ======================================================

TRUNCATE TABLE resident_profile;

-- ======================================================
-- 演示村民数据（共30户，覆盖一区、二区、三区、移民新村）
-- ======================================================
INSERT INTO resident_profile (tenant_id, owner_name, total_people, village_group, phone, id_card, address, remark) VALUES
    (1, '李国富', 5, '一区', '13800000001', '440106199001011234', 'A1-01', ''),
    (1, '王秀英', 4, '一区', '13800000002', '440106199002021235', 'A1-02', ''),
    (1, '张德明', 6, '一区', '13800000003', '440106199003031236', 'A1-03', ''),
    (1, '刘伟强', 3, '一区', '13800000004', '440106199004041237', 'A2-01', ''),
    (1, '陈丽华', 4, '一区', '13800000005', '440106199005051238', 'A2-02', ''),
    (1, '杨志刚', 5, '一区', '13800000006', '440106199006061239', 'A2-03', ''),
    (1, '赵秀兰', 3, '一区', '13800000007', '440106199007071240', 'A3-01', ''),

    (1, '黄永强', 4, '二区', '13800000011', '440106199011111244', 'B1-01', ''),
    (1, '周美芳', 6, '二区', '13800000012', '440106199012121245', 'B1-02', ''),
    (1, '吴建平', 5, '二区', '13800000013', '440106199013131246', 'B1-03', ''),
    (1, '徐秀珍', 3, '二区', '13800000014', '440106199014141247', 'B2-01', ''),
    (1, '孙明杰', 4, '二区', '13800000015', '440106199015151248', 'B2-02', ''),
    (1, '胡德胜', 7, '二区', '13800000016', '440106199016161249', 'B2-03', ''),
    (1, '朱丽芳', 3, '二区', '13800000017', '440106199017171250', 'B3-01', ''),

    (1, '林永康', 5, '三区', '13800000021', '440106199021212254', 'C1-01', ''),
    (1, '何彩凤', 4, '三区', '13800000022', '440106199022222255', 'C1-02', ''),
    (1, '罗志强', 6, '三区', '13800000023', '440106199023232256', 'C1-03', ''),
    (1, '梁秀英', 3, '三区', '13800000024', '440106199024242257', 'C2-01', ''),
    (1, '郑明辉', 4, '三区', '13800000025', '440106199025252258', 'C2-02', ''),
    (1, '谢玉兰', 5, '三区', '13800000026', '440106199026262259', 'C2-03', ''),
    (1, '韩德才', 3, '三区', '13800000027', '440106199027272260', 'C3-01', ''),

    (1, '唐建英', 4, '移民新村', '13800000031', '440106199031313264', 'D1-01', ''),
    (1, '曹明华', 5, '移民新村', '13800000032', '440106199032323265', 'D1-02', ''),
    (1, '邓秀梅', 3, '移民新村', '13800000033', '440106199033333266', 'D2-01', ''),
    (1, '彭德福', 6, '移民新村', '13800000034', '440106199034343267', 'D2-02', ''),
    (1, '蒋丽萍', 4, '移民新村', '13800000035', '440106199035353268', 'D3-01', ''),
    (1, '蔡永昌', 5, '移民新村', '13800000036', '440106199036363269', 'D3-02', '');

-- ======================================================
-- 验证数据
-- ======================================================
-- SELECT COUNT(*) FROM resident_profile;  -- 应返回 30