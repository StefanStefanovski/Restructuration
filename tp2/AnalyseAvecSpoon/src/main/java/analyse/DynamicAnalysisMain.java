package analyse;

import java.util.List;
import java.util.ArrayList;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.compiler.Environment;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.visitor.filter.InvocationFilter;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtForEach;
import spoon.support.reflect.code.CtStatementImpl;
import spoon.reflect.reference.CtExecutableReference;

public class DynamicAnalysisMain {
    public void analyze(String args[]) {
	Arguments arguments = new Arguments();
	boolean isParsed = arguments.parseArguments(args);

	if(!isParsed)
	    return;
	String sourceCodePath = arguments.getSource();
	String experiment_output_filepath = arguments.getTarget();
		
	Launcher launcher = new MavenLauncher(sourceCodePath, MavenLauncher.SOURCE_TYPE.APP_SOURCE); // r
	launcher.setSourceOutputDirectory("./output/");
	Environment environment = launcher.getEnvironment();
	environment.setAutoImports(true);
		
	System.out.println("Run Launcher and fetch model.");
	launcher.run();
	CtModel model = launcher.getModel(); // returns the model of the project

	List<CtInvocation<?>> calls = model.getElements(new TypeFilter<>(CtInvocation.class));
	for (int j = 0; j < calls.size(); ++j) {
	    CtInvocation call = calls.get(j);

	    if (! (call.getExecutable().getDeclaration() instanceof CtMethod)) continue;

	    CtElement parent = call.getParent();
	    while (parent != null && ! (parent instanceof CtMethod)) {
		parent = parent.getParent();
	    }

	    if (parent == null) continue;
	    
	    CtMethod method = (CtMethod) parent;
	    
	    String callerClass = method.getDeclaringType().getSimpleName();
	    String callerMethod = method.getSimpleName();
	    String callerStr = callerClass + "__" + callerMethod;

	    CtMethod callee = (CtMethod) call.getExecutable().getDeclaration();
	    String calleeClass = callee.getDeclaringType().getSimpleName();
	    String calleeMethod = callee.getSimpleName();
	    String calleeStr = calleeClass + "__" + calleeMethod;

	    CtStatement loggerCall =
		launcher.getFactory().Code().createCodeSnippetStatement("com.supanadit.restsuite.util.Analyses.getInstance().logAppel(\"" + callerStr + "\", \"" + calleeStr + "\")");


	    if (call.getParent() instanceof CtIf || call.getParent() instanceof CtForEach) {
		((CtStatement) call.getParent()).insertBefore(loggerCall);		    
	    } else {
		call.insertBefore(loggerCall);
	    }
	}
	
	launcher.prettyprint();
    }
    
    public static void main(String[] args) {
	new DynamicAnalysisMain().analyze(args);
    }
}
