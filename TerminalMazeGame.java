import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TerminalMazeGame {

    static final int WIDTH = 31;
    static final int HEIGHT = 15;
    static char[][] maze;
    static int playerX, playerY;
    static int exitX, exitY;
    static List<int[]> enemies = new ArrayList<>();
    static boolean gameOver = false;
    static boolean moved = false;
    static long firstMoveTime = 0;
    static boolean enemySpawned = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        while (true) {
            generateMaze();
            runGame();
        }
    }

    static void generateMaze() {
        maze = new char[HEIGHT][WIDTH];
        Random rand = new Random();


        for (int y = 0; y < HEIGHT; y++)
            for (int x = 0; x < WIDTH; x++)
                maze[y][x] = '|';

        carve(1, 1);


        playerX = 1;
        playerY = 1;
        maze[playerY][playerX] = '@';


        exitX = WIDTH - 2;
        exitY = HEIGHT - 2;
        maze[exitY][exitX] = 'E';

        enemies.clear();
        gameOver = false;
        moved = false;
        enemySpawned = false;
        firstMoveTime = 0;
    }

    static void carve(int x, int y) {
        maze[y][x] = ' ';
        int[][] dirs = {{0,-2},{0,2},{2,0},{-2,0}};
        shuffle(dirs);

        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx > 0 && nx < WIDTH-1 && ny > 0 && ny < HEIGHT-1 && maze[ny][nx] == '|') {
                maze[y + d[1]/2][x + d[0]/2] = ' ';
                carve(nx, ny);
            }
        }
    }

    static void shuffle(int[][] dirs) {
        Random rand = new Random();
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int[] tmp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = tmp;
        }
    }

    static void runGame() throws IOException, InterruptedException {
        Thread inputThread = new Thread(() -> {
            try {
                while (!gameOver) {
                    if (System.in.available() > 0) {
                        int key = System.in.read();
                        moved = false;
                        switch (key) {
                            case 'w': movePlayer(0,-1); break;
                            case 's': movePlayer(0,1); break;
                            case 'a': movePlayer(-1,0); break;
                            case 'd': movePlayer(1,0); break;
                        }
                        if (moved && firstMoveTime == 0)
                            firstMoveTime = System.currentTimeMillis();
                    }
                    Thread.sleep(10);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        while (!gameOver) {
            clearScreen();
            printMaze();


            if (!enemySpawned && firstMoveTime > 0 && System.currentTimeMillis() - firstMoveTime > 1000) {
                spawnEnemy();
                enemySpawned = true;
            }

            moveEnemies();
            checkCollision();

            Thread.sleep(150);
        }

        System.out.println("Game Over! Press Enter to continue...");
        System.in.read();
    }

    static void movePlayer(int dx, int dy) {
        int nx = playerX + dx;
        int ny = playerY + dy;
        if (maze[ny][nx] != '|') {
            maze[playerY][playerX] = ' ';
            playerX = nx;
            playerY = ny;
            maze[playerY][playerX] = '@';
            moved = true;
        }
        if (playerX == exitX && playerY == exitY)
            generateMaze();
    }

    static void spawnEnemy() {

        Random rand = new Random();
        int bestX = 0, bestY = 0;
        int maxDist = -1;

        for (int y = 1; y < HEIGHT - 1; y++) {
            for (int x = 1; x < WIDTH - 1; x++) {
                if (maze[y][x] == ' ' && !(x == playerX && y == playerY)) {
                    int dist = Math.abs(playerX - x) + Math.abs(playerY - y);
                    if (dist > maxDist) {
                        maxDist = dist;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }

        enemies.add(new int[]{bestX, bestY});
    }

    static void moveEnemies() {
        for (int[] e : enemies) {
            if (e[0] < playerX && maze[e[1]][e[0]+1] != '|') e[0]++;
            else if (e[0] > playerX && maze[e[1]][e[0]-1] != '|') e[0]--;
            if (e[1] < playerY && maze[e[1]+1][e[0]] != '|') e[1]++;
            else if (e[1] > playerY && maze[e[1]-1][e[0]] != '|') e[1]--;
        }
    }

    static void checkCollision() {
        for (int[] e : enemies) {
            if (e[0] == playerX && e[1] == playerY)
                gameOver = true;
        }
    }

    static void printMaze() {
        char[][] display = new char[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++)
            System.arraycopy(maze[y], 0, display[y], 0, WIDTH);

        for (int[] e : enemies)
            display[e[1]][e[0]] = 'X';

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++)
                System.out.print(display[y][x]);
            System.out.println();
        }
    }

    static void clearScreen() {

        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
