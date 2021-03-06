package dk.swissarmyronin.d2y;

import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import com.github.systemdir.gml.model.EdgeGraphicDefinition;
import com.github.systemdir.gml.model.NodeGraphicDefinition;
import com.github.systemdir.gml.model.NodeGraphicDefinition.Form;
import com.github.systemdir.gml.model.YedGmlGraphicsProvider;
import com.google.common.base.MoreObjects;
import com.paypal.digraph.parser.GraphNode;

public class GraphicsProvider implements YedGmlGraphicsProvider<GraphNode, DefaultEdge, Object> {

	@Override
	public NodeGraphicDefinition getVertexGraphics(GraphNode node) {
		Form form;
		switch (MoreObjects.firstNonNull((String) node.getAttribute("shape"), "none")) {
		case "box":
			form = Form.rectangle;
			break;
		case "diamond":
			form = Form.diamond;
			break;
		default:
			form = Form.ellipse;
			break;
		}

		return new NodeGraphicDefinition.Builder().setForm(form).build();
	}

	@Override
	public EdgeGraphicDefinition getEdgeGraphics(DefaultEdge edge, GraphNode edgeSource,
			GraphNode edgeTarget) {
		return new EdgeGraphicDefinition.Builder()
				.setTargetArrow(EdgeGraphicDefinition.ArrowType.SHORT_ARROW).build();
	}

	@Override
	public NodeGraphicDefinition getGroupGraphics(Object group, Set<GraphNode> groupElements) {
		throw new RuntimeException("Groups are not supported");
	}

}