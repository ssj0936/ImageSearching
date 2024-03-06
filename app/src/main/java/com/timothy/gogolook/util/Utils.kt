package com.timothy.gogolook.util

import java.util.LinkedList

abstract class LRUCache<T>{
    val capacity = HISTORY_MAX_SIZE
    abstract fun add(el:T)
    abstract fun toList():List<T>
    abstract fun isEmpty():Boolean
    abstract val size:Int
}

class LRUCacheImpl<T>(list: LinkedList<T> = LinkedList()): LRUCache<T>() {
    private val linkedList = list
    override val size: Int
        get() = linkedList.size

    override fun add(el: T) {
        linkedList.remove(el)
        linkedList.offer(el)

        while(linkedList.size > capacity)
            linkedList.poll()
    }
    override fun toList(): List<T> = linkedList.toList()
    override fun isEmpty(): Boolean = (size==0)
}