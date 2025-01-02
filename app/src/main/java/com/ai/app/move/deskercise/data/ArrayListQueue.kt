package com.ai.app.move.deskercise.data

class ArrayListQueue<T> : Queue<T> {

    private val list = arrayListOf<T>()
    override val count: Int
        get() = list.size

    override fun enqueue(element: T): Boolean {
        list.add(element)
        return true
    }

    override fun dequeue(): T? =
        if (isEmpty) null else list.removeAt(0)

    override fun peek(): T? = list.getOrNull(0)
}
