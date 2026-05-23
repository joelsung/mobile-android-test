package com.example.app.data.crossref

data class CrossReference(
    val fromBookId: Int,
    val fromChapter: Int,
    val fromVerse: Int,
    val toBookId: Int,
    val toChapter: Int,
    val toVerseStart: Int,
    val toVerseEnd: Int,
    val votes: Int
) {
    fun toDisplayRef(): String {
        val name = korShortNames.getOrElse(toBookId - 1) { "?" }
        return if (toVerseStart == toVerseEnd) "$name $toChapter:$toVerseStart"
        else "$name $toChapter:$toVerseStart-$toVerseEnd"
    }

    companion object {
        val korShortNames = listOf(
            "창", "출", "레", "민", "신", "수", "삿", "룻", "삼상", "삼하",
            "왕상", "왕하", "대상", "대하", "스", "느", "에", "욥", "시", "잠",
            "전", "아", "사", "렘", "애", "겔", "단", "호", "욜", "암",
            "옵", "욘", "미", "나", "합", "습", "학", "슥", "말",
            "마", "막", "눅", "요", "행", "롬", "고전", "고후", "갈", "엡",
            "빌", "골", "살전", "살후", "딤전", "딤후", "딛", "몬", "히", "약",
            "벧전", "벧후", "요일", "요이", "요삼", "유", "계"
        )
    }
}
