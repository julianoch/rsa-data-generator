package edu.concordia.rsa.model.input;

import java.util.LinkedHashSet;
import java.util.Set;

public class Request implements Comparable<Request> {
	private int id;
	private Node source;
	private Node destination;
	private int demand;
	
	// Advanced attributes
	private int connections;
	private Set<Request> originalRequests = new LinkedHashSet<>();
	private int shortestPathLength;

	public Request(int id, Node source, Node destination, int demand) {
		this.id = id;
		this.source = source;
		this.destination = destination;
		this.demand = demand;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Node getSource() {
		return source;
	}

	public void setSource(Node source) {
		this.source = source;
	}

	public Node getDestination() {
		return destination;
	}

	public void setDestination(Node destination) {
		this.destination = destination;
	}

	public int getDemand() {
		return demand;
	}

	public void setDemand(int demand) {
		this.demand = demand;
	}

	@Override
	public int compareTo(Request o) {
		if (o.getDemand() != this.getDemand()) return o.getDemand() - this.getDemand();

		if (o.getConnections() != this.getConnections()) return o.getConnections() - this.getConnections();

		return this.getId() - o.id;
	}

	@Override
	public String toString() {
		return id + ":" + demand + ":" + connections;
	}

	@Override
	public boolean equals(Object obj) {
		return ((Request) obj).getId() == this.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public Set<Request> getOriginalRequests() {
		return originalRequests;
	}

	public void addOriginalRequests(Request originalRequest) {
		this.originalRequests.add(originalRequest);
	}

	public void addAllOriginalRequests(Set<Request> originalRequests) {
		this.originalRequests.addAll(originalRequests);
	}

	public int getConnections() {
		return connections;
	}

	public void setConnections(int connections) {
		this.connections = connections;
	}

	public int getShortestPathLength() {
		return shortestPathLength;
	}

	public void setShortestPathLength(int shortestPathLength) {
		this.shortestPathLength = shortestPathLength;
	}

}
