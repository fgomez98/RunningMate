package com.itba.runningMate.repository.run

import com.itba.runningMate.domain.Run
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface RunRepository {

    fun getRun(): Flowable<List<Run>>

    fun getRunLazy(): Flowable<List<Run>>

    fun getRun(uid: Long): Single<Run>

    fun getRunMetrics(uid: Long): Single<Run>

    fun insertRun(run: Run): Single<Long>

    fun getTotalDistance(): Single<Double>

    suspend fun getMaxTime(): Long?

    suspend fun getMaxKcal(): Double?

    suspend fun getMaxSpeed(): Double?

    fun deleteRun(run: Run): Completable

    fun deleteRun(runId: Long): Completable

    fun updateTitle(runId: Long, title: String): Completable

}