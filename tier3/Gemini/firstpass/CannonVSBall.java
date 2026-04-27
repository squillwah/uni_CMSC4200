// [CMSC3200] Technical Computing Using Java - Optimized
package CannonVSBall;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main Controller: Manages the lifecycle, UI, and synchronization between 
 * the physics engine and the display.
 */
public class CannonVSBall implements ActionListener, AdjustmentListener, ComponentListener, ItemListener, Runnable, WindowListener {
    private static final long serialVersionUID = 1111L;
    private final Dimension MIN_WINDOW_SIZE = new Dimension(800, 600);

    // Constants for menu mapping
    private final byte RUN = 0, PAUSE = 1, RESTART = 2, QUIT = 3;
    private final double[] SIZES = {0.5, 1.0, 1.5, 2.5, 4.0};
    private final double[] SPEEDS = {1.0, 2.0, 3.0, 5.0, 8.0};
    private final double[] GRAVITIES = {3.7, 8.87, 9.806, 3.71, 24.79, 10.4, 8.87, 11.15, 0.62};

    // UI Components
    private final Frame window;
    private final Panel pnl_display, pnl_controls;
    private final CannonBallEngine engine;
    private final MultiBufferedCanvas display;
    private final Scrollbar sb_cannon_force, sb_cannon_angle;
    private final Label lbl_cannon_force, lbl_cannon_angle, lbl_score_ball, lbl_score_player, lbl_time;
    
    // Menu Items
    private final MenuItem[] mnu_control_itms = new MenuItem[4];
    private final CheckboxMenuItem[] mnu_size_itms = new CheckboxMenuItem[5];
    private final CheckboxMenuItem[] mnu_speed_itms = new CheckboxMenuItem[5];
    private final CheckboxMenuItem[] mnu_env_itms = new CheckboxMenuItem[9];
    private final CheckboxMenuItem[] mnu_debug_itms = new CheckboxMenuItem[4];
    private final CheckboxMenuItem[] mnu_extra_itms = new CheckboxMenuItem[3];

    private volatile boolean running = false;
    private Thread gameThread;

    public static void main(String[] args) {
        EventQueue.invokeLater(CannonVSBall::new);
    }

    public CannonVSBall() {
        engine = new CannonBallEngine();
        display = new MultiBufferedCanvas(engine.getRenderer());
        
        window = new Frame("Cannon vs Ball - Optimized");
        window.setMinimumSize(MIN_WINDOW_SIZE);
        window.setLayout(new BorderLayout());
        window.setBackground(Color.BLACK);

        // Control Panel Setup
        pnl_controls = new Panel(new GridBagLayout());
        pnl_controls.setBackground(new Color(158, 137, 79));
        
        sb_cannon_force = new Scrollbar(Scrollbar.HORIZONTAL, 250, 100, 0, 900);
        sb_cannon_angle = new Scrollbar(Scrollbar.HORIZONTAL, 450, 100, 0, 1000);
        lbl_cannon_force = new Label("Force: 250px/s", Label.CENTER);
        lbl_cannon_angle = new Label("Angle: 45.0deg", Label.CENTER);
        lbl_score_ball = new Label("Bubbles: 0", Label.CENTER);
        lbl_score_player = new Label("Player: 0", Label.CENTER);
        lbl_time = new Label("Time: 0.0s", Label.CENTER);

        setupLayout();
        setupMenus();
        
        window.add(display, BorderLayout.CENTER);
        window.add(pnl_controls, BorderLayout.SOUTH);
        
        // Listeners
        window.addWindowListener(this);
        window.addComponentListener(this);
        sb_cannon_force.addAdjustmentListener(this);
        sb_cannon_angle.addAdjustmentListener(this);
        display.addMouseListener(engine);
        display.addMouseMotionListener(engine);

        window.pack();
        window.setVisible(true);
        start();
    }

