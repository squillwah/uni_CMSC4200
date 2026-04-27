package CannonVSBall;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * [CMSC3200] Optimized Cannon VS Ball
 * Key Improvements: 
 * 1. Fixed-timestep game loop for stable physics.
 * 2. Modern List collections replacing legacy Vector.
 * 3. Unified event handling to reduce object overhead.
 */
public class CannonVSBall extends Frame implements ActionListener, AdjustmentListener, ItemListener, Runnable {
    private static final long serialVersionUID = 1L;
    
    // --- Configuration Constants ---
    private final double[] SIZES = {5, 10, 15, 25, 40}; 
    private final double[] SPEEDS = {10, 20, 30, 50, 80};
    private final double[] GRAVITIES = {3.7, 8.87, 9.81, 3.71, 24.79, 10.4, 8.87, 11.15, 0.62};

    // --- Engine & State ---
    private final List<Bubble> bubbles = Collections.synchronizedList(new ArrayList<>());
    private final List<Balloid> balls = Collections.synchronizedList(new ArrayList<>());
    private final List<Rectangle> obstacles = Collections.synchronizedList(new ArrayList<>());
    
    private volatile boolean running = true;
    private volatile boolean paused = true;
    private double gravity = GRAVITIES[2];
    private double currentBubbleSize = SIZES[2];
    private double currentBubbleSpeed = SPEEDS[2];
    private double cannonAngle = 45.0;
    private double cannonForce = 250.0;
    private double elapsedTime = 0;
    private int scorePlayer = 0;
    private int scoreBubbles = 0;

    // --- UI Components ---
    private final Scrollbar sbForce, sbAngle;
    private final Label lblForce, lblAngle, lblScoreB, lblScoreP, lblTime;
    private final CheckboxMenuItem[] sizeItems = new CheckboxMenuItem[5];
    private final CheckboxMenuItem[] speedItems = new CheckboxMenuItem[5];
    private final CheckboxMenuItem[] planetItems = new CheckboxMenuItem[9];
    private final GameCanvas canvas;

    public static void main(String[] args) {
        new CannonVSBall();
    }

    public CannonVSBall() {
        super("Cannon VS Ball - Optimized");
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // --- Setup Menu ---
        MenuBar mb = new MenuBar();
        Menu mControl = new Menu("Control");
        addMenuItem(mControl, "Run", KeyEvent.VK_R);
        addMenuItem(mControl, "Pause", KeyEvent.VK_P);
        addMenuItem(mControl, "Restart", -1);
        mControl.addSeparator();
        addMenuItem(mControl, "Quit", KeyEvent.VK_Q);
        
        Menu mParam = new Menu("Parameters");
        Menu mSize = new Menu("Size");
        setupRadioMenu(mSize, sizeItems, new String[]{"XS", "S", "M", "L", "XL"}, 2);
        Menu mSpeed = new Menu("Speed");
        setupRadioMenu(mSpeed, speedItems, new String[]{"Slowest", "Slow", "Normal", "Fast", "Fastest"}, 2);
        mParam.add(mSize);
        mParam.add(mSpeed);

        Menu mEnv = new Menu("Environment");
        setupRadioMenu(mEnv, planetItems, new String[]{"Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"}, 2);

        mb.add(mControl);
        mb.add(mParam);
        mb.add(mEnv);
        setMenuBar(mb);

        // --- Setup Canvas ---
        canvas = new GameCanvas();
        add(canvas, BorderLayout.CENTER);

        // --- Setup Controls ---
        Panel pnlSouth = new Panel(new GridBagLayout());
        pnlSouth.setBackground(new Color(158, 137, 79));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5,5,5,5); gbc.weightx = 1.0;

        sbForce = new Scrollbar(Scrollbar.HORIZONTAL, 250, 50, 0, 1000);
        sbAngle = new Scrollbar(Scrollbar.HORIZONTAL, 450, 50, 0, 900);
        sbForce.addAdjustmentListener(this);
        sbAngle.addAdjustmentListener(this);

