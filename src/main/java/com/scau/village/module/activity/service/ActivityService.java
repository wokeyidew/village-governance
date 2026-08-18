package com.scau.village.module.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.activity.entity.Activity;
import com.scau.village.module.activity.vo.QRCodeVO;

/**
 * 活动服务接口
 * @author system
 * @since 2026-07-17
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 生成活动二维码
     * 支持签到/签退的固定码或动态码（10秒刷新）
     *
     * @param activityId 活动ID
     * @param type       类型：signin-签到，checkout-签退
     * @param isDynamic  是否动态码（true-动态，false-固定）
     * @return 二维码VO（包含Base64图片和相关信息）
     */
    QRCodeVO generateQRCode(Integer activityId, String type, Boolean isDynamic);

    /**
     * 村民扫码签到（自助签到）
     * 解析二维码内容，验证有效性和时间窗口，完成签到
     *
     * @param scanData 扫码得到的JSON字符串
     * @param userId   当前用户ID
     * @return 签到结果信息
     */
    String scanSignIn(String scanData, Integer userId);

    /**
     * 村民扫码签退（自助签退）
     * 解析二维码内容，验证有效性和时间窗口，完成签退并发放积分
     *
     * @param scanData 扫码得到的JSON字符串
     * @param userId   当前用户ID
     * @return 签退结果信息（含参与时长）
     */
    String scanCheckout(String scanData, Integer userId);
}