-- V2 规则约束与决策追踪迁移
-- 依赖：init_full_code.sql 已创建 points_rule、points_apply、score_evidence、important_contribution、activity_participation
-- 兼容：MySQL 9.6；本脚本可重复执行

-- 创建规则约束表：保存规则版本、时间窗口、次数上限和审核要求。
CREATE TABLE IF NOT EXISTS rule_constraint (
  id BIGINT NOT NULL AUTO_INCREMENT,
  rule_id INT NOT NULL,
  rule_version VARCHAR(20) NOT NULL,
  window_type VARCHAR(20) NOT NULL,
  window_value INT NOT NULL DEFAULT 0,
  max_times INT NOT NULL DEFAULT 0,
  require_photo TINYINT NOT NULL DEFAULT 0,
  require_review_flow VARCHAR(20) DEFAULT 'single',
  min_period_days INT DEFAULT NULL,
  min_frequency VARCHAR(32) DEFAULT NULL,
  effective_from DATETIME NOT NULL,
  effective_to DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_rule_constraint_version (rule_id, rule_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建规则观察事件表：保存混装、绿化破坏、垃圾持续存在等可审计事实。
CREATE TABLE IF NOT EXISTS rule_observation_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id INT NOT NULL,
  user_id INT NOT NULL,
  rule_id INT NOT NULL,
  rule_version VARCHAR(20) NOT NULL,
  event_code VARCHAR(40) NOT NULL,
  event_time DATETIME NOT NULL,
  source_apply_id VARCHAR(64) DEFAULT NULL,
  evidence_id VARCHAR(64) DEFAULT NULL,
  created_by INT DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_observation_source_event (source_apply_id, event_code),
  KEY idx_observation_user_rule_time (tenant_id, user_id, rule_id, event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建规则阶梯发生表：记录 #43-45 的累计次数和撤销标记，不重算历史阶梯。
CREATE TABLE IF NOT EXISTS rule_occurrence (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id INT NOT NULL,
  user_id INT NOT NULL,
  family_code VARCHAR(64) NOT NULL,
  occurrence_no INT NOT NULL,
  event_time DATETIME NOT NULL,
  source_apply_id VARCHAR(64) NOT NULL,
  rule_version VARCHAR(20) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  cancelled TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_rule_occurrence_idempotency (idempotency_key),
  KEY idx_rule_occurrence_user_family (tenant_id, user_id, family_code, event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建规则组件定义表：保存 #16 门前三包的卫生、绿化、秩序三个独立子项。
CREATE TABLE IF NOT EXISTS rule_component (
  id BIGINT NOT NULL AUTO_INCREMENT,
  rule_id INT NOT NULL,
  rule_version VARCHAR(20) NOT NULL,
  component_code VARCHAR(32) NOT NULL,
  component_name VARCHAR(100) NOT NULL,
  points INT NOT NULL DEFAULT 0,
  required TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_rule_component_version (rule_id, rule_version, component_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建规则组件结果表：保存每次申请的组件通过、不通过或未检查结果。
CREATE TABLE IF NOT EXISTS rule_component_result (
  id BIGINT NOT NULL AUTO_INCREMENT,
  apply_id VARCHAR(64) NOT NULL,
  component_id BIGINT NOT NULL,
  result VARCHAR(20) NOT NULL,
  evidence_id VARCHAR(64) DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_rule_component_result_apply (apply_id, component_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建审核记录表：保存 single/double 审核各阶段及最终决定。
CREATE TABLE IF NOT EXISTS rule_review (
  id BIGINT NOT NULL AUTO_INCREMENT,
  apply_id VARCHAR(64) NOT NULL,
  stage_no INT NOT NULL,
  reviewer_id INT NOT NULL,
  decision VARCHAR(20) NOT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  review_time DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_rule_review_apply_stage (apply_id, stage_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 为 points_apply 增加规则版本、窗口、行为、审核、组件和持续时间字段（幂等动态 DDL）。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'rule_version');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN rule_version VARCHAR(20) DEFAULT ''1.0''', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'window_start');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN window_start DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'window_end');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN window_end DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'constraint_snapshot');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN constraint_snapshot JSON DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'validation_status');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN validation_status VARCHAR(20) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'decision_family_code');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN decision_family_code VARCHAR(64) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'decision_occurrence_no');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN decision_occurrence_no INT DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'component_result_json');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN component_result_json JSON DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'activity_id');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN activity_id INT DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'review_stage');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN review_stage VARCHAR(20) DEFAULT ''single''', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'behavior_code');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN behavior_code VARCHAR(32) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'duration_start');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN duration_start DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'duration_end');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN duration_end DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'duration_hours');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN duration_hours INT DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'validation_snapshot');
SET @s := IF(@c = 0, 'ALTER TABLE points_apply ADD COLUMN validation_snapshot JSON DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 为 points_rule 增加阶梯族、阶梯序号和规则版本字段（幂等动态 DDL）。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_rule' AND column_name = 'rule_family_code');
SET @s := IF(@c = 0, 'ALTER TABLE points_rule ADD COLUMN rule_family_code VARCHAR(64) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_rule' AND column_name = 'step_no');
SET @s := IF(@c = 0, 'ALTER TABLE points_rule ADD COLUMN step_no INT DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_rule' AND column_name = 'rule_version');
SET @s := IF(@c = 0, 'ALTER TABLE points_rule ADD COLUMN rule_version VARCHAR(20) DEFAULT ''1.0''', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 为 score_evidence 增加行为代码和观察时间字段（幂等动态 DDL）。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'score_evidence' AND column_name = 'behavior_code');
SET @s := IF(@c = 0, 'ALTER TABLE score_evidence ADD COLUMN behavior_code VARCHAR(32) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'score_evidence' AND column_name = 'observed_at');
SET @s := IF(@c = 0, 'ALTER TABLE score_evidence ADD COLUMN observed_at DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'score_evidence' AND column_name = 'observation_type');
SET @s := IF(@c = 0, 'ALTER TABLE score_evidence ADD COLUMN observation_type VARCHAR(20) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 为 important_contribution 增加规则关联、版本、期间、调解和二审字段（幂等动态 DDL）。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'rule_id');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN rule_id INT DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'rule_version');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN rule_version VARCHAR(20) DEFAULT ''1.0''', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'source_ref');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN source_ref VARCHAR(128) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'adopted_at');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN adopted_at DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'second_approved_by');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN second_approved_by INT DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'second_approved_time');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN second_approved_time DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'subject_ref');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN subject_ref VARCHAR(128) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'period_start');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN period_start DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'period_end');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN period_end DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'care_frequency');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN care_frequency VARCHAR(32) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'case_ref');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN case_ref VARCHAR(128) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'parties_json');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN parties_json JSON DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'outcome');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN outcome VARCHAR(20) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'outcome_confirmed_at');
SET @s := IF(@c = 0, 'ALTER TABLE important_contribution ADD COLUMN outcome_confirmed_at DATETIME DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 为 activity_participation 增加规则关联、版本、核验状态和证据字段（幂等动态 DDL）。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'activity_participation' AND column_name = 'rule_id');
SET @s := IF(@c = 0, 'ALTER TABLE activity_participation ADD COLUMN rule_id INT DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'activity_participation' AND column_name = 'rule_version');
SET @s := IF(@c = 0, 'ALTER TABLE activity_participation ADD COLUMN rule_version VARCHAR(20) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'activity_participation' AND column_name = 'verification_status');
SET @s := IF(@c = 0, 'ALTER TABLE activity_participation ADD COLUMN verification_status VARCHAR(20) DEFAULT ''pending''', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'activity_participation' AND column_name = 'evidence_id');
SET @s := IF(@c = 0, 'ALTER TABLE activity_participation ADD COLUMN evidence_id VARCHAR(64) DEFAULT NULL', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 为重要贡献建立意见来源唯一索引，防止同一意见重复认定。
SET @c := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND index_name = 'uk_contribution_source_rule');
SET @s := IF(@c = 0, 'CREATE UNIQUE INDEX uk_contribution_source_rule ON important_contribution (tenant_id, rule_id, source_ref)', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 为调解案件建立案件来源唯一索引，防止同一案件重复奖励。
SET @c := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND index_name = 'uk_mediation_case_rule');
SET @s := IF(@c = 0, 'CREATE UNIQUE INDEX uk_mediation_case_rule ON important_contribution (tenant_id, rule_id, case_ref)', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 为 #43-45 写入阶梯族、阶梯序号和规则版本；不修改 points 或历史流水。
UPDATE points_rule SET rule_family_code = 'poultry_free_range', step_no = 1, rule_version = '1.0' WHERE id = 43 AND (rule_family_code IS NULL OR step_no IS NULL OR rule_version IS NULL);
UPDATE points_rule SET rule_family_code = 'poultry_free_range', step_no = 2, rule_version = '1.0' WHERE id = 44 AND (rule_family_code IS NULL OR step_no IS NULL OR rule_version IS NULL);
UPDATE points_rule SET rule_family_code = 'poultry_free_range', step_no = 3, rule_version = '1.0' WHERE id = 45 AND (rule_family_code IS NULL OR step_no IS NULL OR rule_version IS NULL);

-- 为滚动 30 天约束写入 #5、#10；重复执行由唯一键和 INSERT IGNORE 保证幂等。
INSERT IGNORE INTO rule_constraint
  (rule_id, rule_version, window_type, window_value, max_times, require_photo, require_review_flow, effective_from)
VALUES
  (5, '1.0', 'rolling_days', 30, 1, 1, 'single', CURRENT_TIMESTAMP),
  (10, '1.0', 'rolling_days', 30, 1, 1, 'single', CURRENT_TIMESTAMP);

-- 为活动规则 #12 写入自然月四次上限和双重审核约束。
INSERT IGNORE INTO rule_constraint
  (rule_id, rule_version, window_type, window_value, max_times, require_photo, require_review_flow, effective_from)
VALUES (12, '1.0', 'natural_month', 1, 4, 1, 'double', CURRENT_TIMESTAMP);

-- 为门前三包写入三个独立组件；已确认允许部分完成，因此 required=0。
INSERT IGNORE INTO rule_component
  (rule_id, rule_version, component_code, component_name, points, required)
VALUES
  (16, '1.0', 'sanitation', '卫生', 5, 0),
  (16, '1.0', 'greenery', '绿化', 5, 0),
  (16, '1.0', 'order', '秩序', 5, 0);

-- 为规则 #37 写入垃圾堆积持续时间约束，超过 48 小时才允许扣分。
INSERT IGNORE INTO rule_constraint
  (rule_id, rule_version, window_type, window_value, max_times, require_photo, require_review_flow, effective_from)
VALUES
  (37, '1.0', 'duration_hours', 48, 0, 1, 'single', CURRENT_TIMESTAMP);

-- ROLLBACK
-- 回滚请执行同目录 V2__rule_constraint_rollback.sql；生产环境回滚前应备份新增字段中的规则决策、证据和审核数据。
