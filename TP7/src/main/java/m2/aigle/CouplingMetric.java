package m2.aigle;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.declaration.CtMethod;
import java.util.List;

public class CouplingMetric {
    public static double compute(CtClass a, CtClass b, int totalCalls) {
        int numberOfCalls = computeNumberOfCalls(a, b);

        return ((double) numberOfCalls) / totalCalls;
    }

    public static int computeNumberOfCalls(CtClass c, CtClass destination) {
        List<CtMethod> methods = c.getElements(new TypeFilter<CtMethod>(CtMethod.class));
        int numberOfCalls = 0;

        for (CtMethod m : methods) {
            List<CtInvocation> invocations = c.getElements(new TypeFilter<CtInvocation>(CtInvocation.class));

            for (CtInvocation i : invocations) {
                CtExecutableReference reference = i.getExecutable();

                if (reference.getDeclaringType() != null && reference.getDeclaration() != null) {
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
}
