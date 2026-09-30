-- V2 规则约束与决策追踪回滚脚本
-- 说明：仅删除 V2 新增结构，不删除 points_apply、points_flow、score_evidence 等既有业务数据。

-- 删除规则组件结果表。
DROP TABLE IF EXISTS rule_component_result;

-- 删除规则组件定义表。
DROP TABLE IF EXISTS rule_component;

-- 删除审核记录表。
DROP TABLE IF EXISTS rule_review;

-- 删除规则阶梯发生表。
DROP TABLE IF EXISTS rule_occurrence;

-- 删除规则观察事件表。
DROP TABLE IF EXISTS rule_observation_event;

-- 删除规则约束表。
DROP TABLE IF EXISTS rule_constraint;

-- 删除重要贡献唯一索引（按存在性动态执行）。
SET @c := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND index_name = 'uk_contribution_source_rule'); SET @s := IF(@c > 0, 'DROP INDEX uk_contribution_source_rule ON important_contribution', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND index_name = 'uk_mediation_case_rule'); SET @s := IF(@c > 0, 'DROP INDEX uk_mediation_case_rule ON important_contribution', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 清除 #43-45 的阶梯元数据，保留原 points_rule 分值和历史数据。
SET @has_family := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_rule' AND column_name = 'rule_family_code');
SET @has_step := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_rule' AND column_name = 'step_no');
SET @has_version := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_rule' AND column_name = 'rule_version');
SET @s := IF(@has_family > 0 AND @has_step > 0 AND @has_version > 0, 'UPDATE points_rule SET rule_family_code = NULL, step_no = NULL, rule_version = NULL WHERE id IN (43,44,45) AND rule_family_code = ''poultry_free_range''', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 删除 points_apply 新增字段（按存在性动态执行，兼容重复回滚）。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'rule_version'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN rule_version', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'window_start'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN window_start', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'window_end'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN window_end', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'constraint_snapshot'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN constraint_snapshot', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'validation_status'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN validation_status', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'decision_family_code'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN decision_family_code', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'decision_occurrence_no'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN decision_occurrence_no', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'component_result_json'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN component_result_json', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'activity_id'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN activity_id', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'review_stage'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN review_stage', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'behavior_code'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN behavior_code', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'duration_start'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN duration_start', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'duration_end'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN duration_end', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'duration_hours'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN duration_hours', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_apply' AND column_name = 'validation_snapshot'); SET @s := IF(@c > 0, 'ALTER TABLE points_apply DROP COLUMN validation_snapshot', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 删除 points_rule 新增字段。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_rule' AND column_name = 'rule_family_code'); SET @s := IF(@c > 0, 'ALTER TABLE points_rule DROP COLUMN rule_family_code', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_rule' AND column_name = 'step_no'); SET @s := IF(@c > 0, 'ALTER TABLE points_rule DROP COLUMN step_no', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'points_rule' AND column_name = 'rule_version'); SET @s := IF(@c > 0, 'ALTER TABLE points_rule DROP COLUMN rule_version', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 删除 score_evidence 新增字段。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'score_evidence' AND column_name = 'behavior_code'); SET @s := IF(@c > 0, 'ALTER TABLE score_evidence DROP COLUMN behavior_code', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'score_evidence' AND column_name = 'observed_at'); SET @s := IF(@c > 0, 'ALTER TABLE score_evidence DROP COLUMN observed_at', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'score_evidence' AND column_name = 'observation_type'); SET @s := IF(@c > 0, 'ALTER TABLE score_evidence DROP COLUMN observation_type', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 删除 important_contribution 新增字段。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'rule_id'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN rule_id', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'rule_version'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN rule_version', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'source_ref'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN source_ref', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'adopted_at'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN adopted_at', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'second_approved_by'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN second_approved_by', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'second_approved_time'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN second_approved_time', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'subject_ref'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN subject_ref', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'period_start'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN period_start', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'period_end'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN period_end', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'care_frequency'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN care_frequency', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'case_ref'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN case_ref', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'parties_json'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN parties_json', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'outcome'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN outcome', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'important_contribution' AND column_name = 'outcome_confirmed_at'); SET @s := IF(@c > 0, 'ALTER TABLE important_contribution DROP COLUMN outcome_confirmed_at', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;

-- 删除 activity_participation 新增字段。
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'activity_participation' AND column_name = 'rule_id'); SET @s := IF(@c > 0, 'ALTER TABLE activity_participation DROP COLUMN rule_id', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'activity_participation' AND column_name = 'rule_version'); SET @s := IF(@c > 0, 'ALTER TABLE activity_participation DROP COLUMN rule_version', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'activity_participation' AND column_name = 'verification_status'); SET @s := IF(@c > 0, 'ALTER TABLE activity_participation DROP COLUMN verification_status', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
SET @c := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'activity_participation' AND column_name = 'evidence_id'); SET @s := IF(@c > 0, 'ALTER TABLE activity_participation DROP COLUMN evidence_id', 'SELECT 1'); PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
