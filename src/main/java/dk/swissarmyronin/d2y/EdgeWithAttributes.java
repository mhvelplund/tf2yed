package dk.swissarmyronin.d2y;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.MoreObjects;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** {@code DefaultEdge} that can store the original {@link GraphEdge}. */
@RequiredArgsConstructor
class EdgeWithAttributes extends DefaultEdge {
	private static final long serialVersionUID = 1L;

	@NonNull
	private final Map<String, Object> attributes;

	public EdgeWithAttributes(GraphEdge edge) {
		this(edge.getAttributes());
	}

	@SuppressWarnings("serial")
	public EdgeWithAttributes(@NonNull String label) {
		this(new HashMap<String,Object>() {
			{
				put("label", label);
			}
		});
	}

	public String getLabel() {
		return MoreObjects.firstNonNull((String) attributes.get("label"), "");
	}

	@Override
	public GraphNode getSource() {
		return (GraphNode) super.getSource();
	}

	@Override
	public GraphNode getTarget() {
		return (GraphNode) super.getTarget();
	}

}