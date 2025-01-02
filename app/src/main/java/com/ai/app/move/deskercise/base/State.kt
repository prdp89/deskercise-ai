package com.ai.app.move.deskercise.base

sealed class State<T> {
    class Starting<T> : State<T>()
    class Success<T>(val data: T) : State<T>()
    class Error<T>(val error: Throwable) : State<T>()
}
