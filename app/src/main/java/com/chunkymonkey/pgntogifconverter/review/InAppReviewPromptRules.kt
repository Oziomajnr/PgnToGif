package com.chunkymonkey.pgntogifconverter.review

/**
 * Pure rules for when to call the Play In-App Review API.
 * Google may still choose not to show the dialog (quota, user history).
 */
data class InAppReviewPromptState(
    val valuableActionCount: Int = 0,
    val lastPromptEpochMs: Long = 0L,
    val reviewPromptAttemptCount: Int = 0,
)

object InAppReviewPromptRules {

    const val MIN_VALUABLE_ACTIONS: Int = 3
    const val COOLDOWN_MS: Long = 30L * 24 * 60 * 60 * 1000
    const val MAX_PROMPT_ATTEMPTS: Int = 5

    fun shouldOffer(state: InAppReviewPromptState, nowEpochMs: Long): Boolean {
        if (state.valuableActionCount < MIN_VALUABLE_ACTIONS) return false
        if (state.reviewPromptAttemptCount >= MAX_PROMPT_ATTEMPTS) return false
        if (state.lastPromptEpochMs == 0L) return true
        return nowEpochMs - state.lastPromptEpochMs >= COOLDOWN_MS
    }
}
