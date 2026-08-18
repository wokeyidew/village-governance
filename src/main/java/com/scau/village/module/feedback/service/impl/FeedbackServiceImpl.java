package com.scau.village.module.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.feedback.dto.FeedbackReplyDto;
import com.scau.village.module.feedback.dto.FeedbackSubmitDto;
import com.scau.village.module.feedback.entity.Feedback;
import com.scau.village.module.feedback.mapper.FeedbackMapper;
import com.scau.village.module.feedback.service.FeedbackService;
import com.scau.village.module.feedback.vo.FeedbackVO;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 意见反馈服务实现类
 *
 * @author system
 * @since 2026-07-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public Feedback submit(Integer userId, Integer tenantId, FeedbackSubmitDto dto) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setTenantId(tenantId);
        feedback.setCategory(dto.getCategory());
        feedback.setContent(dto.getContent());
        feedback.setImages(dto.getImages());
        feedback.setContact(dto.getContact());
        feedback.setStatus("pending");
        feedback.setCreateTime(LocalDateTime.now());
        save(feedback);
        log.info("反馈提交成功，id={}, userId={}", feedback.getId(), userId);
        return feedback;
    }

    @Override
    public Page<FeedbackVO> getMyFeedbackList(Integer userId, Integer page, Integer size) {
        Page<Feedback> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId)
                .orderByDesc(Feedback::getCreateTime);
        Page<Feedback> result = page(pageParam, wrapper);
        return convertToVOPage(result);
    }

    @Override
    public FeedbackVO getFeedbackDetail(Integer id, Integer userId, String role) {
        Feedback feedback = getById(id);
        if (feedback == null || feedback.getDeleted() == 1) {
            throw new BusinessException(404, "反馈不存在");
        }
        // 村民只能看自己的
        if (!"VILLAGE_ADMIN".equals(role) && !feedback.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看");
        }
        return convertToVO(feedback);
    }

    @Override
    public Page<FeedbackVO> getAdminFeedbackList(Integer tenantId, String status, Integer page, Integer size) {
        Page<Feedback> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getTenantId, tenantId)
                .eq(status != null && !status.isEmpty(), Feedback::getStatus, status)
                .orderByDesc(Feedback::getCreateTime);
        Page<Feedback> result = page(pageParam, wrapper);
        return convertToVOPage(result);
    }

    @Override
    @Transactional
    public void replyFeedback(FeedbackReplyDto dto, Integer adminUserId) {
        Feedback feedback = getById(dto.getId());
        if (feedback == null || feedback.getDeleted() == 1) {
            throw new BusinessException(404, "反馈不存在");
        }
        feedback.setReply(dto.getReply());
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setRepliedBy(adminUserId);
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            feedback.setStatus(dto.getStatus());
        } else {
            // 默认改为已处理
            feedback.setStatus("resolved");
        }
        updateById(feedback);
        log.info("反馈回复成功，id={}, adminUserId={}", dto.getId(), adminUserId);
    }

    @Override
    @Transactional
    public void deleteFeedback(Integer id, Integer adminUserId) {
        Feedback feedback = getById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈不存在");
        }
        removeById(id);
        log.info("反馈删除成功，id={}, adminUserId={}", id, adminUserId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将 Feedback 实体转换为 FeedbackVO
     * 会查询用户表获取提交人姓名/头像，以及回复人姓名
     */
    private FeedbackVO convertToVO(Feedback feedback) {
        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);

        // 提交用户信息
        User user = userMapper.selectById(feedback.getUserId());
        if (user != null) {
            vo.setUserName(user.getRealName());
            vo.setUserAvatar(user.getAvatar());
        }

        // 回复人信息
        if (feedback.getRepliedBy() != null) {
            User admin = userMapper.selectById(feedback.getRepliedBy());
            vo.setReplyByName(admin != null ? admin.getRealName() : "管理员");
        }
        return vo;
    }

    /**
     * 将分页的 Feedback 转换为分页的 FeedbackVO
     */
    private Page<FeedbackVO> convertToVOPage(Page<Feedback> page) {
        Page<FeedbackVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            List<FeedbackVO> voList = page.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            voPage.setRecords(voList);
        }
        return voPage;
    }
}