package analyse;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.compiler.Environment;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtField;
import spoon.reflect.visitor.filter.TypeFilter;
import static java.lang.Math.floor;

public class SpoonMain {
    private CtModel model;
    
    public static void main(String[] args) {
	new SpoonMain().analysis(args);
    }

    public void analysis(String[] args) {
	System.out.println("Begin Analysis");

	// Parsing arguments using JCommander
	Arguments arguments = new Arguments();
	boolean isParsed = arguments.parseArguments(args);

	// if there was a problem parsing the arguments then the program is terminated.
	if(!isParsed)
	    return;
		
	// Parsed Arguments
	String experiment_source_code = arguments.getSource();
	String experiment_output_filepath = arguments.getTarget();
		
	// Load project (APP_SOURCE only, no TEST_SOURCE for now)
	Launcher launcher = null;
	if(arguments.isMavenProject() ) {
	    launcher = new MavenLauncher(experiment_source_code, MavenLauncher.SOURCE_TYPE.APP_SOURCE); // requires M2_HOME environment variable
	}else {
	    launcher = new Launcher();
	    launcher.addInputResource(experiment_source_code + "/src");
	}
		
	// Setting the environment for Spoon
	Environment environment = launcher.getEnvironment();
	environment.setCommentEnabled(true); // represent the comments from the source code in the AST
	environment.setAutoImports(true); // add the imports dynamically based on the typeReferences inside the AST nodes.
	//		environment.setComplianceLevel(0); // sets the java compliance level.
		
	System.out.println("Run Launcher and fetch model.");
	launcher.run(); // creates model of project
	model = launcher.getModel(); // returns the model of the project

	
	// basic type filter to retrive all methods in your model
	List<CtClass> classList = model.getElements(new TypeFilter<CtClass>(CtClass.class));
	System.out.println("Nombre de classes : " + classList.size());

	int linesOfCode = 0;
	for (CtClass c : classList) {
	    linesOfCode += (c.toString().split("\n")).length;
	}
	System.out.println("Nombre de lignes de code : " + linesOfCode);

	List<CtMethod> methodList = model.getElements(new TypeFilter<CtMethod>(CtMethod.class));
	System.out.println("Nombre de méthodes : " + methodList.size());

	List<CtPackage> packageList = new ArrayList<>(model.getAllPackages());
	packageList.removeIf(p -> p.getSimpleName().equals("unnamed package"));
	System.out.println("Nombre de packages : " + packageList.size());

	System.out.println("Nombre moyen de méthodes par classes : " + ((float) methodList.size()) / classList.size());

	int linesOfCodeInMethods = 0;
	for (CtMethod m : methodList) {
	    if (! m.isAbstract()) {
		linesOfCodeInMethods += m.getBody().getStatements().size();
	    }
	}
	System.out.println("Nombre moyen de ligne de code par méthode : " + ((float) linesOfCodeInMethods) / methodList.size());

	List<CtField> fieldList = model.getElements(new TypeFilter<CtField>(CtField.class));
	System.out.println("Nombre moyen d'attributs par classe : " + ((double) fieldList.size()) / classList.size());

	Map<CtClass, Integer> mapByMethod = analyzeTenPercentClassByMethod(classList);
	Map<CtClass, Integer> mapByField = analyzeTenPercentClassByField(classList);
	List<CtClass> classesByMethod = new ArrayList<>(mapByMethod.keySet());
	List<CtClass> classesByField = new ArrayList<>(mapByField.keySet());
	
	List<CtClass> intersect = classesByMethod;
	intersect.removeIf(c -> ! classesByField.contains(c));
	
	System.out.println("Liste des classes appartenants aux deux catégories précédentes : ");
	for (CtClass c : intersect) {
	    System.out.println(" - " + c.getSimpleName());
	}

	if (intersect.isEmpty()) {
	    System.out.println("Vide");
	}

	analyzeClassThatOwnsMoreThanXMethods(classList, 10);

	int max = 0;
	for (CtMethod m : methodList) {
	    int parameters = m.getParameters().size();

	    if (parameters > max) {
		max = parameters;
	    }
	}

	System.out.println("Nombre maximal de paramètres par rapport à toutes les méthodes de l'application : " + max);
	
	System.out.println("End Analysis");
    }

