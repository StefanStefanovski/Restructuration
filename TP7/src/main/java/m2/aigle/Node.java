package m2.aigle;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import spoon.reflect.declaration.CtClass;

public class Node implements Comparable<Node> {
    private CtClass cls = null;
    private Node leftChild = null;
	private Node rightChild = null;
    private double score = -1;

    private Node(Node leftChild, Node rightChild, CtClass cls) {
        this.cls = cls;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public Node(Node leftChild, Node rightChild) {
       this(leftChild, rightChild, null);
    }

    public Node(CtClass cls) {
        this(null, null, cls);
    }
    public CtClass getCtClass() {
    	return cls;
    }

    public boolean isLeaf() {
        return leftChild == null && rightChild == null && cls != null;
    }
    
    public Double getScore() {
    	return score;
    }
    
    public Node getLeftChild() {
		return leftChild;
	}

	public Node getRightChild() {
		return rightChild;
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
	
    public void computeScore(int totalCalls) {
    	List<CtClass> classes = findClasses();
    	
    	double max = 0;
    	for (CtClass c1 : classes) {
    		for (CtClass c2 : classes) {
    			if (! c1.equals(c2)) {
    				double score = CouplingMetric.compute(c1, c2, totalCalls);
    				if (score > max) {
    					max = score;
    				}
    			}
    		}
    	}
    	
    	score = max;
    }

	@Override
	public int compareTo(Node o) {
		return getScore().compareTo(o.getScore());
	}
    
	public static void traversePreOrder(StringBuilder sb, String padding, String pointer, Node node) {
	    if (node != null) {
	        sb.append(padding);
	        sb.append(pointer);
	        sb.append(node.getScore());
	        sb.append("\n");
	 
	        StringBuilder paddingBuilder = new StringBuilder(padding);
	        paddingBuilder.append("│  ");
	 
	        String paddingForBoth = paddingBuilder.toString();
	        String pointerForRight = "└──";
	        String pointerForLeft = (node.getRightChild() != null) ? "├──" : "└──";
	 
	        traversePreOrder(sb, paddingForBoth, pointerForLeft, node.getLeftChild());
	        traversePreOrder(sb, paddingForBoth, pointerForRight, node.getRightChild());
	    }
	}

	public void print(PrintStream os) {
	    StringBuilder sb = new StringBuilder();
	    traversePreOrder(sb, "", "", this);
	    os.print(sb.toString());
	}
}
