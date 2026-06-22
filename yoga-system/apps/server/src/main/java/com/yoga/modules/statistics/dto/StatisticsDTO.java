package com.yoga.modules.statistics.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计数据聚合 DTO
 *  - overview(): 首页看板概览
 *  - range(days): 多维度统计区间
 */
@Data
public class StatisticsDTO {

    // ========== overview() 字段 ==========
    /** 今日营收 */
    private BigDecimal todayRevenue;
    /** 月累计营收 */
    private BigDecimal monthRevenue;
    /** 今日预约数 */
    private Long todayBookings;
    /** 今日签到数 */
    private Long todayCheckIns;
    /** 总会员数 */
    private Long totalMembers;
    /** 活跃会员数 */
    private Long activeMembers;
    /** 近 7 天出勤率 (百分比) */
    private Double attendanceRate;

    // ========== range(days) 字段 ==========
    /** 区间内总营收 */
    private BigDecimal revenue;
    /** 营收环比变化 (百分比, 可选) */
    private Double revenueDelta;
    /** 区间内总预约数 */
    private Long bookings;
    /** 签到率 (百分比) */
    private Double checkInRate;
    /** 区间内新增会员数 */
    private Long newMembers;
    /** 区间内课程数 */
    private Long totalCourses;
    /** 订单类型分布 [{name,value}, ...] */
    private List<Map<String, Object>> orderTypeDist;

    // ========== 排名字段 (overview + range 都有) ==========
    /** 课程上座率 Top N */
    private List<CourseRank> topCourses;
    /** 教练课时 Top N */
    private List<CoachRank> topCoaches;

    /** 课程上座率排名 */
    @Data
    public static class CourseRank {
        private Long courseTypeId;
        private String courseTypeName;
        private Double rate;
    }

    /** 教练课时排名 */
    @Data
    public static class CoachRank {
        private Long coachId;
        private String coachName;
        private Long hours;
    }
}
