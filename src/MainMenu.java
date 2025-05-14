import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.*;

public class MainMenu extends JPanel {
    private Image backgroundImage;
    private Clip backgroundMusicClip;  // Clip to control background music
    private Clip startSoundClip;       // Clip for the start sound

    public MainMenu(JFrame frame) {
        // Play the background music in a loop
        playBackgroundMusic("/asset/game-music-loop-7-145285.wav");

        // Load background image from src folder
        backgroundImage = new ImageIcon(getClass().getResource("asset/PacmanImage.jpg")).getImage();

        setPreferredSize(new Dimension(608, 672));
        setLayout(new BorderLayout());

        Dimension buttonSize = new Dimension(180, 45);

        JButton startButton = createRoundedButton("Start Game", buttonSize);
        JButton howToPlayButton = createRoundedButton("How to Play", buttonSize);
        JButton highScoresButton = createRoundedButton("High Scores", buttonSize); // New button
        JButton exitButton = createRoundedButton("Exit", buttonSize);

        // Adjust button actions
        startButton.addActionListener(e -> {
            stopBackgroundMusic();  // Stop background music when starting the game
            playSound("/asset/start.wav");  // Play start sound

            // Proceed to the game
            frame.getContentPane().removeAll();
            PacMan game = new PacMan();
            frame.add(game);
            frame.pack();
            game.requestFocus();
            frame.revalidate();
        });

        howToPlayButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                    "Use arrow keys to move Pac-Man.\n" +
                            "Avoid ghosts and collect all the dots to win!",
                    "How to Play",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        highScoresButton.addActionListener(e -> {
            // Display high scores
            JOptionPane.showMessageDialog(frame, "High Scores: " + PacMan.getHighScores(), "High Scores", JOptionPane.INFORMATION_MESSAGE);
        });

        exitButton.addActionListener(e -> System.exit(0));

        // Create panel for the first row of buttons (inline)
        JPanel topButtonRow = new JPanel();
        topButtonRow.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Space between buttons
        topButtonRow.setOpaque(false);
        topButtonRow.add(startButton);
        topButtonRow.add(howToPlayButton);
        topButtonRow.add(exitButton);

        // Create panel for the second row (High Scores button centered below)
        JPanel bottomButtonRow = new JPanel();
        bottomButtonRow.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20)); // Space between buttons and top row
        bottomButtonRow.setOpaque(false);
        bottomButtonRow.add(highScoresButton);

        // Create the container panel and add both rows
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS));
        buttonContainer.setOpaque(false);
        buttonContainer.add(topButtonRow);  // Add top row of buttons
        buttonContainer.add(bottomButtonRow);  // Add bottom row of buttons

        // Spacer panel to adjust the position of the button container (lift it slightly)
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(608, 100)); // Adjust height (100px from bottom)
        spacer.setOpaque(false);  // Make the spacer transparent

        // Add spacer and button container to the bottom of the frame
        add(spacer, BorderLayout.SOUTH);  // Spacer pushes the buttons up
        add(buttonContainer, BorderLayout.SOUTH); // Add buttons below the spacer
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private JButton createRoundedButton(String text, Dimension size) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(141, 132, 14));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };

        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBackground(new Color(255, 239, 23));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(234, 220, 21));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 239, 23));
            }
        });

        return button;
    }

    // Method to play background music in a loop
    public void playBackgroundMusic(String filePath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource(filePath));
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioStream);
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music continuously
            backgroundMusicClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to stop the background music
    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null) {
            backgroundMusicClip.stop(); // Stop the looping music
        }
    }

    // Method to play other sound effects (like the start sound)
    public void playSound(String filePath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource(filePath));
            startSoundClip = AudioSystem.getClip();
            startSoundClip.open(audioStream);
            startSoundClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
