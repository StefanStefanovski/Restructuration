package m2.aigle;

public class Node {
    private CtClass cls = null;
    private Node leftChild = null;
    private Node rightChild = null;
    private

    public Node(Node leftChild, Node rightChild, CtClass cls) {
        this.cls = cls;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public Node(Node leftChild, Node rightChild) {
       this(leftChild, rightChild, null);
    }

    public Node() {
        this(null, null);
    }

    public boolean isLeaf() {
        return leftChild == null && rightChild == null && cls != null;
    }

    public List<CtClass> findClasses() {
        List<CtClass> result = new ArrayList<>();

        if (! isLeaf()) {
            result.addAll(leftChild.findClasses());
            result.addAll(rightChild.findClasses());
        } else {
            result.add(cls);
        }

        return result;
    }
}
