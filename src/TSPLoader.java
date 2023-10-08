import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
                    if (i==j) {
                        cost=0;
                    } else if (j > i) {
                        Element edge = (Element) edgeList.item(j-1);
                        cost = Double.parseDouble(edge.getAttribute("cost"));
                    } else {
                        Element edge = (Element) edgeList.item(j);
                        cost = Double.parseDouble(edge.getAttribute("cost"));
                    }
                    adjacencyMatrix[i][j] = cost;
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
        if (size==null) {
            size = rand.nextInt(30);
        }
        double[][] tsp = new double[size][size];

        for (int i=0; i<tsp.length; i++) {
            for (int j=0; j<=i; j++) {
                if (i==j) {
                    tsp[i][j] = 0;
                } else {
                    tsp[i][j] = rand.nextDouble()*100;
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
}
