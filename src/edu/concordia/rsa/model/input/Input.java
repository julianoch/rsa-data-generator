package edu.concordia.rsa.model.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.concordia.rsa.utils.InputDataReader;
import edu.concordia.rsa.utils.InputDataReader.InputDataReaderException;

public class Input {
	public static final boolean aggregate = false;
	public static final boolean directed = false;
	public static final boolean problemRSA = true;

	public static final double SLOT_WIDTH = 12.5;
	public static final double MODULATION_EFFICIENCY = 2;

	private int vertexNumber;

	public int linkNumber;
	public int requestNumber;
	public int aggregatedRequestNumber;
	public int nonAggregatedRequestNumber;
	public int slotNumber;

	public List<Node> nodes;
	public List<Link> links;
	public List<Request> requests;

	private int[][] requestsMatrix;

	public double total_demand;

	public Integer[][] demandMatrix;
	public Integer[] firstRequest;
	public Integer[] lastStartingSlot;

	public Set<Request>[][] nodePairRequestMatrix;

	public int totalConnections = 0;

	public Input(String instance, boolean demandInGbps, boolean requestHasId, boolean generate, double totalLoad, int slotNum) throws Exception {
		InputDataReader reader = new InputDataReader(instance);

		if (generate)
			slotNumber = slotNum;
		else
			slotNumber = reader.readInt();
		vertexNumber = reader.readInt();
		linkNumber = reader.readInt();

		readNodes(reader);
		readLinks(reader);
		computeAdjacentLinks();

		readRequests(reader, demandInGbps, requestHasId, aggregate);
		createSlots();
		computeDemandMatrix();
//		logger.trace("demandMatrix = " + Arrays.deepToString(requestSlotGrid));
//		logger.trace("demandMap = " + demandMap);
		computeFirstRequest();
		computeLastStartingSlot();
	}

	private void readNodes(InputDataReader reader) throws IOException, InputDataReaderException {
		int[] nodeNames = reader.readIntArray();
		nodes = new ArrayList<>(vertexNumber);
		for (int i : nodeNames) {
			nodes.add(new Node(i));
		}
	}

	private void readLinks(InputDataReader reader) throws Exception {
		int[][] linksVertices = reader.readIntArrayArray();
		if (linksVertices.length != linkNumber)
			throw new Exception("Linknum not compatible.");

		links = new ArrayList<>(linkNumber);
		for (int l = 0; l < linkNumber; l++) {
			Node source = nodes.get(linksVertices[l][0]);
			Node destination = nodes.get(linksVertices[l][1]);
			links.add(new Link(l, source, destination));
		}
	}

	private void computeAdjacentLinks() {
		for (Node v : nodes) {
			Set<Link> adjacentLinks = new LinkedHashSet<>(vertexNumber - 1);
			for (Link l : links) {
				if (l.getVertex1().equals(v) || l.getVertex2().equals(v)) {
					adjacentLinks.add(l);
				}
			}
			v.setAdjacentLinks(adjacentLinks);
		}
	}

	private void readRequests(InputDataReader reader, boolean demandInGbps, boolean requestHasId, boolean aggregate) throws IOException, InputDataReaderException {
		int requestStartIndex = requestHasId ? 1 : 0;

		int[][] demand = null;
		if (problemRSA) {
			demand = reader.readIntArrayArray();
			// TODO fix demand array
			for (int i = 0; i < demand.length; i++) {
				demand[i][requestStartIndex]--;
				demand[i][requestStartIndex + 1]--;
			}
			nonAggregatedRequestNumber = demand.length;
			getAggregatedRequests(demand, requestStartIndex);

		} else {
			requestsMatrix = reader.readIntArrayArray();
			for (int i = 0; i < vertexNumber; i++) {
				for (int j = 0; j < vertexNumber; j++) {
					nonAggregatedRequestNumber += requestsMatrix[i][j];
				}
			}
		}
		aggregatedRequestNumber = countAggregatedRequests();

		if (aggregate) {
			requestNumber = aggregatedRequestNumber;
			readAggregatedRequests();
		} else {
			requestNumber = nonAggregatedRequestNumber;
			readNonAggregatedRequests(demand, requestStartIndex);
		}

		if (demandInGbps) {
			for (Request k : requests) {
				k.setDemand(k.getDemand() * 3 / 100);
			}
		}

		for (Request request : requests)
			if (problemRSA)
				total_demand += request.getDemand();
			else
				total_demand += request.getConnections();

		Collections.sort(requests);

		System.out.println("Request count = " + requestNumber);
		System.out.println("Total demand = " + total_demand);
//		logger.trace("requests: " + requests);
	}

