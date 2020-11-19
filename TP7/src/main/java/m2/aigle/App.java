package m2.aigle;

import java.util.List;
import java.util.Set;
import java.util.Queue;
import java.util.LinkedList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.io.IOException;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.support.reflect.declaration.CtClassImpl;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.compiler.Environment;
import spoon.Launcher;
import spoon.MavenLauncher;

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

        int totalCalls = CouplingMetric.totalCalls(classList);

        try {
        	System.out.println("Génération du graphe pondéré ...");
            loop(totalCalls, model);
            System.out.println("Done !");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        List<MicroService> microServices = new HierarchicalClustering(classList, new CouplingMetric(), totalCalls).run();
        
        System.out.println("Liste des micro-services : ");
        
        for (MicroService ms : microServices) {
        	System.out.println(" - " + ms);
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
