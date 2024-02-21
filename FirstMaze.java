import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;

public class FirstMaze {

    public static void main(String[] args) {
        Maze maze = new Maze(15); // Creates a grid
        maze.createMazeHuntAndKill(); // Creates a maze
        maze.braiding(5); // 1 / x , how much braiding
        maze.findGreatestDistance(); // Solves the most difficult version of said maze
        System.out.println(maze.toString());
        System.out.println(maze.deadEnds().size());
    }
} // 68, 60, 61 -- 56, 61, 66, 62 -- 50, 52, 54

class Cell {
    public enum Direction { 
        NORTH, SOUTH, WEST, EAST
    }

    private Map<Direction, Cell> adjacent = new EnumMap<>(Direction.class);
    private ArrayList<Cell> linked = new ArrayList<>();
    private int x;
    private int y;
    private int distance = Integer.MAX_VALUE; // For dijsktras
    private boolean optimalPath = false; // For visuals
    private Cell predecessor = null; // For visuals
    private boolean visited = false; // For the hunt and kill algorithm

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addAdjacentCell(Cell cell, Direction direction) {
        adjacent.put(direction, cell);
    }

    public Cell getAdjacentCell(Direction direction) {
        return adjacent.get(direction);
    }

    public boolean hasDirection(Direction direction) {
        return adjacent.containsKey(direction);
    }

    public Map<Direction, Cell> getAllAdjacentCells() {
        return adjacent;
    }

    public ArrayList<Cell> getAdjacentAsList() {
        ArrayList<Cell> adjacentList = new ArrayList<>();
        for (Cell cell : adjacent.values()) {
            if (cell != null) {
                adjacentList.add(cell);
            }
        }
        return adjacentList;
    }

    public void setPredecessor(Cell cell) {
        predecessor = cell;
    }

    public int getIntDistance() {
        return distance;
    }

    public void setOptimalPath(boolean value) {
        optimalPath = value;
    }

    public boolean isOptimalPath() {
        return optimalPath;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public ArrayList<Cell> getUnvisitedNeighbors() {
        ArrayList<Cell> unvisitedNeighbors = new ArrayList<>();
        for (Cell cell : getAdjacentAsList()) {
            if (!cell.isVisited())
                unvisitedNeighbors.add(cell);
        }
        return unvisitedNeighbors;
    }

    public ArrayList<Cell> getVisitedNeighbors() {
        ArrayList<Cell> visitedNeighbors = new ArrayList<>();
        for (Cell neighbor : getAdjacentAsList()) {
            if (neighbor.isVisited()) {
                visitedNeighbors.add(neighbor);
            }
        }
        return visitedNeighbors;
    }

    public void addLink(Cell cell) { // Link the two cells
        if (!linked.contains(cell)) {
            linked.add(cell);
            cell.addLink(this);
        }
    }

    public boolean isLinked(Cell cell) {
        return linked.contains(cell);
    }

    public String getName() {
        return x + ":" + y;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public ArrayList<Cell> getLinked() {
        return linked;
    }

    public Cell getPredecessor() {
        return predecessor;
    }

    public Cell getRandom() {
        return getAdjacentAsList().get(Maze.random.nextInt(getAdjacentAsList().size()));
    }

    public Cell getRandomUnlinked() {
        ArrayList<Cell> adjacentList = getAdjacentAsList();
        Cell cell;
        do {
            cell = adjacentList.get(Maze.random.nextInt(adjacentList.size()));
        } while (cell.isLinked(this));
        return cell;
    }

    public void reset() {
        setDistance(Integer.MAX_VALUE);
        setOptimalPath(false);
        setPredecessor(null);
        setVisited(false);
    }
}

class Maze {
    public static Random random = new Random();
    public Cell[][] maze;
    public int size;
    private boolean numberDisplay = true;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public Cell getCell(int x, int y) {
        return maze[x][y];
    }

    public int getSize() {
        return size;
    }

    public Cell getRandomCell() {
        return getCell(random.nextInt(size), random.nextInt(size));
    }

    public ArrayList<Cell> deadEnds() {
        ArrayList<Cell> numberOfDeadEnds = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (getCell(i, j).getLinked().size() == 1)
                    numberOfDeadEnds.add(getCell(i, j));
            }
        }
        return numberOfDeadEnds;
    }

    public void braiding(int fraction) {
        for (Cell cells : this.deadEnds()) {
            if (random.nextInt(fraction) == 0)
                cells.addLink(cells.getRandomUnlinked());
        }
    }

