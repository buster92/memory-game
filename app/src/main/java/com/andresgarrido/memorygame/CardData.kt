package com.andresgarrido.memorygame

class CardData(id: Int, _image: Int) {
    var state = CardState.HIDE
    var image: Int = _image
}

enum class CardState {
    HIDE,
    SHOW
}