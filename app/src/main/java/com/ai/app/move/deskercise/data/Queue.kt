package com.ai.app.move.deskercise.data

interface Queue<T> {

    val count: Int
    val isEmpty: Boolean
        get() = count == 0

    fun enqueue(element: T): Boolean
    fun dequeue(): T?
    fun peek(): T?
}
