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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import com.github.systemdir.gml.YedGmlWriter;
import com.google.common.base.Functions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import com.paypal.digraph.parser.GraphParserException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	private TerraformVertexLabelProvider vertexLabelProvider = new TerraformVertexLabelProvider();

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

	/** Filter boring elements. */
	private void filterBoringElements(UndirectedGraph<GraphNode, EdgeWithAttributes> graph) {
		graph.removeAllVertices(graph
				.vertexSet().parallelStream().filter(v -> v.getId().contains("[root] root")
						|| v.getId().contains("[root] provider") || v.getId().contains("[root] meta"))
				.collect(Collectors.toList()));
	}

	/** Convert a Graphviz graph to an intermediate format. */
	private SimpleGraph<GraphNode, EdgeWithAttributes> getGraph(GraphParser parser) {
		SimpleGraph<GraphNode, EdgeWithAttributes> graph = new SimpleGraph<>(
				EdgeWithAttributes.class);
		
		val c = new Consumer<GraphNode>() {			
			@Override
			public void accept(GraphNode n) {
				String type = vertexLabelProvider.apply(n).split("\\.")[0];
				n.setAttribute("type", type);
				graph.addVertex(n);
			}
		};

		parser.getNodes().values().forEach(c::accept);
		parser.getEdges().values().forEach(e -> graph.addEdge(e.getNode1(), e.getNode2(), new EdgeWithAttributes(e)));

		return graph;
	}
	
	/** Group elements in modules. */
	private Map<String, Set<GraphNode>> groupModuleElements(
			UndirectedGraph<GraphNode, EdgeWithAttributes> graph,
			final TerraformVertexLabelProvider vertexLabelProvider) {
		Map<String, Set<GraphNode>> groupMapping = new HashMap<>();
		graph.vertexSet().forEach(v -> {
			String label = vertexLabelProvider.apply(v);
			String[] labelParts = label.split("\\.");
			if ("module".equals(labelParts[0])) {
				String module = labelParts[1];
				label = Arrays.asList(labelParts).subList(2, labelParts.length).stream().collect(Collectors.joining("."));
				Set<GraphNode> group = MoreObjects.firstNonNull(groupMapping.get(module), new HashSet<>());
				group.add(v);
				groupMapping.put(module, group);
				v.setAttribute("label", label);
			}
		});
		return groupMapping;
	}

	private void run(String[] args) throws GraphParserException, UnsupportedEncodingException, IOException {
		String inputFileName = args[0];
		String outputFilename = args.length > 1 ? args[1] : null;

		// Read .dot file
		val parser = new GraphParser(new FileInputStream(inputFileName));

		// Convert to intermediate graph
		UndirectedGraph<GraphNode, EdgeWithAttributes> graph = getGraph(parser);

		// Remove Terraform noise
		filterBoringElements(graph);

		Map<String, Set<GraphNode>> groupMapping = groupModuleElements(graph, vertexLabelProvider);
		
		groupMapping.values().forEach(s -> {
			s.forEach(g -> {
				String label = vertexLabelProvider.apply(g);
				String[] labelParts = label.split("\\.");
				String nodeType = labelParts[0];
				String nodeLabel = labelParts[1];

				Set<EdgeWithAttributes> edgesOf = graph.edgesOf(g);
				
				Set<GraphNode> sources = edgesOf.parallelStream()
						.filter((Predicate<EdgeWithAttributes>) e -> e.getTarget().equals(g))
						.map(e -> e.getSource())
						.collect(Collectors.toSet());
				
				Set<GraphNode> targets = edgesOf.parallelStream()
						.filter((Predicate<EdgeWithAttributes>) e -> e.getSource().equals(g))
						.map(e -> e.getTarget())
						.collect(Collectors.toSet());

				if ("var".equals(nodeType) || "output".equals(nodeType)) {
					sources.forEach(src -> {
						targets.forEach(dst -> {
							graph.addEdge(src, dst, new EdgeWithAttributes(nodeLabel));
						});
					});
					graph.removeVertex(g); // This will also remove unused vars and outputs
				}
			});
		});

		// Output GML
		OutputStream out;
		if (outputFilename != null) {
			out = new FileOutputStream(new File(outputFilename));
		} else {
			out = System.out;
		}

		val writer = new YedGmlWriter.Builder<>(
				new TerraformGraphicsProvider(), YedGmlWriter.PRINT_LABELS)
						.setVertexLabelProvider(vertexLabelProvider)
						.setEdgeLabelProvider(EdgeWithAttributes::getLabel)
						.setGroups(groupMapping, Functions.identity()).build();

		try (Writer output = new BufferedWriter(new OutputStreamWriter(out, "utf-8"))) {
			writer.export(output, graph);
		}
	}
}
