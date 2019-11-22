package com.sectionadapter

fun <T> List<T>.move(fromPos: Int, toPos: Int): List<T> {
    val newList = toMutableList()
    val elem = newList.removeAt(fromPos)
    newList.add(toPos, elem)
    return newList
}