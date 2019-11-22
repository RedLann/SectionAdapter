package com.sectionadapter

interface Node {
    var key: String

    fun areContentsTheSame(obj: Node): Boolean {
        if (this === obj) return true
        if (javaClass != obj.javaClass) return false
        return propertyDiff(obj)
    }

    fun propertyDiff(obj: Node): Boolean
}

interface Section : Node {
    var canBeCollapsed: Boolean
    var nodes: List<Node>
}