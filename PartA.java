import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class PartA {
	public static void main(String[] args) {

		if (args.length == 0) return;

		String file = args[0] + ".callgraph";
		String temp;
		int support = 3, confidence = 65; //initially set to default values
		int funcSupport;
		String[] callgraphNode, functionName;
		Scanner s;
		//Counts the number of time each function is called
		HashMap<String, Integer> nodeSupport = new HashMap<String, Integer>();
		//Keeps track of how many times each pair occurs
		HashMap<String[], Integer> pairSupport = new HashMap<String[], Integer>();
		//Keeps track of all method call pairs in each function
		HashMap<String, List<String>> funcPairs = new HashMap<String, List<String>>();

		if (args.length > 1) {
			support = Integer.parseInt(args[1]);
			confidence = Integer.parseInt(args[2]);
		}

		try {
			s = new Scanner(new File(file));
			while (s.hasNextLine()) {
				temp = goToNextNode(s);
				if (temp.isEmpty())
					break;
				callgraphNode = temp.split("\\s+");
				functionName = callgraphNode[5].split("\'");
				funcSupport = Integer.parseInt(callgraphNode[6].substring(6)) - 1; //Ignore call from null function
				nodeSupport.put(functionName[1], funcSupport);
			}
		} catch (FileNotFoundException e) {}
	}

	// Jumps to next callgraph node
	public static String goToNextNode(Scanner s) {
		String temp = "";
		while(s.hasNextLine()) {
			temp = s.nextLine();
			if (!temp.contains("null") && temp.contains("Call graph"))
				break;
		}
		return temp;
	}
}