    public Map<CtClass, Integer> analyzeTenPercentClassByMethod(List<CtClass> classList) {
	Map<CtClass, Integer> methodsPerClass = new HashMap<>();
	for (CtClass c : classList) {
	    methodsPerClass.put(c, c.filterChildren(new TypeFilter<CtMethod>(CtMethod.class)).list().size());
	}

	TreeMap<CtClass, Integer> methodsPerClassSorted = new TreeMap<>(new ValueComparator(methodsPerClass));
	methodsPerClassSorted.putAll(methodsPerClass);
	
	int threshold = (int) floor(((float) classList.size()) * 0.1);

	Map<CtClass, Integer> result = new TreeMap<>(new ValueComparator(methodsPerClass));
	for (int i = 0; i < threshold; ++i) {
	    Map.Entry entry = methodsPerClassSorted.pollFirstEntry();
	    result.put((CtClass) entry.getKey(), (Integer) entry.getValue());
	}
	
	System.out.println("10% des classes ayant le plus de méthodes : ");
	for (Map.Entry entry : result.entrySet()) {	
	    CtClass c = (CtClass) entry.getKey();

	    String name = c.getSimpleName();
	    if (c.isAnonymous()) {
		name = "{classe anonyme}";
	    }
		
	    System.out.println(" - " + name + " : " + entry.getValue() + " méthodes");
	}

	return result;
    }

    public Map<CtClass, Integer> analyzeTenPercentClassByField(List<CtClass> classList) {
	Map<CtClass, Integer> fieldsPerClass = new HashMap<>();
	for (CtClass c : classList) {
	    fieldsPerClass.put(c, c.filterChildren(new TypeFilter<CtField>(CtField.class)).list().size());
	}

	TreeMap<CtClass, Integer> fieldsPerClassSorted = new TreeMap<>(new ValueComparator(fieldsPerClass));
	fieldsPerClassSorted.putAll(fieldsPerClass);
	
	int threshold = (int) floor(((float) classList.size()) * 0.1);
	Map<CtClass, Integer> result = new TreeMap<>(new ValueComparator(fieldsPerClass));
	for (int i = 0; i < threshold; ++i) {
	    Map.Entry entry = fieldsPerClassSorted.pollFirstEntry();
		    
	    result.put((CtClass) entry.getKey(), (Integer) entry.getValue());
	}

	System.out.println("10% des classes ayant le plus de champs : ");
	for (Map.Entry entry : result.entrySet()) {	
	    CtClass c = (CtClass) entry.getKey();

	    String name = c.getSimpleName();
	    if (c.isAnonymous()) {
		name = "{classe anonyme}";
	    }
		
	    System.out.println(" - " + name + " : " + entry.getValue() + " attributs");
	}

	return result;
    }

    public void analyzeClassThatOwnsMoreThanXMethods(List<CtClass> classList, int x) {
	Map<CtClass, Integer> methodsPerClass = new HashMap<>();
	for (CtClass c : classList) {
	    methodsPerClass.put(c, c.filterChildren(new TypeFilter<CtMethod>(CtMethod.class)).list().size());
	}

	List<CtClass> result = new ArrayList<>();
	for (Map.Entry<CtClass, Integer> entry : methodsPerClass.entrySet()) {
	    if (((Integer) entry.getValue()) > x) {
		result.add((CtClass) entry.getKey());
	    }
	}
	
	System.out.println("Liste des classes ayant plus de " + x + " méthodes : ");
	for (CtClass c : result) {
	    System.out.println(" - " + c.getSimpleName());
	}
    }


    public class ValueComparator implements Comparator {
	private Map map;
 
	public ValueComparator(Map map) {
	    this.map = map;
	}
 
	public int compare(Object keyA, Object keyB) {
	    int compare = ((Comparable) map.get(keyB)).compareTo((Comparable) map.get(keyA));

	    if (compare == 0) {
		return 1;
	    }

	    return compare;
	}
    } 
}