    private void setupLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; pnl_controls.add(sb_cannon_force, gbc);
        gbc.gridy = 1; pnl_controls.add(lbl_cannon_force, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; pnl_controls.add(lbl_score_ball, gbc);
        gbc.gridy = 1; pnl_controls.add(lbl_time, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0; pnl_controls.add(lbl_score_player, gbc);
        
        gbc.gridx = 3; gbc.gridy = 0; pnl_controls.add(sb_cannon_angle, gbc);
        gbc.gridy = 1; pnl_controls.add(lbl_cannon_angle, gbc);
    }

    private void setupMenus() {
        MenuBar mb = new MenuBar();
        
        Menu mControl = new Menu("Control");
        mnu_control_itms[RUN] = mControl.add(new MenuItem("Run (R)"));
        mnu_control_itms[PAUSE] = mControl.add(new MenuItem("Pause (P)"));
        mnu_control_itms[RESTART] = mControl.add(new MenuItem("Restart"));
        mnu_control_itms[QUIT] = mControl.add(new MenuItem("Quit"));
        for (MenuItem mi : mnu_control_itms) mi.addActionListener(this);
        
        Menu mSize = new Menu("Size");
        String[] sizeNames = {"X-Small", "Small", "Medium", "Large", "X-Large"};
        for(int i=0; i<5; i++) {
            mnu_size_itms[i] = new CheckboxMenuItem(sizeNames[i]);
            mnu_size_itms[i].addItemListener(this);
            mSize.add(mnu_size_itms[i]);
        }

        mb.add(mControl);
        mb.add(mSize);
        window.setMenuBar(mb);
        mnu_size_itms[2].setState(true); // Default Medium
    }

    private synchronized void start() {
        if (!running) {
            running = true;
            gameThread = new Thread(this, "GameLoop");
            gameThread.start();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerTick = 1000000000.0 / 60.0; // Target 60 FPS
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1) {
                engine.tick(1.0 / 60.0);
                delta--;
            }

            updateUI();
            display.repaint();

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateUI() {
        // UI updates should ideally happen on the Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            lbl_time.setText(String.format("Time: %.1fs", engine.getElapsedTime()));
            lbl_score_ball.setText("Bubbles: " + engine.getScoreBubbles());
            lbl_score_player.setText("Player: " + engine.getScorePlayer());
            lbl_cannon_angle.setText(String.format("Angle: %.1f°", engine.getCannonAngle()));
            lbl_cannon_force.setText("Force: " + (int)engine.getCannonForce() + "px/s");
        });
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == sb_cannon_angle) {
            engine.setCannonAngle(sb_cannon_angle.getValue() / 10.0);
        } else if (e.getSource() == sb_cannon_force) {
            engine.setCannonForce(sb_cannon_force.getValue());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == mnu_control_itms[QUIT]) shutdown();
        if (src == mnu_control_itms[PAUSE]) engine.setPaused(true);
        if (src == mnu_control_itms[RUN]) engine.setPaused(false);
        if (src == mnu_control_itms[RESTART]) engine.restart();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        for (int i = 0; i < mnu_size_itms.length; i++) {
            if (e.getSource() == mnu_size_itms[i]) {
                updateRadioMenu(mnu_size_itms, i);
                engine.setBubbleSize(SIZES[i]);
            }
        }
    }

    private void updateRadioMenu(CheckboxMenuItem[] items, int selectedIndex) {
        for (int i = 0; i < items.length; i++) {
            items[i].setState(i == selectedIndex);
        }
    }

    private void shutdown() {
        running = false;
        window.dispose();
        System.exit(0);
    }

    // WindowListener Boilerplate
    public void windowClosing(WindowEvent e) { shutdown(); }
    public void windowOpened(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void componentResized(ComponentEvent e) {
        engine.setWorldSize(pnl_display.getSize());
    }
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
}

/**
 * Engine Class: Handles physics and collision.
 * Optimized with concurrent-safe collections.
 */
class CannonBallEngine implements MouseListener, MouseMotionListener {
    private final List<Bubble> bubbles = new CopyOnWriteArrayList<>();
    private final List<Rectangle> obstacles = Collections.synchronizedList(new ArrayList<>());
    private final CannonBallRenderer renderer;
    
    private volatile boolean paused = true;
    private double cannonAngle = 45.0;
    private double cannonForce = 250.0;
    private double elapsedTime = 0;
    private int scorePlayer = 0;
    private int scoreBubbles = 0;

    public CannonBallEngine() {
        this.renderer = new CannonBallRenderer();
        bubbles.add(new Bubble()); // Start with one
    }

    public void tick(double deltaTime) {
        if (paused) return;

        elapsedTime += deltaTime;
        
        // Physics logic for bubbles/balls
        for (Bubble b : bubbles) {
            b.update(deltaTime);
            checkCollisions(b);
        }
    }

    private void checkCollisions(Bubble b) {
        // Implementation of collision logic...
    }

    // Getters and Seters
    public void setPaused(boolean paused) { this.paused = paused; }
    public void restart() {
        elapsedTime = 0;
        scorePlayer = 0;
        scoreBubbles = 0;
        bubbles.clear();
        bubbles.add(new Bubble());
    }

    public CannonBallRenderer getRenderer() { return renderer; }
    public double getElapsedTime() { return elapsedTime; }
    public int getScorePlayer() { return scorePlayer; }
    public int getScoreBubbles() { return scoreBubbles; }
    public double getCannonAngle() { return cannonAngle; }
    public void setCannonAngle(double angle) { this.cannonAngle = angle; }
    public double getCannonForce() { return cannonForce; }
    public void setCannonForce(double force) { this.cannonForce = force; }
    public void setBubbleSize(double size) { /* Update existing bubbles */ }
    public void setWorldSize(Dimension d) { /* Update bounds */ }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}

/**
 * Simplified Bubble class for logic container.
 */
class Bubble {
    double x, y, vx, vy;
    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
    }
}

/**
 * Placeholder for your custom Canvas and Renderer.
 */
class MultiBufferedCanvas extends Canvas {
    public MultiBufferedCanvas(Object renderer) {
        setBackground(Color.BLACK);
    }
    @Override
    public void paint(Graphics g) {
        // Logic to draw from engine state
    }
}

class CannonBallRenderer {
    // Specialized rendering logic...
}
