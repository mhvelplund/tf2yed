# tf2yed
Convert Terraform graphs to GML for use with yEd.

Usage:

    tf2yed input.dot [output.gml]
    
If no outputs is specified, output gous to `stdout`.

## Advanced

Build an executable with:

    mvn clean package
    cat script/stub.sh target/tf2yed-*-jar-with-dependencies.jar > tf2yed && chmod +x tf2yed

Move the `tf2yed` app to your path, then enter a Terraform directory and run:

    terraform init
    tf2yed <(terraform graph) > graph.gml
    
Open `graph.gml` with yEd, and execute the steps:

* "Tools->Fit Node to Label"
* "Layout->Hierarchial"

I generally order the graph bottom to top, and follow up with "Grouping->AutoGrouping"
and/or "Tools->Centrality Measures" to get a good overview.