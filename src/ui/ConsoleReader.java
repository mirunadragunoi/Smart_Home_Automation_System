package ui;

import java.util.Scanner;

public final class ConsoleReader {
    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readNonEmptyLine(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) {
                return s;
            }
            System.out.println("Textul nu poate fi gol. Incearca din nou.");
        }
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Introdu un numar intreg valid.");
            }
        }
    }

    public int readPositiveInt(String prompt) {
        while (true) {
            int v = readInt(prompt);
            if (v > 0) {
                return v;
            }
            System.out.println("Valoarea trebuie sa fie > 0.");
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Introdu un numar real valid.");
            }
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String s = readLine(prompt + " (d/n): ").toLowerCase();
            if (s.equals("d") || s.equals("da") || s.equals("y") || s.equals("yes")) {
                return true;
            }
            if (s.equals("n") || s.equals("nu")) {
                return false;
            }
            System.out.println("Raspunde cu d (da) sau n (nu).");
        }
    }

    public int readChoice(String prompt, int minInclusive, int maxInclusive) {
        while (true) {
            int c = readInt(prompt);
            if (c >= minInclusive && c <= maxInclusive) {
                return c;
            }
            System.out.println("Alege o optiune intre " + minInclusive + " si " + maxInclusive + ".");
        }
    }
}
