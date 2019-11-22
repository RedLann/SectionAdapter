package com.sectionadapter

import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class DataBindingSectionAdapter<S : Section, N : Node, SectionDataBinding : ViewDataBinding, NodeDataBinding : ViewDataBinding> :
    SectionAdapter<S, N>() {
    abstract override fun onCreateNodeViewHolder(parent: ViewGroup): BaseBindingNodeViewHolder

    abstract override fun onCreateSectionViewHolder(parent: ViewGroup): BaseBindingSectionViewHolder

    abstract inner class BaseBindingSectionViewHolder(val binding: SectionDataBinding) :
        SectionAdapter<S, N>.BaseSectionViewHolder(binding.root) {
        abstract override fun bindSection(section: S)
    }

    abstract inner class BaseBindingNodeViewHolder(val binding: NodeDataBinding) :
        SectionAdapter<S, N>.BaseNodeViewHolder(binding.root) {
        abstract override fun bindNode(node: N)
    }
}