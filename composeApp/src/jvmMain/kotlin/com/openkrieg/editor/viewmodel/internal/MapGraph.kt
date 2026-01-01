package com.openkrieg.editor.viewmodel.internal

import com.riskrieg.map.Territory
import com.riskrieg.map.territory.Border
import org.jgrapht.Graph

data class MapGraph(val vertexSet: Set<Territory>, val edgeSet: Set<Border>) {
	constructor(graph: Graph<Territory, Border>) : this(graph.vertexSet(), graph.edgeSet())
}
