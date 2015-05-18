import javafx.util.Pair;

import java.util.*;

public class SudokuSolver {
    private final int[][] field;
    private final String FIELD_NAME;
    private final int FIELD_SIZE;
    private final int BLOCK_WIDTH;
    private final int BLOCK_HEIGHT;
    private int foundSolutionNumber;
    private List<Pair<Pair<Integer, Integer>, List<Integer>>> variants;
    private Result result;

    public SudokuSolver(String fieldName, int[][] field, int blockWidth, int blockHeight) {
        this.field = cloneField(field);
        FIELD_NAME = fieldName;
        BLOCK_WIDTH = blockWidth;
        BLOCK_HEIGHT = blockHeight;
        FIELD_SIZE = blockWidth * blockHeight;
        if (!checkStartField()) throw new RuntimeException("Invalid field!");
    }
    public Result solve() {
        result = createResult();
        foundSolutionNumber = 0;
        findPrimitiveNumbers();
        computeVariants();
        findSolutions(0);
        return result;
    }

    private int[][] cloneField(int[][] field) {
        Objects.requireNonNull(field);
        int[][] result = new int[field.length][];
        for (int i = 0; i < field.length; i++)
            result[i] = field[i].clone();
        return result;
    }

    private boolean checkStartField() {
        if (field.length == 0 || field.length != FIELD_SIZE) return false;

        for (int row = 0; row < FIELD_SIZE; row++) {
            if (field[row] == null || field[row].length != FIELD_SIZE) return false;
            for (int column = 0; column < FIELD_SIZE; column++)
                if (field[row][column] > FIELD_SIZE || field[row][column] < 0 || !checkStartNumber(row, column))
                    return false;
        }

        return true;
    }
    private boolean checkStartNumber(int row, int column) {
        int number = field[row][column];
        return
                number == 0
                || (theSameInARow(row, number) == 1
                && theSameInAColumn(column, number) == 1
                && theSameInASquare(row, column, number) == 1);
    }

    private boolean checkNumber(int row, int column, int number) {
        return
                number == 0
                || (theSameInARow(row, number) == 0
                && theSameInAColumn(column, number) == 0
                && theSameInASquare(row, column, number) == 0);
    }
    private int theSameInARow(int row, int number) {
        int countTheSame = 0;
        for (int column = 0; column < FIELD_SIZE; column++)
            if (field[row][column] == number) countTheSame++;
        return countTheSame;
    }
    private int theSameInAColumn(int column, int number) {
        int countTheSame = 0;
        for (int row = 0; row < FIELD_SIZE; row++)
            if (field[row][column] == number) countTheSame++;
        return countTheSame;
    }
    private int theSameInASquare(int row, int column, int number) {
        int countTheSame = 0;
        row -= row % BLOCK_HEIGHT;
        column -= column % BLOCK_WIDTH;
        for (int i = row; i < row + BLOCK_HEIGHT; i++)
            for (int j = column; j < column + BLOCK_WIDTH; j++)
                if (field[i][j] == number) countTheSame++;
        return countTheSame;
    }

    private void findPrimitiveNumbers() {
        boolean somethingChanged = true;
        while (somethingChanged) {
            somethingChanged = false;
            for (int tempRow = 0; tempRow < FIELD_SIZE; tempRow++)
                FIND_WHERE_ONLY_ONE_NUMBER:
                for (int tempColumn = 0; tempColumn < FIELD_SIZE; tempColumn++) {
                    if (field[tempRow][tempColumn] != 0) continue FIND_WHERE_ONLY_ONE_NUMBER;
                    int currentChanged = 0;
                    int number = -1, row = -1, column = -1;
                    for (int tempNumber = 1; tempNumber <= FIELD_SIZE; tempNumber++)
                        if (checkNumber(tempRow, tempColumn, tempNumber)) {
                            if (++currentChanged > 1) continue FIND_WHERE_ONLY_ONE_NUMBER;
                            number = tempNumber;
                            row = tempRow;
                            column = tempColumn;
                        }
                    if (number != -1) {
                        field[row][column] = number;
                        somethingChanged = true;
                    }
                }
        }
    }
    private void computeVariants() {
        variants = new ArrayList<>();
        for (int row = 0; row < FIELD_SIZE; row++)
            for (int column = 0; column < FIELD_SIZE; column++)
                if (field[row][column] == 0) {
                    List<Integer> variants0 = new ArrayList<>();
                    variants.add(new Pair<>(new Pair<>(row, column), variants0));
                    for (int number = 1; number <= FIELD_SIZE; number++)
                        if (checkNumber(row, column, number))
                            variants0.add(number);
                }
    }
    private void findSolutions(int currentVariantNumber) {
        if (currentVariantNumber == variants.size()) {
            saveSolution();
            return;
        }
        Pair<Pair<Integer, Integer>, List<Integer>> currentPositionAndVariants = variants.get(currentVariantNumber);
        Pair<Integer, Integer> position = currentPositionAndVariants.getKey();
        int row = position.getKey();
        int column = position.getValue();
        List<Integer> currentVariants = currentPositionAndVariants.getValue();
        currentVariants.stream().filter(number -> checkNumber(row, column, number)).forEach(number -> {
            field[row][column] = number;
            findSolutions(currentVariantNumber + 1);
            field[row][column] = 0;
        });
    }

    private Result createResult() {
        return new Result(new Solution(field, FIELD_NAME + " : unsolved"));
    }
    private void saveSolution() {
        result.solutions.add(new Solution(field, FIELD_NAME + " : variant number " + ++foundSolutionNumber));
    }

    public Result getResult() {
        return result;
    }

    public class Result {

        private String name = FIELD_NAME;
        private Solution startField;
        private List<Solution> solutions = new ArrayList<>();

        private Result(Solution startField) {
            this.startField = startField;
        }

        public Solution getStartField() {
            return startField;
        }
        public List<Solution> getSolutions() {
            return Collections.unmodifiableList(solutions);
        }
        public String getName() {
            return name;
        }
    }

    public class Solution {
        private String name;
        private String[] field;

        private Solution(int[][] field, String name) {
            this.name = name;

            int fieldSize = field.length;
            this.field = new String[fieldSize];
            for (int row = 0; row < fieldSize; row++) {
                StringJoiner str = new StringJoiner(" ");
                for (int number : field[row])
                    str.add((number < 10 ? " " : "") + number);
                this.field[row] = str.toString();
            }
        }

        public String getName() {
            return name;
        }
        public String[] getField() {
            return field.clone();
        }
    }
}