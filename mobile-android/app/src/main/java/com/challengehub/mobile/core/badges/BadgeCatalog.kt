package com.challengehub.mobile.core.badges

import com.challengehub.mobile.core.network.BadgeDto

data class BadgeCatalogItem(
    val code: String,
    val emoji: String,
    val title: String,
    val description: String,
)

val BadgeCatalog = listOf(
    BadgeCatalogItem("first_video", "🎯", "Перший челендж", "Опублікувати перше відео-звіт."),
    BadgeCatalogItem("weekly_warrior", "⚡", "Воїн тижня", "Завершити 3 челенджі."),
    BadgeCatalogItem("social_butterfly", "🦋", "Соціальний метелик", "Отримати 10 лайків на свої відео."),
    BadgeCatalogItem("creative_mind", "🎨", "Творчий розум", "Створити 5 власних челенджів."),
    BadgeCatalogItem("xp_hunter", "💎", "Мисливець за XP", "Набрати 1000 XP."),
    BadgeCatalogItem("premium_star", "⭐", "Premium-зірка", "Активувати ChallengeHub Premium."),
)

fun catalogBadge(code: String): BadgeCatalogItem = BadgeCatalog.firstOrNull { it.code == code }
    ?: BadgeCatalogItem(code, "🏆", code, "Досягнення ChallengeHub.")

fun earnedBadgeItems(apiBadges: List<BadgeDto>): List<BadgeCatalogItem> {
    val codes = apiBadges.map { it.badgeCode }.toSet()
    return BadgeCatalog.filter { it.code in codes }
}
