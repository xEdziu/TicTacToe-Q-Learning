import java.util.Random;
import java.util.Scanner;

public class TicTacToeGame {
    private final QLearningAgent agent;
    private final Scanner scanner;
    private final Random rand;

    public TicTacToeGame(QLearningAgent agent) {
        this.agent = agent;
        this.scanner = new Scanner(System.in);
        this.rand = new Random();
    }

    public void play() {
        System.out.println("=== Kółko-Krzyżyk (X = Ty, O = AI) ===");
        System.out.println("Wybierz pola 0–8 według wzoru:\n0 1 2\n3 4 5\n6 7 8\n");

        while (true) {
            Board board = new Board();
            byte currentPlayer = 1; // człowiek (X) zaczyna

            // Tablica pomocnicza do wolnych pól
            int[] emptyPos = new int[9];

            while (true) {
                printBoard(board);
                if (currentPlayer == 1) {
                    // Ruch gracza
                    System.out.print("> Twój ruch (0-8): ");
                    int move;
                    try {
                        move = scanner.nextInt();
                    } catch (Exception e) {
                        System.out.println("Niepoprawny input – wpisz cyfrę 0–8.");
                        scanner.nextLine();
                        continue;
                    }
                    if (move < 0 || move >= 9 || !board.isEmpty(move)) {
                        System.out.println("To pole jest niedostępne, spróbuj ponownie.");
                        continue;
                    }
                    board.makeMove(move, (byte)1);
                    if (board.isWin((byte)1)) {
                        printBoard(board);
                        System.out.println("Gratulacje, wygrałeś!");
                        break;
                    }
                    if (board.isFull()) {
                        printBoard(board);
                        System.out.println("Remis!");
                        break;
                    }
                    currentPlayer = 2;
                } else {
                    // Ruch AI
                    byte[] fields = board.getFields();
                    int stateIndex = board.getStateIndex();
                    int action = agent.chooseAction(stateIndex, fields);
                    if (!board.makeMove(action, (byte)2)) {
                        // Zabezpieczenie na wypadek dziwnej sytuacji – wylosuj wówczas pole
                        int numEmpty = board.getEmptyPositions(emptyPos);
                        action = emptyPos[rand.nextInt(numEmpty)];
                        board.makeMove(action, (byte)2);
                    }
                    System.out.println("AI wybrało pole: " + action);
                    if (board.isWin((byte)2)) {
                        printBoard(board);
                        System.out.println("Niestety, AI wygrało. Spróbuj ponownie!");
                        break;
                    }
                    if (board.isFull()) {
                        printBoard(board);
                        System.out.println("Remis!");
                        break;
                    }
                    currentPlayer = 1;
                }
            }

            System.out.print("Zagrać jeszcze raz? (t/n): ");
            String odp = scanner.next();
            if (!odp.toLowerCase().startsWith("t")) {
                System.out.println("Do zobaczenia!");
                break;
            }
        }
        scanner.close();
    }

    // Wyświetlenie planszy w konsoli
    private void printBoard(Board board) {
        byte[] f = board.getFields();
        System.out.println();
        for (int i = 0; i < 9; i++) {
            char c = '.';
            if (f[i] == 1) c = 'X';
            else if (f[i] == 2) c = 'O';
            System.out.print(c + " ");
            if (i % 3 == 2) System.out.println();
        }
        System.out.println();
    }
}
