package com.keyflare.elastik.routing.router

import com.keyflare.elastik.routing.context.ElastikContext
import com.keyflare.elastik.routing.tree.StaticRouterTreeBuilder
import com.keyflare.elastik.routing.tree.StaticRouterTreeBuilderDelegate

abstract class StaticRouter(context: ElastikContext) :
    BaseRouter(context),
    StaticRouterTreeBuilder by StaticRouterTreeBuilderDelegate()
