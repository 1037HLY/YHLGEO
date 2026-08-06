package com.geosurvey.toolbox.utils

import com.geosurvey.toolbox.data.database.AttitudeEntity
import kotlin.math.*

object GeologicalAnalysisUtils {

    /**
     * 走向分组（玫瑰花图使用）
     * 将走向按10度一组分组
     */
    fun groupStrikeByDirection(strikes: List<Float>, interval: Int = 10): Map<Int, Int> {
        val groups = mutableMapOf<Int, Int>()
        for (strike in strikes) {
            // 将走向归一化到0-180度（走向是线状，无方向性）
            var normalized = strike % 180
            if (normalized < 0) normalized += 180
            val group = ((normalized / interval).toInt() * interval)
            groups[group] = (groups[group] ?: 0) + 1
        }
        return groups
    }

    /**
     * 计算走向平均值
     */
    fun calculateMeanStrike(strikes: List<Float>): Float {
        if (strikes.isEmpty()) return 0f
        // 走向是线状数据，需要特殊处理
        var sumSin = 0.0
        var sumCos = 0.0
        for (strike in strikes) {
            val rad = Math.toRadians((strike % 180).toDouble())
            sumSin += sin(2 * rad)
            sumCos += cos(2 * rad)
        }
        val meanRad = 0.5 * atan2(sumSin, sumCos)
        var mean = Math.toDegrees(meanRad).toFloat()
        if (mean < 0) mean += 180
        return mean
    }

    /**
     * 计算倾角平均值
     */
    fun calculateMeanDip(dips: List<Float>): Float {
        if (dips.isEmpty()) return 0f
        return dips.average().toFloat()
    }

    /**
     * 统计走向分布（用于玫瑰花图）
     */
    data class RoseData(
        val angle: Float,
        val count: Int,
        val percentage: Float
    )

    fun getRoseData(strikes: List<Float>, interval: Int = 10): List<RoseData> {
        val groups = groupStrikeByDirection(strikes, interval)
        val total = strikes.size
        return groups.map { (angle, count) ->
            RoseData(
                angle = angle.toFloat(),
                count = count,
                percentage = (count.toFloat() / total) * 100
            )
        }.sortedBy { it.angle }
    }

    /**
     * 计算产状数据的统计信息
     */
    data class AttitudeStatistics(
        val totalCount: Int,
        val meanStrike: Float,
        val meanDip: Float,
        val maxStrike: Float,
        val minStrike: Float,
        val maxDip: Float,
        val minDip: Float
    )

    fun calculateStatistics(attitudes: List<AttitudeEntity>): AttitudeStatistics {
        if (attitudes.isEmpty()) {
            return AttitudeStatistics(0, 0f, 0f, 0f, 0f, 0f, 0f)
        }
        val strikes = attitudes.map { it.strike }
        val dips = attitudes.map { it.dip }
        return AttitudeStatistics(
            totalCount = attitudes.size,
            meanStrike = calculateMeanStrike(strikes),
            meanDip = calculateMeanDip(dips),
            maxStrike = strikes.maxOrNull() ?: 0f,
            minStrike = strikes.minOrNull() ?: 0f,
            maxDip = dips.maxOrNull() ?: 0f,
            minDip = dips.minOrNull() ?: 0f
        )
    }
}
