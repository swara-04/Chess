import javax.swing.SwingUtilities; // ensures the UI is created on the Event Dispatch Thread

// Entry point - just bootstraps the window on the right thread and lets everything else take over
public class Main {
    public static void main(String[] args) {
        // Swing isn't thread-safe, so all UI creation must happen on the EDT
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}