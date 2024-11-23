/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package im.vector.lib.core.utils.test.fakes

import im.vector.lib.core.utils.timer.Clock
import io.mockk.every
import io.mockk.mockk

class FakeClock : Clock by mockk() {
    fun givenEpoch(epoch: Long) {
        every { epochMillis() } returns epoch
    }
}
