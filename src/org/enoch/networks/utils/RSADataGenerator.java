package org.enoch.networks.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Julian Enoch (julian.enoch@gmail.com)
 *
 */
public class RSADataGenerator {
	public static final double SLOT_WIDTH = 12.5;
	public static final double MODULATION_EFFICIENCY = 2;

	private static List<int[]> demandList = new ArrayList<>();
	private static double load = 0;
	private static List<Integer> volumeList = new ArrayList<>();
	private static List<int[]> trafficNodePairList;
	private static int volumeIndex;

	public static void main(String[] args) throws Exception {
		int trafficNodesPercentage = 10;
		int vertexNumber = 21;
		double totalLoad = 1000;

		int[][] demand = generateData(trafficNodesPercentage, vertexNumber, totalLoad);

		System.out.println("demand.length " + demand.length);
		System.out.println("load " + load);
		System.out.println(Arrays.deepToString(demand));

	}

	public static int[][] generateData(int trafficNodesPercentage, int vertexNumber, double totalLoad) throws Exception {
		fillVolumeList(totalLoad);

		List<Integer> nodeList = new ArrayList<>();
		for (int node = 0; node < vertexNumber; node++)
			nodeList.add(node);
		Collections.shuffle(nodeList);

		int trafficNodesNumber = (int) Math.ceil(vertexNumber * trafficNodesPercentage / 100.);
		
		int trafficNodePairNumber = trafficNodesNumber * (trafficNodesNumber - 1);
		trafficNodePairList = new ArrayList<>(trafficNodePairNumber);
		for (int i = 0; i < trafficNodesNumber; i++) {
			for (int j = 0; j < trafficNodesNumber; j++) {
				if (i != j) trafficNodePairList.add(new int[] { nodeList.get(i), nodeList.get(j) });
			}
		}

		Collections.shuffle(trafficNodePairList);

		for (int i = 0; i < trafficNodePairNumber; i++) {
			try {
				addRequest(i);
			} catch (IndexOutOfBoundsException e) {
				throw new Exception("The offered load is too low");
			}
		}

		while (volumeIndex < volumeList.size()) {
			Collections.shuffle(trafficNodePairList);
			int i = 0;
			while (volumeIndex < volumeList.size() && i < trafficNodePairNumber) {
				addRequest(i++);
			}
		}

		int[][] demand = new int[demandList.size()][];
		for (int i = 0; i < demandList.size(); i++) {
			demand[i] = demandList.get(i);
		}

		return demand;
	}

	private static void fillVolumeList(double totalLoad) {
		double requests100 = Math.ceil(totalLoad * 7 / 1000);
		double requests200 = Math.ceil(totalLoad * 2 / 2000);
		double requests400 = Math.ceil(totalLoad / 4000);
		for (int i = 0; i < requests400; i++)
			volumeList.add(400);
		for (int i = 0; i < requests200; i++)
			volumeList.add(200);
		for (int i = 0; i < requests100; i++)
			volumeList.add(100);
	}

	private static void addRequest(int i) {
		int[] nodePair = trafficNodePairList.get(i);
		int source = nodePair[0];
		int destination = nodePair[1];
		Integer volume = volumeList.get(volumeIndex++);
		int[] request = new int[3];
		request[0] = source;
		request[1] = destination;
		request[2] = (int) (volume / (SLOT_WIDTH * MODULATION_EFFICIENCY));
		load += volume;
		demandList.add(request);
	}

}
