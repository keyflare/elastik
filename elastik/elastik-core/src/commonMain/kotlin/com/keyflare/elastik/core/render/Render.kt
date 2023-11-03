package com.keyflare.elastik.core.render

interface SingleRender {
    val content: Content
    val transition: Transition?

    interface Content
}

interface BackstackRender {
    val decoration: Decoration?
    val transition: Transition?
    val defaultChildrenTransition: Transition?

    interface Decoration
}

interface Transition

object NoRender : SingleRender, BackstackRender {
    override val content: SingleRender.Content = ContentStub
    override val decoration: BackstackRender.Decoration? = null
    override val transition: Transition? = null
    override val defaultChildrenTransition: Transition? = null

    private object ContentStub : SingleRender.Content
}
