package dk.swissarmyronin.d2y;

import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.paypal.digraph.parser.GraphNode;

public class VertexLabelProvider implements Function<GraphNode, String> {
	@Override
	public String apply(GraphNode v) {
		return MoreObjects.firstNonNull((String) v.getAttribute("label"), v.getId());
	}
}