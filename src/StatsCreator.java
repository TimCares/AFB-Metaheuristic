import result_src.AFBResultStats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import com.hsh.parser.Dataset;
import com.hsh.parser.Parser;

public class StatsCreator {
    public void costPerNBirds() throws IOException {
        Random rand = null;
        AFB<int[]> solver = null;
        AFBResultStats<int[]> res = null;

        Set<String> files = TSPLoader.listFile("eil101.tsp");
        Dataset dataset = null;
        double[][] tsp = null;

        if (files.size() == 1) {
            System.out.println("Running for one problem...");
            dataset = Parser.read(files.iterator().next());
            tsp = TSPLoader.generateTSPFromNodes(dataset.getNodes());
        }

        for (String path: files) {
            ArrayList<Double> cost = new ArrayList<>();
            ArrayList<Long> time = new ArrayList<>();

            if (files.size() != 1) {
                dataset = Parser.read(files.iterator().next());
                tsp = TSPLoader.generateTSPFromNodes(dataset.getNodes());
            }

            for (int n_birds=5; n_birds<=1000; n_birds+=5) {
                rand = new Random();
                rand.setSeed(42);

                solver = new AFB_TSP_Track(
                        n_birds,
                        0.1589684022681154,//0.01,
                        0.4624556400235943, //0.67,
                        0.33611898159023834, //0.07,
                        0.6979749881176104, //0.75,
                        200_000,
                        tsp,
                        rand
                        //,0.01
                );
                res = (AFBResultStats<int[]>) solver.solve();
                cost.add(res.bestCost);
                time.add(res.timeInMs);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("./data/experiments/" + TSPLoader.getProblemName(path) + " " + getCurrTime() + "_birds.csv"))) {
                for (Double value : cost) {
                    writer.append(value.toString());
                    writer.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("./data/experiments/" + TSPLoader.getProblemName(path) + " " + getCurrTime() + "_time.csv"))) {
                for (Long value : time) {
                    writer.append(value.toString());
                    writer.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void collectStatistics() throws IOException {
        Random rand = null;
        AFB<int[]> solver = null;
        AFBResultStats<int[]> res = null;
        double times = 0;

        String file = TSPLoader.listFile("pr2392.tsp").iterator().next();
        Dataset dataset = null;
        double[][] tsp = null;

        dataset = Parser.read(file);
        tsp = TSPLoader.generateTSPFromNodes(dataset.getNodes());

        int n_trails_per_problem = 10;
        Double[][] distances = new Double[n_trails_per_problem][4_000_000];

        rand = new Random();
        for (int k=0; k< n_trails_per_problem; k++) {

            solver = new AFB_TSP_TopN_Opt3_S_NN_ES_Track(
                    200,
                    0.1589684022681154,//0.01,
                    0.4624556400235943, //0.67,
                    0.33611898159023834, //0.07,
                    0.6979749881176104, //0.75,
                    4_000_000,
                    tsp,
                    rand
                    ,0.001
                    ,0.5
            );

            res = (AFBResultStats<int[]>) solver.solve();
            System.out.println("Cost for \"" + "pr2392" + "\": " + res.bestCost);
            times += res.timeInMs;
            
        
            res.costOverTime.toArray(distances[k]);
        }

        to_csv(TSPLoader.getProblemName("tsp/pr2392.tsp"), computeColumnMeans(distances));
    }

    public void to_csv(String problem, AFBResultStats<int[]> results) {
        String fileName = "./data/experiments/" + problem + " " + getCurrTime() + ".csv";

        Double[] data = new Double[results.costOverTime.size()];
        writeLines(fileName, results.costOverTime.toArray(data));
    }

    public void to_csv(String problem, Double[] results) {
        String fileName = "./data/experiments/" + problem + " " + getCurrTime() + ".csv";

        writeLines(fileName, results);
    }

    public void writeLines(String fileName, Double[] data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Double value : data) {
                writer.append(value.toString());
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrTime() {
        LocalDateTime timePoint = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy, hh:mm");
        return timePoint.format(formatter);
    }

    public Double[] computeColumnMeans(Double[][] array) {
        int numRows = array.length;
        int numCols = array[0].length;
        Double[] means = new Double[numCols];

        for (int col = 0; col < numCols; col++) {
            double sum = 0;
            for (int row = 0; row < numRows; row++) {
                if (array[row][col] != null) {
                    sum += array[row][col];
                }
            }
            means[col] = sum / numRows;
        }

        return means;
    }
}
