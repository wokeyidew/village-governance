package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.common.utils.DesensitizationUtils;
import com.scau.village.module.points.dto.AdminUserPointsVO;
import com.scau.village.module.points.service.PointsService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 积分综合服务实现类
 * 封装积分模块中涉及多个子模块的综合业务逻辑
 *
 * @author system
 * @since 2026-07-18
 */
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserService userService;

    @Override
    public AdminUserPointsVO getUserPointsByAdmin(Integer userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserPointsVO(user);
    }

    @Override
    public AdminUserPointsVO getUserPointsByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userService.getOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserPointsVO(user);
    }

    /**
     * 构建用户积分信息VO
     *
     * @param user 用户实体
     * @return 用户积分信息VO
     */
    private AdminUserPointsVO buildUserPointsVO(User user) {
        AdminUserPointsVO vo = new AdminUserPointsVO();
        vo.setUserId(user.getId());
        vo.setRealName(user.getRealName());
        vo.setPhone(DesensitizationUtils.mobile(user.getPhone()));
        vo.setPoints(user.getPoints());
        vo.setVillageGroup(user.getVillageGroup());
        vo.setRole(user.getRole());
        return vo;
    }
}