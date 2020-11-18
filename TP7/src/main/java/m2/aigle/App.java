package m2.aigle;

import java.util.List;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.TypeFilter;
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

        System.out.println("Run Launcher and fetch model.");
        launcher.run();
        model = launcher.getModel(); // returns the model of the project

       List<CtClass> classList = model.getElements(new TypeFilter<CtClass>(CtClass.class));

       CtClass a = null;
       CtClass b = null;

       for (CtClass c : classList) {
           if (c.getSimpleName().equals("RestPanel")) {
               a = c;
           } else if (c.getSimpleName().equals("Converter")) {
               b = c;
           }
       }

       System.out.println(new CouplingMetric().compute(a, b, classList));
       // for (CtClass c : classList) {
       //     List<CtMethod> methods = c.getElements(new TypeFilter<CtMethod>(CtMethod.class));
       // }
    }
}
