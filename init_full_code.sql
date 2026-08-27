-- ======================================================
-- 龙胜惠民通 · 完整数据库初始化脚本
-- 版本：2.0
-- 日期：2026-08-28
-- 说明：包含全部表结构 + 基础数据 + 测试数据
--       新增 feedback 表（意见反馈）
--       为 notice、policy 表增加 deleted 字段（逻辑删除）
--       为 user 表增加 resident_profile_id 字段（关联居民档案）
--       为 activity_qrcode 表增加 tenant_id 字段（租户隔离）
--       新增 score_evidence 表（证据链评分）
--       新增 rectification_task 表（整改闭环）
--       新增 appeal_record 表（申诉与复核）
--       新增 publish_snapshot 表（红黑榜公示快照）
--       新增 offline_sync_record 表（离线同步记录）
--       为 points_apply 表增加 has_evidence 字段（是否有证据）
--       为 score_evidence 表增加 tenant_id 字段（租户隔离）
--       【v2.0 新增】用户表增加 total_earned_points 和 available_points
--       【v2.0 新增】积分规则表增加 behavior_type、max_times_per_day、max_times_per_month
--       【v2.0 新增】季度快照表 quarterly_snapshot
--       【v2.0 新增】重要贡献认定表 important_contribution
--       【v2.0 新增】活动参与记录表 activity_participation
-- 执行：mysql -u root -p < init_full.sql
-- ======================================================

-- 删除并重新创建数据库
DROP DATABASE IF EXISTS village_db;
CREATE DATABASE village_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE village_db;

SET NAMES utf8mb4;

-- ======================================================
-- 1. 租户表
-- ======================================================
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(32) NOT NULL,
  `tenant_name` varchar(100) NOT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `contact_person` varchar(32) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tenant_code` (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tenant` VALUES (1,'longsheng','龙胜村',NULL,NULL,NULL,1,'2026-06-09 02:30:03');

