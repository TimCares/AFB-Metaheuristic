import result_src.AFBResult;
import result_src.AFBResultStats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        boolean statistics = true;
        if (statistics) {
            collectStatistics();
            return;
        }

        // Pa561 optimal route => 2763
        //String xmlFilePath = "../data/pa561.xml";
        //String xmlFilePath = "../data/pa561.xml";
        String xmlFilePath = "./data/eil101.xml";
        //String xmlFilePath = "../data/rl5934.xml";
        double[][] tsp = TSPLoader.generateTSPMatrix(xmlFilePath);

        System.out.println("Solving...");

        Random rand = new Random();
        AFB<int[]> solver = new AFB_TSP(
            200,
            0.01,
            0.67,
            0.07,
            1.00,
            1000,
            tsp,
            rand
        );

        int repeat = 1;
        double times = 0;
        double distance = 0;
        AFBResult<int[]> res = null;
        for (int i=0; i<repeat; i++) {
            long start = System.currentTimeMillis();
            res = solver.solve();
            times += (System.currentTimeMillis() - start) / 1000F;
            distance += res.bestCost;
        }

        int[] tour = res.bestPosition;
        for (int i=0; i<tour.length; i++) {
            System.out.print(tour[i]);
            if (i != tour.length-1) {
                System.out.print(" -> ");
            }
            if ((i%20)==0 && i!=0) {
                System.out.println();
            }
        }
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        System.out.println();
        System.out.println("Distance: " + distance/repeat);
        System.out.println("Time: " + df.format(times/repeat) + " seconds");
    }

    public static void collectStatistics() {
        Random rand = null;
        AFB<int[]> solver = null;
        AFBResultStats<int[]> res = null;
        double times = 0;

        Set<String> files = listFiles();

        for (String path: files) {
            rand = new Random();
            rand.setSeed(42);

            solver = new AFB_TSP_Track(
                    200,
                    0.01,
                    0.67,
                    0.07,
                    1.00,
                    300000,
                    Objects.requireNonNull(TSPLoader.generateTSPMatrix(path)),
                    rand
            );

            res = (AFBResultStats<int[]>) solver.solve();
            System.out.println("Cost for \"" + path + "\": " + res.bestCost);
            times += res.timeInMs;
            to_csv(path, res);
        }
        System.out.println("Avg. Time: " + times/files.size() + " ms");
    }

    public static void to_csv(String fileName, AFBResultStats<int[]> results) {
        Pattern pattern = Pattern.compile("data/(.*).xml");
        Matcher matcher = pattern.matcher(fileName);
        matcher.find();

        LocalDateTime timePoint = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy, hh:mm");

        fileName = "./data/experiments/" + matcher.group(1) + " " + timePoint.format(formatter) + ".csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Double value : results.costOverTime) {
                writer.append(value.toString());
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Set<String> listFiles() {
        return Stream.of(Objects.requireNonNull(new File("./data/").listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .map((x) -> "./data/" + x)
                .collect(Collectors.toSet());
    }

}
