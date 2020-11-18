package analyse;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.compiler.Environment;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import guru.nidi.graphviz.model.MutableNode;
import static guru.nidi.graphviz.model.Factory.*;

public class GraphVizMain {
    private CtModel model;
    private Map<String, MutableNode> nodes = new HashMap<>();
    
    public void analyze(String args[]) {
	System.out.println("Begin Analysis");

	Arguments arguments = new Arguments();
	boolean isParsed = arguments.parseArguments(args);

	if(!isParsed)
	    return;
		
	String sourceCodePath = arguments.getSource();
	String experiment_output_filepath = arguments.getTarget();
		
	Launcher launcher = new MavenLauncher(sourceCodePath, MavenLauncher.SOURCE_TYPE.APP_SOURCE); // r
	Environment environment = launcher.getEnvironment();
	environment.setAutoImports(true);
		
	System.out.println("Run Launcher and fetch model.");
	launcher.run();
	model = launcher.getModel(); // returns the model of the project

	try {
	    loop();
	    render();
	} catch (IOException e) {
	    e.printStackTrace();
	}


	// try {
	//     Graphviz.fromGraph(g).height(4096).width(4096).render(Format.PNG).toFile(new File("./dependency_graph"));
	// } catch (IOException e) {
	//     e.printStackTrace();
	// }
    }

    private void loop() throws IOException {
	CtType<?> mainClass = model.getElements(new AbstractFilter<CtType<?>>(CtType.class) {
		@Override
		public boolean matches(CtType<?> c) {
		    return c.getSimpleName().equals("Main");
		}
	    }).get(0);

	Set<String> processed = new HashSet<>();
	Queue<CtType<?>> queue = new LinkedList<>();
	queue.add(mainClass);

	
	BufferedWriter bw = new BufferedWriter(new FileWriter("dependency_graph.dot"));
	bw.write(" digraph mon_graphe {\n");

	while (true) {
	    CtType<?> c = queue.poll();

	    if (c == null) {
		break;
	    }

	    if (processed.contains(c.getSimpleName())) {
		continue;
	    }
	    
	    Set<CtType> targets = new HashSet<>();
	    
	    List<CtTypeReference> refs = c.getElements(new TypeFilter<>(CtTypeReference.class));

	    Set<String> names = new HashSet<>();
	    for (CtTypeReference ref : refs) {
		CtType type = (CtType) ref.getDeclaration();
		
		if (type == null
		    || type.isShadow()
		    || type.getSimpleName().equals(c.getSimpleName())
		    || names.contains(type.getSimpleName())) continue;

		names.add(type.getSimpleName());

		targets.add(type);
	    }

	    String sourceStr = c.getSimpleName();

	    //MutableNode sourceNode = getNode(sourceStr);
	    // g = g.add(sourceNode);
	    
	    for (CtType<?> target : targets) {
		String targetStr = target.getSimpleName();

		// MutableNode targetNode = getNode(targetStr);
		// g = g.add(targetNode);
		//sourceNode.addLink(targetNode);
		bw.write(sourceStr + "->" + targetStr + ";\n");

		queue.add(target);
	    }

	    processed.add(c.getSimpleName());
	}

	bw.write("}\n");

	bw.close();
    }

    private void render() throws IOException {
	MutableGraph g = mutGraph("dependency_graph").setDirected(true);
	BufferedReader reader = new BufferedReader(new FileReader("dependency_graph.dot"));
	reader.readLine();

	String line;
	while ((line = reader.readLine()) != null) {
	    if (line.contains("}") || line.contains("}")) continue;

	    line = line.replace(";", "");
	    String tokens[] = line.split("->");
	    String source = tokens[0];
	    String target = tokens[1];

	    g.setDirected(true).add(mutNode(source).addLink(mutNode(target)));
	}

	try {
	    Graphviz.fromGraph(g).render(Format.PNG).toFile(new File("./dependency_graph.png"));
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	new GraphVizMain().analyze(args);
    }
		// MutableGraph g2 = mutGraph("example1").setDirected(true).add(
	//         mutNode("a").add(Color.RED).addLink(mutNode("b")));
	// try {
	// 	
	// } catch (IOException e) {
	// 	// TODO Auto-generated catch block
	// 	
	// }

	// List<CtMethod<?>> methodList = model.getElements(new TypeFilter<>(CtMethod.class));
	    
	// for (CtMethod<?> method : methodList) {
	//     CtExecutableReference executableReference = method.getReference();
	//     List<CInvocation> calls = model.getElements(new InvocationFilter(executableReference));

	//     for (CInvocation call : calls) {
		
	//     }
	// }

	// Graph g = graph("example1").directed()
	// 		.graphAttr().with(Rank.dir(RankDir.LEFT_TO_RIGHT))
	// 		.nodeAttr().with(Font.name("arial"))
	// 		.linkAttr().with("class", "link-class")
	// 		.with(
	// 				node("a").with(Color.RED).link(node("b")),
	// 				node("b").link(
	// 						to(node("c"))//.with(attr("weight", 5), Style.DASHED)
	// 						)
	// 				);
	// try {
	// 	Graphviz.fromGraph(g).height(100).render(Format.PNG).toFile(new File("./ex1.png"));
	// } catch (IOException e) {
	// 	// TODO Auto-generated catch block
	// 	e.printStackTrace();
	// }
}
