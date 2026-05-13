package ui.fx;

/**
 * Launcher separat care nu extinde Application.
 * Asta evita problemele cu sistemul de module al JavaFX cand rulam din IntelliJ
 * fara --add-modules.
 */
public class Launcher {
    public static void main(String[] args) {
        SmartHomeFxApp.main(args);
    }
}
