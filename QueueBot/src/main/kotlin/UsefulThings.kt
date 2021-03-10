fun getNumeral(number : Int) : String{
    return number.toString() + when(number) {
        1 -> "srt"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}
