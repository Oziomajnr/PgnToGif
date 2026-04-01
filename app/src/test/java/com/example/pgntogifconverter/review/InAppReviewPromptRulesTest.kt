package com.example.pgntogifconverter.review

import com.chunkymonkey.pgntogifconverter.review.InAppReviewPromptRules
import com.chunkymonkey.pgntogifconverter.review.InAppReviewPromptState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InAppReviewPromptRulesTest {

    private val now = 1_000_000_000_000L

    @Test
    fun shouldOffer_insufficientActions_returnsFalse() {
        val state = InAppReviewPromptState(valuableActionCount = 2, lastPromptEpochMs = 0L, reviewPromptAttemptCount = 0)
        assertFalse(InAppReviewPromptRules.shouldOffer(state, now))
    }

    @Test
    fun shouldOffer_minActionsNeverPrompted_returnsTrue() {
        val state = InAppReviewPromptState(
            valuableActionCount = InAppReviewPromptRules.MIN_VALUABLE_ACTIONS,
            lastPromptEpochMs = 0L,
            reviewPromptAttemptCount = 0,
        )
        assertTrue(InAppReviewPromptRules.shouldOffer(state, now))
    }

    @Test
    fun shouldOffer_maxAttemptsReached_returnsFalse() {
        val state = InAppReviewPromptState(
            valuableActionCount = 10,
            lastPromptEpochMs = 0L,
            reviewPromptAttemptCount = InAppReviewPromptRules.MAX_PROMPT_ATTEMPTS,
        )
        assertFalse(InAppReviewPromptRules.shouldOffer(state, now))
    }

    @Test
    fun shouldOffer_withinCooldown_returnsFalse() {
        val last = now - InAppReviewPromptRules.COOLDOWN_MS + 1
        val state = InAppReviewPromptState(
            valuableActionCount = 5,
            lastPromptEpochMs = last,
            reviewPromptAttemptCount = 1,
        )
        assertFalse(InAppReviewPromptRules.shouldOffer(state, now))
    }

    @Test
    fun shouldOffer_afterCooldown_returnsTrue() {
        val last = now - InAppReviewPromptRules.COOLDOWN_MS
        val state = InAppReviewPromptState(
            valuableActionCount = 5,
            lastPromptEpochMs = last,
            reviewPromptAttemptCount = 1,
        )
        assertTrue(InAppReviewPromptRules.shouldOffer(state, now))
    }
}
