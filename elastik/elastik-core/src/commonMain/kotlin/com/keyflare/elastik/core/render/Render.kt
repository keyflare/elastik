package com.keyflare.elastik.core.render

interface SingleRender {
    val content: Content
    val transition: Transition?

    interface Content
}

interface BackstackRender {
    val container: Container
    val transition: Transition?
    val defaultChildrenTransition: Transition?

    interface Container
}

object NoRender : SingleRender, BackstackRender {
    override val content: SingleRender.Content = ContentStub
    override val container: BackstackRender.Container = ContainerStub
    override val transition: Transition? = null
    override val defaultChildrenTransition: Transition? = null

    private object ContentStub : SingleRender.Content
    private object ContainerStub : BackstackRender.Container
}

interface Transition
