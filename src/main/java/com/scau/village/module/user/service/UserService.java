package com.scau.village.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.user.dto.RegisterDto;
import com.scau.village.module.user.dto.UpdateProfileDto;
import com.scau.village.module.user.entity.User;

/**
 * 用户服务接口
 * @author system
 * @since 2026-07-17
 */
public interface UserService extends IService<User> {

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户对象，不存在返回null
     */
    User findByPhone(String phone);

    /**
     * 保存用户（密码已加密）
     * @param user 用户对象
     */
    void saveWithEncryptedPassword(User user);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param rawPassword 明文新密码
     */
    void updatePassword(Long userId, String rawPassword);

    /**
     * 用户注册（完整信息）
     * @param dto 注册数据传输对象
     */
    void register(RegisterDto dto);

    /**
     * 更新当前用户个人资料（头像、真实姓名、手机号、密码）
     * @param userId 当前用户ID
     * @param dto 更新数据
     */
    void updateProfile(Integer userId, UpdateProfileDto dto);

    // ==================== 微信登录 & 信息更新（新增） ====================

    /**
     * 根据微信 openid 查询用户
     * @param openid 微信 openid
     * @return 用户对象，不存在返回 null
     */
    User getByOpenid(String openid);

    /**
     * 村民更新自己的个人信息（手机号、真实姓名、身份证号）
     * 包含格式校验（手机号、身份证、姓名长度）和手机号唯一性校验
     * @param userId    当前用户ID
     * @param phone     新手机号（必须符合格式）
     * @param realName  新真实姓名（不超过20字符）
     * @param idCard    新身份证号（可选，格式校验）
     */
    void updateUserInfo(Integer userId, String phone, String realName, String idCard);

    /**
     * 管理员更新任意用户的个人信息
     * 仅做基本格式校验和手机号唯一性校验
     * @param userId    目标用户ID
     * @param phone     新手机号
     * @param realName  新真实姓名
     * @param idCard    新身份证号
     */
    void adminUpdateUserInfo(Integer userId, String phone, String realName, String idCard);
}