import result_src.AFBResultStats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatsCreator {
    public void costPerNBirds() {
        Random rand = null;
        AFB<int[]> solver = null;
        AFBResultStats<int[]> res = null;

        Set<String> files = listFiles();

        for (String path: files) {
            ArrayList<Double> cost = new ArrayList<>();
            ArrayList<Long> time = new ArrayList<>();
            for (int n_birds=5; n_birds<=1000; n_birds+=5) {
                rand = new Random();
                rand.setSeed(42);

                solver = new AFB_TSP_Track(
                        n_birds,
                        0.01,
                        0.67,
                        0.07,
                        1.00,
                        200000,
                        Objects.requireNonNull(TSPLoader.generateTSPMatrix(path)),
                        rand
                );
                res = (AFBResultStats<int[]>) solver.solve();
                cost.add(res.bestCost);
                time.add(res.timeInMs);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("./data/experiments/" + getProblemName(path) + "_birds.csv"))) {
                for (Double value : cost) {
                    writer.append(value.toString());
                    writer.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("./data/experiments/" + getProblemName(path) + "_time.csv"))) {
                for (Long value : time) {
                    writer.append(value.toString());
                    writer.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void collectStatistics() {
        Random rand = null;
        AFB<int[]> solver = null;
        AFBResultStats<int[]> res = null;
        double times = 0;

        Set<String> files = listFiles();

        for (String path: files) {
            rand = new Random();
            rand.setSeed(42);

            solver = new AFB_TSP_Track(
                    3,
                    0.1589684022681154,//0.01,
                    0.4624556400235943, //0.67,
                    0.33611898159023834, //0.07,
                    0.6979749881176104, //0.75,
                    20_000_000,
                    Objects.requireNonNull(TSPLoader.generateTSPMatrix(path)),
                    rand
            );

            res = (AFBResultStats<int[]>) solver.solve();
            System.out.println("Cost for \"" + path + "\": " + res.bestCost);
            times += res.timeInMs;
            to_csv(getProblemName(path), res);
        }
        System.out.println("Avg. Time: " + times/files.size() + " ms");
    }

    public void to_csv(String problem, AFBResultStats<int[]> results) {
        LocalDateTime timePoint = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy, hh:mm");

        String fileName = "./data/experiments/" + problem + " " + timePoint.format(formatter) + ".csv";

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

    public Set<String> listFiles() {
        return Stream.of(Objects.requireNonNull(new File("./data/").listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .map((x) -> "./data/" + x)
                .collect(Collectors.toSet());
    }

    public String getProblemName(String fileName) {
        Pattern pattern = Pattern.compile("data/(.*).xml");
        Matcher matcher = pattern.matcher(fileName);
        matcher.find();
        return matcher.group(1);
    }
}
