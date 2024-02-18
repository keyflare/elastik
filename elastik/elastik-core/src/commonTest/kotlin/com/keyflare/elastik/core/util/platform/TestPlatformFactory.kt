package com.keyflare.elastik.core.util.platform

import com.keyflare.elastik.core.context.ElastikPlatform
import kotlinx.coroutines.CoroutineScope

internal fun createTestPlatform(scope: CoroutineScope) = ElastikPlatform(
    lifecycleEventsSource = TestLifecycleEventsSource(),
    backEventsSource = TestBackEventsSource(scope),
)
