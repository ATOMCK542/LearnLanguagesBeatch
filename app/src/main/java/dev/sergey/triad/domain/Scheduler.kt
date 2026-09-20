package dev.sergey.triad.domain

interface Scheduler {
    fun preview(item: ReviewItem, now: Long): Map<Rating, ReviewItem>
    fun review(item: ReviewItem, rating: Rating, now: Long): ReviewItem
    fun newItem(profileId: Long, conceptId: String, targetLang: AppLanguage, now: Long): ReviewItem
}
