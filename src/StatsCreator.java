import result_src.AFBResultStats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hsh.parser.Dataset;
import com.hsh.parser.Parser;

public class StatsCreator {
    public void costPerNBirds() throws IOException {
        Random rand = null;
        AFB<int[]> solver = null;
        AFBResultStats<int[]> res = null;

        Set<String> files = TSPLoader.listFiles("ali535.tsp");
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
                        5_000_000,
                        tsp,
                        rand
                        //,0.25
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

        Set<String> files = TSPLoader.listFiles("dsj1000.tsp");
        Dataset dataset = null;
        double[][] tsp = null;

        if (files.size() == 1) {
            System.out.println("Running for one problem...");
            dataset = Parser.read(files.iterator().next());
            tsp = TSPLoader.generateTSPFromNodes(dataset.getNodes());
        }

        for (String path: files) {
            if (files.size() != 1) {
                dataset = Parser.read(files.iterator().next());
                tsp = TSPLoader.generateTSPFromNodes(dataset.getNodes());
            }

            rand = new Random();
            rand.setSeed(42);

            solver = new AFB_TSP_Track(
                    200,
                    0.1589684022681154,//0.01,
                    0.4624556400235943, //0.67,
                    0.33611898159023834, //0.07,
                    0.6979749881176104, //0.75,
                    4_000_000,
                    tsp,
                    rand
                    //,0.25
            );

            res = (AFBResultStats<int[]>) solver.solve();
            System.out.println("Cost for \"" + path + "\": " + res.bestCost);
            times += res.timeInMs;
            to_csv(TSPLoader.getProblemName(path), res);
        }
        System.out.println("Avg. Time: " + times/files.size() + " ms");
    }

    public void to_csv(String problem, AFBResultStats<int[]> results) {
        String fileName = "./data/experiments/" + problem + " " + getCurrTime() + ".csv";

        Double[] data = new Double[results.costOverTime.size()];
        writeLines(fileName, results.costOverTime.toArray(data));
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
}
