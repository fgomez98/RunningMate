package com.itba.runningMate.mainpage.fragments.feed

import com.itba.runningMate.domain.Achievements
import com.itba.runningMate.domain.Level
import com.itba.runningMate.domain.Run

interface FeedView {

    fun showRecentActivity(recentRuns: List<Run>)

    fun showAchievements(achievements: List<Achievements>)

    fun showCurrentLevel(level: Level, distance: Double)

    fun launchLevelsActivity()

    fun launchAchievementsActivity()

    fun launchRunDetailActivity(runId: Long)

    fun launchPastRunsActivity()

    fun startLevelShimmerAnimation()

    fun stopLevelShimmerAnimation()

    fun startRecentActivityShimmerAnimation()

    fun stopRecentActivityShimmerAnimation()

    fun startAchievementsShimmerAnimation()

    fun stopAchievementsShimmerAnimation()

}