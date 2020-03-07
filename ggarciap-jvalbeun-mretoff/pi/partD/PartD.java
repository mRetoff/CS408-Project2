import java.util.Set;
import java.util.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.ArrayList;
public class PartD {



        public abstract class Method extends AttributeElement {

                private final Collection<Variable> params;
                private final Collection<Variable> retVars;
                private final boolean isPrivate;

                protected Method(Collection<Variable> params,
                         Collection<Variable> retVars,
                         boolean isPrivate) {
                this.params = params;
                this.retVars = retVars;
                this.isPrivate = isPrivate;
                }

                public Collection<Variable> getParameters() {
                        return params;
                }

                //all parameters. For instance methods, this variable
                public abstract Collection<Variable> getAllParameters();

                public Collection<Variable> getRetVars() {
                        return retVars;
                }

                public abstract boolean isInstance();

                public boolean isStatic() {
                        return !isInstance();
                }

                public boolean isPrivate() { return isPrivate; }
        }



        public abstract class Numberable {

                public abstract int getID();

                @Override
                public final int hashCode() {
                        return getID();
                }

                @Override
                public final boolean equals(Object obj) {
                        return this == obj;
                }

         }

        public abstract class BasicElement extends Numberable {
        }

        public abstract class Variable extends AttributeElement {

        }

        public abstract class Type extends BasicElement {

        }

        /**
        * An instance of this class is a element in points-to analysis
        * that carries some attributes.
        */

        public abstract class AttributeElement extends BasicElement {

                private Map<String, Object> attributes = new HashMap<>(4);

                public boolean hasAttribute(String name) {
                        return attributes.containsKey(name);
                }

                public Object getAttribute(String name) {
                        return attributes.get(name);
                }

                public void setAttribute(String name, Object attr) {
                        attributes.put(name, attr);
                }


                public <T> void addToAttributeSet(String name, T elem) {
                        attributes.putIfAbsent(name, new HashSet<>());
                        ((Set<T>) attributes.get(name)).add(elem);
                }

                public <T> Set<T> getAttributeSet(String name) {
                        return (Set<T>) attributes.getOrDefault(name, Collections.emptySet());
                }
        }

        abstract class Obj extends AttributeElement {

                private final Type type;

                protected Obj(Type type) {
                        this.type = type;
                }

                public Type getType() {
                        return type;
                }

        }

        abstract class ContextComp {

            protected final PointsToAnalysis pta;
            //Need an object for the graph creation
            protected final Map<Method, Integer> method2ctxNumber = new HashMap<>();

            protected ContextComp(PointsToAnalysis pta, ObjectAllocationGraph oag) {
                this.pta = pta;
                this.oag = oag;
                computeContext();
            }

            public int contextNumberOf(Method method) {
                return method2ctxNumber.get(method);
            }

            public abstract String getAnalysisName();

            protected abstract int computeContextNumberOf(Method method);

            private void computeContext() {
                for (Method method : pta.reachableMethods()) {
                    if (method.isInstance()) {
                        method2ctxNumber.put(method, computeContextNumberOf(method));
                    }
                }
            }
        }


        public interface ptoAnalysis{
        // For points-to set and return the object in the point-to analysis
        Set<Obj> allObjects();

        // Objects pointed by variable var
        Set<Obj> pointsToSetOf(Variable var);

        int pointsToSetSizeOf(Variable var);

        //Variables declared
        Set<Variable> variablesDeclaredIn(Method method);

        // For object allocation relations, objects alloceted
        Set<Obj> objectsAllocatedIn(Method method);

        // the callee methods of method.
        Set<Method> calleesOf(Method method);
        // Reachable methods
        Set<Method> reachableMethods();

        Set<Method> methodsInvokedOn(Obj obj);
        //he receiver objects of method
        Set<Obj> receiverObjectsOf(Method method);

        // the type which contains the allocation site of object
        Type declaringAllocationTypeOf(Obj obj);

        Type declaringTypeOf(Method method);

        }


