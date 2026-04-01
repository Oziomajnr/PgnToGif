package com.chunkymonkey.pgntogifconverter.review

import com.chunkymonkey.pgntogifconverter.preference.PreferenceService

/**
 * Persists engagement counters and cooldown for in-app review prompts.
 */
class InAppReviewPromptController(
    private val preferenceService: PreferenceService,
) {

    fun recordValuableAction() {
        val c = preferenceService.getInt(KEY_VALUABLE_ACTIONS, 0)
        if (c < Int.MAX_VALUE) {
            preferenceService.saveData(KEY_VALUABLE_ACTIONS, c + 1)
        }
    }

    fun shouldOfferReview(nowEpochMs: Long): Boolean {
        val state = InAppReviewPromptState(
            valuableActionCount = preferenceService.getInt(KEY_VALUABLE_ACTIONS, 0),
            lastPromptEpochMs = preferenceService.getLong(KEY_LAST_PROMPT_MS, 0L),
            reviewPromptAttemptCount = preferenceService.getInt(KEY_PROMPT_ATTEMPTS, 0),
        )
        return InAppReviewPromptRules.shouldOffer(state, nowEpochMs)
    }

    fun markReviewFlowLaunched(nowEpochMs: Long) {
        preferenceService.saveData(KEY_LAST_PROMPT_MS, nowEpochMs)
        val attempts = preferenceService.getInt(KEY_PROMPT_ATTEMPTS, 0)
        if (attempts < Int.MAX_VALUE) {
            preferenceService.saveData(KEY_PROMPT_ATTEMPTS, attempts + 1)
        }
    }

    companion object {
        internal const val KEY_VALUABLE_ACTIONS = "in_app_review_valuable_actions"
        internal const val KEY_LAST_PROMPT_MS = "in_app_review_last_prompt_ms"
        internal const val KEY_PROMPT_ATTEMPTS = "in_app_review_prompt_attempts"
    }
}
