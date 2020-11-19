package m2.aigle;
import spoon.reflect.declaration.CtClass;
public interface QualityFunction {
    public double score(CtClass a, CtClass b, int totalCalls);
}