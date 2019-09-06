package dk.swissarmyronin.d2y;

import static com.github.systemdir.gml.YedGmlWriter.PrintLabels.PRINT_VERTEX_LABELS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.stream.Collectors;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.github.systemdir.gml.YedGmlWriter;
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

	private void run(String[] args)
			throws GraphParserException, UnsupportedEncodingException, IOException {
		String inputFileName = args[0];
		String outputFilename = args.length > 1 ? args[1] : null;

		// Read .dot file
		GraphParser parser = new GraphParser(new FileInputStream(inputFileName));
		
		// Convert to intermediate graph
		UndirectedGraph<GraphNode, DefaultEdge> graph = getGraph(parser);

		filterTerraformElements(graph);


		// Output GML
		OutputStream out;
		if (outputFilename != null) {
			out = new FileOutputStream(new File(outputFilename));
		} else {
			out = System.out;
		}

		YedGmlWriter<GraphNode, DefaultEdge, Object> writer = new YedGmlWriter.Builder<>(new TerraformGraphicsProvider(), PRINT_VERTEX_LABELS)
						.setVertexLabelProvider(new TerraformVertexLabelProvider())
						.build();

		try (Writer output = new BufferedWriter(new OutputStreamWriter(out, "utf-8"))) {
			writer.export(output, graph);
		}
	}

	private void filterTerraformElements(UndirectedGraph<GraphNode, DefaultEdge> graph) {
		graph.removeAllVertices(graph
				.vertexSet().parallelStream().filter(v -> 
						v.getId().contains("[root] root") || 
						v.getId().contains("[root] provider") || 
						v.getId().contains("[root] meta"))
				.collect(Collectors.toList()));
	}

	private SimpleGraph<GraphNode, DefaultEdge> getGraph(GraphParser parser) {
		SimpleGraph<GraphNode, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

		parser.getNodes().values().forEach(node -> graph.addVertex(node));
		parser.getEdges().values().forEach(edge -> graph.addEdge(edge.getNode1(), edge.getNode2()));

		return graph;
	}
}
