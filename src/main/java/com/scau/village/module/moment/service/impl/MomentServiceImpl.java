package com.scau.village.module.moment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.moment.dto.CommentVO;
import com.scau.village.module.moment.dto.MomentDto;
import com.scau.village.module.moment.dto.MomentVO;
import com.scau.village.module.moment.entity.Moment;
import com.scau.village.module.moment.entity.MomentComment;
import com.scau.village.module.moment.entity.MomentLike;
import com.scau.village.module.moment.mapper.MomentCommentMapper;
import com.scau.village.module.moment.mapper.MomentLikeMapper;
import com.scau.village.module.moment.mapper.MomentMapper;
import com.scau.village.module.moment.service.MomentService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 动态服务实现类
 *
 * @author system
 * @since 2026-07-17
 */
@Service
@RequiredArgsConstructor
public class MomentServiceImpl extends ServiceImpl<MomentMapper, Moment> implements MomentService {

    private final MomentLikeMapper momentLikeMapper;
    private final MomentCommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Moment publish(Integer userId, Integer tenantId, MomentDto dto) {
        if (tenantId == null) {
            tenantId = 1;
        }

        Moment moment = new Moment();
        moment.setUserId(userId);
        moment.setContent(dto.getContent());
        moment.setImages(dto.getImages());
        moment.setLikesCount(0);
        moment.setCommentsCount(0);
        moment.setStatus(1);
        moment.setTenantId(tenantId);
        moment.setCreateTime(LocalDateTime.now());
        save(moment);
        return moment;
    }

    @Override
    public Page<MomentVO> getMomentList(Integer tenantId, Integer userId, Integer page, Integer size) {
        if (tenantId == null) {
            tenantId = 1;
        }
        Page<MomentVO> pageParam = new Page<>(page, size);
        return baseMapper.selectMomentList(pageParam, tenantId, userId);
    }

    @Override
    public MomentVO getMomentDetail(Long momentId, Integer userId) {
        Moment moment = getById(momentId);
        if (moment == null || moment.getStatus() != 1) {
            throw new BusinessException("动态不存在");
        }

        User user = userMapper.selectById(moment.getUserId());

        MomentVO vo = new MomentVO();
        vo.setId(moment.getId());
        vo.setUserId(moment.getUserId());
        vo.setUserName(user != null ? user.getRealName() : "已注销");
        vo.setUserAvatar(user != null ? user.getAvatar() : null);
        vo.setContent(moment.getContent());
        vo.setImages(moment.getImages());
        vo.setLikesCount(moment.getLikesCount());
        vo.setCommentsCount(moment.getCommentsCount());
        vo.setCreateTime(moment.getCreateTime());

        LambdaQueryWrapper<MomentLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(MomentLike::getMomentId, momentId)
                   .eq(MomentLike::getUserId, userId);
        vo.setIsLiked(momentLikeMapper.selectCount(likeWrapper) > 0);

        vo.setComments(getComments(momentId));

        return vo;
    }

    @Override
    @Transactional
    public boolean toggleLike(Long momentId, Integer userId, Integer tenantId) {
        if (tenantId == null) {
            tenantId = 1;
        }

        LambdaQueryWrapper<MomentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MomentLike::getMomentId, momentId)
               .eq(MomentLike::getUserId, userId);

        MomentLike existing = momentLikeMapper.selectOne(wrapper);
        if (existing != null) {
            momentLikeMapper.deleteById(existing.getId());
            Moment moment = getById(momentId);
            if (moment != null && moment.getLikesCount() > 0) {
                moment.setLikesCount(moment.getLikesCount() - 1);
                updateById(moment);
            }
            return false;
        } else {
            MomentLike like = new MomentLike();
            like.setMomentId(momentId);
            like.setUserId(userId);
            like.setTenantId(tenantId);
            like.setCreateTime(LocalDateTime.now());
            momentLikeMapper.insert(like);
            Moment moment = getById(momentId);
            if (moment != null) {
                moment.setLikesCount(moment.getLikesCount() + 1);
                updateById(moment);
            }
            return true;
        }
    }

    @Override
    @Transactional
    public MomentComment comment(Long momentId, Integer userId, String content, Integer tenantId) {
        if (tenantId == null) {
            tenantId = 1;
        }

        Moment moment = getById(momentId);
        if (moment == null || moment.getStatus() != 1) {
            throw new BusinessException("动态不存在");
        }

        MomentComment comment = new MomentComment();
        comment.setMomentId(momentId);
        comment.setUserId(userId);
        comment.setParentId(0L);
        comment.setContent(content);
        comment.setTenantId(tenantId);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        moment.setCommentsCount(moment.getCommentsCount() + 1);
        updateById(moment);

        return comment;
    }

    @Override
    @Transactional
    public Moment updateMoment(Long momentId, Integer userId, MomentDto dto) {
        Moment moment = getById(momentId);
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }
        if (moment.getStatus() != 1) {
            throw new BusinessException("动态已被删除，无法修改");
        }
        // 只能修改自己的动态
        if (!moment.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此动态");
        }
        // 更新内容（可修改文字和图片）
        if (dto.getContent() != null) {
            moment.setContent(dto.getContent());
        }
        if (dto.getImages() != null) {
            moment.setImages(dto.getImages());
        }
        moment.setUpdateTime(LocalDateTime.now());
        updateById(moment);
        return moment;
    }

    @Override
    @Transactional
    public void deleteMoment(Long momentId, Integer userId, String role) {
        Moment moment = getById(momentId);
        if (moment == null) {
            throw new BusinessException("动态不存在");
        }
        // 作者本人 或 管理员（角色为 VILLAGE_ADMIN 或 SUPER_ADMIN）可以删除
        boolean isAuthor = moment.getUserId().equals(userId);
        boolean isAdmin = "VILLAGE_ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
        if (!isAuthor && !isAdmin) {
            throw new BusinessException("无权删除此动态");
        }
        moment.setStatus(0);
        updateById(moment);
    }

    @Override
    public List<CommentVO> getComments(Long momentId) {
        List<MomentComment> comments = commentMapper.selectByMomentId(momentId);
        return comments.stream().map(c -> {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setContent(c.getContent());
            vo.setCreateTime(c.getCreateTime());
            User user = userMapper.selectById(c.getUserId());
            vo.setUserName(user != null ? user.getRealName() : "已注销");
            vo.setUserAvatar(user != null ? user.getAvatar() : null);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MomentComment updateComment(Long commentId, Integer userId, String content) {
        MomentComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        // 只能修改自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此评论");
        }
        comment.setContent(content);
        commentMapper.updateById(comment);
        return comment;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Integer userId, String role) {
        MomentComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        // 作者本人 或 管理员可以删除
        boolean isAuthor = comment.getUserId().equals(userId);
        boolean isAdmin = "VILLAGE_ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
        if (!isAuthor && !isAdmin) {
            throw new BusinessException("无权删除此评论");
        }
        // 删除评论后，需要减少动态的评论数
        Moment moment = getById(comment.getMomentId());
        if (moment != null && moment.getCommentsCount() > 0) {
            moment.setCommentsCount(moment.getCommentsCount() - 1);
            updateById(moment);
        }
        commentMapper.deleteById(commentId);
    }
}