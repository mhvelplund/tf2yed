package dk.swissarmyronin.d2y;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.github.systemdir.gml.YedGmlWriter;
import com.github.systemdir.gml.model.EdgeGraphicDefinition;
import com.github.systemdir.gml.model.NodeGraphicDefinition;
import com.github.systemdir.gml.model.NodeGraphicDefinition.Form;
import com.github.systemdir.gml.model.YedGmlGraphicsProvider;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.paypal.digraph.parser.GraphParserException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	public static void main(String[] args) {
		try {
			Preconditions.checkArgument(args.length > 0, "No filename provided");
			new Main().run(args);
		} catch (GraphParserException e) {
			log.error("Invalid .dot file \"{}\": {}", args[0], e.getCause().getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private void run(String[] args) throws GraphParserException, UnsupportedEncodingException, IOException {
		String fileName = args[0];

		GraphParser parser = new GraphParser(new FileInputStream(fileName));
		Map<String, GraphNode> nodes = parser.getNodes();
		Map<String, GraphEdge> edges = parser.getEdges();
		
      SimpleGraph<GraphNode, DefaultEdge> graph = new SimpleGraph<GraphNode, DefaultEdge>(DefaultEdge.class);

		for (GraphNode node : nodes.values()) {
			log.info("NODE: {} {}", node.getId(), node.getAttributes());
         graph.addVertex(node);
		}

		for (GraphEdge edge : edges.values()) {
			graph.addEdge(edge.getNode1(), edge.getNode2());
			log.info("EDGE: {} {}", edge.getId(), edge.getAttributes());
		}
		
		Function<GraphNode, String> vertexLabelProvider = v -> MoreObjects.firstNonNull((String)v.getAttribute("label"), v.getId());
		
		YedGmlWriter<GraphNode, DefaultEdge, Object> writer = 
				new YedGmlWriter.Builder<>(new GraphicsProvider(), YedGmlWriter.PrintLabels.PRINT_VERTEX_LABELS)
				.setVertexLabelProvider(vertexLabelProvider)
				.build();
				

		// write to file
		File outputFile = new File(args[0] + ".gml");
		try (Writer output = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"))) {
			writer.export(output, graph);
		}
	}
}

class GraphicsProvider implements YedGmlGraphicsProvider<GraphNode, DefaultEdge, Object> {
   @Override
	public NodeGraphicDefinition getVertexGraphics(GraphNode node) {
		Form form;
		switch (MoreObjects.firstNonNull((String)node.getAttribute("shape"), "none")) {
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
		
		return new NodeGraphicDefinition.Builder()
				.setForm(form)
				.build();
	}

   @Override
   public EdgeGraphicDefinition getEdgeGraphics(DefaultEdge edge, GraphNode edgeSource, GraphNode edgeTarget) {
       return new EdgeGraphicDefinition.Builder()
               .setTargetArrow(EdgeGraphicDefinition.ArrowType.SHORT_ARROW)
               .build();
   }

   @Override
   public NodeGraphicDefinition getGroupGraphics(Object group, Set<GraphNode> groupElements) {
       throw new RuntimeException("Groups are not supported");
   }
}
