package m2.aigle;
import java.util.List;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;
public class CouplingMetric implements QualityFunction {
    private int totalCalls;

    public CouplingMetric() {
    }

    public static double compute(CtClass a, CtClass b, int totalCalls) {
        int numberOfCalls = computeNumberOfCalls(a, b);
        return ((double) (numberOfCalls)) / totalCalls;
    }

    public static int computeNumberOfCalls(CtClass c, CtClass destination) {
        List<CtMethod> methods = c.getElements(new TypeFilter<CtMethod>(CtMethod.class));
        int numberOfCalls = 0;
        for (CtMethod m : methods) {
            List<CtInvocation> invocations = c.getElements(new TypeFilter<CtInvocation>(CtInvocation.class));
            for (CtInvocation i : invocations) {
                CtExecutableReference reference = i.getExecutable();
                if ((reference.getDeclaringType() != null) && (reference.getDeclaration() != null)) {
                    CtType declaration = reference.getDeclaringType().getDeclaration();
                    if (destination != null) {
                        if (declaration.equals(destination)) {
                            ++numberOfCalls;
                        }
                    } else {
                        ++numberOfCalls;
                    }
                }
            }
        }
        return numberOfCalls;
    }

    public static int totalCalls(List<CtClass> classList) {
        int totalCalls = 0;
        for (CtClass c : classList) {
            totalCalls += CouplingMetric.computeNumberOfCalls(c, null);
        }
        return totalCalls;
    }

    @Override
    public double score(CtClass a, CtClass b, int totalCalls) {
        return compute(a, b, totalCalls);
    }
}