import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class PartA {
	public static void main(String[] args) {
		String file = args[0] + ".callgraph";
		String temp;
		int support = 3, confidence = 65;
		Scanner s;
		HashMap<String[2], int> map = new HashMap<String[2], int>();
		if (args.length > 1) {
			support = Integer.parseInt(args[1]);
			confidence = Integer.parseInt(args[2]);
		}

		try {
			s = new Scanner(new File(file));
			while (s.hasNextLine()) {
				goToNextNode(s);
				temp = s.nextLine();
				if (temp.isEmpty()) // If line is empty, jump to next callgraph node
					continue;
				
			}
		} catch (FileNotFoundException e) {}
	}

	// Jumps to next callgraph node
	public static void goToNextNode(Scanner s) {
		String temp;
		while(s.hasNextLine()) {
			temp = s.nextLine();
			if (!temp.contains("null") && temp.contains("Call graph"))
				break;
		}
	}
}