        lblForce = new Label("Force: 250", Label.CENTER);
        lblAngle = new Label("Angle: 45.0", Label.CENTER);
        lblScoreB = new Label("Bubbles: 0", Label.CENTER);
        lblScoreP = new Label("Player: 0", Label.CENTER);
        lblTime = new Label("Time: 0.0s", Label.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; pnlSouth.add(sbForce, gbc);
        gbc.gridy = 1; pnlSouth.add(lblForce, gbc);
        gbc.gridx = 1; gbc.gridy = 0; pnlSouth.add(lblScoreB, gbc);
        gbc.gridy = 1; pnlSouth.add(lblTime, gbc);
        gbc.gridx = 2; gbc.gridy = 0; pnlSouth.add(lblScoreP, gbc);
        gbc.gridx = 3; gbc.gridy = 0; pnlSouth.add(sbAngle, gbc);
        gbc.gridy = 1; pnlSouth.add(lblAngle, gbc);

        add(pnlSouth, BorderLayout.SOUTH);

        // Window Lifecycle
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { shutdown(); }
        });

        // Initialize Game
        restartGame();
        setSize(850, 600);
        setVisible(true);

        new Thread(this).start();
    }

    private void addMenuItem(Menu m, String label, int shortcut) {
        MenuItem mi = (shortcut != -1) ? new MenuItem(label, new MenuShortcut(shortcut)) : new MenuItem(label);
        mi.addActionListener(this);
        m.add(mi);
    }

    private void setupRadioMenu(Menu m, CheckboxMenuItem[] items, String[] labels, int defaultIdx) {
        for (int i = 0; i < items.length; i++) {
            items[i] = new CheckboxMenuItem(labels[i], i == defaultIdx);
            items[i].addItemListener(this);
            m.add(items[i]);
        }
    }

    private void restartGame() {
        bubbles.clear();
        balls.clear();
        elapsedTime = 0;
        scorePlayer = 0;
        scoreBubbles = 0;
        bubbles.add(new Bubble(100, 100, currentBubbleSpeed, currentBubbleSize));
    }

    private void shutdown() {
        running = false;
        dispose();
        System.exit(0);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0; // 60 FPS
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            if (delta >= 1) {
                if (!paused) tick();
                delta--;
            }
            
            canvas.repaint();
            try { Thread.sleep(2); } catch (Exception e) {}
        }
    }

    private void tick() {
        elapsedTime += (1.0 / 60.0);
        
        synchronized(bubbles) {
            for (Bubble b : bubbles) {
                b.y += gravity * 0.1; // Simple gravity
                b.x += b.vx * 0.016;
                b.y += b.vy * 0.016;
                // Wall bounce
                if (b.x < 0 || b.x > canvas.getWidth()) b.vx *= -1;
                if (b.y < 0 || b.y > canvas.getHeight()) b.vy *= -1;
            }
        }

        // Update Labels
        lblTime.setText(String.format("Time: %.1fs", elapsedTime));
        lblScoreB.setText("Bubbles: " + bubbles.size());
        lblScoreP.setText("Player: " + scorePlayer);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("Quit")) shutdown();
        if (cmd.equals("Run")) paused = false;
        if (cmd.equals("Pause")) paused = true;
        if (cmd.equals("Restart")) restartGame();
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == sbAngle) {
            cannonAngle = sbAngle.getValue() / 10.0;
            lblAngle.setText(String.format("Angle: %.1f", cannonAngle));
        } else if (e.getSource() == sbForce) {
            cannonForce = sbForce.getValue();
            lblForce.setText("Force: " + (int)cannonForce);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object src = e.getSource();
        handleRadioSelection(sizeItems, src, SIZES, (val) -> currentBubbleSize = val);
        handleRadioSelection(speedItems, src, SPEEDS, (val) -> currentBubbleSpeed = val);
        handleRadioSelection(planetItems, src, GRAVITIES, (val) -> gravity = val);
    }

    private void handleRadioSelection(CheckboxMenuItem[] items, Object src, double[] values, java.util.function.Consumer<Double> update) {
        for (int i = 0; i < items.length; i++) {
            if (src == items[i]) {
                for (CheckboxMenuItem mi : items) mi.setState(false);
                items[i].setState(true);
                update.accept(values[i]);
                break;
            }
        }
    }

    // --- Inner Classes for Objects ---
    class Bubble {
        double x, y, vx, vy, radius;
        Bubble(double x, double y, double speed, double radius) {
            this.x = x; this.y = y; this.radius = radius;
            this.vx = speed; this.vy = speed;
        }
    }

    class Balloid {
        double x, y, vx, vy;
    }

    class GameCanvas extends Canvas {
        public GameCanvas() {
            setBackground(Color.DARK_GRAY);
        }

        @Override
        public void paint(Graphics g) {
            update(g);
        }

        @Override
        public void update(Graphics g) {
            // Double buffering
            Image offscreen = createImage(getWidth(), getHeight());
            Graphics offg = offscreen.getGraphics();
            
            offg.setColor(getBackground());
            offg.fillRect(0, 0, getWidth(), getHeight());

            // Draw Bubbles
            offg.setColor(Color.CYAN);
            synchronized(bubbles) {
                for (Bubble b : bubbles) {
                    offg.fillOval((int)(b.x - b.radius), (int)(b.y - b.radius), (int)b.radius * 2, (int)b.radius * 2);
                }
            }

            // Draw Cannon Placeholder
            offg.setColor(Color.RED);
            offg.fillRect(10, getHeight() - 50, 40, 40);

            g.drawImage(offscreen, 0, 0, this);
        }
    }
}