    public Maze(int size) {
        maze = new Cell[size][size];
        this.size = size;
        // Creates a new maze with a bunch of cells
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                maze[i][j] = new Cell(i, j);
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Add the adjacent cells to the list
                // Make sure to check for boundary conditions
                if (i > 0)
                    maze[i][j].addAdjacentCell(maze[i - 1][j], Cell.Direction.NORTH); // Up
                if (i < size - 1)
                    maze[i][j].addAdjacentCell(maze[i + 1][j], Cell.Direction.SOUTH); // Down
                if (j > 0)
                    maze[i][j].addAdjacentCell(maze[i][j - 1], Cell.Direction.WEST); // Left
                if (j < size - 1)
                    maze[i][j].addAdjacentCell(maze[i][j + 1], Cell.Direction.EAST); // Right
            }
        }
    }

    /**
     * A simple maze generating algoritm, but quite poor
     * Will always leave the right most and up most row empty
     * Will always the leave the optimal solution of wiggeling upwards to the right
     */
    public void createMazeBinaryTree() {
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                Cell currentCell = getCell(i, j);
                ArrayList<Cell> adjacent = new ArrayList<>();
                if (currentCell.hasDirection(Cell.Direction.NORTH))
                    adjacent.add(currentCell.getAdjacentCell(Cell.Direction.NORTH));
                if (currentCell.hasDirection(Cell.Direction.EAST))
                    adjacent.add(currentCell.getAdjacentCell(Cell.Direction.EAST));

                if (adjacent.size() > 0) {
                    int ran = random.nextInt(adjacent.size());
                    Cell adj = adjacent.get(ran);
                    currentCell.addLink(adj);
                }
            }
        }
    }

    /**
     * A simple maze generating algoritm, and a step up from binary
     * Will always leave the up most row empty
     * Still easily beatable with the simple move of going upp wards
     */
    public void createMazeSideWinder() {
        for (Cell[] row : maze) {
            ArrayList<Cell> run = new ArrayList<>();
            for (Cell cell : row) {
                run.add(cell);
                boolean shouldClose = !cell.hasDirection(Cell.Direction.EAST)
                        || (cell.hasDirection(Cell.Direction.NORTH) && random.nextInt(2) == 0);
                if (shouldClose) {
                    Cell member = run.get(random.nextInt(run.size()));
                    if (member.hasDirection(Cell.Direction.NORTH)) {
                        member.addLink(member.getAdjacentCell(Cell.Direction.NORTH));
                        run.clear();
                    }
                } else {
                    cell.addLink(cell.getAdjacentCell(Cell.Direction.EAST));
                }
            }
        }
    }

    /** A truly non-biased maze generating algorithm */
    public void createMazeAldousBroder() {
        Cell currentCell = this.getRandomCell();
        int visited = (size * size) - 1;
        while (visited > 0) {
            Cell adjacent = currentCell.getRandom();
            if (adjacent.getLinked().isEmpty()) {
                currentCell.addLink(adjacent);
                visited--;
            }
            currentCell = adjacent;
        }
    }

    /** Also a non biased maze generating algorithm */
    public void createMazeWilsons() {
        ArrayList<Cell> unvisited = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                unvisited.add(getCell(i, j));
            }
        }

        // Randomly select and remove the first cell from unvisited
        Cell firstCell = unvisited.remove(random.nextInt(unvisited.size()));

        while (!unvisited.isEmpty()) {
            // Start a random walk from a random unvisited cell
            Cell cell = unvisited.get(random.nextInt(unvisited.size()));
            ArrayList<Cell> path = new ArrayList<>();
            path.add(cell);

            while (unvisited.contains(cell)) {
                cell = cell.getRandom(); // Get a random adjacent cell
                int position = path.indexOf(cell);

                if (position != -1) {
                    // Loop detected, trim the path
                    path = new ArrayList<>(path.subList(0, position + 1));
                } else {
                    // No loop, add the cell to the path
                    path.add(cell);
                }
            }

            // Link the cells in the path and remove them from unvisited
            for (int index = 0; index < path.size() - 1; index++) {
                Cell current = path.get(index);
                Cell next = path.get(index + 1);
                current.addLink(next);
                unvisited.remove(current);
            }
        }
    }

    /**
     * Trades biases with actually difficult mazes
     * Will be longer than others and more unbiased turnes
     */
    public void createMazeHuntAndKill() {
        Cell current = getCell(random.nextInt(size), random.nextInt(size));
        current.setVisited(true);

        // Continue until there are unvisited cells
        while (true) {
            // Step 1: Walk
            List<Cell> unvisitedNeighbors = current.getUnvisitedNeighbors();
            if (!unvisitedNeighbors
                    .isEmpty()) {
                Cell neighbor = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                current.addLink(neighbor); // Link current cell with neighbor
                neighbor.setVisited(true); // Mark neighbor as visited
                current = neighbor; // Move to neighbor
            } else {
                // Step 2: Hunt
                boolean found = false;
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        Cell cell = maze[i][j];
                        if (!cell.isVisited() && !cell.getVisitedNeighbors().isEmpty()) {
                            current = cell;
                            current.setVisited(true); // Mark the current cell as visited
                            // Link the current cell to one of its visited neighbors
                            List<Cell> visitedNeighbors = current.getVisitedNeighbors();
                            if (!visitedNeighbors.isEmpty()) {
                                Cell visitedNeighbor = visitedNeighbors.get(random.nextInt(visitedNeighbors.size()));
                                current.addLink(visitedNeighbor);
                                found = true; // Break out of the hunt loop
                                break; // Break inner loop
                            }
                        }
                    }
                    if (found) {
                        break; // Break outer loop
                    }
                }
                if (!found)
                    break;
            }
        }
    }

    /**
     * Trades biases with actually difficult mazes
     * Will be longer than others and more unbiased turnes
     * Will be faster but more memory intensive
     */
    public void createMazeRecursiveBackTracker() {
        Stack<Cell> stack = new Stack<>();
        stack.push(getRandomCell());
        while (!stack.isEmpty()) {
            Cell current = stack.lastElement();
            List<Cell> neighbors = current.getAdjacentAsList().stream()
                    .filter(n -> n.getLinked().isEmpty())
                    .collect(Collectors.toList());
            if (neighbors.isEmpty())
                stack.pop();
            else {
                Cell neighbor = neighbors.get(random.nextInt(neighbors.size()));
                current.addLink(neighbor);
                stack.push(neighbor);
            }
        }
    }

    /**
     * With a generated maze find the firthest distance between two points
     * This will most likley yeild the most difficult maze to solve
     */
    public void findGreatestDistance() {
        Cell start = getCell(0, 0);
        solveDijkstra(start, getCell(size - 1, size - 1));
        start = this.findFarthestCellFrom(start);
        // Found a first node
        this.resetCells();
        solveDijkstra(start, getCell(size - 1, size - 1));
        Cell end = findFarthestCellFrom(start);
        this.resetCells();
        solveDijkstra(end, start);
    }

    public Cell findFarthestCellFrom(Cell start) {
        Cell maxDist = start;
        for (Cell[] cells : maze) {
            for (Cell cell : cells) {
                if (cell.getIntDistance() > maxDist.getIntDistance())
                    maxDist = cell;
            }
        }
        return maxDist;
    }

    public void resetCells() {
        for (Cell[] cells : maze) {
            for (Cell cell : cells) {
                cell.reset();
            }
        }
    }

    @Override
    public String toString() { // To display the maze
        StringBuilder output = new StringBuilder();
        String top = "+---";
        for (int i = 0; i < getSize(); i++) {
            output.append(top);
        }
        output.append("+\n");

        for (Cell[] row : maze) {
            StringBuilder topBuilder = new StringBuilder("|");
            StringBuilder bottomBuilder = new StringBuilder("+");
            for (Cell cell : row) {
                String eastBoundary = cell.isLinked(cell.getAdjacentCell(Cell.Direction.EAST)) ? " " : "|";
                String southBoundary = cell.isLinked(cell.getAdjacentCell(Cell.Direction.SOUTH)) ? "   " : "---";
                if (numberDisplay) {
                    String cellText = cell.isOptimalPath()
                            ? ANSI_GREEN + String.format("%3d", cell.getIntDistance()) + ANSI_RESET
                            : String.format("%3d", cell.getIntDistance());
                    topBuilder.append(cellText).append(eastBoundary);
                } else {
                    topBuilder.append("   ").append(eastBoundary);
                }

                bottomBuilder.append(southBoundary).append("+");
            }
            output.append(topBuilder).append("\n").append(bottomBuilder).append("\n");
        }
        return output.toString();
    }

    public void solveDijkstra(Cell start, Cell end) {
        start.setDistance(0);

        PriorityQueue<Cell> frontier = new PriorityQueue<>(Comparator.comparingInt(Cell::getIntDistance));
        frontier.add(start);

        while (!frontier.isEmpty()) {
            Cell currentCell = frontier.poll();

            for (Cell linkedCell : currentCell.getLinked()) {
                int newDist = currentCell.getIntDistance() + 1;
                if (newDist < linkedCell.getIntDistance()) {
                    linkedCell.setDistance(newDist);
                    linkedCell.setPredecessor(currentCell); // Set the predecessor
                    frontier.add(linkedCell);
                }
            }
        }
        Cell endCell = end;
        while (endCell != null) { // To keep track of the solution path
            endCell.setOptimalPath(true);
            endCell = endCell.getPredecessor();
        }
    }
}
