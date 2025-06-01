# Projekt: Gra Kółko-Krzyżyk (3×3) kontra AI z użyciem Q-Learning

## 1. Wstęp

Celem niniejszego projektu jest implementacja gry Kółko-Krzyżyk (3×3) z inteligentnym przeciwnikiem opartym na algorytmie Q-Learning. Rozwiązanie powstało w języku Java, z naciskiem na maksymalną wydajność obliczeń (minimalizacja alokacji obiektów, praca na tablicach prymitywnych zamiast struktur dynamicznych). Projekt został przygotowany w ramach zaliczenia przedmiotu „Systemy Sztucznej Inteligencji” na Politechnice Wrocławskiej.

W plikach źródłowych znajdują się klasy:

* `Constants.java` – zawiera stałe (tablica potęg 3, linie zwycięstwa).
* `Board.java` – reprezentacja planszy (tablica `byte[9]`), operacje na polach, generowanie indeksu stanu w systemie trójkowym.
* `QLearningAgent.java` – logika Q-Learningu, tablica `double[19683][9]`, metody wyboru akcji i aktualizacji wartości Q.
* `TicTacToeTrainer.java` – trening agenta metodą self-play kontra losowy gracz, minimalizacja alokacji w pętli.
* `TicTacToeGame.java` – interaktywny interfejs konsolowy (człowiek kontra AI).
* `Main.java` – punkt wejścia, próba wczytania Q-tablicy (jeśli istnieje), trening (jeśli brak pliku), uruchomienie gry.

W poniższym dokumencie omówiono:

1. Podstawy teoretyczne (formuła Q-Learningu, reprezentacja stanu w systemie trójkowym).
2. Struktura projektu (opis poszczególnych klas, pełne nazwy plików).
3. Omówienie hiperparametrów i wzorów użytych w algorytmie.

---

## 2. Wprowadzenie teoretyczne

### 2.1. Gra Kółko-Krzyżyk (Tic-Tac-Toe 3×3)

Kółko-Krzyżyk to prosta gra na planszy 3×3, w której dwóch graczy (oznaczanych symbolami „X” oraz „O”) na przemian wstawia swoje symbole w wolne pola. Wygrywa ten, który pierwszy ustawi trzy swoje symbole w jednej linii (poziomej, pionowej lub ukośnej). Jeśli wszystkie pola zostaną zapełnione, a żaden z graczy nie uzyska trzech w rzędzie – następuje remis.

Stan planszy można zapisać jako tablicę dziewięciu pól, gdzie każde pole przyjmuje jedną z wartości:

* 0 – puste
* 1 – symbol X (człowiek lub przeciwnik losowy)
* 2 – symbol O (agent Q-Learning)

Łączna liczba możliwych układów pól to $3^9 = 19\,683$. Dzięki temu można zbudować tablicę Q o wymiarach (liczba stanów) × (liczba akcji) = 19 683 × 9.

### 2.2. Q-Learning (uczenie ze wzmocnieniem)

Q-Learning to algorytm uczenia ze wzmocnieniem, w którym agent dąży do maksymalizacji oczekiwanej sumy zdyskontowanych nagród, ucząc się na podstawie interakcji ze środowiskiem. W każdej chwili agent znajduje się w stanie $s$, wybiera akcję $a$, przechodzi do nowego stanu $s'$ i otrzymuje nagrodę $r$. Algorytm utrzymuje tablicę wartości $Q (s, a) $, która jest sukcesywnie aktualizowana.

Kluczowa formuła aktualizacji to:

$$
Q(s,a) \leftarrow Q(s,a) + \alpha \bigl(r + \gamma \max_{a'} Q(s',a') - Q(s,a)\bigr)
$$

gdzie:

* $\alpha \in (0,1] $ – współczynnik uczenia (learning rate)
* $\gamma \in [0,1] $ – współczynnik dyskontowania przyszłych nagród (discount factor)
* $r$ – natychmiastowa nagroda za wykonanie akcji $a$ w stanie $s$
* $\max_{a'} Q (s',a') $ – maksymalna przewidywana wartość dla przyszłego stanu $s'$
* $Q (s',a') $ – poprzednia wartość funkcji Q dla stanu $s$ i akcji $a$
* $\epsilon$ – parametr eksploracji w strategii ε-greedy (wybór losowej akcji z prawdopodobieństwem $\epsilon$, w przeciwnym razie akcji maksymalizującej $Q (s, a) $)

#### 2.2.1. Funkcja nagrody

W grze Tic-Tac-Toe nagroda $r$ jest definiowana następująco:

* $r = +1$, jeśli agent (O) wygrał partię (osiągnął trzy O w linii).
* $r = -1$, jeśli agent (O) przegrał (X uzyskało trzy w linii).
* $r = 0$ w przypadku remisu lub podczas ruchów niekończących gry (w prostym wariancie nie stosujemy kar za długość gry).
* Dodatkowo, w przypadku gdy następny ruch agenta spowoduje możliwość wygranej przeciwnika, agent otrzymuje nagrodę $r = -0{,}8$ za ostatni ruch, co zmusza go do unikania przegranej.

Nagrodę wypłaca się tylko w stanach terminalnych (po zakończeniu partii). W pozostałych krokach wartość nagrody wynosi 0, a aktualizacja Q opiera się na obserwacji $\max_{a'} Q (s',a') $.

#### 2.2.2. Strategia ε-greedy

Aby agent unikał utknięcia w lokalnie optymalnej polityce, w każdej decyzji ruchu stosuje się:

* z prawdopodobieństwem $\epsilon$ – eksploracja: wybór losowej akcji spośród wolnych pól,
* z prawdopodobieństwem $1 - \epsilon$ – eksploatacja: wybór akcji maksymalizującej wartość $Q (s, a) $ spośród dozwolonych ruchów.

Z czasem parametr $\epsilon$ jest zmniejszany według reguły:

$$
\epsilon \leftarrow \epsilon \times \text{(faktor zanikania)}
$$

Dodatkowo wprowadzono dolną granicę $\epsilon_{\min} $, np. $0 {,} 01$, aby agent nie spadł całkowicie do czystej eksploatacji.

---

## 3. Reprezentacja stanu i akcji

### 3.1. Kodowanie stanu w postaci liczby w systemie trójkowym

Każde z dziewięciu pól może przyjmować wartości $\{0, 1, 2\}$. Stan planszy traktujemy jako wektor długości 9 w bazie 3:

$$
(\text{fields}[0], \,\text{fields}[1], \dots, \text{fields}[8]),
$$

a jego indeks w tablicy Q obliczamy jako:

$$
\text{stateIndex} = \sum_{i=0}^{8} \bigl(\text{fields}[i]\bigr) \times 3^i.
$$

Tablica potęg 3 (3^0, 3^1, …, 3^8) jest przygotowana w klasie `Constants`. Wynikiem tej sumy jest liczba całkowita w przedziale $[0, 19682]$. Dzięki temu można zainicjalizować tablicę:

19683 (liczba stanów) × 9 (liczba akcji).

### 3.2. Zbiór akcji

W stanie $s$ (aktualnej planszy) dostępne akcje to puste pola:

* Jeśli $\text{fields}[i] = 0$, to akcja „umieść symbol O (2) w polu i” jest dozwolona.
* Jeśli $\text{fields}[i] \neq 0$, to akcja jest nielegalna i nie bierze udziału w wyborze.

Podczas wyboru ruchu agent:

1. Zbiera wszystkie indeksy i, dla których fields\[i] = 0, do tablicy pomocniczej.
2. Jeśli wylosuje eksplorację (z prawdopodobieństwem $\epsilon$), to wybiera losowo jeden indeks z tej tablicy.
3. W przeciwnym razie iteruje po tej tablicy i wybiera indeks a, dla którego wartość Q(s,a) jest największa.

---

## 4. Struktura projektu i opis plików

Poniżej znajduje się lista plików źródłowych wraz z opisem odpowiedzialności każdej klasy:

```
Constants.java
Board.java
QLearningAgent.java
TicTacToeTrainer.java
TicTacToeGame.java
Main.java
```

### 4.1. Constants.java

Zawiera dwie tablice statyczne:

1. Tablica potęg liczby 3: 3^0, 3^1, …, 3^8.
2. Lista ośmiu linii zwycięstwa (wiersze, kolumny i przekątne), każda reprezentowana jako trójka indeksów pól.

Dzięki temu metody obliczania stanu i sprawdzania zwycięstwa są uproszczone, a same tablice inicjalizowane są tylko raz podczas ładowania klasy.

### 4.2. Board.java

Reprezentuje planszę za pomocą tablicy `byte[9]`, w której pola mogą mieć wartości 0,1 lub 2. Klasa udostępnia następujące metody:

* Sprawdzenie, czy pole jest puste.
* Wykonanie ruchu w zadanym polu (pod warunkiem, że jest wolne).
* Cofnięcie ruchu (ustawienie pola na 0).
* Sprawdzenie, czy dany gracz ma trzy w linii (wykorzystanie tablicy linii zwycięstwa z Constants).
* Sprawdzenie, czy plansza jest pełna (remis).
* Pobranie liczby pustych pól.
* Wypełnienie podanej tablicy indeksami pustych pól (zwrócenie liczby wypełnionych pozycji).
* Obliczenie indeksu stanu w tablicy Q jako liczby w systemie trójkowym.
* Klonowanie planszy (utworzenie kopii tablicy fields).

### 4.3. QLearningAgent.java

Zawiera główną strukturę danych i logikę Q-Learningu:

* Statyczna tablica dwuwymiarowa `double[19683][9]` przechowująca wartości Q(s,a) dla wszystkich stanów i wszystkich dziewięciu akcji.
* Hiperparametry: $\alpha$ (learning rate), $\gamma$ (discount factor), $\epsilon$ (stopień eksploracji).
* Metoda wyboru akcji w stanie `boardIndex` przy danym układzie pól (polecenie strategii ε-greedy).
* Metoda aktualizacji wartości Q według formuły Q-Learningu:

  $$
  Q(s,a) \leftarrow Q(s,a) + \alpha \Bigl(r + \gamma \max_{a'} Q(s',a') - Q(s,a)\Bigr).
  $$

  Jeżeli stan s′ jest terminalny, to przyjmujemy $\max_{a'} Q(s',a') = 0$.
* Metoda zmniejszania wartości epsilon zgodnie z czynnikiem zanikania i dolną granicą.
* Metody serializacji: zapis i odczyt całej tablicy Q do/z pliku (format binarny).

### 4.4. TicTacToeTrainer.java

Odpowiada za trening agenta w trybie self-play przeciwko losowemu przeciwnikowi:

- Konstruktor przyjmuje instancję agenta, liczbę epizodów, współczynnik zanikania epsilon oraz minimalne epsilon.
- W każdej epoce:
    1. Tworzona jest nowa plansza.
    2. Losowo wybierane jest, czy agent (O) czy przeciwnik (X) zaczyna grę (X zawsze wykonuje pierwszy ruch).
    3. W pętli pojedynczej partii gracze wykonują ruchy na zmianę:
        - Jeśli ruch wykonuje agent:
            - Wybiera akcję metodą `chooseAction`, wykonuje ruch.
            - Sprawdzane jest, czy agent wygrał (nagroda +1), dopuścił do natychmiastowej wygranej przeciwnika (kara -0,8), lub czy nastąpił remis (nagroda 0). W każdym z tych przypadków epizod się kończy, ruch jest cofany, a pętla przerywana.
        - Jeśli ruch wykonuje przeciwnik:
            - Wybiera losowe wolne pole.
            - Jeśli wygra, agent otrzymuje -1 za poprzedni ruch; jeśli jest remis, agent otrzymuje 0. W obu przypadkach epizod się kończy, ruch jest cofany, a pętla przerywana. Jeśli gra trwa, ruch wraca do agenta.
    4. Po każdej epoce wywoływana jest metoda `decayEpsilon`.
    5. Co 10 000 epizodów wypisywany jest postęp treningu (numer epizodu i aktualna wartość epsilon).

Dzięki tej procedurze agent uczy się grać coraz lepiej przeciwko losowemu przeciwnikowi, aż do momentu, gdy będzie w stanie przynajmniej remisować.

### 4.5. TicTacToeGame.java

Implementuje interfejs konsolowy do rozgrywki człowiek vs AI:

* Wyświetla instrukcje, w jaki sposób numerować pola (0–8).
* W pętli głównej każdej gry:

    1. Tworzy nową planszę.
    2. Gracz (X) wykonuje ruch: sprawdza poprawność wpisanego indeksu, wstawia symbol, sprawdza zwycięstwo lub remis.
    3. AI (O) wybiera ruch według metody `chooseAction` (używana jest wczytana tablica Q; w razie błędu losuje jedno z wolnych pól). Po ruchu sprawdza zwycięstwo lub remis.
    4. Gdy gra się kończy, wyświetla komunikat o wyniku i pyta, czy zagrać jeszcze raz.

Wszystkie operacje przeglądania tablicy i sprawdzania dostępnych pól wykonują się w czasie stałym lub O(9), bez tworzenia nowych obiektów w pętli gry.

### 4.6. Main.java

Punkt wejścia programu:

* Ustawia hiperparametry agenta: alfa, gamma, epsilon początkowe, liczba epizodów, wnsczas zanikania epsilon oraz minimalne epsilon.
* Tworzy instancję QLearningAgent.
* Próbuje wczytać plik z zapisaną tablicą Q (np. qtable.dat).

    * Jeśli wczytanie się powiedzie, pomija trening i przechodzi od razu do gry.
    * Jeśli wczytanie się nie powiedzie (brak pliku lub niepoprawny format), rozpoczyna trening:
      • tworzy instancję TicTacToeTrainer, uruchamia metodę train(),
      • po zakończeniu treningu zapisuje tablicę Q do pliku.
* Na końcu uruchamia interfejs gry przez utworzenie instancji TicTacToeGame i wywołanie metody play().

Dzięki temu przy pierwszym uruchomieniu agent uczy się od zera, a przy kolejnych odpaleniach gry korzysta z już wytrenowanej tablicy Q, co znacznie skraca czas przygotowania do rozgrywki.

---

## 5. Szczegółowe wzory i wyjaśnienia

1. **Kodowanie stanu**

   Każda z 9 pozycji na planszy może być w trzech stanach {0, 1, 2}. Stan jest traktowany jako wektor długości 9 w bazie 3:

   $$
   (\text{fields}[0],\ \text{fields}[1],\,\dots,\text{fields}[8]),
   $$

   a indeks w tablicy Q obliczamy jako:

   $$
   \text{stateIndex} \;=\; \sum_{i=0}^{8} \bigl(\text{fields}[i]\bigr)\times 3^i.
   $$

   Tablica potęg 3 (3^0, 3^1, …, 3^8) jest zainicjalizowana w klasie Constants. Wynikowa liczba jest w przedziale \[0, 19682].


2. **Formuła Q-Learningu**

   Dla pary (s,a) (stan, akcja) mamy:

   $$
   Q(s,a) \leftarrow Q(s,a) + \alpha \bigl(r + \gamma \max_{a'} Q(s',a') - Q(s,a)\bigr),
   $$

   gdzie:

    * $\alpha$ to współczynnik uczenia (np. 0,1).
    * $\gamma$ to współczynnik dyskontowania (np. 0,9).
    * $r$ to nagroda otrzymana po przejściu do stanu $s'$.
    * $\max_{a'} Q(s',a')$ to maksymalna wartość pośród wszystkich 9 akcji w stanie $s'$. Jeśli $s'$ jest stanem terminalnym, traktujemy $\max_{a'} Q(s',a') = 0$.


3. **Strategia ε-greedy**

   Agent działa według reguły:

    * z prawdopodobieństwem $\epsilon$ wybiera losową akcję spośród wolnych pól (eksploracja),
    * z prawdopodobieństwem $1 - \epsilon$ wybiera akcję maksymalizującą wartość $Q(s,a)$ spośród dozwolonych ruchów (eksploatacja).

   Wartość $\epsilon$ jest zmniejszana po każdej epoce według wzoru:

   $$
   \epsilon \leftarrow \epsilon \times \text{(czynnik zanikania)},
   $$

   z dolną granicą $\epsilon_{\min}$. Przykład: $\epsilon_{\text{start}} = 0{,}5$, $\text{czynnik zanikania} = 0{,}9999$, $\epsilon_{\min} = 0{,}01$.


4. **Funkcja nagrody**

   W Tic-Tac-Toe nagroda $r$ przyjmuje wartości:

    * $+1$, jeśli agent (O) wygra partię.
    * $-1$, jeśli agent (O) przegra.
    * $0$ w przypadku remisu lub podczas ruchów niekończących gry.

   Nagrody są przyznawane wyłącznie w stanach terminalnych (koniec partii).


5. **Liczba stanów i akcji**

    * Liczba możliwych stanów: $3^9 = 19\,683$.
    * Liczba możliwych akcji: maksymalnie 9 (jedna dla każdego pola), choć w praktyce legalnych jest tyle, ile pustych pól w danym stanie.


## 6. Dobór i znaczenie hiperparametrów

1. **α (alfy) – współczynnik uczenia**

    * Zalecana wartość: 0,1
    * Określa, jak silnie najnowsze doświadczenie (obserwacja nagrody i przyszłych wartości) zmienia przenikającą wartość $Q(s,a)$. Zbyt duże α → niestabilne skoki wartości, zbyt małe → powolne uczenie.

2. **γ (gamma) – współczynnik dyskontowania**

    * Zalecana wartość: 0,9
    * Wartość z przedziału \[0,1]. Im bliżej 1, tym większe znaczenie przyszłych nagród. W Tic-Tac-Toe, ze względu na płytką strukturę gry, wartość bliska 1 pozwala uwzględnić korzyści z końcowych wygranych.

3. **ε (epsilon) – parametr strategii ε-greedy**

    * Początkowe: 0,7 (70% eksploracji)
    * Po każdej epoce: ε ← ε × 0,999999
    * Minimalne ε (ε min): 0,01
    * Dzięki temu agent w początkowych epizodach intensywnie eksploruje, a z czasem coraz częściej eksploatuje nabytą wiedzę.

4. **Liczba epizodów (episodes)**

    * Domyślnie 10 010 000.
    * Można modyfikować w zależności od potrzeb. Większa liczba epizodów wymaga dłuższego czasu treningu, ale pozwala agentowi lepiej poznać całą przestrzeń stanów i osiągnąć wyższy poziom gry.

---

## 7. Uwagi końcowe

* Dzięki reprezentacji stanu w systemie trójkowym (tablica `double [19683][9]`) oraz pracy na tablicach prymitywnych (bez HashMap, bez generowania String, bez alokacji w gorących pętlach), projekt jest bardzo wydajny. Trening 50 000 epizodów na przeciętnym laptopie trwa zazwyczaj kilka–kilkanaście sekund.
* Po pierwszym uruchomieniu plik qtable.dat zostanie zapisany w katalogu, z którego uruchamiano program. Przy kolejnych uruchomieniach (o ile plik istnieje) trening zostanie pominięty, a agent będzie gotowy do gry natychmiast po starcie.
* Aby zmienić wartości hiperparametrów (np. liczbę epizodów, wartości α, γ, εstart), edytuj sekcję inicjalizacji w pliku Main.java.
* Projekt można rozwijać: np. zmienić przeciwnika na drugiego agenta Q-Learning, wprowadzić raporty statystyk (wygrane/porażki/remisy) w trakcie treningu lub uwzględnić symetrię planszy w celu przyspieszenia konwergencji.
