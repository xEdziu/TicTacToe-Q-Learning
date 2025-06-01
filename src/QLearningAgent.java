import java.io.*;
import java.util.Random;

/**
 * Agent Q-learning do gry w kółko-krzyżyk 3 × 3.
 * Uczy się optymalnej strategii na podstawie tablicy Q.
 */
public class QLearningAgent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Liczba możliwych stanów w 3 × 3 tic-tac-toe = 3^9 = 19683 */
    private static final int NUM_STATES = 19683;
    /** Liczba wszystkich pól = 9 (ilość możliwych akcji) */
    private static final int NUM_ACTIONS = 9;

    /** Główna tablica Q: Q[stateIndex][action] */
    private final double[][] qTable;

    /** Współczynnik uczenia */
    private final double alpha;
    /** Współczynnik dyskonta */
    private final double gamma;
    /** Parametr epsilon-greedy (maleje w czasie) */
    private double epsilon;

    private final Random rand;

    /**
     * Tworzy nowego agenta Q-learning.
     * @param alpha współczynnik uczenia
     * @param gamma współczynnik dyskonta
     * @param epsilon parametr eksploracji
     */
    public QLearningAgent(double alpha, double gamma, double epsilon) {
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.rand = new Random();
        qTable = new double[NUM_STATES][NUM_ACTIONS];
    }

    /**
     * Wybiera akcję dla danego stanu metodą epsilon-greedy.
     * @param boardIndex indeks stanu planszy (Board.getStateIndex())
     * @param fields tablica pól planszy
     * @return indeks wybranego ruchu (0-8)
     */
    public int chooseAction(int boardIndex, byte[] fields) {
        double[] qValues = qTable[boardIndex];
        int numEmpty = 0;
        int[] emptyPos = new int[9];
        for (int i = 0; i < 9; i++) {
            if (fields[i] == 0) {
                emptyPos[numEmpty++] = i;
            }
        }
        if (rand.nextDouble() < epsilon) {
            return emptyPos[rand.nextInt(numEmpty)];
        }
        int bestAction = emptyPos[0];
        double bestQ = qValues[bestAction];
        for (int k = 1; k < numEmpty; k++) {
            int a = emptyPos[k];
            double q = qValues[a];
            if (q > bestQ) {
                bestQ = q;
                bestAction = a;
            }
        }
        return bestAction;
    }

    /**
     * Aktualizuje wartość Q po pojedynczym kroku.
     * Q(s,a) ← Q(s,a) + α * (r + γ * max_a' Q(s',a') - Q(s,a))
     * @param stateIndex indeks bieżącego stanu
     * @param action wykonana akcja
     * @param reward nagroda
     * @param nextStateIndex indeks następnego stanu (lub -1, jeśli terminalny)
     */
    public void update(int stateIndex, int action, double reward, int nextStateIndex) {
        double oldQ = qTable[stateIndex][action];
        double maxQNext = 0.0;
        if (nextStateIndex >= 0) {
            double[] qNext = qTable[nextStateIndex];
            maxQNext = qNext[0];
            for (int i = 1; i < NUM_ACTIONS; i++) {
                double v = qNext[i];
                if (v > maxQNext) maxQNext = v;
            }
        }
        double newQ = oldQ + alpha * (reward + gamma * maxQNext - oldQ);
        qTable[stateIndex][action] = newQ;
    }

    /**
     * Zmniejsza parametr epsilon (np. po każdej epoce).
     * @param factor współczynnik zmniejszania
     * @param minEpsilon minimalna wartość epsilon
     */
    public void decayEpsilon(double factor, double minEpsilon) {
        epsilon *= factor;
        if (epsilon < minEpsilon) {
            epsilon = minEpsilon;
        }
    }

    /**
     * Zapisuje tablicę Q do pliku binarnego (serializacja).
     * @param filename ścieżka do pliku
     */
    public void saveQTable(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(qTable);
            System.out.println("Zapisano Q-table do: " + filename);
        } catch (IOException e) {
            System.err.println("Błąd zapisu Q-table: " + e.getMessage());
        }
    }

    /**
     * Wczytuje tablicę Q z pliku binarnego (serializacja).
     * @param filename ścieżka do pliku
     * @return true, jeśli wczytano poprawnie, false w przeciwnym razie
     */
    public boolean loadQTable(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            if (obj instanceof double[][] loaded) {
                if (loaded.length == NUM_STATES && loaded[0].length == NUM_ACTIONS) {
                    for (int i = 0; i < NUM_STATES; i++) {
                        System.arraycopy(loaded[i], 0, qTable[i], 0, NUM_ACTIONS);
                    }
                    System.out.println("Wczytano Q-table z: " + filename);
                    return true;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Nie udało się wczytać Q-table: " + e.getMessage());
        }
        return false;
    }

    /**
     * Zwraca bieżącą wartość epsilon (np. do logowania).
     * @return wartość epsilon
     */
    public double getEpsilon() {
        return epsilon;
    }
}