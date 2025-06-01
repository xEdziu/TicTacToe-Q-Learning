public class Constants {
    // Liczby 3^0, 3^1, ... 3^8
    public static final int[] POW3 = new int[9];
    // Wszystkie osiem linii zwycięstwa: trzy wiersze, trzy kolumny, dwie przekątne
    public static final int[][] WIN_LINES = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // wiersze
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // kolumny
            {0, 4, 8}, {2, 4, 6}             // przekątne
    };

    static {
        // Wypełniamy tablicę potęg 3
        int val = 1;
        for (int i = 0; i < 9; i++) {
            POW3[i] = val;
            val *= 3;
        }
    }
}
