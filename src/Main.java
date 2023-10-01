import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        int[][] tsp = readTSP("./data/tsp_3.txt"); // createRandomTSP(10);
        System.out.println("TSP Matrix:");
        System.out.println("--------------------------------------------------");
        for (int[] ints : tsp) {
            for (int j = 0; j < tsp.length; j++) {
                System.out.printf("%4d", ints[j]);
            }
            System.out.println();
        }
        System.out.println("--------------------------------------------------");
        System.out.println();

        System.out.println("Solving...");

        AFB solver = new AFB(10, 0.25, 0.01, 0.67, 0.07, 0.75, 5000, tsp);

        long start = System.currentTimeMillis();
        Object[] res = solver.solve();
        float sec = (System.currentTimeMillis() - start) / 1000F;

        int[] tour = (int[]) res[0];
        for (int i=0; i<tour.length; i++) {
            System.out.print(tour[i]);
            if (i != tour.length-1) {
                System.out.print(" -> ");
            }
            if ((i%20)==0 && i!=0) {
                System.out.println();
            }
        }
        System.out.println();
        System.out.println("Distance: " + (double) res[1]);
        System.out.println("Time: " + sec + " seconds");
    }

    public static int[][] createRandomTSP(Integer size) {
        Random rand = new Random();
        rand.setSeed(42);
        if (size==null) {
            size = rand.nextInt(30);
        }
        int[][] tsp = new int[size][size];

        for (int i=0; i<tsp.length; i++) {
            for (int j=0; j<=i; j++) {
                if (i==j) {
                    tsp[i][j] = 0;
                } else {
                    tsp[i][j] = rand.nextInt(100);
                }
                tsp[j][i] = tsp[i][j];
            }
        }
        return tsp;
    }

    public static int[][] readTSP(String filePath) {
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
            int[][] twoDArray = lines.stream()
                    .map(line -> line.trim().replaceAll("\\s{2,}", " ").split("\\s+"))
                    .map(arr -> {
                        int[] row = new int[arr.length];
                        for (int i = 0; i < arr.length; i++) {
                            row[i] = Integer.parseInt(arr[i]);
                        }
                        return row;
                    })
                    .collect(Collectors.toList())
                    .toArray(new int[lines.size()][]);

            return twoDArray;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}