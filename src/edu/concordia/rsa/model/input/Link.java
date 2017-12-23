package edu.concordia.rsa.model.input;

import org.jgrapht.graph.DefaultWeightedEdge;

public class Link extends DefaultWeightedEdge {
	private static final long serialVersionUID = 1L;

	private int id;

	private Node vertex1;
	private Node vertex2;

	public Link(int id, Node vertex1, Node vertex2) {
		this.id = id;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
	}

	public Node getVertex1() {
		return vertex1;
	}

	public void setVertex1(Node node1) {
		this.vertex1 = node1;
	}

	public Node getVertex2() {
		return vertex2;
	}

	public void setVertex2(Node node2) {
		this.vertex2 = node2;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		return ((Link) obj).getId() == this.id;
	}

	@Override
	public String toString() {
		return "" + id;
	}

	@Override
	public int hashCode() {
		return id;
	}
}
