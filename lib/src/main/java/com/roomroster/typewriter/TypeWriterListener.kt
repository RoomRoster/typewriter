package com.roomroster.typewriter

interface TypeWriterListener {
    fun onTypingStart(value: String)
    fun onTypingFinished(value: String)
    fun onCharacterTyped(char: String, position: Int)
}