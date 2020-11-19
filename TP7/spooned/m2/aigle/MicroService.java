package m2.aigle;
import java.util.List;
import spoon.reflect.declaration.CtClass;
public class MicroService {
    private List<CtClass> classes;

    public MicroService(List<CtClass> classes) {
        this.classes = classes;
    }

    @Override
    public String toString() {
        String result = "{ ";
        for (int i = 0; i < classes.size(); ++i) {
            result += classes.get(i).getSimpleName();
            if (i != (classes.size() - 1)) {
                result += ", ";
            }
        }
        return result + "}";
    }
}