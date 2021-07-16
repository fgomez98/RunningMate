package com.itba.runningMate.achievements.achievement

import androidx.recyclerview.widget.RecyclerView
import com.itba.runningMate.domain.Achievements

class AchievementViewHolder(private val achievementView: AchievementElementView) :
    RecyclerView.ViewHolder(achievementView) {


    fun bind(achievement: Achievements, achieved: Boolean) {
        achievementView.bind(achievement, achieved)
    }

}