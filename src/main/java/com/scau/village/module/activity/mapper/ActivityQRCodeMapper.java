package com.scau.village.module.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.activity.entity.ActivityQRCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 活动二维码 Mapper 接口
 *
 * @author system
 * @since 2026-07-19
 */
@Mapper
public interface ActivityQRCodeMapper extends BaseMapper<ActivityQRCode> {

    /**
     * 根据活动ID和类型查询有效的二维码记录
     *
     * @param activityId 活动ID
     * @param type       类型：signin/checkout
     * @return 二维码记录
     */
    @Select("SELECT * FROM activity_qrcode WHERE activity_id = #{activityId} AND qrcode_type = #{type} AND status = 1 ORDER BY id DESC LIMIT 1")
    ActivityQRCode selectValidByActivityAndType(@Param("activityId") Integer activityId,
                                                @Param("type") String type);

    /**
     * 根据固定码和类型查询有效的二维码记录
     *
     * @param fixedCode 固定码
     * @param type      类型：signin/checkout
     * @return 二维码记录
     */
    @Select("SELECT * FROM activity_qrcode WHERE fixed_code = #{fixedCode} AND qrcode_type = #{type} AND status = 1")
    ActivityQRCode selectByFixedCodeAndType(@Param("fixedCode") String fixedCode,
                                            @Param("type") String type);

    /**
     * 根据动态key查询二维码记录
     *
     * @param qrcodeKey 动态key
     * @return 二维码记录
     */
    @Select("SELECT * FROM activity_qrcode WHERE qrcode_key = #{qrcodeKey}")
    ActivityQRCode selectByQrcodeKey(@Param("qrcodeKey") String qrcodeKey);

    /**
     * 将指定活动ID和类型的二维码状态设为失效
     *
     * @param activityId 活动ID
     * @param type       类型
     */
    @Select("UPDATE activity_qrcode SET status = 0 WHERE activity_id = #{activityId} AND qrcode_type = #{type}")
    void invalidateByActivityAndType(@Param("activityId") Integer activityId,
                                     @Param("type") String type);
}