package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.routing.tree.StaticRouterTreeBuilder
import com.keyflare.elastik.core.routing.tree.StaticRouterTreeBuilderDelegate

abstract class StaticRouter(context: ElastikContext) :
    BaseRouter(context),
    StaticRouterTreeBuilder by StaticRouterTreeBuilderDelegate()
