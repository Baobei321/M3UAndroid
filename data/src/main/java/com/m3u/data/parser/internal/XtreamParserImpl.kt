package com.m3u.data.parser.internal

import com.m3u.core.architecture.dispatcher.Dispatcher
import com.m3u.core.architecture.dispatcher.M3uDispatchers.IO
import com.m3u.core.architecture.logger.Logger
import com.m3u.core.architecture.logger.execute
import com.m3u.data.api.xtream.XtreamCategory
import com.m3u.data.api.xtream.XtreamInfo
import com.m3u.data.api.xtream.XtreamLive
import com.m3u.data.api.xtream.XtreamSerial
import com.m3u.data.api.xtream.XtreamVod
import com.m3u.data.api.xtream.XtreamVodInfo
import com.m3u.data.database.model.DataSource
import com.m3u.data.parser.XtreamInput
import com.m3u.data.parser.XtreamOutput
import com.m3u.data.parser.XtreamParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

internal class XtreamParserImpl @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
    private val logger: Logger
) : XtreamParser {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun execute(input: XtreamInput): XtreamOutput {
        val (address, username, password, type) = input
        val requiredLives = type == null || type == DataSource.Xtream.TYPE_LIVE
        val requiredVods = type == null || type == DataSource.Xtream.TYPE_VOD
        val requiredSeries = type == null || type == DataSource.Xtream.TYPE_SERIES
        val infoUrl = XtreamParser.createInfoUrl(address, username, password)
        val liveStreamsUrl = XtreamParser.createActionUrl(
            address,
            username,
            password,
            XtreamParser.Action.GET_LIVE_STREAMS
        )
        val vodStreamsUrl = XtreamParser.createActionUrl(
            address,
            username,
            password,
            XtreamParser.Action.GET_VOD_STREAMS
        )
        val seriesStreamsUrl = XtreamParser.createActionUrl(
            address,
            username,
            password,
            XtreamParser.Action.GET_SERIES_STREAMS
        )
        val liveCategoriesUrl = XtreamParser.createActionUrl(
            address,
            username,
            password,
            XtreamParser.Action.GET_LIVE_CATEGORIES
        )
        val vodCategoriesUrl = XtreamParser.createActionUrl(
            address,
            username,
            password,
            XtreamParser.Action.GET_VOD_CATEGORIES
        )
        val serialCategoriesUrl = XtreamParser.createActionUrl(
            address,
            username,
            password,
            XtreamParser.Action.GET_SERIES_CATEGORIES
        )
        val info: XtreamInfo = newCall(infoUrl) ?: return XtreamOutput()
        val allowedOutputFormats = info.userInfo.allowedOutputFormats

        val lives: List<XtreamLive> = if (requiredLives) newCall(liveStreamsUrl) ?: emptyList() else emptyList()
        val vods: List<XtreamVod> = if (requiredVods) newCall(vodStreamsUrl) ?: emptyList() else emptyList()
        val series: List<XtreamSerial> = if (requiredSeries) newCall(seriesStreamsUrl) ?: emptyList() else emptyList()

        val liveCategories: List<XtreamCategory> = if (requiredLives) newCall(liveCategoriesUrl) ?: emptyList() else emptyList()
        val vodCategories: List<XtreamCategory> = if (requiredVods) newCall(vodCategoriesUrl) ?: emptyList() else emptyList()
        val serialCategories: List<XtreamCategory> = if (requiredSeries) newCall(serialCategoriesUrl) ?: emptyList() else emptyList()

        return XtreamOutput(
            lives = lives,
            vods = vods,
            series = series,
            liveCategories = liveCategories,
            vodCategories = vodCategories,
            serialCategories = serialCategories,
            allowedOutputFormats = allowedOutputFormats
        )
    }

    override suspend fun getVodInfo(
        input: XtreamInput,
        vodId: Int
    ): XtreamVodInfo? {
        val (address, username, password, type) = input
        check(type == DataSource.Xtream.TYPE_VOD) { "xtream input type must be `vod`" }
        return newCall(
            XtreamParser.createActionUrl(
                address,
                username,
                password,
                XtreamParser.Action.GET_VOD_INFO,
                XtreamParser.GET_VOD_INFO_PARAM_VOD_ID to vodId
            )
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend inline fun <reified T> newCall(url: String): T? = withContext(ioDispatcher) {
        logger.execute {
            okHttpClient.newCall(
                Request.Builder().url(url).build()
            )
                .execute()
                .takeIf { it.isSuccessful }
                ?.body
                ?.byteStream()
                ?.let { json.decodeFromStream(it) }
        }
    }
}