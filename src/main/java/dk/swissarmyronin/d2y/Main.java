package dk.swissarmyronin.d2y;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import com.github.systemdir.gml.YedGmlWriter;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.paypal.digraph.parser.GraphParserException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	public static void main(String[] args) {
		try {
			Preconditions.checkArgument(args.length > 0, "Syntax: dottoyed input.dot [output.gml]");
			new Main().run(args);
		} catch (GraphParserException e) {
			log.error("Invalid .dot file \"{}\": {}", args[0], e.getCause().getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/** Remove uninteresting items from the graph. */
	private void filterBoringElements(UndirectedGraph<GraphNode, EdgeWithAttributes> graph) {
		graph.removeAllVertices(graph
				.vertexSet().parallelStream().filter(v -> 
						v.getId().contains("[root] root") || 
						v.getId().contains("[root] provider") || 
						v.getId().contains("[root] meta"))
				.collect(Collectors.toList()));
	}

	/** Convert a Graphviz graph to an intermediate format. */
	private SimpleGraph<GraphNode, EdgeWithAttributes> getGraph(GraphParser parser) {
		SimpleGraph<GraphNode, EdgeWithAttributes> graph = new SimpleGraph<>(EdgeWithAttributes.class);

		parser.getNodes().values().forEach(node -> graph.addVertex(node));
		parser.getEdges().values().forEach(edge -> graph.addEdge(edge.getNode1(), edge.getNode2(), new EdgeWithAttributes(edge)));

		return graph;
	}

	private void run(String[] args)
			throws GraphParserException, UnsupportedEncodingException, IOException {
		String inputFileName = args[0];
		String outputFilename = args.length > 1 ? args[1] : null;

		// Read .dot file
		GraphParser parser = new GraphParser(new FileInputStream(inputFileName));
		
		// Convert to intermediate graph
		UndirectedGraph<GraphNode, EdgeWithAttributes> graph = getGraph(parser);

		filterBoringElements(graph);

		// Output GML
		OutputStream out;
		if (outputFilename != null) {
			out = new FileOutputStream(new File(outputFilename));
		} else {
			out = System.out;
		}
		
		Map<String, Set<GraphNode>> groupMapping = new HashMap<>();

		YedGmlWriter<GraphNode, EdgeWithAttributes, String> writer = new YedGmlWriter.Builder<>(
				new TerraformGraphicsProvider(), YedGmlWriter.PRINT_LABELS)
						.setVertexLabelProvider(new TerraformVertexLabelProvider())
						.setEdgeLabelProvider(EdgeWithAttributes::getLabel)
						.setGroups(groupMapping, Functions.identity())
						.build();

		try (Writer output = new BufferedWriter(new OutputStreamWriter(out, "utf-8"))) {
			writer.export(output, graph);
		}
	}
}
