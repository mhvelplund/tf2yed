package dk.swissarmyronin.d2y;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.paypal.digraph.parser.GraphNode;

class TerraformVertexLabelProvider extends VertexLabelProvider {
	private static final Pattern P = Pattern.compile("\"\\[root\\] ([a-zA-Z_0-9.-]+)\"");

	@Override
	public String apply(GraphNode v) {
		String label = super.apply(v);
		Matcher matcher = P.matcher(label);
		return matcher.matches() ? matcher.group(1) : label;
	}
}