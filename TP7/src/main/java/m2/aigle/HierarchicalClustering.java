package m2.aigle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import spoon.reflect.declaration.CtClass;

public class HierarchicalClustering {
    private QualityFunction qualityFunction;
	private List<CtClass> classes;
	private int totalCalls;

	public HierarchicalClustering(List<CtClass> classes, QualityFunction qualityFunction, int totalCalls) {
		this.classes = classes;
    	this.qualityFunction = qualityFunction;
    	this.totalCalls = totalCalls;
    }
	
	public List<MicroService> run() {
		Node dendro = buildDendro();
		
		return extractMicroServices(dendro);
	}
	
	private List<MicroService> extractMicroServices(Node dendro) {
		List<MicroService> result = new ArrayList<>();
		Queue<Node> nodeQueue = new LinkedList<>();
		nodeQueue.offer(dendro);
		
		while (! nodeQueue.isEmpty()) {
			Node current = nodeQueue.poll();
			Node leftChild = current.getLeftChild();
			Node rightChild = current.getRightChild();
			
			if (current.getScore() > (leftChild.getScore() + rightChild.getScore()) / 2) {
				result.add(new MicroService(current.findClasses()));
			} else {
				nodeQueue.offer(leftChild);
				nodeQueue.offer(rightChild);
			}
		}
		
		return result;
	}
	
	private Node buildDendro() {
		List<Node> clusters = new ArrayList<>();
		
		for (CtClass cls : classes) {
			clusters.add(new Node(cls));
		}
		

		Set<Node> initialNodeSet = new HashSet<>(clusters);
		
		while (clusters.size() > 1) {
			Collections.sort(clusters);

			Node first = clusters.get(clusters.size() - 2);
			Node second = clusters.get(clusters.size() - 1);
			Node newNode = new Node(first, second);
			newNode.computeScore(CouplingMetric.totalCalls(newNode.findClasses()));
			
			clusters.remove(first);
			clusters.remove(second);
			clusters.add(newNode);
			
		}
				
		return clusters.get(0);
	}
}
