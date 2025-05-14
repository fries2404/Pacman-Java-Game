import javax.swing.*;


public class App {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac-Man");
        frame.setSize(608, 672); // Specify window size
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MainMenu menu = new MainMenu(frame);
        frame.add(menu);
        frame.pack();
        frame.setVisible(true);
    }
}
