import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws Exception {
        List<Path> files = Files.list(Paths.get(""))
                .filter(path -> path.toString().matches("^\\d*field\\d+\\.txt$"))
                .collect(Collectors.toList());
        for (Path file : files) {
            int[][] field;
            String fileName = file.toString().replaceAll("\\.txt$", "");
            boolean NINE = !fileName.startsWith("12");
            SudokuSolver.Result result;
            if (NINE) {
                field = Files.lines(file)
                        .map(s -> s.replaceAll(" ", ""))
                        .filter(s -> !s.isEmpty())
                        .map(s -> s.codePoints().map(i -> i - '0').toArray())
                        .toArray(int[][]::new);

                result = new SudokuSolver(fileName, field, 3, 3).solve();
            } else {
                field = Files.lines(file)
                        .map(s -> s.split(" "))
                        .map(intSequence -> IntStream
                                .range(0, 12)
                                .map(index -> Integer.parseInt(intSequence[index]))
                                .toArray())
                        .toArray(int[][]::new);
                result = new SudokuSolver(fileName, field, 4, 3).solve();
            }

            String ans = new GsonBuilder().setPrettyPrinting().create().toJson(result);
            Files.write(Paths.get(fileName + ".solved.txt"), ans.getBytes());

        }

        Files.list(Paths.get("")).filter(f -> f.toString().endsWith(".solved.txt")).forEach(f -> {
            try {
                Files.delete(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
