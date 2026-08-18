package com.scau.village.module.moment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.module.moment.dto.CommentVO;
import com.scau.village.module.moment.dto.MomentDto;
import com.scau.village.module.moment.dto.MomentVO;
import com.scau.village.module.moment.entity.Moment;
import com.scau.village.module.moment.entity.MomentComment;

import java.util.List;

/**
 * 动态服务接口
 *
 * @author system
 * @since 2026-07-17
 */
public interface MomentService {

    /**
     * 发布动态
     *
     * @param userId   发布用户ID
     * @param tenantId 租户ID
     * @param dto      动态内容
     * @return 发布的动态对象
     */
    Moment publish(Integer userId, Integer tenantId, MomentDto dto);

    /**
     * 分页获取动态列表
     *
     * @param tenantId 租户ID
     * @param userId   当前用户ID（用于判断是否已点赞）
     * @param page     页码
     * @param size     每页大小
     * @return 分页结果
     */
    Page<MomentVO> getMomentList(Integer tenantId, Integer userId, Integer page, Integer size);

    /**
     * 获取动态详情（含评论列表）
     *
     * @param momentId 动态ID
     * @param userId   当前用户ID（用于判断是否已点赞）
     * @return 动态详情VO
     */
    MomentVO getMomentDetail(Long momentId, Integer userId);

    /**
     * 点赞/取消点赞（切换状态）
     *
     * @param momentId 动态ID
     * @param userId   当前用户ID
     * @param tenantId 租户ID
     * @return true-点赞，false-取消点赞
     */
    boolean toggleLike(Long momentId, Integer userId, Integer tenantId);

    /**
     * 发表评论
     *
     * @param momentId 动态ID
     * @param userId   评论用户ID
     * @param content  评论内容
     * @param tenantId 租户ID
     * @return 评论对象
     */
    MomentComment comment(Long momentId, Integer userId, String content, Integer tenantId);

    /**
     * 修改动态（仅作者本人可修改）
     *
     * @param momentId 动态ID
     * @param userId   当前用户ID（用于权限校验）
     * @param dto      修改后的动态内容
     * @return 修改后的动态对象
     */
    Moment updateMoment(Long momentId, Integer userId, MomentDto dto);

    /**
     * 删除动态（作者本人或管理员可删除）
     *
     * @param momentId 动态ID
     * @param userId   当前用户ID
     * @param role     当前用户角色（用于判断是否有管理员权限）
     */
    void deleteMoment(Long momentId, Integer userId, String role);

    /**
     * 获取动态的所有评论列表
     *
     * @param momentId 动态ID
     * @return 评论VO列表
     */
    List<CommentVO> getComments(Long momentId);

    /**
     * 修改评论（仅评论作者本人可修改）
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID（用于权限校验）
     * @param content   修改后的评论内容
     * @return 修改后的评论对象
     */
    MomentComment updateComment(Long commentId, Integer userId, String content);

    /**
     * 删除评论（作者本人或管理员可删除）
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @param role      当前用户角色（用于判断是否有管理员权限）
     */
    void deleteComment(Long commentId, Integer userId, String role);
}