package com.silas.omaster.data

import com.silas.omaster.R
import com.silas.omaster.model.ColorCard
import com.silas.omaster.model.ColorInfo
import com.silas.omaster.model.ColorRole

object ColorCardLibrary {

    val allCards = listOf(
        ColorCard(
            id = "urban_warm",
            colors = listOf(
                ColorInfo("D4A574", R.string.color_brick_red, ColorRole.PRIMARY),
                ColorInfo("8B4513", R.string.color_dark_brown, ColorRole.PRIMARY),
                ColorInfo("FFE4C4", R.string.color_bisque, ColorRole.ACCENT),
                ColorInfo("4A4A4A", R.string.color_dark_gray, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_urban_warm,
            descriptionResId = R.string.colorwalk_desc_urban_warm,
            tipsResId = R.string.colorwalk_tips_urban_warm,
            challengeResId = R.string.colorwalk_challenge_urban_warm,
            sceneTags = listOf(
                R.string.colorwalk_scene_architecture,
                R.string.colorwalk_scene_street,
                R.string.colorwalk_scene_light
            )
        ),
        ColorCard(
            id = "seaside_fresh",
            colors = listOf(
                ColorInfo("87CEEB", R.string.color_sky_blue, ColorRole.PRIMARY),
                ColorInfo("B0E0E6", R.string.color_powder_blue, ColorRole.PRIMARY),
                ColorInfo("FFFFFF", R.string.color_white, ColorRole.ACCENT),
                ColorInfo("F5DEB3", R.string.color_wheat, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_seaside_fresh,
            descriptionResId = R.string.colorwalk_desc_seaside_fresh,
            tipsResId = R.string.colorwalk_tips_seaside_fresh,
            challengeResId = R.string.colorwalk_challenge_seaside_fresh,
            sceneTags = listOf(
                R.string.colorwalk_scene_seaside,
                R.string.colorwalk_scene_sky,
                R.string.colorwalk_scene_minimal
            )
        ),
        ColorCard(
            id = "forest_green",
            colors = listOf(
                ColorInfo("228B22", R.string.color_forest_green, ColorRole.PRIMARY),
                ColorInfo("90EE90", R.string.color_light_green, ColorRole.PRIMARY),
                ColorInfo("8B4513", R.string.color_saddle_brown, ColorRole.ACCENT),
                ColorInfo("F5F5DC", R.string.color_beige, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_forest_green,
            descriptionResId = R.string.colorwalk_desc_forest_green,
            tipsResId = R.string.colorwalk_tips_forest_green,
            challengeResId = R.string.colorwalk_challenge_forest_green,
            sceneTags = listOf(
                R.string.colorwalk_scene_park,
                R.string.colorwalk_scene_plants,
                R.string.colorwalk_scene_nature
            )
        ),
        ColorCard(
            id = "industrial_cool",
            colors = listOf(
                ColorInfo("708090", R.string.color_slate_gray, ColorRole.PRIMARY),
                ColorInfo("4682B4", R.string.color_steel_blue, ColorRole.PRIMARY),
                ColorInfo("2F4F4F", R.string.color_dark_slate_gray, ColorRole.ACCENT),
                ColorInfo("C0C0C0", R.string.color_silver, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_industrial_cool,
            descriptionResId = R.string.colorwalk_desc_industrial_cool,
            tipsResId = R.string.colorwalk_tips_industrial_cool,
            challengeResId = R.string.colorwalk_challenge_industrial_cool,
            sceneTags = listOf(
                R.string.colorwalk_scene_industrial,
                R.string.colorwalk_scene_geometry,
                R.string.colorwalk_scene_bridge
            )
        ),
        ColorCard(
            id = "sunset_glow",
            colors = listOf(
                ColorInfo("FF7F50", R.string.color_coral, ColorRole.PRIMARY),
                ColorInfo("FFB6C1", R.string.color_light_pink, ColorRole.PRIMARY),
                ColorInfo("9370DB", R.string.color_medium_purple, ColorRole.ACCENT),
                ColorInfo("191970", R.string.color_midnight_blue, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_sunset_glow,
            descriptionResId = R.string.colorwalk_desc_sunset_glow,
            tipsResId = R.string.colorwalk_tips_sunset_glow,
            challengeResId = R.string.colorwalk_challenge_sunset_glow,
            sceneTags = listOf(
                R.string.colorwalk_scene_sunset,
                R.string.colorwalk_scene_skyline,
                R.string.colorwalk_scene_reflection
            )
        ),
        ColorCard(
            id = "neon_night",
            colors = listOf(
                ColorInfo("FF1493", R.string.color_deep_pink, ColorRole.PRIMARY),
                ColorInfo("00FFFF", R.string.color_cyan, ColorRole.PRIMARY),
                ColorInfo("FFD700", R.string.color_gold, ColorRole.ACCENT),
                ColorInfo("000080", R.string.color_navy, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_neon_night,
            descriptionResId = R.string.colorwalk_desc_neon_night,
            tipsResId = R.string.colorwalk_tips_neon_night,
            challengeResId = R.string.colorwalk_challenge_neon_night,
            sceneTags = listOf(
                R.string.colorwalk_scene_night,
                R.string.colorwalk_scene_neon,
                R.string.colorwalk_scene_city
            )
        ),
        ColorCard(
            id = "vintage_film",
            colors = listOf(
                ColorInfo("D2B48C", R.string.color_tan, ColorRole.PRIMARY),
                ColorInfo("A0522D", R.string.color_sienna, ColorRole.PRIMARY),
                ColorInfo("F0E68C", R.string.color_khaki, ColorRole.ACCENT),
                ColorInfo("556B2F", R.string.color_dark_olive_green, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_vintage_film,
            descriptionResId = R.string.colorwalk_desc_vintage_film,
            tipsResId = R.string.colorwalk_tips_vintage_film,
            challengeResId = R.string.colorwalk_challenge_vintage_film,
            sceneTags = listOf(
                R.string.colorwalk_scene_old_street,
                R.string.colorwalk_scene_cafe,
                R.string.colorwalk_scene_story
            )
        ),
        ColorCard(
            id = "minimalist_bw",
            colors = listOf(
                ColorInfo("000000", R.string.color_black, ColorRole.PRIMARY),
                ColorInfo("FFFFFF", R.string.color_white, ColorRole.PRIMARY),
                ColorInfo("808080", R.string.color_gray, ColorRole.ACCENT),
                ColorInfo("D3D3D3", R.string.color_light_gray, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_minimalist_bw,
            descriptionResId = R.string.colorwalk_desc_minimalist_bw,
            tipsResId = R.string.colorwalk_tips_minimalist_bw,
            challengeResId = R.string.colorwalk_challenge_minimalist_bw,
            sceneTags = listOf(
                R.string.colorwalk_scene_black_white,
                R.string.colorwalk_scene_architecture,
                R.string.colorwalk_scene_shadow
            )
        ),
        ColorCard(
            id = "spring_blossom",
            colors = listOf(
                ColorInfo("FFB7C5", R.string.color_cherry_blossom, ColorRole.PRIMARY),
                ColorInfo("FF69B4", R.string.color_hot_pink, ColorRole.PRIMARY),
                ColorInfo("98FB98", R.string.color_pale_green, ColorRole.ACCENT),
                ColorInfo("FFFACD", R.string.color_lemon_chiffon, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_spring_blossom,
            descriptionResId = R.string.colorwalk_desc_spring_blossom,
            tipsResId = R.string.colorwalk_tips_spring_blossom,
            challengeResId = R.string.colorwalk_challenge_spring_blossom,
            sceneTags = listOf(
                R.string.colorwalk_scene_flower,
                R.string.colorwalk_scene_park,
                R.string.colorwalk_scene_soft_light
            )
        ),
        ColorCard(
            id = "coffee_time",
            colors = listOf(
                ColorInfo("6F4E37", R.string.color_coffee, ColorRole.PRIMARY),
                ColorInfo("D2691E", R.string.color_chocolate, ColorRole.PRIMARY),
                ColorInfo("FFF8DC", R.string.color_cornsilk, ColorRole.ACCENT),
                ColorInfo("A0522D", R.string.color_sienna, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_coffee_time,
            descriptionResId = R.string.colorwalk_desc_coffee_time,
            tipsResId = R.string.colorwalk_tips_coffee_time,
            challengeResId = R.string.colorwalk_challenge_coffee_time,
            sceneTags = listOf(
                R.string.colorwalk_scene_cafe,
                R.string.colorwalk_scene_still_life,
                R.string.colorwalk_scene_indoor
            )
        ),
        ColorCard(
            id = "ocean_deep",
            colors = listOf(
                ColorInfo("006994", R.string.color_ocean_blue, ColorRole.PRIMARY),
                ColorInfo("003366", R.string.color_dark_blue, ColorRole.PRIMARY),
                ColorInfo("40E0D0", R.string.color_turquoise, ColorRole.ACCENT),
                ColorInfo("F0FFF0", R.string.color_honeydew, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_ocean_deep,
            descriptionResId = R.string.colorwalk_desc_ocean_deep,
            tipsResId = R.string.colorwalk_tips_ocean_deep,
            challengeResId = R.string.colorwalk_challenge_ocean_deep,
            sceneTags = listOf(
                R.string.colorwalk_scene_water,
                R.string.colorwalk_scene_blue_hour,
                R.string.colorwalk_scene_reflection
            )
        ),
        ColorCard(
            id = "autumn_leaves",
            colors = listOf(
                ColorInfo("FF4500", R.string.color_orange_red, ColorRole.PRIMARY),
                ColorInfo("DAA520", R.string.color_goldenrod, ColorRole.PRIMARY),
                ColorInfo("8B0000", R.string.color_dark_red, ColorRole.ACCENT),
                ColorInfo("F5DEB3", R.string.color_wheat, ColorRole.ACCENT)
            ),
            themeResId = R.string.colorwalk_theme_autumn_leaves,
            descriptionResId = R.string.colorwalk_desc_autumn_leaves,
            tipsResId = R.string.colorwalk_tips_autumn_leaves,
            challengeResId = R.string.colorwalk_challenge_autumn_leaves,
            sceneTags = listOf(
                R.string.colorwalk_scene_autumn,
                R.string.colorwalk_scene_park,
                R.string.colorwalk_scene_backlight
            )
        )
    )

    fun getRandomCard(): ColorCard = allCards.random()

    fun getCardById(id: String): ColorCard? = allCards.find { it.id == id }
}