-- ======================================================
-- 2. 用户表（新增 resident_profile_id 字段）
--    v2.0 新增 total_earned_points、available_points
-- ======================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `openid` varchar(64) DEFAULT NULL,
  `phone` varchar(11) DEFAULT NULL,
  `real_name` varchar(32) DEFAULT NULL,
  `id_card` varchar(18) DEFAULT NULL,
  `village_group` varchar(32) DEFAULT NULL,
  `is_party_member` tinyint DEFAULT '0',
  `role` varchar(20) DEFAULT NULL,
  `points` int DEFAULT '0',
  `password` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deleted` tinyint DEFAULT '0',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `resident_profile_id` int NULL COMMENT '关联居民档案ID',
  -- ====== v2.0 新增字段 ======
  `total_earned_points` int DEFAULT 0 COMMENT '总获得积分（永久累加，只增不减）',
  `available_points` int DEFAULT 0 COMMENT '当前可用积分（兑换时扣减）',
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_resident_profile` (`resident_profile_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化：将现有 points 值赋给 total_earned_points 和 available_points
INSERT INTO `user` VALUES 
(50,1,NULL,'13800138000','村民1',NULL,NULL,0,'VILLAGER',118,'$2a$10$DQE7l5D6.AkXsorJWKEj5.aDOj0BNHYds3U8/sAv3WZSWw521HOFC','2026-06-20 17:06:06',0,NULL,NULL,118,118),
(51,1,NULL,'13800138001','村委管理员',NULL,NULL,0,'VILLAGE_ADMIN',0,'$2a$10$DQE7l5D6.AkXsorJWKEj5.aDOj0BNHYds3U8/sAv3WZSWw521HOFC','2026-06-20 17:06:15',0,NULL,NULL,0,0),
(52,1,NULL,'13800138002','张三',NULL,NULL,0,'VILLAGER',150,'$2a$10$DQE7l5D6.AkXsorJWKEj5.aDOj0BNHYds3U8/sAv3WZSWw521HOFC','2026-07-18 08:00:00',0,NULL,NULL,150,150),
(53,1,NULL,'13800138003','李四',NULL,NULL,0,'VILLAGER',90,'$2a$10$DQE7l5D6.AkXsorJWKEj5.aDOj0BNHYds3U8/sAv3WZSWw521HOFC','2026-07-18 08:00:00',0,NULL,NULL,90,90),
(54,1,NULL,'13800138004','王五',NULL,NULL,0,'VILLAGER',200,'$2a$10$DQE7l5D6.AkXsorJWKEj5.aDOj0BNHYds3U8/sAv3WZSWw521HOFC','2026-07-18 08:00:00',0,NULL,NULL,200,200);

-- ======================================================
-- 3. 积分规则表（63条）
--    v2.0 新增 behavior_type、max_times_per_day、max_times_per_month
-- ======================================================
DROP TABLE IF EXISTS `points_rule`;
CREATE TABLE `points_rule` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `rule_name` varchar(128) DEFAULT NULL,
  `category` varchar(32) DEFAULT NULL,
  `points` int DEFAULT NULL,
  `audit_flow` varchar(10) DEFAULT NULL,
  `need_photo` tinyint DEFAULT '0',
  `max_times_per_day` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `sort_order` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  -- ====== v2.0 新增字段 ======
  `behavior_type` varchar(20) DEFAULT 'daily' COMMENT 'daily/important/activity',
  `max_times_per_month` int DEFAULT 0 COMMENT '每月上限（0=不限）',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status_sort` (`tenant_id`,`status`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `points_rule` VALUES 
(1,1,'庭院地面干净整洁，无垃圾杂物','庭院环境',10,'single',1,1,1,10,'2026-06-09 02:30:16','daily',0),
(2,1,'物品摆放有序，农具柴草堆放整齐','庭院环境',8,'single',1,1,1,20,'2026-06-09 02:30:16','daily',0),
(3,1,'庭院内有三种以上花卉绿植，布局美观','庭院环境',12,'double',1,1,1,30,'2026-06-09 02:30:16','daily',0),
(4,1,'围墙、房屋外立面整洁无破损，无乱涂乱画','庭院环境',10,'single',1,1,1,40,'2026-06-09 02:30:16','daily',0),
(5,1,'严格按照四类垃圾分类，连续一个月无混装','垃圾分类与处理',15,'single',1,1,1,50,'2026-06-09 02:30:16','daily',0),
(6,1,'正确使用分类垃圾桶，桶身保持干净','垃圾分类与处理',8,'single',1,1,1,60,'2026-06-09 02:30:16','daily',0),
(7,1,'垃圾每日清理，不堆积在庭院或公共区域','垃圾分类与处理',10,'single',1,1,1,70,'2026-06-09 02:30:16','daily',0),
(8,1,'积极配合垃圾收集人员，主动将垃圾送到指定地点','垃圾分类与处理',8,'single',0,1,1,80,'2026-06-09 02:30:16','daily',0),
(9,1,'每周主动清理自家周边公共道路上的垃圾','公共环境维护',5,'single',1,1,1,90,'2026-06-09 02:30:16','daily',0),
(10,1,'爱护公共绿化，一个月内无任何破坏行为','公共环境维护',10,'single',1,1,1,100,'2026-06-09 02:30:16','daily',0),
(11,1,'发现公共设施损坏及时报告','公共环境维护',5,'single',1,1,1,110,'2026-06-09 02:30:16','daily',0),
(12,1,'积极参与村里组织的公共环境整治活动','公共环境维护',12,'double',1,1,1,120,'2026-06-09 02:30:16','activity',4),
(13,1,'家禽始终实行圈养，圈舍规范牢固卫生','家禽管理',12,'single',1,1,1,130,'2026-06-09 02:30:16','daily',0),
(14,1,'定期清理家禽粪便，无异味散发','家禽管理',10,'single',1,1,1,140,'2026-06-09 02:30:16','daily',0),
(15,1,'圈养区域与居住区域合理隔离','家禽管理',8,'single',1,1,1,150,'2026-06-09 02:30:16','daily',0),
(16,1,'门前三包落实到位（卫生、绿化、秩序）','门前三包',15,'single',1,1,1,160,'2026-06-09 02:30:16','daily',0),
(17,1,'家庭关系和谐，被邻居或村民称赞','家风美',5,'single',0,1,1,170,'2026-06-09 02:30:16','daily',0),
(18,1,'长期照顾家中老人，事迹突出','家风美',10,'double',1,1,1,180,'2026-06-09 02:30:16','important',0),
(19,1,'积极参与村里组织的关爱老人活动','家风美',5,'single',0,1,1,190,'2026-06-09 02:30:16','activity',4),
(20,1,'注重子女品德教育，子女在学校或村里表现优秀','家风美',8,'single',0,1,1,200,'2026-06-09 02:30:16','daily',0),
(21,1,'主动调解邻里矛盾，成功化解纠纷','家风美',8,'double',1,1,1,210,'2026-06-09 02:30:16','important',0),
(22,1,'主动制止不文明行为','文明美',5,'single',0,1,1,220,'2026-06-09 02:30:16','daily',0),
(23,1,'积极宣传文明风尚，带动他人讲文明','文明美',8,'single',0,1,1,230,'2026-06-09 02:30:16','daily',0),
(24,1,'爱护公共文化设施，发现问题及时报告并协助处理','文明美',8,'single',1,1,1,240,'2026-06-09 02:30:16','daily',0),
(25,1,'参与文明创建活动表现突出','文明美',10,'double',0,1,1,250,'2026-06-09 02:30:16','activity',4),
(26,1,'积极参与公益活动','参与美',8,'single',0,1,1,260,'2026-06-09 02:30:16','activity',4),
(27,1,'在公益活动中发挥重要作用','参与美',5,'double',1,1,1,270,'2026-06-09 02:30:16','activity',4),
(28,1,'为村里发展建设提出建设性意见被采纳','参与美',10,'double',1,1,1,280,'2026-06-09 02:30:16','important',0),
(29,1,'参与村里民主管理，提出有价值的建议','参与美',8,'single',0,1,1,290,'2026-06-09 02:30:16','daily',0),
(30,1,'带动他人参与公益活动或民主管理','参与美',8,'single',0,1,1,300,'2026-06-09 02:30:16','daily',0),
(31,1,'庭院地面有明显垃圾杂物','庭院环境',-5,'single',1,1,1,310,'2026-06-09 02:30:16','daily',0),
(32,1,'物品摆放杂乱，影响美观和通行','庭院环境',-8,'single',1,1,1,320,'2026-06-09 02:30:16','daily',0),
(33,1,'庭院内无任何绿化且杂乱','庭院环境',-10,'single',1,1,1,330,'2026-06-09 02:30:16','daily',0),
(34,1,'围墙、房屋外立面破损或有乱涂乱画不清理','庭院环境',-8,'single',1,1,1,340,'2026-06-09 02:30:16','daily',0),
(35,1,'不进行垃圾分类，混装垃圾','垃圾分类与处理',-10,'single',1,1,1,350,'2026-06-09 02:30:16','daily',0),
(36,1,'有分类垃圾桶但不用，随意倾倒垃圾','垃圾分类与处理',-12,'single',1,1,1,360,'2026-06-09 02:30:16','daily',0),
(37,1,'垃圾堆积在庭院或公共区域超过两天','垃圾分类与处理',-8,'single',1,1,1,370,'2026-06-09 02:30:16','daily',0),
(38,1,'拒绝配合垃圾收集人员工作','垃圾分类与处理',-15,'single',0,1,1,380,'2026-06-09 02:30:16','daily',0),
(39,1,'对自家周边公共道路上的垃圾视而不见超过一周','公共环境维护',-10,'single',1,1,1,390,'2026-06-09 02:30:16','daily',0),
(40,1,'破坏公共绿化（践踏草坪、攀折花木）','公共环境维护',-12,'single',1,1,1,400,'2026-06-09 02:30:16','daily',0),
(41,1,'损坏公共设施不报告','公共环境维护',-10,'single',1,1,1,410,'2026-06-09 02:30:16','daily',0),
(42,1,'拒不参与村里组织的公共环境整治活动','公共环境维护',-15,'single',0,1,1,420,'2026-06-09 02:30:16','daily',0),
(43,1,'家禽散养（第一次发现）','家禽管理',-8,'single',1,1,1,430,'2026-06-09 02:30:16','daily',0),
(44,1,'家禽散养（第二次发现）','家禽管理',-15,'single',1,1,1,440,'2026-06-09 02:30:16','daily',0),
(45,1,'家禽散养（第三次及以上）','家禽管理',-20,'single',1,1,1,450,'2026-06-09 02:30:16','daily',0),
(46,1,'圈舍脏乱差，有异味散发','家禽管理',-10,'single',1,1,1,460,'2026-06-09 02:30:16','daily',0),
(47,1,'圈养区域设置不合理，影响周边居民','家禽管理',-12,'single',1,1,1,470,'2026-06-09 02:30:16','daily',0),
(48,1,'环保意识淡薄，对环保工作漠不关心','环保意识',-10,'single',0,1,1,480,'2026-06-09 02:30:16','daily',0),
(49,1,'随意焚烧垃圾或排放污水','环保意识',-15,'double',1,1,1,490,'2026-06-09 02:30:16','daily',0),
(50,1,'阻挠他人环保行为','环保意识',-12,'single',1,1,1,500,'2026-06-09 02:30:16','daily',0),
(51,1,'门前三包未落实（脏乱差、破坏绿化、乱堆乱放）','门前三包',-15,'single',1,1,1,510,'2026-06-09 02:30:16','daily',0),
(52,1,'家庭内部经常发生争吵，影响邻里','家风差',-5,'single',0,1,1,520,'2026-06-09 02:30:16','daily',0),
(53,1,'不照顾家中老人，被举报核实','家风差',-10,'double',1,1,1,530,'2026-06-09 02:30:16','daily',0),
(54,1,'对子女教育不当，子女有不良行为','家风差',-8,'single',0,1,1,540,'2026-06-09 02:30:16','daily',0),
(55,1,'与邻居发生严重矛盾','家风差',-8,'single',0,1,1,550,'2026-06-09 02:30:16','daily',0),
(56,1,'有不文明言行（说脏话、粗话，不礼貌待人）','文明差',-5,'single',0,1,1,560,'2026-06-09 02:30:16','daily',0),
(57,1,'违反村规民约','文明差',-8,'single',0,1,1,570,'2026-06-09 02:30:16','daily',0),
(58,1,'故意损坏公共文化设施','文明差',-10,'single',1,1,1,580,'2026-06-09 02:30:16','daily',0),
(59,1,'参与不文明活动','文明差',-8,'single',0,1,1,590,'2026-06-09 02:30:16','daily',0),
(60,1,'拒绝参与公益活动','参与差',-8,'single',0,1,1,600,'2026-06-09 02:30:16','daily',0),
(61,1,'阻碍村里发展建设','参与差',-10,'single',0,1,1,610,'2026-06-09 02:30:16','daily',0),
(62,1,'不参与民主管理','参与差',-8,'single',0,1,1,620,'2026-06-09 02:30:16','daily',0),
(63,1,'对村里事务漠不关心','参与差',-5,'single',0,1,1,630,'2026-06-09 02:30:16','daily',0);

-- ======================================================
-- 4. 活动表
-- ======================================================
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `title` varchar(128) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `reward_points` int DEFAULT NULL,
  `description` text,
  `max_participants` int DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `activity` VALUES 
(1,1,'龙胜村人居环境大扫除','2026-07-20 08:00:00','2026-07-20 12:00:00','龙胜村各自然村',20,'集中清理村道、公共区域卫生，请自带扫帚、垃圾袋、手套等工具。',100,1,'2026-07-10 09:00:00'),
(2,1,'"美丽庭院"创建评比活动','2026-08-01 09:00:00','2026-08-31 17:00:00','龙胜村各农户庭院',50,'评比标准：庭院整洁、绿化美化、物品有序、垃圾分类。设一等奖1名、二等奖2名、三等奖3名。',80,1,'2026-07-12 10:00:00'),
(3,1,'乡村振兴主题文艺晚会','2026-07-25 19:00:00','2026-07-25 21:30:00','龙胜村村委会广场',15,'节目包括歌舞、小品、有奖问答等，欢迎村民踊跃参加！有奖问答答对即送礼品。',200,1,'2026-07-13 14:00:00'),
(4,1,'垃圾分类知识培训讲座','2026-07-28 14:30:00','2026-07-28 16:00:00','龙胜村文化活动中心',10,'邀请环保专家讲解垃圾分类知识，现场有互动环节，参与有奖。',60,1,'2026-07-15 08:30:00'),
(5,1,'关爱老人·送温暖志愿活动','2026-08-05 09:00:00','2026-08-05 12:00:00','龙胜村老人活动中心',25,'组织志愿者为村里独居老人打扫卫生、剪头发、陪聊天。招募志愿者15名。',15,1,'2026-07-16 10:00:00');

-- ======================================================
-- 5. 活动报名表（增加签退字段）
-- ======================================================
DROP TABLE IF EXISTS `activity_registration`;
CREATE TABLE `activity_registration` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `activity_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `participant_name` varchar(32) DEFAULT NULL,
  `phone` varchar(11) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `signed_in` tinyint DEFAULT '0',
  `sign_time` datetime DEFAULT NULL,
  `checked_out` tinyint DEFAULT '0' COMMENT '是否签退 0-未 1-已',
  `checkout_time` datetime DEFAULT NULL COMMENT '签退时间',
  `duration_minutes` int DEFAULT NULL COMMENT '参与时长（分钟）',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `activity_registration` VALUES 
(1,1,1,50,'村民1','13800138000','自带扫帚和垃圾袋',1,'2026-07-20 08:05:00',0,NULL,NULL,'2026-07-15 09:30:00'),
(2,1,1,51,'村委管理员','13800138001','负责协调指挥',1,'2026-07-20 07:55:00',0,NULL,NULL,'2026-07-15 09:40:00'),
(3,1,2,50,'村民1','13800138000','报名参加评比',0,NULL,0,NULL,NULL,'2026-07-16 10:00:00'),
(4,1,2,51,'村委管理员','13800138001','作为评委参与',0,NULL,0,NULL,NULL,'2026-07-16 10:30:00'),
(5,1,3,50,'村民1','13800138000','带家人一起参加',0,NULL,0,NULL,NULL,'2026-07-17 14:20:00'),
(6,1,3,51,'村委管理员','13800138001','负责现场组织',0,NULL,0,NULL,NULL,'2026-07-17 14:40:00'),
(7,1,4,50,'村民1','13800138000','想学习更多分类知识',0,NULL,0,NULL,NULL,'2026-07-18 09:00:00'),
(8,1,4,51,'村委管理员','13800138001','代表村委参加',0,NULL,0,NULL,NULL,'2026-07-18 09:20:00'),
(9,1,5,50,'村民1','13800138000','报名参加志愿服务',0,NULL,0,NULL,NULL,'2026-07-19 08:00:00'),
(10,1,5,51,'村委管理员','13800138001','协助组织活动',0,NULL,0,NULL,NULL,'2026-07-19 08:30:00');

-- ======================================================
-- 6. 通知表（增加 deleted 字段）
-- ======================================================
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `title` varchar(128) DEFAULT NULL,
  `content` text,
  `images` varchar(500) DEFAULT NULL,
  `is_top` tinyint DEFAULT '0',
  `read_count` int DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `notice` VALUES 
(1,1,'关于2026年7月人居环境检查的通知','各位村民：根据上级工作部署，定于7月25日-28日对全村进行人居环境检查。检查内容包括：庭院卫生、垃圾分类、家禽圈养、门前三包等。请各位村民提前做好准备工作，确保检查达标。检查结果将作为"美丽庭院"评比的重要依据。',NULL,1,128,'2026-07-15 08:00:00',0),
(2,1,'龙胜村"美丽庭院"创建评比活动方案','为推动乡村振兴，提升人居环境质量，我村决定开展"美丽庭院"创建评比活动。评比时间：8月1日-31日。评比标准：庭院整洁有序、绿化美化良好、垃圾分类正确、无乱搭乱建。获奖家庭将获得积分奖励和荣誉证书。报名请到村委会登记，或联系各小组组长。',NULL,1,95,'2026-07-12 09:00:00',0),
(3,1,'7月25日乡村振兴主题晚会通知','为丰富村民精神文化生活，龙胜村定于7月25日（周六）晚上7点在村委会广场举办"乡村振兴"主题文艺晚会。晚会节目丰富多彩，包括歌舞、小品、有奖问答等环节，欢迎广大村民相互转告，踊跃参加！有奖问答答对即送精美礼品。',NULL,0,67,'2026-07-13 14:00:00',0),
(4,1,'垃圾分类知识培训讲座报名通知','为进一步推进垃圾分类工作，提高村民环保意识，我村定于7月28日下午2:30在文化活动中心举办垃圾分类知识培训讲座。届时将邀请环保专家现场授课，并有互动环节。请有意参加的村民于7月25日前到村委会报名，或联系小组组长。',NULL,0,42,'2026-07-14 10:00:00',0),
(5,1,'停水通知','因供水管道检修，7月22日8:00-18:00全村停水。请各位村民提前做好储水准备，并互相转告。给您带来的不便，敬请谅解！',NULL,0,156,'2026-07-18 16:00:00',0);

-- ======================================================
-- 7. 政策表（增加 deleted 字段）
-- ======================================================
DROP TABLE IF EXISTS `policy`;
CREATE TABLE `policy` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `category` varchar(32) DEFAULT NULL,
  `title` varchar(128) DEFAULT NULL,
  `content` text,
  `attachment_url` varchar(255) DEFAULT NULL,
  `effective_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `policy` VALUES 
(1,1,'医保','2026年城乡居民基本医疗保险缴费通知','2026年度城乡居民基本医疗保险缴费标准为每人380元，缴费截止日期为2026年12月31日。请各位村民尽快通过"粤省事"或到村委会办理缴费手续。逾期未缴费将影响2026年度医保待遇。',NULL,'2026-01-01 00:00:00','2026-07-01 09:00:00',0),
(2,1,'养老','城乡居民养老保险缴费指南','2026年度城乡居民养老保险缴费标准为每年180元、240元、360元、600元、900元、1200元、1800元、3600元、4800元等多个档次，村民可根据自身经济情况选择。多缴多得，长缴多得。缴费截止日期2026年12月20日。',NULL,'2026-01-01 00:00:00','2026-07-02 10:00:00',0),
(3,1,'农业补贴','2026年耕地地力保护补贴政策','根据省、市、区文件精神，2026年耕地地力保护补贴标准为每亩83元。补贴对象为拥有耕地承包权的种地农民。请各位村民核对土地确权面积，如有异议请及时联系村委会。补贴资金将通过"一卡通"直接发放到户。',NULL,'2026-06-01 00:00:00','2026-07-03 08:30:00',0),
(4,1,'危房改造','2026年农村危房改造政策','2026年农村危房改造补助对象为农村低保户、分散供养特困人员、易返贫致贫户等低收入群体。补助标准为每户1.5-3万元不等，根据房屋危险等级确定。符合条件的村民请尽快向村委会提交申请材料。',NULL,'2026-01-01 00:00:00','2026-07-04 11:00:00',0),
(5,1,'医保','2026年医疗救助政策解读','2026年医疗救助政策扩大了救助范围，将因病致贫重病患者纳入救助对象。救助标准：住院自负费用超过2万元部分，按50%-70%比例救助，年度最高救助5万元。具体申请流程请咨询村委会或镇民政办。',NULL,'2026-01-01 00:00:00','2026-07-05 09:30:00',0);

-- ======================================================
-- 8. 商品表（不变）
-- ======================================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `name` varchar(64) DEFAULT NULL,
  `points_needed` int DEFAULT NULL,
  `stock` int DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `description` text,
  `status` tinyint DEFAULT '1',
  `version` int DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `product` VALUES 
(1,1,'金龙鱼食用油 5L',120,30,NULL,'非转基因压榨一级花生油，5升装',1,0,'2026-07-10 08:00:00'),
(2,1,'立白洗洁精 1kg',50,50,NULL,'高效去油洗洁精，1千克装',1,0,'2026-07-10 08:30:00'),
(3,1,'蓝月亮洗衣液 3kg',80,40,NULL,'深层洁净护理洗衣液，3千克装',1,0,'2026-07-10 09:00:00'),
(4,1,'维达卷纸 10卷装',60,60,NULL,'卫生卷纸，10卷装，4层加厚',1,0,'2026-07-10 09:30:00'),
(5,1,'飘柔洗发水 400ml',70,35,NULL,'丝质柔顺洗发水，400毫升',1,0,'2026-07-10 10:00:00'),
(6,1,'舒肤佳香皂 3块装',30,80,NULL,'纯白清香型香皂，3块装',1,0,'2026-07-10 10:30:00'),
(7,1,'格力电风扇 落地扇',300,10,NULL,'落地风扇，大出风口，静音省电',1,0,'2026-07-10 11:00:00'),
(8,1,'苏泊尔电饭煲 4L',450,8,NULL,'智能电饭煲，4升容量，适合家庭使用',1,0,'2026-07-10 11:30:00'),
(9,1,'维达抽纸 3包装',40,70,NULL,'抽纸，3包装，柔软亲肤',1,0,'2026-07-10 12:00:00'),
(10,1,'海天酱油 1.9L',45,45,NULL,'金标生抽，1.9升装',1,0,'2026-07-10 12:30:00');

-- ======================================================
-- 9. 兑换记录表（不变）
-- ======================================================
DROP TABLE IF EXISTS `exchange_record`;
CREATE TABLE `exchange_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `user_id` int DEFAULT NULL,
  `product_id` int DEFAULT NULL,
  `exchange_code` varchar(32) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `used_time` datetime DEFAULT NULL,
  `expire_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `verified_by` int DEFAULT NULL,
  `verify_method` varchar(20) DEFAULT 'manual',
  PRIMARY KEY (`id`),
  KEY `idx_code_tenant` (`exchange_code`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `exchange_record` VALUES 
(1,1,50,1,'ASDF1234','used','2026-07-15 09:00:00','2026-07-22 09:00:00','2026-07-15 09:00:00',NULL,NULL),
(2,1,50,2,'QWER5678','used','2026-07-14 14:30:00','2026-07-21 14:30:00','2026-07-14 14:30:00',NULL,NULL),
(3,1,50,4,'ZXCV9012','pending',NULL,'2026-07-20 16:00:00','2026-07-13 16:00:00',NULL,NULL),
(4,1,50,6,'UIOP3456','pending',NULL,'2026-07-19 10:00:00','2026-07-12 10:00:00',NULL,NULL),
(5,1,51,9,'TYUI7890','expired',NULL,'2026-07-10 08:00:00','2026-07-03 08:00:00',NULL,NULL),
(6,1,51,10,'GHJK2345','used','2026-07-08 11:00:00','2026-07-15 11:00:00','2026-07-08 11:00:00',NULL,NULL),
(7,1,50,5,'LMNO6789','pending',NULL,'2026-07-18 14:00:00','2026-07-11 14:00:00',NULL,NULL),
(8,1,50,3,'PQRS0123','used','2026-07-07 10:30:00','2026-07-14 10:30:00','2026-07-07 10:30:00',NULL,NULL);

-- ======================================================
-- 10. 积分申报表（含原数据 + 3名村民）
-- ======================================================
DROP TABLE IF EXISTS `points_apply`;
CREATE TABLE `points_apply` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `user_id` int DEFAULT NULL,
  `rule_id` int DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `images` varchar(500) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `auditor_id` int DEFAULT NULL,
  `audit_remark` varchar(255) DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `source_type` varchar(20) DEFAULT 'user',
  `inspector_id` int DEFAULT NULL,
  `inspection_batch_id` bigint DEFAULT NULL,
  `inspection_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status_time` (`tenant_id`,`status`,`create_time`),
  KEY `idx_source_type` (`source_type`),
  KEY `idx_inspection_batch_id` (`inspection_batch_id`),
  KEY `idx_inspection_date` (`inspection_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `points_apply` VALUES 
(1,1,50,1,'今天把庭院打扫得干干净净，地面无垃圾杂物','/upload/20260715_yard_1.jpg','approved',51,'合格，继续保持！','2026-07-15 10:00:00','2026-07-15 09:00:00','user',NULL,NULL,NULL),
(2,1,50,2,'农具柴草按要求整齐堆放，庭院物品摆放有序','/upload/20260714_yard_2.jpg','approved',51,'做得很规范！','2026-07-14 15:00:00','2026-07-14 14:00:00','user',NULL,NULL,NULL),
(3,1,50,5,'坚持垃圾分类投放已满一个月，无混装现象','/upload/20260713_garbage_1.jpg','pending',NULL,NULL,NULL,'2026-07-13 09:30:00','user',NULL,NULL,NULL),
(4,1,50,9,'每周主动清理家门口周边公共道路的垃圾','/upload/20260712_road_1.jpg','approved',51,'带动了周边邻居，值得表扬！','2026-07-12 17:00:00','2026-07-12 16:00:00','user',NULL,NULL,NULL),
(5,1,50,13,'家禽全部实行圈养，圈舍卫生规范','/upload/20260711_chicken_1.jpg','pending',NULL,NULL,NULL,'2026-07-11 08:00:00','user',NULL,NULL,NULL),
(6,1,50,16,'门前三包落实到位，卫生、绿化、秩序都好','/upload/20260710_yard_3.jpg','approved',51,'非常棒，全村示范！','2026-07-10 11:00:00','2026-07-10 10:00:00','user',NULL,NULL,NULL),
(7,1,52,1,'打扫庭院卫生，保持干净整洁','','approved',51,'合格','2026-07-18 10:00:00','2026-07-18 09:00:00','user',NULL,NULL,NULL),
(8,1,52,5,'坚持垃圾分类一个月，无混装','','approved',51,'很好','2026-07-18 10:00:00','2026-07-18 09:30:00','user',NULL,NULL,NULL),
(9,1,53,2,'农具柴草摆放整齐，庭院有序','','approved',51,'通过','2026-07-18 10:00:00','2026-07-18 09:40:00','user',NULL,NULL,NULL),
(10,1,54,1,'庭院地面干净无杂物','','approved',51,'合格','2026-07-18 10:00:00','2026-07-18 09:50:00','user',NULL,NULL,NULL),
(11,1,54,9,'每周主动清理公共道路垃圾','','approved',51,'值得表扬','2026-07-18 10:00:00','2026-07-18 10:00:00','user',NULL,NULL,NULL);

-- ======================================================
-- 11. 积分流水表（含新村民）
-- ======================================================
DROP TABLE IF EXISTS `points_flow`;
CREATE TABLE `points_flow` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `change_amount` int DEFAULT NULL,
  `source_type` varchar(20) DEFAULT NULL,
  `source_id` int DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `tenant_id` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `points_flow` VALUES 
(1,50,10,'apply',1,'积分申报审核通过:庭院地面干净整洁，无垃圾杂物','2026-07-15 10:00:00',1),
(2,50,8,'apply',2,'积分申报审核通过:物品摆放有序，农具柴草堆放整齐','2026-07-14 15:00:00',1),
(3,50,5,'apply',4,'积分申报审核通过:每周主动清理自家周边公共道路上的垃圾','2026-07-12 17:00:00',1),
(4,50,15,'apply',6,'积分申报审核通过:门前三包落实到位（卫生、绿化、秩序）','2026-07-10 11:00:00',1),
(5,52,10,'apply',7,'积分申报审核通过:庭院地面干净整洁，无垃圾杂物','2026-07-18 10:00:00',1),
(6,52,15,'apply',8,'积分申报审核通过:严格按照四类垃圾分类，连续一个月无混装','2026-07-18 10:00:00',1),
(7,53,8,'apply',9,'积分申报审核通过:物品摆放有序，农具柴草堆放整齐','2026-07-18 10:00:00',1),
(8,54,10,'apply',10,'积分申报审核通过:庭院地面干净整洁，无垃圾杂物','2026-07-18 10:00:00',1),
(9,54,5,'apply',11,'积分申报审核通过:每周主动清理自家周边公共道路上的垃圾','2026-07-18 10:00:00',1);

-- ======================================================
-- 12. 检查批次表（暂空）
-- ======================================================
DROP TABLE IF EXISTS `inspection_batch`;
CREATE TABLE `inspection_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_name` varchar(100) NOT NULL COMMENT '批次名称',
  `inspection_date` date NOT NULL COMMENT '检查日期',
  `scope` varchar(255) DEFAULT NULL COMMENT '检查范围描述',
  `inspector_group` varchar(100) DEFAULT NULL COMMENT '检查小组/负责人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_inspection_date` (`inspection_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================
-- 13. 检查户汇总表（暂空）
-- ======================================================
DROP TABLE IF EXISTS `inspection_household`;
CREATE TABLE `inspection_household` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` bigint NOT NULL COMMENT '关联检查批次ID',
  `user_id` int NOT NULL COMMENT '户主用户ID',
  `total_score` int NOT NULL DEFAULT '0' COMMENT '本次检查总得分',
  `detail_json` json DEFAULT NULL COMMENT '详细得分JSON',
  `inspector_id` int DEFAULT NULL COMMENT '检查员ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '检查备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_batch_user` (`batch_id`,`user_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================
-- 14. 动态表（不变）
-- ======================================================
DROP TABLE IF EXISTS `moment`;
CREATE TABLE `moment` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `content` text,
  `images` varchar(1000) DEFAULT NULL,
  `likes_count` int DEFAULT 0,
  `comments_count` int DEFAULT 0,
  `status` tinyint DEFAULT 1,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tenant_id` int NOT NULL,
  INDEX idx_user_id (`user_id`),
  INDEX idx_create_time (`create_time`),
  INDEX idx_tenant_id (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `moment` VALUES 
(1,50,'今天把家门口的庭院彻底打扫了一遍，还种了几盆花，看着舒服多了！欢迎大家来我家参观指导🌺🌿',NULL,2,3,1,'2026-07-15 09:30:00','2026-07-15 09:30:00',1),
(2,50,'积极响应村里号召，把垃圾分类做好！今天开始坚持每天分类投放，为龙胜村环境出一份力♻️',NULL,1,2,1,'2026-07-14 18:20:00','2026-07-14 18:20:00',1),
(3,51,'龙胜村2026年第二季度人居环境评比即将开始，请大家提前做好庭院卫生，评分标准见村务通知📢',NULL,1,4,1,'2026-07-13 10:00:00','2026-07-13 10:00:00',1),
(4,50,'参加村里组织的"美丽庭院"评比活动，我家拿了二等奖！感谢村委的鼓励，我会继续加油💪','https://example.com/images/beautiful_yard.jpg',2,5,1,'2026-07-12 16:45:00','2026-07-12 16:45:00',1),
(5,51,'今晚7点村委会广场有"乡村振兴"主题晚会，欢迎广大村民前来观看！现场还有有奖问答环节🎉',NULL,2,6,1,'2026-07-11 14:00:00','2026-07-11 14:00:00',1);

-- ======================================================
-- 15. 动态点赞表
-- ======================================================
DROP TABLE IF EXISTS `moment_like`;
CREATE TABLE `moment_like` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `moment_id` bigint NOT NULL,
  `user_id` int NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `tenant_id` int NOT NULL,
  UNIQUE KEY uk_moment_user (`moment_id`,`user_id`),
  INDEX idx_moment_id (`moment_id`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `moment_like` VALUES 
(1,1,51,'2026-07-15 09:35:00',1),
(2,1,50,'2026-07-15 09:40:00',1),
(3,2,51,'2026-07-14 18:30:00',1),
(4,3,50,'2026-07-13 10:10:00',1),
(5,4,51,'2026-07-12 16:50:00',1),
(6,5,50,'2026-07-11 14:10:00',1);

-- ======================================================
-- 16. 动态评论表
-- ======================================================
DROP TABLE IF EXISTS `moment_comment`;
CREATE TABLE `moment_comment` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `moment_id` bigint NOT NULL,
  `user_id` int NOT NULL,
  `parent_id` bigint DEFAULT 0,
  `content` varchar(500) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `tenant_id` int NOT NULL,
  INDEX idx_moment_id (`moment_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_parent_id (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `moment_comment` VALUES 
(1,1,51,0,'吴姐真勤快！院子收拾得很漂亮，大家可以向她学习！','2026-07-15 09:40:00',1),
(2,1,50,0,'谢谢主任夸奖！还要继续努力💪','2026-07-15 09:50:00',1),
(3,1,50,0,'我家也正准备收拾呢，向你们学习！','2026-07-15 10:00:00',1),
(4,2,51,0,'很好！垃圾分类是大事，大家都要重视起来！','2026-07-14 18:35:00',1),
(5,2,50,0,'已经坚持一周了，感觉挺好的','2026-07-14 19:00:00',1),
(6,3,50,0,'收到！已经通知家人了，保证搞好卫生','2026-07-13 10:15:00',1),
(7,3,51,0,'好的主任，我们组已经通知到位！','2026-07-13 10:30:00',1),
(8,3,50,0,'具体什么时候评比？我们好提前准备','2026-07-13 10:45:00',1),
(9,3,51,0,'定在下周三上午，请大家做好准备！','2026-07-13 11:00:00',1),
(10,4,51,0,'恭喜恭喜！确实很漂亮，实至名归！👏','2026-07-12 16:55:00',1),
(11,4,50,0,'一等奖是隔壁李姐家的，确实更精致，向人家学习！','2026-07-12 17:10:00',1),
(12,4,50,0,'你们家已经很好了！继续保持！','2026-07-12 17:20:00',1),
(13,4,50,0,'我也想参加下一次评比，怎么报名呀？','2026-07-12 17:30:00',1),
(14,4,51,0,'关注村务通知，下次会提前发通知的！','2026-07-12 17:40:00',1),
(15,5,50,0,'太好了！一定去参加！','2026-07-11 14:15:00',1),
(16,5,51,0,'我带孩子一起去，有儿童节目吗？','2026-07-11 14:30:00',1),
(17,5,51,0,'有小朋友的节目，放心带孩子来！','2026-07-11 14:40:00',1),
(18,5,50,0,'有奖问答的题目难不难？','2026-07-11 14:50:00',1),
(19,5,51,0,'都是乡村振兴和村规民约的常识，不难的！','2026-07-11 15:00:00',1),
(20,5,50,0,'好的，我提前到！','2026-07-11 15:10:00',1);

-- ======================================================
-- 17. 更新动态表的点赞数和评论数
-- ======================================================
UPDATE moment SET likes_count = 2, comments_count = 3 WHERE id = 1;
UPDATE moment SET likes_count = 1, comments_count = 2 WHERE id = 2;
UPDATE moment SET likes_count = 1, comments_count = 4 WHERE id = 3;
UPDATE moment SET likes_count = 2, comments_count = 5 WHERE id = 4;
UPDATE moment SET likes_count = 2, comments_count = 6 WHERE id = 5;

-- ======================================================
-- 18. 订阅消息表
-- ======================================================
DROP TABLE IF EXISTS `subscribe_message`;
CREATE TABLE `subscribe_message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '管理员用户ID',
  `tenant_id` int NOT NULL COMMENT '租户ID',
  `template_id` varchar(64) NOT NULL COMMENT '微信模板ID',
  `openid` varchar(64) NOT NULL COMMENT '管理员微信openid',
  `status` tinyint DEFAULT '1' COMMENT '订阅状态：1-已订阅，0-已取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_template` (`user_id`,`template_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================
-- 19. 评比发布表
-- ======================================================
DROP TABLE IF EXISTS `inspection_publish`;
CREATE TABLE `inspection_publish` (
  `id` int NOT NULL AUTO_INCREMENT,
  `batch_id` bigint NOT NULL COMMENT '检查批次ID',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态：draft-草稿，published-已发布',
  `result_json` json DEFAULT NULL COMMENT '评比结果快照JSON',
  `published_at` datetime DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `tenant_id` int NOT NULL COMMENT '租户ID',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch` (`batch_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================
-- 20. 活动二维码表
-- ======================================================
DROP TABLE IF EXISTS `activity_qrcode`;
CREATE TABLE `activity_qrcode` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL DEFAULT 1 COMMENT '租户ID',
  `activity_id` int NOT NULL COMMENT '活动ID',
  `qrcode_type` varchar(20) NOT NULL COMMENT '类型：signin-签到，checkout-签退',
  `qrcode_key` varchar(64) DEFAULT NULL COMMENT '动态码唯一标识（动态码时非空）',
  `fixed_code` varchar(64) NOT NULL COMMENT '固定码（活动期间不变）',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-有效，0-失效',
  `expire_time` datetime DEFAULT NULL COMMENT '动态码过期时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fixed_code` (`fixed_code`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_activity_type` (`activity_id`, `qrcode_type`),
  KEY `idx_qrcode_key` (`qrcode_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动二维码表';

-- ======================================================
-- 21. 意见反馈表（新增）
-- ======================================================
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '提交用户ID',
  `tenant_id` int NOT NULL COMMENT '租户ID',
  `category` varchar(20) NOT NULL COMMENT '类型：complaint-投诉，suggestion-建议，question-咨询，other-其他',
  `content` text NOT NULL COMMENT '反馈内容',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片URL列表，逗号分隔',
  `contact` varchar(64) DEFAULT NULL COMMENT '联系方式（可选）',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending-待处理，processing-处理中，resolved-已处理，closed-已关闭',
  `reply` text DEFAULT NULL COMMENT '管理员回复',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `replied_by` int DEFAULT NULL COMMENT '回复人ID（管理员）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见反馈表';

-- ======================================================
-- 22. 操作日志表（空）
-- ======================================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `operation_type` varchar(32) DEFAULT NULL,
  `content` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `tenant_id` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================
-- 23. 警告日志表（空）
-- ======================================================
DROP TABLE IF EXISTS `warning_log`;
CREATE TABLE `warning_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `warning_type` varchar(32) DEFAULT NULL,
  `warning_content` varchar(255) DEFAULT NULL,
  `occurred_at` datetime DEFAULT NULL,
  `is_resolved` tinyint DEFAULT '0',
  `resolved_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================
-- 24. 居民档案表（用于微信注册时匹配户主信息）
-- ======================================================
DROP TABLE IF EXISTS `resident_profile`;
CREATE TABLE `resident_profile` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tenant_id INT NOT NULL COMMENT '租户ID',
    owner_name VARCHAR(100) NOT NULL COMMENT '户主姓名',
    family_members VARCHAR(500) COMMENT '家庭成员姓名（逗号分隔）',
    phone VARCHAR(30) COMMENT '联系方式（支持座机号）',
    id_card VARCHAR(30) COMMENT '身份证号',
    village_group VARCHAR(32) COMMENT '所属区域（一区/二区/三区）',
    address VARCHAR(300) COMMENT '门牌号/地号',
    household_code VARCHAR(32) COMMENT '户编号',
    total_people INT DEFAULT 0 COMMENT '人数',
    remark VARCHAR(600) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0,
    INDEX idx_tenant_phone (tenant_id, phone),
    INDEX idx_tenant_owner (tenant_id, owner_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='居民档案表';

-- ======================================================
-- 25. 评分证据表（功能一：证据链评分）
-- ======================================================
DROP TABLE IF EXISTS `score_evidence`;
CREATE TABLE `score_evidence` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `apply_id` bigint NOT NULL COMMENT '关联积分申请/评分记录ID（points_apply.id）',
    `photo_urls` varchar(1000) DEFAULT NULL COMMENT '照片URL列表（逗号分隔）',
    `photo_count` int DEFAULT '0' COMMENT '照片数量',
    `location` varchar(100) DEFAULT NULL COMMENT '拍摄位置（GPS或地址）',
    `inspector_id` bigint DEFAULT NULL COMMENT '检查人ID',
    `batch_id` bigint DEFAULT NULL COMMENT '关联检查批次ID',
    `rule_version` varchar(20) DEFAULT NULL COMMENT '规则版本号',
    `has_watermark` tinyint DEFAULT '0' COMMENT '是否已加水印：0-否，1-是',
    `rule_name` varchar(100) DEFAULT NULL COMMENT '扣分规则名称（冗余）',
    `user_name` varchar(50) DEFAULT NULL COMMENT '户主姓名（冗余）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` int NOT NULL DEFAULT 1 COMMENT '租户ID',
    `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_apply_id` (`apply_id`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_inspector_id` (`inspector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分证据表';


-- ======================================================
-- 26. 整改任务表（功能二：整改闭环）
-- ======================================================
DROP TABLE IF EXISTS `rectification_task`;
CREATE TABLE `rectification_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `apply_id` bigint NOT NULL COMMENT '关联积分申请/评分记录ID',
    `user_id` bigint NOT NULL COMMENT '责任户主用户ID',
    `batch_id` bigint DEFAULT NULL COMMENT '关联检查批次ID',
    `rule_name` varchar(100) NOT NULL COMMENT '扣分规则名称',
    `requirement` varchar(500) DEFAULT NULL COMMENT '整改要求',
    `deadline` datetime NOT NULL COMMENT '整改截止时间',
    `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending待整改/reviewing待复核/resolved已销项/overdue逾期',
    `before_photos` varchar(1000) DEFAULT NULL COMMENT '整改前照片（原证据照片）',
    `after_photos` varchar(1000) DEFAULT NULL COMMENT '整改后照片（村民提交）',
    `submit_time` datetime DEFAULT NULL COMMENT '村民提交整改时间',
    `submit_remark` varchar(500) DEFAULT NULL COMMENT '整改说明',
    `review_result` varchar(20) DEFAULT NULL COMMENT '复核结果：passed通过/rejected不通过',
    `review_remark` varchar(500) DEFAULT NULL COMMENT '复核备注',
    `reviewer_id` bigint DEFAULT NULL COMMENT '复核人ID',
    `review_time` datetime DEFAULT NULL COMMENT '复核时间',
    `reward_points` int DEFAULT '0' COMMENT '整改奖励积分（扣分50%）',
    `inspector_id` bigint DEFAULT NULL COMMENT '检查人ID',
    `tenant_id` int NOT NULL COMMENT '租户ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_apply_id` (`apply_id`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='整改任务表';


-- ======================================================
-- 27. 申诉记录表（功能五：申诉与复核）
-- ======================================================
DROP TABLE IF EXISTS `appeal_record`;
CREATE TABLE `appeal_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `apply_id` bigint NOT NULL COMMENT '关联积分申请/评分记录ID',
    `user_id` bigint NOT NULL COMMENT '申诉人用户ID',
    `user_name` varchar(50) DEFAULT NULL COMMENT '申诉人姓名（冗余）',
    `reason` varchar(500) NOT NULL COMMENT '申诉理由',
    `evidence_photos` varchar(1000) DEFAULT NULL COMMENT '补充证据照片（逗号分隔）',
    `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending申诉中/resolved已处理',
    `decision` varchar(20) DEFAULT NULL COMMENT '复核决定：upheld维持/modified修改/revoked撤销',
    `decision_detail` varchar(500) DEFAULT NULL COMMENT '处理说明',
    `reviewer_id` bigint DEFAULT NULL COMMENT '复核人ID',
    `reviewer_name` varchar(50) DEFAULT NULL COMMENT '复核人姓名（冗余）',
    `review_time` datetime DEFAULT NULL COMMENT '复核时间',
    `batch_id` bigint DEFAULT NULL COMMENT '关联检查批次ID',
    `tenant_id` int NOT NULL COMMENT '租户ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_apply_id` (`apply_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申诉记录表';


-- ======================================================
-- 28. 红黑榜公示快照表（功能六：自动红黑榜）
-- ======================================================
DROP TABLE IF EXISTS `publish_snapshot`;
CREATE TABLE `publish_snapshot` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `batch_id` bigint NOT NULL COMMENT '关联检查批次ID',
    `month` varchar(7) NOT NULL COMMENT '月份标识，如2026-08',
    `snapshot_data` json NOT NULL COMMENT '全量快照数据（排名+分数+标签）',
    `red_list` text DEFAULT NULL COMMENT '红榜户ID列表（JSON格式）',
    `black_list` text DEFAULT NULL COMMENT '黑榜户ID列表（JSON格式）',
    `publish_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `publish_by` bigint DEFAULT NULL COMMENT '发布人ID',
    `publish_by_name` varchar(50) DEFAULT NULL COMMENT '发布人姓名（冗余）',
    `tenant_id` int NOT NULL COMMENT '租户ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_id` (`batch_id`),
    KEY `idx_month` (`month`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红黑榜公示快照表';


-- ======================================================
-- 29. 离线同步记录表（功能四：离线巡检，幂等性保证）
-- ======================================================
DROP TABLE IF EXISTS `offline_sync_record`;
CREATE TABLE `offline_sync_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `client_event_id` varchar(64) NOT NULL COMMENT '客户端事件ID（UUID）',
    `processed` tinyint DEFAULT '1' COMMENT '是否已处理：1-已处理',
    `process_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
    `tenant_id` int NOT NULL COMMENT '租户ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_client_event_id` (`client_event_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离线同步记录表';


-- ======================================================
-- 30. 为 points_apply 表增加 has_evidence 字段（功能一）
-- ======================================================
ALTER TABLE `points_apply` ADD COLUMN IF NOT EXISTS `has_evidence` tinyint(1) DEFAULT '0' COMMENT '是否有证据：0-无，1-有';


-- ======================================================
-- ====== v2.0 新增表结构 ======
-- ======================================================

-- ======================================================
-- 31. 季度快照表（v2.0）
-- ======================================================
DROP TABLE IF EXISTS `quarterly_snapshot`;
CREATE TABLE `quarterly_snapshot` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` int NOT NULL COMMENT '租户ID',
    `quarter` varchar(10) NOT NULL COMMENT '季度标识，如：2026-Q3',
    `user_id` int NOT NULL COMMENT '用户ID',
    `quarter_earned_points` int DEFAULT 0 COMMENT '本季获得积分',
    `quarter_net_points` int DEFAULT 0 COMMENT '本季净积分（含扣分和整改恢复）',
    `previous_quarter_points` int DEFAULT 0 COMMENT '上季获得积分',
    `progress_points` int DEFAULT 0 COMMENT '进步分（本季-上季）',
    `rank_points` int DEFAULT NULL COMMENT '积分排名（1=最高）',
    `rank_progress` int DEFAULT NULL COMMENT '进步排名（1=进步最大）',
    `tag` varchar(20) DEFAULT NULL COMMENT 'red/progress/normal/warning',
    `rule_count` int DEFAULT 0 COMMENT '当季加分事项数量（去重，用于同名次排序）',
    `activity_count` int DEFAULT 0 COMMENT '当季活动参与次数（用于同名次排序）',
    `no_penalty_days` int DEFAULT 0 COMMENT '当季连续零扣分天数（用于同名次排序）',
    `last_activity_time` datetime DEFAULT NULL COMMENT '本季最后一次积分变动时间（用于同分排序）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_quarter` (`tenant_id`, `quarter`, `user_id`),
    KEY `idx_quarter_tag` (`quarter`, `tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='季度快照表';

-- ======================================================
-- 32. 重要贡献认定表（v2.0）
-- ======================================================
DROP TABLE IF EXISTS `important_contribution`;
CREATE TABLE `important_contribution` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` int NOT NULL COMMENT '租户ID',
    `user_id` int NOT NULL COMMENT '贡献人用户ID',
    `contribution_type` varchar(20) DEFAULT NULL COMMENT 'rescue/report/mediate/resource/help',
    `contribution_desc` varchar(500) NOT NULL COMMENT '贡献描述',
    `points` int NOT NULL COMMENT '认定积分',
    `status` varchar(20) DEFAULT 'pending' COMMENT 'pending/approved/rejected',
    `approved_by` int DEFAULT NULL COMMENT '认定人ID',
    `approved_time` datetime DEFAULT NULL COMMENT '认定时间',
    `evidence_photos` varchar(1000) DEFAULT NULL COMMENT '佐证照片（逗号分隔）',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='重要贡献认定表';

-- ======================================================
-- 33. 活动参与记录表（v2.0）
-- ======================================================
DROP TABLE IF EXISTS `activity_participation`;
CREATE TABLE `activity_participation` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` int NOT NULL COMMENT '租户ID',
    `user_id` int NOT NULL COMMENT '参与用户ID',
    `activity_id` int DEFAULT NULL COMMENT '关联activity表ID',
    `activity_name` varchar(100) NOT NULL COMMENT '活动名称',
    `activity_type` varchar(20) DEFAULT 'regular' COMMENT 'regular/event/emergency',
    `earned_points` int DEFAULT 0 COMMENT '获得积分',
    `participate_time` datetime NOT NULL COMMENT '参与时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `participate_time`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与记录表';


-- ======================================================
-- 验证数据插入情况
-- ======================================================
-- SELECT COUNT(*) FROM tenant;                    -- 1
-- SELECT COUNT(*) FROM user;                      -- 5
-- SELECT COUNT(*) FROM points_rule;               -- 63
-- SELECT COUNT(*) FROM activity;                  -- 5
-- SELECT COUNT(*) FROM activity_registration;     -- 10
-- SELECT COUNT(*) FROM notice;                    -- 5
-- SELECT COUNT(*) FROM policy;                    -- 5
-- SELECT COUNT(*) FROM product;                   -- 10
-- SELECT COUNT(*) FROM exchange_record;           -- 8
-- SELECT COUNT(*) FROM points_apply;              -- 11
-- SELECT COUNT(*) FROM points_flow;               -- 9
-- SELECT COUNT(*) FROM moment;                    -- 5
-- SELECT COUNT(*) FROM moment_like;               -- 6
-- SELECT COUNT(*) FROM moment_comment;            -- 20
-- SELECT COUNT(*) FROM subscribe_message;         -- 0
-- SELECT COUNT(*) FROM inspection_publish;        -- 0
-- SELECT COUNT(*) FROM activity_qrcode;           -- 0
-- SELECT COUNT(*) FROM feedback;                  -- 0
-- SELECT COUNT(*) FROM resident_profile;          -- 0（执行 resident_data.sql 后应为 748）
-- SELECT COUNT(*) FROM score_evidence;            -- 0
-- SELECT COUNT(*) FROM rectification_task;        -- 0
-- SELECT COUNT(*) FROM appeal_record;             -- 0
-- SELECT COUNT(*) FROM publish_snapshot;          -- 0
-- SELECT COUNT(*) FROM offline_sync_record;       -- 0
-- SELECT COUNT(*) FROM quarterly_snapshot;        -- 0（v2.0新增）
-- SELECT COUNT(*) FROM important_contribution;    -- 0（v2.0新增）
-- SELECT COUNT(*) FROM activity_participation;    -- 0（v2.0新增）


-- ======================================================
-- 脚本结束
-- ======================================================