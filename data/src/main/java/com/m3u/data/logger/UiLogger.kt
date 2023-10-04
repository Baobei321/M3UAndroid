package com.m3u.data.logger

import com.m3u.core.architecture.Logger
import com.m3u.data.service.UiService
import javax.inject.Inject

/**
 * A collector of banner service.
 * Its messages will be deliver to users just like a global snack bar.
 * @see UiService
 */
class UiLogger @Inject constructor(
    private val uiService: UiService
) : Logger {
    override fun log(text: String) {
        uiService.snack(text)
    }

    override fun log(throwable: Throwable) {
        throwable.message?.let(::log)
    }
}