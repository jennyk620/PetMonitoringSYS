package com.example.hpilitev3.domain.pagin_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.hpilitev3.data.db.Sensor
import com.example.hpilitev3.data.db.SensorDao

/**
 * Created by Jaehyeon on 2022/10/10.
 */
class SensorPagingSource(
    private val dao: SensorDao
): PagingSource<Int, Sensor>() {

    override fun getRefreshKey(state: PagingState<Int, Sensor>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Sensor> {

        return try {
            val page = params.key?: 0
            val results = dao.getSensors()
            val nextPage = if(results.count() == PAGING_SIZE) page + 1 else null

            LoadResult.Page(data = results, nextKey = nextPage, prevKey = null)

        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }

    companion object {
        const val PAGING_SIZE = 1000
    }
}