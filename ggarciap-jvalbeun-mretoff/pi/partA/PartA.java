import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.ArrayList;

public class PartA {
	//Counts the number of time each function is called
	private static HashMap<String, Integer> nodeSupport = new HashMap<String, Integer>();
	//Keeps track of how many times each pair occurs
	private static HashMap<String, Integer> pairSupport = new HashMap<String, Integer>();
	//Keeps track of all function calls within a method
	private static HashMap<String, ArrayList<String>> funcCalls = new HashMap<String, ArrayList<String>>();

	public static void main(String[] args) {

		if (args.length == 0) return;

		String file = args[0] + ".callgraph";
		String temp, calledFunction;
		int support = 3; //initially set to default values
		double confidence = 65.0;
		String[] callgraphNode, functionName;
		Scanner s;

		if (args.length > 1) {
			support = Integer.parseInt(args[1]);
			confidence = Double.parseDouble(args[2]);
		}

		try {
			s = new Scanner(new File(file));
			while (s.hasNextLine()) {
				temp = goToNextNode(s);
				if (temp.isEmpty())
					break; //End of callgraph
				callgraphNode = temp.split("\\s+");
				functionName = callgraphNode[5].split("\'");
				funcCalls.put(functionName[1], new ArrayList<String>());
				while (s.hasNextLine() && !(temp = s.nextLine()).isEmpty()) {
					if (!temp.contains("external")) { //Ignore external calls
						calledFunction = temp.substring(37, temp.length() - 1);
						if (!funcCalls.get(functionName[1]).contains(calledFunction)) {
							funcCalls.get(functionName[1]).add(calledFunction);
							if (!nodeSupport.containsKey(calledFunction))
								nodeSupport.put(calledFunction, 1);
							else
								nodeSupport.put(calledFunction, nodeSupport.get(calledFunction) + 1);
						}
					}
				}
				getPairs(functionName[1]);
			}
		} catch (FileNotFoundException e) {}
		printBugReport(support, confidence);
	}

	// Jumps to next callgraph node
	public static String goToNextNode(Scanner s) {
		String temp = "";
		while(s.hasNextLine()) {
			temp = s.nextLine();
			if (!temp.contains("null function") && temp.contains("Call graph"))
				break;
		}
		return temp;
	}

	public static void getPairs(String currFunction) {
		String pair;
		for (int i = 0; i < funcCalls.get(currFunction).size(); i++) {
			for (int j = i + 1; j < funcCalls.get(currFunction).size(); j++) {
				//Sort pair alphabetically
				if (funcCalls.get(currFunction).get(i).compareTo(funcCalls.get(currFunction).get(j)) < 0)
					pair = funcCalls.get(currFunction).get(i) + "*" + funcCalls.get(currFunction).get(j);
				else
					pair = funcCalls.get(currFunction).get(j) + "*" + funcCalls.get(currFunction).get(i);
				if (pairSupport.containsKey(pair))
					pairSupport.put(pair, pairSupport.get(pair) + 1);
				else
					pairSupport.put(pair, 1);
			}
		}
	}

	public static void printBugReport(int support, double confidence) {
		double conf1, conf2;
		String[] pair;

		for (String a : pairSupport.keySet()) {
			if (pairSupport.get(a) >= support) {
				pair = a.split("\\*");
				conf1 = (pairSupport.get(a).doubleValue() / nodeSupport.get(pair[0]).doubleValue()) * 100;
				conf2 = (pairSupport.get(a).doubleValue() / nodeSupport.get(pair[1]).doubleValue()) * 100;
				for (String c : funcCalls.keySet()) {
					if (conf1 >= confidence && funcCalls.get(c).contains(pair[0]) && !funcCalls.get(c).contains(pair[1]))
						System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n", pair[0], c, pair[0], pair[1], pairSupport.get(a), conf1);
					if (conf2 >= confidence && funcCalls.get(c).contains(pair[1]) && !funcCalls.get(c).contains(pair[0]))
						System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n", pair[1], c, pair[0], pair[1], pairSupport.get(a), conf2);
				}
			}
		}
	}
}
