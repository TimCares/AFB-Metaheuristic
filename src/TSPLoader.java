import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.text.AbstractDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hsh.parser.*;

public class TSPLoader {
    public static double[][] generateTSPMatrix(String xmlFilePath) {
        try {
            // Initialize the XML parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(xmlFilePath));

            // Get the root element
            Element root = doc.getDocumentElement();

            // Get the graph element
            Element graphElement = (Element) root.getElementsByTagName("graph").item(0);

            // Get the number of vertices
            NodeList vertexList = graphElement.getElementsByTagName("vertex");
            int numVertices = vertexList.getLength();

            // Initialize the adjacency matrix
            double[][] adjacencyMatrix = new double[numVertices][numVertices];

            // Fill the adjacency matrix with edge costs
            for (int i = 0; i < numVertices; i++) {
                Element vertex = (Element) vertexList.item(i);
                NodeList edgeList = vertex.getElementsByTagName("edge");
                for (int j = 0; j < numVertices; j++) {
                    double cost;
                    if (i == j) {
                        cost = 0;
                    } else if (j > i) {
                        Element edge = (Element) edgeList.item(j - 1);
                        cost = Double.parseDouble(edge.getAttribute("cost"));
                    } else {
                        Element edge = (Element) edgeList.item(j);
                        cost = Double.parseDouble(edge.getAttribute("cost"));
                    }
                    adjacencyMatrix[i][j] = Math.round(cost);
                }
            }

            return adjacencyMatrix;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double[][] createRandomTSP(Integer size) {
        Random rand = new Random();
        rand.setSeed(42);
        if (size == null) {
            size = rand.nextInt(30);
        }
        double[][] tsp = new double[size][size];

        for (int i = 0; i < tsp.length; i++) {
            for (int j = 0; j <= i; j++) {
                if (i == j) {
                    tsp[i][j] = 0;
                } else {
                    tsp[i][j] = rand.nextDouble() * 100;
                }
                tsp[j][i] = tsp[i][j];
            }
        }
        return tsp;
    }

    public static double[][] readTSP(String filePath) {
        try {
            List<String> lines = new ArrayList<>();

            // Read the file into a list of lines
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }

            // Convert the lines into a 2D array
            double[][] twoDArray = lines.stream()
                    .map(line -> line.trim().replaceAll("\\s{2,}", " ").split("\\s+"))
                    .map(arr -> {
                        double[] row = new double[arr.length];
                        for (int i = 0; i < arr.length; i++) {
                            row[i] = Integer.parseInt(arr[i]);
                        }
                        return row;
                    })
                    .collect(Collectors.toList())
                    .toArray(new double[lines.size()][]);

            return twoDArray;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double[][] generateTSPFromNodes(Node[] nodes) {
        double[][] tsp = new double[nodes.length][nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                if (i == j) {
                    tsp[i][j] = 0;
                } else {
                    tsp[i][j] = nodes[i].distance(nodes[j]);
                }
            }
        }
        return tsp;
    }

    static ArrayList<String> geoProblems = new ArrayList<String>(Arrays.asList(
            "ali535.tsp",
            "bayg29.tsp",
            "bays29.tsp",
            "burma14.tsp",
            "burma14.tsp",
            "gr96.tsp",
            "gr137.tsp",
            "gr202.tsp",
            "gr229.tsp",
            "gr431.tsp",
            "gr666.tsp",
            "ulysses16.tsp",
            "ulysses22.tsp"));

    public static Set<String> listFiles(boolean includeGeoProblems, int maxSize) {
        ArrayList<String> tests = new ArrayList<String>(Arrays.asList(
            "eil101.tsp", "pr1002.tsp", "pr2392.tsp"
        ));
        return Stream.of(Objects.requireNonNull(new File("./data/tsp/").listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .filter(file -> !geoProblems.contains(file))
                .filter(file -> maxSize <= 0 || Integer.parseInt(file.replaceAll("[^0-9]", "")) <= maxSize)
                .filter(file -> tests.contains(file))
                .map((x) -> "./data/tsp/" + x)
                .collect(Collectors.toSet());
    }

    public static Set<String> listFile(String file) {
        Stream<String> stringStream = Stream.of("./data/tsp/" + file);
        return stringStream.collect(Collectors.toSet());
    }

    public static Map<String, Integer> getBestCosts() {
        Map<String, Integer> resultMap = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("./data/tsp_opt.txt"));
            for (String line : lines) {
                String[] parts = line.split(" : ");
                resultMap.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    public static String getProblemName(String fileName) {
        Pattern pattern = Pattern.compile("tsp/(.*).tsp");
        Matcher matcher = pattern.matcher(fileName);
        matcher.find();
        return matcher.group(1);
    }
}
