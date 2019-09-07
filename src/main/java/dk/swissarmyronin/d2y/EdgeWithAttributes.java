package dk.swissarmyronin.d2y;

import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.MoreObjects;
import com.paypal.digraph.parser.GraphEdge;

import lombok.RequiredArgsConstructor;

/** {@code DefaultEdge} that can store the original {@link GraphEdge}. */
@RequiredArgsConstructor
class EdgeWithAttributes extends DefaultEdge {
	private static final long serialVersionUID = 1L;
	private final Map<String, Object> attributes;
	
	public EdgeWithAttributes(GraphEdge edge) {
		this(edge.getAttributes());
	}
	
	public String getLabel() {
		return MoreObjects.firstNonNull((String) attributes.get("label"), "");
	}
}