import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.sound.sampled.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {

    // Static variable to track high score
    private static int highScore = 0;

    // Method to update high score
    public static void updateHighScore(int score) {
        if (score > highScore) {
            highScore = score; // Update high score if current score is higher
        }
    }

    // Method to retrieve high scores
    public static String getHighScores() {
        return "Highest Score: " + highScore; // Return the high score as a string
    }

    class Block {
        int x, y, width, height;
        Image image;
        int startX, startY;
        char direction = 'U';
        double velocityX = 0;
        double velocityY = 0;
        int speed;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
            this.speed = tileSize / 4;
        }

        void updateDirection(char direction) {
            this.direction = direction;
            updateVelocity();
        }

        void updateVelocity() {
            switch (this.direction) {
                case 'U' -> { velocityX = 0; velocityY = -speed; }
                case 'D' -> { velocityX = 0; velocityY = speed; }
                case 'L' -> { velocityX = -speed; velocityY = 0; }
                case 'R' -> { velocityX = speed; velocityY = 0; }
            }
        }

        void reset() {
            x = startX;
            y = startY;
        }
    }

    private final int rowCount = 21, columnCount = 19, tileSize = 32;
    private final int boardWidth = columnCount * tileSize;
    private final int boardHeight = rowCount * tileSize;

    private Image wallImage, blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage;
    private Image pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;
    private Image pacmanCloseUpImage, pacmanCloseDownImage, pacmanCloseLeftImage, pacmanCloseRightImage;

    private final String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX XoXX X",
            "X    X       X    X",
            "X XX XXXX XXXX XX X",
            "X  X X       X X  X",
            "XX X X XXrXX X X XX",
            "X        p        X",
            "X XXbX XXXXX X XX X",
            "X  X X       X X  X",
            "XX X X XXXXX X X XX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls = new HashSet<>();
    HashSet<Block> foods = new HashSet<>();
    HashSet<Block> ghosts = new HashSet<>();
    Block pacman;
    Timer gameLoop;
    char currentInputDirection = 'R';
    char pendingInputDirection = 'R';
    Random random = new Random();
    int score = 0, lives = 3;
    boolean gameOver = false;
    Block ghostFollowingPacman;

    int frameCounter = 0;
    int animationFrame = 0;

    private JButton pauseButton, resumeButton, exitButton, resetButton;
    private boolean isPaused = false;

    public PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        setLayout(null);

        // Load images
        wallImage = new ImageIcon(getClass().getResource("/asset/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/asset/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/asset/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/asset/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/asset/redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("/asset/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/asset/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/asset/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/asset/pacmanRight.png")).getImage();

        pacmanCloseUpImage = new ImageIcon(getClass().getResource("/asset/pacmanCloseUp.png")).getImage();
        pacmanCloseDownImage = new ImageIcon(getClass().getResource("/asset/pacmanCloseDown.png")).getImage();
        pacmanCloseLeftImage = new ImageIcon(getClass().getResource("/asset/pacmanCloseLeft.png")).getImage();
        pacmanCloseRightImage = new ImageIcon(getClass().getResource("/asset/pacmanCloseRight.png")).getImage();

        // Buttons
        pauseButton = createRoundedButton("Pause", boardWidth - 100, 10, 80, 30, e -> pauseGame());
        resumeButton = createRoundedButton("Resume", boardWidth / 2 - 60, boardHeight / 2 - 40, 120, 30, e -> resumeGame());
        exitButton = createRoundedButton("Main Menu", boardWidth / 2 - 60, boardHeight / 2 + 10, 120, 30, e -> goToMainMenu());
        resetButton = createRoundedButton("Reset", boardWidth / 2 - 60, boardHeight / 2 + 60, 120, 30, e -> resetGame());

        resumeButton.setVisible(false);
        exitButton.setVisible(false);
        resetButton.setVisible(false); // Hide reset button initially

        add(pauseButton);
        add(resumeButton);
        add(exitButton);
        add(resetButton);

        loadMap();
        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    private JButton createRoundedButton(String text, int x, int y, int width, int height, ActionListener listener) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };

        button.setBounds(x, y, width, height);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 0, 128));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.addActionListener(listener);

        return button;
    }

    private void pauseGame() {
        isPaused = true;
        gameLoop.stop();
        pauseButton.setVisible(false);
        resumeButton.setVisible(true);
        exitButton.setVisible(true);
        repaint();
    }

    private void resumeGame() {
        isPaused = false;
        gameLoop.start();
        resumeButton.setVisible(false);
        exitButton.setVisible(false);
        pauseButton.setVisible(true);
        repaint();
    }

    private void goToMainMenu() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.getContentPane().removeAll();
        MainMenu mainMenu = new MainMenu(topFrame);
        topFrame.add(mainMenu);
        topFrame.pack();
        mainMenu.requestFocusInWindow();
        topFrame.revalidate();
        topFrame.repaint();
    }

    void loadMap() {
        walls.clear();
        foods.clear();
        ghosts.clear();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                int x = c * tileSize;
                int y = r * tileSize;
                char ch = tileMap[r].charAt(c);

                switch (ch) {
                    case 'X' -> walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                    case 'b', 'o', 'p', 'r' -> {
                        Image ghostImage = switch (ch) {
                            case 'b' -> blueGhostImage;
                            case 'o' -> orangeGhostImage;
                            case 'p' -> pinkGhostImage;
                            default -> redGhostImage;
                        };
                        char direction = switch (ch) {
                            case 'b' -> 'U';
                            case 'o' -> 'D';
                            case 'p' -> 'L';
                            default -> 'R';
                        };
                        Block ghost = new Block(ghostImage, x, y, tileSize, tileSize);
                        ghost.updateDirection(direction);
                        ghosts.add(ghost);
                    }
                    case 'P' -> pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                    case ' ' -> foods.add(new Block(null, x + 14, y + 14, 4, 4));
                }
            }
        }

        pacman.speed = tileSize / 4;
        pacman.updateDirection('R');
        ghostFollowingPacman = (Block) ghosts.toArray()[random.nextInt(ghosts.size())];
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Block wall : walls) g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        for (Block food : foods) {
            g.setColor(Color.WHITE);
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        for (Block ghost : ghosts) g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.setColor(Color.YELLOW);
            Font font = new Font("Arial", Font.BOLD, 40); // Set font size and make it bold
            g.setFont(font);

            // Calculate the width and height of the text to center it
            String gameOverText = "Game Over";
            String scoreText = "Score: " + score;

            // Center the text
            int gameOverWidth = g.getFontMetrics().stringWidth(gameOverText);
            int scoreWidth = g.getFontMetrics().stringWidth(scoreText);
            int x = tileSize * columnCount / 2 - gameOverWidth / 2;
            int y = tileSize * rowCount / 2;

            // Draw the centered text
            g.drawString(gameOverText, x, y - 20); // Draw Game Over
            g.drawString(scoreText, x, y + 20);    // Draw Score

            resetButton.setVisible(true);  // Show reset button
            pauseButton.setVisible(false);  // Hide pause button
        }
        else {
            g.drawString("x" + lives + " Score: " + score, tileSize / 2, tileSize / 2);
            pauseButton.setVisible(true);  // Ensure the pause button is visible when game is not over
        }

        drawAnimatedPacman(g);
    }

    void drawAnimatedPacman(Graphics g) {
        Image currentImage = switch (pacman.direction) {
            case 'U' -> (animationFrame % 2 == 0) ? pacmanUpImage : pacmanCloseUpImage;
            case 'D' -> (animationFrame % 2 == 0) ? pacmanDownImage : pacmanCloseDownImage;
            case 'L' -> (animationFrame % 2 == 0) ? pacmanLeftImage : pacmanCloseLeftImage;
            default -> (animationFrame % 2 == 0) ? pacmanRightImage : pacmanCloseRightImage;
        };
        g.drawImage(currentImage, pacman.x, pacman.y, pacman.width, pacman.height, null);
    }

    // Method to handle game over and update high score
    void endGame() {
        updateHighScore(score); // Update high score when game ends
        gameOver = true;
        repaint();
    }

    public void move() {
        Block temp = new Block(null, pacman.x, pacman.y, pacman.width, pacman.height);
        temp.speed = pacman.speed;
        temp.updateDirection(pendingInputDirection);
        temp.x += temp.velocityX;
        temp.y += temp.velocityY;
        if (walls.stream().noneMatch(w -> collision(temp, w))) {
            pacman.updateDirection(pendingInputDirection);
            currentInputDirection = pendingInputDirection;
        }

        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls)
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }

        for (Block ghost : ghosts) {
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            boolean hitWall = false;
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) randomMovement(ghost);
            if (collision(ghost, pacman)) {
                if (--lives <= 0) {
                    endGame(); // End game when lives reach zero
                    return;
                }
                resetPositions();
                return;
            }
        }

        Block eaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                eaten = food;
                score += 10;
                playSound("/asset/eat_dot_0.wav"); // Play the eating sound effect
                break;
            }
        }

        if (eaten != null) foods.remove(eaten);
        if (foods.isEmpty()) {
            loadMap(); // Reload the map
            resetPositions(); // Reset positions of characters
        }

        if (++frameCounter >= 5) {
            frameCounter = 0;
            animationFrame++;
        }
    }

    void randomMovement(Block ghost) {
        char[] directions = {'U', 'D', 'L', 'R'};
        ghost.updateDirection(directions[random.nextInt(4)]);
    }

    boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x &&
                a.y < b.y + b.height && a.y + a.height > b.y;
    }

    void resetPositions() {
        pacman.reset();
        pacman.updateDirection('R');
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.updateDirection("UDLR".charAt(random.nextInt(4)));
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !isPaused) {
            move();
            repaint();
        } else if (gameOver) {
            gameLoop.stop();
        }
    }

    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> pendingInputDirection = 'U';
            case KeyEvent.VK_DOWN -> pendingInputDirection = 'D';
            case KeyEvent.VK_LEFT -> pendingInputDirection = 'L';
            case KeyEvent.VK_RIGHT -> pendingInputDirection = 'R';
            case KeyEvent.VK_SPACE -> {  // Toggle pause/resume when Space Bar is pressed
                if (isPaused) {
                    resumeGame();
                } else {
                    pauseGame();
                }
            }
        }

        if (gameOver) {
            score = 0;
            lives = 3;
            gameOver = false;
            loadMap();
            gameLoop.start();
            resetButton.setVisible(false); // Hide reset button after reset
        }
    }

    private void resetGame() {
        score = 0;
        lives = 3;
        gameOver = false;
        loadMap();
        gameLoop.start();
        resetButton.setVisible(false); // Hide reset button after reset
    }

    // Method to play sound effects
    public void playSound(String filePath) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource(filePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac-Man");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().add(new PacMan());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
