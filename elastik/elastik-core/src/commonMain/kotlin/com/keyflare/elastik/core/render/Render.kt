package com.keyflare.elastik.core.render

interface SingleRender {
    val content: Content
    val transition: Transition?

    interface Content
}

interface StackRender {
    val container: Container
    val transition: Transition?
    val defaultChildrenTransition: Transition?

    interface Container
}

object NoRender : SingleRender, StackRender {
    override val content: SingleRender.Content = ContentStub
    override val container: StackRender.Container = ContainerStub
    override val transition: Transition? = null
    override val defaultChildrenTransition: Transition? = null

    private object ContentStub : SingleRender.Content
    private object ContainerStub : StackRender.Container
}

interface Transition
