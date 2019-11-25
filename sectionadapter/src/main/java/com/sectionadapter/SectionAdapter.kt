package com.sectionadapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

/**
 * A powerful adapter.
 */
abstract class SectionAdapter<S : Section, N : Node> :
    RecyclerView.Adapter<SectionAdapter<S, N>.BaseViewHolder>(), ItemTouchHelperAdapter {
    private val differ: AsyncListDiffer<Node> by lazy { AsyncListDiffer(this, ItemDiffCallback()) }
    private var sectionsMapping = mutableMapOf<Int, Section>()
    val dataset: List<Node> get() = differ.currentList

    companion object {
        const val NODE = 0
        const val SECTION = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            NODE -> onCreateNodeViewHolder(parent)
            SECTION -> onCreateSectionViewHolder(parent)
            else -> throw Exception("Wrong viewtype")
        }
    }

    private fun findLimitsForNode(node: Node): Pair<Int, Int> {
        val nodeIndex = differ.currentList.indexOf(node)
        val nodeSection = (sectionsMapping.keys.filter {
            it < nodeIndex
        }.minBy { nodeIndex - it}?.plus(1) ?: 0)
        val nextSection = (sectionsMapping.keys.sorted().firstOrNull {
            it > nodeIndex
        } ?: dataset.size) - 1
        return Pair(nodeSection, nextSection)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (!nodesCanBeMovedBetweenSections) {
            val limits = findLimitsForNode(dataset[fromPosition])
            if (toPosition in limits.first..limits.second) {
                differ.submitList(dataset.move(fromPosition, toPosition))
                return true
            }
        } else {
            differ.submitList(dataset.move(fromPosition, toPosition))
            return true
        }
        return false
    }

    // region Adapter Configuration
    open var nodesCanBeMovedBetweenSections = false
    open var movableSections = false
    open var swipeableSections = false
    open var movableNodes = false
    open var swipeableNodes = false
    open var swipeBack = false

    override fun canItemBeMoved(position: Int): Boolean {
        if (dataset[position] is Section) {
            return movableSections
        }
        return movableNodes
    }

    override fun canItemBeSwiped(position: Int): Boolean {
        if (dataset[position] is Section) {
            return swipeableSections
        }
        return swipeableNodes
    }

    override fun swipeBack(position: Int): Boolean {
        return swipeBack
    }
    // endregion

    override fun onItemMoveEnded(fromPosition: Int, toPosition: Int) {}
    override fun onItemSwiped(position: Int, direction: Int) {}

    abstract fun onCreateNodeViewHolder(parent: ViewGroup): BaseNodeViewHolder
    abstract fun onCreateSectionViewHolder(parent: ViewGroup): BaseSectionViewHolder

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (getItemViewType(position)) {
            NODE -> {
                holder as BaseNodeViewHolder
                holder.bindNode(dataset[position] as N)
            }
            SECTION -> {
                holder as BaseSectionViewHolder
                holder.bindSection(dataset[position] as S)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataset[position]) {
            is Section -> SECTION
            else -> NODE
        }
    }

    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

    abstract inner class BaseNodeViewHolder(view: View) : BaseViewHolder(view) {
        abstract fun bindNode(node: N)
    }

    abstract inner class BaseSectionViewHolder(view: View) : BaseViewHolder(view) {
        protected open fun isCollapsible() = false
        protected open var collapsed: Boolean = false
        abstract fun bindSection(section: S)

        protected fun toggleCollapse(section: S) {
            if (!isCollapsible()) return
            if (collapsed) collapseSection(section)
            else expandSection(section)
        }

        protected open fun collapseSection(section: S) {
            val currentList = differ.currentList.toMutableList()
            currentList.removeAll(section.nodes)
            differ.submitList(currentList)
            collapsed = true
        }

        protected open fun expandSection(section: S) {
            val currentList = differ.currentList.toMutableList()
            var index = currentList.indexOf(section) + 1
            section.nodes.forEach { node ->
                currentList.add(index, node)
                index++
            }
            differ.submitList(currentList)
            collapsed = false
        }
    }

    fun submitNodes(items: List<Node>) {
        sectionsMapping.clear()
        items.forEachIndexed { index, node ->
            if (node is Section)
                sectionsMapping.put(index, node)
        }
        differ.submitList(items)
    }

    fun submitSections(sections: List<Section>) {
        val newItems = mutableListOf<Node>()
        sectionsMapping.clear()
        sections.forEach {
            newItems.add(it)
            val sectionIndex = newItems.size - 1
            newItems.addAll(it.nodes)
            sectionsMapping.put(sectionIndex, it)
        }
        differ.submitList(newItems)
    }

    inner class ItemDiffCallback : DiffUtil.ItemCallback<Node>() {
        override fun areItemsTheSame(oldItem: Node, newItem: Node): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: Node, newItem: Node): Boolean {
            return oldItem.areContentsTheSame(newItem)
        }
    }
}