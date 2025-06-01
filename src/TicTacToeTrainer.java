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
             * Sprawdza, czy przeciwnik może wygrać w następnym ruchu.
             * @param board aktualny stan planszy
             * @param oppPlayer numer przeciwnika (1 lub 2)
             * @return true, jeśli przeciwnik może wygrać w następnym ruchu, false w przeciwnym razie
             */
            private boolean willOpponentWinNext(Board board, byte oppPlayer) {
                int[] emptyPos = new int[9];
                int numEmpty = board.getEmptyPositions(emptyPos);
                for (int i = 0; i < numEmpty; i++) {
                    int pos = emptyPos[i];
                    board.makeMove(pos, oppPlayer);
                    boolean win = board.isWin(oppPlayer);
                    board.undoMove(pos);
                    if (win) return true;
                }
                return false;
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

                    // Losuj, kto zaczyna: 1 = X, 2 = O
                    byte agentPlayer = (rand.nextBoolean() ? (byte)1 : (byte)2);
                    byte oppPlayer = (agentPlayer == 1) ? (byte)2 : (byte)1;
                    byte currentPlayer = 1; // X zawsze zaczyna

                    int prevStateIndex = -1;
                    int prevAction = -1;

                    /*
                     * Pętla pojedynczego epizodu – trwa do zakończenia gry.
                     */
                    while (true) {
                        int stateIndex = board.getStateIndex();

                        if (currentPlayer == agentPlayer) {
                            /*
                             * Ruch agenta – wybór akcji i wykonanie.
                             */
                            byte[] fields = board.getFields();
                            int action = agent.chooseAction(stateIndex, fields);
                            board.makeMove(action, agentPlayer);

                            /*
                             * Sprawdzenie, czy agent wygrał.
                             */
                            if (board.isWin(agentPlayer)) {
                                /*
                                 * Stan terminalny: agent wygrał – nagroda +1.
                                 */
                                agent.update(stateIndex, action, +1.0, -1);
                                board.undoMove(action);
                                break;
                            }

                            /*
                             * Kara za dopuszczenie do natychmiastowej wygranej przeciwnika
                             */
                            if (willOpponentWinNext(board, oppPlayer)) {
                                agent.update(stateIndex, action, -0.8, -1); // kara -0.8
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
                            currentPlayer = oppPlayer;

                        } else {
                            /*
                             * Ruch losowego przeciwnika.
                             */
                            int numEmpty = board.getEmptyPositions(emptyPos);
                            int move = emptyPos[rand.nextInt(numEmpty)];
                            board.makeMove(move, oppPlayer);

                            /*
                             * Sprawdzenie, czy przeciwnik wygrał.
                             */
                            if (board.isWin(oppPlayer)) {
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
                            currentPlayer = agentPlayer;
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