	private void readNonAggregatedRequests(int[][] demand, int requestStartIndex) {
		requests = new ArrayList<>(requestNumber);

		for (int i = 0; i < requestNumber; i++) {
			int node1 = demand[i][requestStartIndex];
			int node2 = demand[i][requestStartIndex + 1];
			int volume = demand[i][requestStartIndex + 2];
			int source, destination;
			if (directed) {
				source = node1;
				destination = node2;
			} else {
				source = Math.min(node1, node2);
				destination = Math.max(node1, node2);
			}
			Request request = getRequest(i, source, destination, volume);
			requests.add(request);
		}
	}

	@SuppressWarnings("unchecked")
	private void getAggregatedRequests(int[][] demand, int requestStartIndex) {
		requestsMatrix = new int[vertexNumber][vertexNumber];
		nodePairRequestMatrix = new LinkedHashSet[vertexNumber][vertexNumber];

		for (int i = 0; i < demand.length; i++) {
			int source = demand[i][requestStartIndex];
			int destination = demand[i][requestStartIndex + 1];
			int d = demand[i][requestStartIndex + 2];
			requestsMatrix[source][destination] += d;
			Set<Request> list = nodePairRequestMatrix[source][destination];
			if (list == null) {
				list = new LinkedHashSet<Request>();
				nodePairRequestMatrix[source][destination] = list;
			}
			Request request = getRequest(i, source, destination, d);
			list.add(request);
		}
	}

	private int countAggregatedRequests() {
		int requestId = 0;
		if (directed) {
			for (int i = 0; i < vertexNumber; i++) {
				for (int j = 0; j < vertexNumber; j++) {
					if (requestsMatrix[i][j] != 0) {
						requestId++;
					}
				}
			}
		} else {
			for (int i = 0; i < vertexNumber; i++) {
				for (int j = i + 1; j < vertexNumber; j++) {
					if (requestsMatrix[i][j] != 0 || requestsMatrix[j][i] != 0) {
						requestId++;
					}
				}
			}
		}
		return requestId;
	}

	private void readAggregatedRequests() {
		requests = new ArrayList<>(aggregatedRequestNumber);
		int requestId = 0;
		if (directed) {
			for (int i = 0; i < vertexNumber; i++) {
				for (int j = 0; j < vertexNumber; j++) {
					int volume = requestsMatrix[i][j];
					if (volume != 0) {
						Request request = getRequest(requestId++, i, j, volume);
						request.addAllOriginalRequests(nodePairRequestMatrix[i][j]);
						requests.add(request);
					}
				}
			}
		} else {
			for (int i = 0; i < vertexNumber; i++) {
				for (int j = i + 1; j < vertexNumber; j++) {
					int volume = requestsMatrix[i][j] + requestsMatrix[j][i];
					if (volume != 0) {
						Request request = getRequest(requestId++, i, j, volume);
						request.addAllOriginalRequests(nodePairRequestMatrix[i][j]);
						if (nodePairRequestMatrix[j][i] != null)
							request.addAllOriginalRequests(nodePairRequestMatrix[j][i]);
						requests.add(request);
					}
				}
			}
		}
	}

	private Request getRequest(int requestId, int i, int j, int volume) {
		return getRequest(requestId, i, j, volume, true);
	}

	private Request getRequest(int requestId, int i, int j, int volume, boolean original) {
		Node source = nodes.get(i);
		Node destination = nodes.get(j);
		Request request;
		if (problemRSA) {
			request = new Request(requestId, source, destination, volume);
			request.setConnections(1);
			totalConnections += 1;
		} else {
			request = new Request(requestId, source, destination, 1);
			request.setConnections(volume);
			totalConnections += volume;
		}
		return request;
	}

	private void createSlots() {
		System.out.println("Slot count = " + Integer.toString(slotNumber));
	}

	private void computeDemandMatrix() {
		int guardBand = 0;
		if (problemRSA)
			guardBand = 1;
		demandMatrix = new Integer[requestNumber][slotNumber];
		for (int k = 0; k < requestNumber; k++) {
			for (int s = 0; s < slotNumber - requests.get(k).getDemand(); s++) {
				demandMatrix[k][s] = requests.get(k).getDemand() + guardBand;
			}
			demandMatrix[k][slotNumber - requests.get(k).getDemand()] = requests.get(k).getDemand();
		}
	}

	private void computeFirstRequest() {
		firstRequest = new Integer[slotNumber];

		int k = 0;
		for (int s = 0; s < slotNumber; s++) {
			while (k < requestNumber && demandMatrix[k][s] == null) {
				k++;
			}
			firstRequest[s] = k;
		}
	}

	private void computeLastStartingSlot() {
		lastStartingSlot = new Integer[requestNumber];
		for (int k = 0; k < requestNumber; k++) {
			lastStartingSlot[k] = slotNumber - requests.get(k).getDemand();
		}
	}

	public static void main(String[] args) throws Exception {
		String instance = "resources/Spain.dat";
		Input input = new Input(instance, false, false, false, 0, 0);
		System.out.println("input read successfully");
	}
}