         //Counts the number of time each function is called
        private static HashMap<String, Integer> nodeSupport = new HashMap<String, Integer>();
        //Keeps track of how many times each pair occurs
        private static HashMap<String, Integer> pairSupport = new HashMap<String, Integer>();
        //Keeps track of all function calls within a method
        private static HashMap<String, ArrayList<String>> funcCalls = new HashMap<String, ArrayList<String>>();
        //Keeprs track of the Methods
        private static List<String> MethodList = new ArrayList<String>();

        public static void main(String[] args) {

                if (args.length == 0) return;

                String file = args[0] + ".callgraph";
                String temp, calledFunction;
                int support = 3; //initially set to default values
                double confidence = 65.0;
                boolean expansion = true;
                String[] callgraphNode, functionName;
                Scanner s;

                if (args.length > 1) {
                        support = Integer.parseInt(args[1]);
                        confidence = Double.parseDouble(args[2]);
                        //expansion = Boolean.parseBoolean(args[3]);
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

                                //Temp ArrList to just do node support of missing added
                                HashMap<String, ArrayList<String>> tempAdded = new HashMap<String, ArrayList<String>>();
                                tempAdded.put(functionName[1], new ArrayList<String>());


                                while (s.hasNextLine() && !(temp = s.nextLine()).isEmpty()) {
                                        if (!temp.contains("external")) { //Ignore external calls

                                                calledFunction = temp.substring(37, temp.length() - 1);

                                                if (!funcCalls.get(functionName[1]).contains(calledFunction)) {
                                                        funcCalls.get(functionName[1]).add(calledFunction);

                                                        if (MethodList.contains(calledFunction)){

                                                                for (Map.Entry<String, ArrayList<String>> mapElement : funcCalls.entrySet()) {
                                                                        String key = (String)mapElement.getKey();
                                                                        if (Objects.equals(key, calledFunction)) {
                                                                                for(String arrlistTo : mapElement.getValue()){
                                                                                        //add functions of scope, including the repeated calls
                                                                                        tempAdded.get(functionName[1]).add(arrlistTo);
                                                                                }

                                                                        }
                                                                }


                                                        }

                                                        // Building the list of fucntions inside main
                                                        if (Objects.equals(functionName[1], "main")){
                                                                MethodList.add(calledFunction);
                                                        }

                                                        if (!nodeSupport.containsKey(calledFunction))
                                                                nodeSupport.put(calledFunction, 1);
                                                        else
                                                                nodeSupport.put(calledFunction, nodeSupport.get(calledFunction) + 1);
                                                }
                                        }
                                }

                                if (expansion) {

                                        //Make arrList temp
                                        ArrayList<String> temporAddedList = new ArrayList<String>();
                                        for (Map.Entry<String, ArrayList<String>> mapElement : tempAdded.entrySet()) {
                                                for(String iters : mapElement.getValue()){
                                                        temporAddedList.add(iters);
                                                }
                                        }

                                        //Find if it already contains particual call function
                                        for (Map.Entry<String, ArrayList<String>> mapElement : funcCalls.entrySet()) {
                                                String key = (String)mapElement.getKey();

                                                if (tempAdded.containsKey(key)){
                                                        for(String iters : mapElement.getValue()){
                                                                if (temporAddedList.contains(iters)){
                                                                        temporAddedList.remove(iters);
                                                                }
                                                        }
                                                }
                                        }



                                        /* Debug satements:
                                        System.out.println("Final list of elementes to expand: \n");
                                        System.out.println(Arrays.toString(temporAddedList.toArray()));
                                        System.out.println(" \n");
                                        */

                                        //Compute node support of expansions with HashMap tempAdded
                                        //iterate the hashmap

                                        for(String iters : temporAddedList){

                                                funcCalls.get(functionName[1]).add(iters);
                                                if (!nodeSupport.containsKey(iters)){
                                                        nodeSupport.put(iters, 1);
                                                }else{
                                                        nodeSupport.put(iters, nodeSupport.get(iters) + 1);
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

