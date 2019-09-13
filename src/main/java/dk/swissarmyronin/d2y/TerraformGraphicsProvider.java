package dk.swissarmyronin.d2y;

import java.util.Set;

import com.github.systemdir.gml.model.EdgeGraphicDefinition;
import com.github.systemdir.gml.model.NodeGraphicDefinition;
import com.github.systemdir.gml.model.NodeGraphicDefinition.Form;
import com.github.systemdir.gml.model.YedGmlGraphicsProvider;
import com.google.common.base.MoreObjects;
import com.paypal.digraph.parser.GraphNode;

class TerraformGraphicsProvider implements YedGmlGraphicsProvider<GraphNode, EdgeWithAttributes, String> {
	@Override
	public NodeGraphicDefinition getVertexGraphics(GraphNode node) {
		return new NodeGraphicDefinition.Builder().setForm(getForm(node)).build();
	}

	private Form getForm(GraphNode node) {
		Form form;
		switch (MoreObjects.firstNonNull((String) node.getAttribute("type"), "none")) {
			case "var":
			case "output":
			case "local":
				form = Form.ellipse;
				break;
			case "module":
				form = Form.roundrectangle;
				break;
			case "data":
				form = Form.parallelogram;
				break;
			default:
				form = Form.rectangle;
				break;
		}
		return form;
	}

	@Override
	public EdgeGraphicDefinition getEdgeGraphics(EdgeWithAttributes edge, GraphNode edgeSource,
			GraphNode edgeTarget) {
		return new EdgeGraphicDefinition.Builder()
				.setTargetArrow(EdgeGraphicDefinition.ArrowType.SHORT_ARROW)
				.build();
	}

	@Override
	public NodeGraphicDefinition getGroupGraphics(String group, Set<GraphNode> groupElements) {
		NodeGraphicDefinition.Builder builder = new NodeGraphicDefinition.Builder()
				.setForm(Form.roundrectangle);
//		builder.setLabelPlacement(LabelPlacement.south);
		return builder.build();
	}
}