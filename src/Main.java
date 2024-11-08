import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

        String inputFilePath = "src/input.txt";
        String outputFilePath = "src/output.txt";

        // String inputFilePath = "input.txt";
        // String outputFilePath = "output.txt";

        int[][] initialGrid = readSudoku(inputFilePath);
        System.out.println("bliat");
        SudokuSolver solver = new SudokuSolver(initialGrid);
        int[][] solution = solver.solve();


        printSudoku(solution, outputFilePath);

    }

    private static int[][] readSudoku(String filePath) throws IOException{
        int sudukuGrid[][] = new int[9][9];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int row = 0;
            
            while ((line = br.readLine()) != null && row < 9) {
                String[] values = line.split(" ");
                
                for (int col = 0; col < 9; col++) {
                    if (values[col].equals("-")) {
                        sudukuGrid[row][col] = 0;
                    } else {
                        sudukuGrid[row][col] = Integer.parseInt(values[col]);
                    }
                }
                row++;
            }
        return sudukuGrid;
        }
    }

    private static void printSudoku(int[][] grid, String filePath) {
        for (int[] row : grid) {
            for (int num : row) {
                System.out.print(num + " ");
            }
            System.out.println();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))){
            for (int[] row : grid) {
                for (int num : row) {
                    writer.write(num + " ");
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
class SudokuSolver {

    private static final int SIZE = 9; 
    private static final int SUBGRID_SIZE = 3;
    private static final int POPULATION_SIZE = 100; 
    private static final int GENERATIONS = 1000; 
    private static final double MUTATION_RATE = 0.05; 

    private int[][] initialGrid;
    private boolean[][] fixed;

    public SudokuSolver(int[][] initialGrid) {
        this.initialGrid = initialGrid;
        this.fixed = new boolean[SIZE][SIZE];
        initializeFixedCells();
    }

    private void initializeFixedCells() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (initialGrid[i][j] != 0) {
                    fixed[i][j] = true;
                }
            }
        }
    }

    public int[][] solve() {
        List<int[][]> population = initializePopulation();
        for (int generation = 0; generation < GENERATIONS; generation++) {
            population = evolvePopulation(population);
            if (isSolutionFound(population)) {
                break;
            }
        }
        return getBestSolution(population);
    }

    private List<int[][]> initializePopulation() {
        List<int[][]> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            int[][] individual = generateRandomIndividual();
            population.add(individual);
        }
        return population;
    }

    private int[][] generateRandomIndividual() {
        int[][] individual = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(initialGrid[i], 0, individual[i], 0, SIZE);
        }
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!fixed[row][col]) {
                    individual[row][col] = 1 + new Random().nextInt(SIZE);
                }
            }
        }
        return individual;
    }

    private int fitness(int[][] individual) {
        int score = 0;
        for (int i = 0; i < SIZE; i++) {
            score += uniqueNumbersInRow(individual, i);
            score += uniqueNumbersInColumn(individual, i);
        }
        for (int row = 0; row < SIZE; row += SUBGRID_SIZE) {
            for (int col = 0; col < SIZE; col += SUBGRID_SIZE) {
                score += uniqueNumbersInSubgrid(individual, row, col);
            }
        }
        return score;
    }

    private int uniqueNumbersInRow(int[][] grid, int row) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < SIZE; i++) {
            set.add(grid[row][i]);
        }
        return set.size();
    }

    private int uniqueNumbersInColumn(int[][] grid, int col) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < SIZE; i++) {
            set.add(grid[i][col]);
        }
        return set.size();
    }


    private int uniqueNumbersInSubgrid(int[][] grid, int startRow, int startCol) {
        Set<Integer> set = new HashSet<>();
        for (int row = startRow; row < startRow + SUBGRID_SIZE; row++) {
            for (int col = startCol; col < startCol + SUBGRID_SIZE; col++) {
                set.add(grid[row][col]);
            }
        }
        return set.size();
    }

    private List<int[][]> evolvePopulation(List<int[][]> population) {
        List<int[][]> newPopulation = new ArrayList<>();
        while (newPopulation.size() < POPULATION_SIZE) {
            int[][] parent1 = selectIndividual(population);
            int[][] parent2 = selectIndividual(population);
            int[][] offspring = crossover(parent1, parent2);
            if (Math.random() < MUTATION_RATE) {
                mutate(offspring);
            }
            newPopulation.add(offspring);
        }
        return newPopulation;
    }

    private int[][] selectIndividual(List<int[][]> population) {
        int[][] bestIndividual = null;
        int bestFitness = Integer.MIN_VALUE;
        for (int[][] individual : population) {
            int fitness = fitness(individual);
            if (fitness > bestFitness) {
                bestFitness = fitness;
                bestIndividual = individual;
            }
        }
        return bestIndividual;
    }

    private int[][] crossover(int[][] parent1, int[][] parent2) {
        int[][] offspring = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                offspring[row][col] = Math.random() < 0.5 ? parent1[row][col] : parent2[row][col];
            }
        }
        return offspring;
    }

    private void mutate(int[][] individual) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!fixed[row][col]) {
                    individual[row][col] = 1 + new Random().nextInt(SIZE);
                }
            }
        }
    }

    
    private boolean isSolutionFound(List<int[][]> population) {
        for (int[][] individual : population) {
            if (fitness(individual) == SIZE * SIZE * 3) {
                return true;
            }
        }
        return false;
    }

    private int[][] getBestSolution(List<int[][]> population) {
        int[][] bestSolution = null;
        int bestFitness = Integer.MIN_VALUE;
        for (int[][] individual : population) {
            int fitness = fitness(individual);
            if (fitness > bestFitness) {
                bestFitness = fitness;
                bestSolution = individual;
            }
        }
        return bestSolution;
    }
}
