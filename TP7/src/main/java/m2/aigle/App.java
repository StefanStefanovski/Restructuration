package m2.aigle;

import java.util.List;
import java.util.Set;
import java.util.Queue;
import java.util.LinkedList;
import java.io.File;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.io.IOException;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtClass;
import spoon.support.reflect.declaration.CtClassImpl;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.compiler.Environment;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.reflect.declaration.CtType;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import static guru.nidi.graphviz.model.Factory.*;

public class App
{
    public static void main( String[] args )
    {
        CtModel model;

        Arguments arguments = new Arguments();
        boolean isParsed = arguments.parseArguments(args);

        if(!isParsed)
            return;

        String sourceCodePath = arguments.getSource();

        Launcher launcher = new MavenLauncher(sourceCodePath, MavenLauncher.SOURCE_TYPE.APP_SOURCE); // r
        Environment environment = launcher.getEnvironment();
        environment.setAutoImports(true);

        launcher.run();
        model = launcher.getModel();

        List<CtClass> classList = model.getElements(new TypeFilter<CtClass>(CtClass.class));

        int totalCalls = 0;
        for (CtClass c : classList) {
            totalCalls += CouplingMetric.computeNumberOfCalls(c, null);
        }

        try {
            loop(totalCalls, model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loop(int totalCalls, CtModel model) throws IOException {
	CtClass mainClass = model.getElements(new AbstractFilter<CtClass>(CtClass.class) {
		@Override
		public boolean matches(CtClass c) {
		    return c.getSimpleName().equals("Main");
		}
	    }).get(0);

	Set<String> processed = new HashSet<>();
	Queue<CtClass> queue = new LinkedList<>();
	queue.add(mainClass);


	BufferedWriter bw = new BufferedWriter(new FileWriter("dependency_graph.dot"));
	bw.write("digraph g {\n");
    bw.write("");

	while (true) {
	    CtClass c = queue.poll();

	    if (c == null) {
		break;
	    }

	    if (processed.contains(c.getSimpleName())) {
		continue;
	    }

	    Set<CtClass> targets = new HashSet<>();

	    List<CtTypeReference> refs = c.getElements(new TypeFilter<>(CtTypeReference.class));

	    Set<String> names = new HashSet<>();
	    for (CtTypeReference ref : refs) {
            if (ref.getDeclaration() instanceof CtClassImpl) {
                CtClass type = (CtClass) ref.getDeclaration();

                if (type == null
                    || type.isShadow()
                    || type.getSimpleName().equals(c.getSimpleName())
                    || names.contains(type.getSimpleName())) continue;

                names.add(type.getSimpleName());

                targets.add(type);
            }
	    }

	    String sourceStr = c.getSimpleName();

	    //MutableNode sourceNode = getNode(sourceStr);
	    // g = g.add(sourceNode);

	    for (CtClass target : targets) {
            String targetStr = target.getSimpleName();
            double weight = CouplingMetric.compute(c, target, totalCalls);
        
            // MutableNode targetNode = getNode(targetStr);
            // g = g.add(targetNode);
            //sourceNode.addLink(targetNode);
            bw.write(sourceStr + "->" + targetStr + " [weight = " + weight + "];\n");

            queue.add(target);
	    }

	    processed.add(c.getSimpleName());
	}

    bw.write("");
	bw.write("}\n");

	bw.close();
    }
}
