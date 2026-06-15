package com.worldcup26.reminder.domain

/**
 * Maps the openfootball English country names to ISO 3166-1 alpha-2 codes (with the
 * `gb-eng` / `gb-sct` extensions flagcdn supports for the home nations) and builds a
 * flag image URL. Flags come from flagcdn.com — free, open, no API key.
 *
 * Knockout placeholders ("2A", "W81") have no mapping and return null, so the UI
 * falls back to showing the placeholder text until real teams are known.
 */
object CountryFlags {

    private const val FLAG_BASE = "https://flagcdn.com/w40"

    private val nameToCode: Map<String, String> = mapOf(
        "Algeria" to "dz",
        "Argentina" to "ar",
        "Australia" to "au",
        "Austria" to "at",
        "Belgium" to "be",
        "Bosnia & Herzegovina" to "ba",
        "Brazil" to "br",
        "Canada" to "ca",
        "Cape Verde" to "cv",
        "Colombia" to "co",
        "Croatia" to "hr",
        "Curaçao" to "cw",
        "Czech Republic" to "cz",
        "DR Congo" to "cd",
        "Ecuador" to "ec",
        "Egypt" to "eg",
        "England" to "gb-eng",
        "France" to "fr",
        "Germany" to "de",
        "Ghana" to "gh",
        "Haiti" to "ht",
        "Iran" to "ir",
        "Iraq" to "iq",
        "Ivory Coast" to "ci",
        "Japan" to "jp",
        "Jordan" to "jo",
        "Mexico" to "mx",
        "Morocco" to "ma",
        "Netherlands" to "nl",
        "New Zealand" to "nz",
        "Norway" to "no",
        "Panama" to "pa",
        "Paraguay" to "py",
        "Portugal" to "pt",
        "Qatar" to "qa",
        "Saudi Arabia" to "sa",
        "Scotland" to "gb-sct",
        "Senegal" to "sn",
        "South Africa" to "za",
        "South Korea" to "kr",
        "Spain" to "es",
        "Sweden" to "se",
        "Switzerland" to "ch",
        "Tunisia" to "tn",
        "Turkey" to "tr",
        "Uruguay" to "uy",
        "USA" to "us",
        "Uzbekistan" to "uz",
    )

    /** Flag image URL for a team name, or null if it is a knockout placeholder. */
    fun flagUrl(teamName: String): String? =
        nameToCode[teamName]?.let { "$FLAG_BASE/$it.png" }
}
