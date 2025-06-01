import java.util.Random;

        /**
         * Klasa odpowiedzialna za trenowanie agenta Q-learning w grze kółko-krzyżyk 3 × 3.
         */
        public class TicTacToeTrainer {
            /** Agent Q-learning */
            private final QLearningAgent agent;
            /** Liczba epizodów treningowych */
            private final int episodes;
            /** Współczynnik zmniejszania epsilon */
            private final double epsilonDecay;
            /** Minimalna wartość epsilon */
            private final double minEpsilon;

            private final Random rand;

            /**
             * Tworzy nowy obiekt trenera.
             * @param agent agent Q-learning
             * @param episodes liczba epizodów treningowych
             * @param epsilonDecay współczynnik zmniejszania epsilon
             * @param minEpsilon minimalna wartość epsilon
             */
            public TicTacToeTrainer(QLearningAgent agent, int episodes, double epsilonDecay, double minEpsilon) {
                this.agent = agent;
                this.episodes = episodes;
                this.epsilonDecay = epsilonDecay;
                this.minEpsilon = minEpsilon;
                this.rand = new Random();
            }

            /**
             * Przeprowadza proces treningu agenta Q-learning.
             * Agent gra jako '2' (O), a przeciwnik wykonuje losowe ruchy jako '1' (X).
             * Po każdym epizodzie zmniejszany jest epsilon.
             * Co 10000 epizodów wypisywany jest postęp treningu.
             */
            public void train() {

                System.out.println("Rozpoczynam trening agenta Q-learning...");

                /*
                 * Tablica do przechowywania indeksów pustych pól.
                 */
                int[] emptyPos = new int[9];

                for (int ep = 1; ep <= episodes; ep++) {
                    /*
                     * Tworzy jedną instancję planszy, która jest modyfikowana i cofana w trakcie epizodu.
                     */
                    Board board = new Board();

                    /*
                     * Agent zawsze gra jako '2' (O), a losowy przeciwnik jako '1' (X).
                     */
                    byte currentPlayer = 2; // agent zaczyna
                    int prevStateIndex = -1;
                    int prevAction = -1;

                    /*
                     * Pętla pojedynczego epizodu – trwa do zakończenia gry.
                     */
                    while (true) {
                        int stateIndex = board.getStateIndex();

                        if (currentPlayer == 2) {
                            /*
                             * Ruch agenta – wybór akcji i wykonanie.
                             */
                            byte[] fields = board.getFields();
                            int action = agent.chooseAction(stateIndex, fields);
                            board.makeMove(action, (byte) 2);

                            /*
                             * Sprawdzenie, czy agent wygrał.
                             */
                            if (board.isWin((byte) 2)) {
                                /*
                                 * Stan terminalny: agent wygrał – nagroda +1.
                                 */
                                agent.update(stateIndex, action, +1.0, -1);
                                board.undoMove(action);
                                break;
                            }
                            /*
                             * Sprawdzenie remisu.
                             */
                            if (board.isFull()) {
                                /*
                                 * Remis: nagroda 0.
                                 */
                                agent.update(stateIndex, action, 0.0, -1);
                                board.undoMove(action);
                                break;
                            }
                            /*
                             * Gra trwa – zapamiętanie stanu i akcji, zmiana gracza.
                             */
                            prevStateIndex = stateIndex;
                            prevAction = action;
                            currentPlayer = 1;
                        } else {
                            /*
                             * Ruch losowego przeciwnika.
                             */
                            byte[] fields = board.getFields();
                            int numEmpty = board.getEmptyPositions(emptyPos);
                            int move = emptyPos[rand.nextInt(numEmpty)];
                            board.makeMove(move, (byte) 1);

                            /*
                             * Sprawdzenie, czy przeciwnik wygrał.
                             */
                            if (board.isWin((byte) 1)) {
                                /*
                                 * Agent otrzymuje -1 za poprzedni ruch.
                                 */
                                if (prevStateIndex >= 0 && prevAction >= 0) {
                                    int nextIndex = board.getStateIndex();
                                    agent.update(prevStateIndex, prevAction, -1.0, nextIndex);
                                }
                                board.undoMove(move);
                                break;
                            }
                            /*
                             * Sprawdzenie remisu.
                             */
                            if (board.isFull()) {
                                if (prevStateIndex >= 0 && prevAction >= 0) {
                                    agent.update(prevStateIndex, prevAction, 0.0, -1);
                                }
                                board.undoMove(move);
                                break;
                            }
                            /*
                             * Gra trwa – wraca ruch do agenta.
                             */
                            currentPlayer = 2;
                        }
                    }

                    /*
                     * Po zakończeniu epizodu zmniejszany jest epsilon.
                     */
                    agent.decayEpsilon(epsilonDecay, minEpsilon);

                    /*
                     * Raportowanie postępu co 10000 epizodów.
                     */
                    if (ep % 10000 == 0) {
                        System.out.printf("Epizod %d/%d – epsilon=%.5f%n", ep, episodes, agent.getEpsilon());
                    }
                }
                System.out.println("Trening zakończony.");
            }
        }