import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Parametry agenta
        double alpha = 0.1;          // współczynnik uczenia
        double gamma = 0.9;          // dyskonto
        double epsilon = 0.7;        // początkowe epsilon (70% eksploracji)
        int episodes = 10010000;        // liczba epizodów treningu
        double epsilonDecay = 0.999999; // współczynnik zanikania epsilon
        double minEpsilon = 0.01;    // dolna granica epsilon

        // Tworzymy agenta
        QLearningAgent agent = new QLearningAgent(alpha, gamma, epsilon);
        Scanner scanner = new Scanner(System.in);
        String qTableFilename = "qtable.dat";
        boolean loaded = agent.loadQTable(qTableFilename);

        if (!loaded) {
            System.out.println("Brak zapisanej Q-tablicy. Rozpoczynam trening...");
            TicTacToeTrainer trainer = new TicTacToeTrainer(agent, episodes, epsilonDecay, minEpsilon);
            trainer.train();
            // Zapytajmy, czy zapisać Q-tablicę po treningu

            System.out.println("Trening zakończony. Czy zapisać Q-tablicę? (t/n)");

            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("t") || input.equals("tak")) {
                System.out.println("Zapisuję Q-tablicę...");
                agent.saveQTable(qTableFilename);
            } else {
                System.out.println("Nie zapisano Q-tablicy.");
            }
        } else {
            System.out.println("Wczytano istniejącą Q-tablicę. Pomijam trening.");
        }

        // Uruchamiamy interaktywną rozgrywkę człowiek kontra AI
        TicTacToeGame game = new TicTacToeGame(agent);
        game.play();
    }
}
