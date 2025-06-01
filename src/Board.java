    /**
     * Reprezentuje planszę do gry w kółko-krzyżyk 3 × 3.
     */
    public class Board implements Cloneable {
        /** Stała: rozmiar planszy 3×3 = 9 pól */
        private static final int N = 9;

        /** Wartości pól: 0 = puste, 1 = X, 2 = O */
        private final byte[] fields;

        /**
         * Tworzy nową pustą planszę.
         */
        public Board() {
            fields = new byte[N];
        }

        /**
         * Sprawdza, czy pole o podanym indeksie jest puste.
         * @param pos indeks pola (0-8)
         * @return true, jeśli pole jest puste, false w przeciwnym razie
         */
        public boolean isEmpty(int pos) {
            return fields[pos] == 0;
        }

        /**
         * Wykonuje ruch: wpisuje wartość gracza (1 lub 2) w pole pos., jeśli jest wolne.
         * @param pos indeks pola (0-8)
         * @param player numer gracza (1 = X, 2 = O)
         * @return true, jeśli ruch został wykonany, false, jeśli pole było zajęte lub niepoprawne
         */
        public boolean makeMove(int pos, byte player) {
            if (pos < 0 || pos >= N || fields[pos] != 0) {
                return false;
            }
            fields[pos] = player;
            return true;
        }

        /**
         * Cofnięcie ruchu (ustawia pole z powrotem na puste).
         * @param pos indeks pola do wyczyszczenia (0-8)
         */
        public void undoMove(int pos) {
            fields[pos] = 0;
        }

        /**
         * Sprawdza, czy gracz wygrał.
         * @param player numer gracza (1 = X, 2 = O)
         * @return true, jeśli gracz wygrał, false w przeciwnym razie
         */
        public boolean isWin(byte player) {
            int[][] lines = Constants.WIN_LINES;
            for (int i = 0; i < lines.length; i++) {
                int a = lines[i][0], b = lines[i][1], c = lines[i][2];
                if (fields[a] == player && fields[b] == player && fields[c] == player) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Sprawdza, czy plansza jest pełna (brak pustych pól).
         * @return true, jeśli plansza jest pełna, false w przeciwnym razie
         */
        public boolean isFull() {
            for (int i = 0; i < N; i++) {
                if (fields[i] == 0) return false;
            }
            return true;
        }

        /**
         * Zwraca liczbę pustych pól na planszy.
         * @return liczba pustych pól
         */
        public int countEmpty() {
            int counter = 0;
            for (int i = 0; i < N; i++) {
                if (fields[i] == 0) counter++;
            }
            return counter;
        }

        /**
         * Wypisuje indeksy wszystkich pustych pól do przekazanej tablicy.
         * @param out tablica, do której zostaną wpisane indeksy pustych pól (musi mieć długość co najmniej 9)
         * @return liczba znalezionych pustych pól (użytych slotów w tablicy out)
         */
        public int getEmptyPositions(int[] out) {
            int idx = 0;
            for (int i = 0; i < N; i++) {
                if (fields[i] == 0) {
                    out[idx++] = i;
                }
            }
            return idx;
        }

        /**
         * Oblicza indeks stanu planszy jako liczbę w systemie trójkowym.
         * @return unikalny indeks stanu planszy (0..19682)
         */
        public int getStateIndex() {
            int index = 0;
            for (int i = 0; i < N; i++) {
                index += fields[i] * Constants.POW3[i];
            }
            return index;
        }

        /**
         * Tworzy głęboką kopię planszy.
         * @return nowy obiekt Board z identycznym stanem pól
         */
        @Override
        public Board clone() {
            Board copy = new Board();
            System.arraycopy(this.fields, 0, copy.fields, 0, N);
            return copy;
        }

        /**
         * Zwraca referencję do wewnętrznej tablicy pól (do odczytu).
         * @return tablica pól planszy
         */
        public byte[] getFields() {
            return fields;
        }
    }