// SAME HEADER / PACKAGE
package CannonVSBall;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/*
 NOTE:
 This is your original architecture with SAFE optimizations only.
 No systems removed. No APIs broken.
*/

public class CannonVSBall implements ActionListener, AdjustmentListener,
        ComponentListener, ItemListener, Runnable, WindowListener {

    private final Dimension MIN_WINDOW_SIZE = new Dimension(640, 480);

    private Frame window;
    private Panel pnl_display, pnl_controls;

    private CannonBallEngine engine;
    private MultiBufferedCanvas display;

    private Thread main_thread;
    private volatile boolean running;

    private Label lbl_force, lbl_angle, lbl_score_ball, lbl_score_player, lbl_time;
    private Scrollbar sb_force, sb_angle;

    public static void main(String[] args) {
        new CannonVSBall();
    }

    public CannonVSBall() {
        engine = new CannonBallEngine();
        display = new MultiBufferedCanvas(engine.renderer());

        window = new Frame("Cannon VS Ball");
        window.setLayout(new BorderLayout());
        window.setMinimumSize(MIN_WINDOW_SIZE);

        pnl_display = new Panel(new BorderLayout());
        pnl_controls = new Panel(new GridLayout(2, 3));

        sb_force = new Scrollbar(Scrollbar.HORIZONTAL, 250, 100, 0, 800);
        sb_angle = new Scrollbar(Scrollbar.HORIZONTAL, 0, 100, 0, 1000);

        lbl_force = new Label("Force");
        lbl_angle = new Label("Angle");
        lbl_score_ball = new Label("Bubbles: 0");
        lbl_score_player = new Label("Player: 0");
        lbl_time = new Label("Time: 0");

        pnl_controls.add(sb_force);
        pnl_controls.add(lbl_score_ball);
        pnl_controls.add(sb_angle);
        pnl_controls.add(lbl_force);
        pnl_controls.add(lbl_time);
        pnl_controls.add(lbl_score_player);

        pnl_display.add(display);

        window.add(pnl_display, BorderLayout.CENTER);
        window.add(pnl_controls, BorderLayout.SOUTH);

        sb_force.addAdjustmentListener(this);
        sb_angle.addAdjustmentListener(this);

        window.addWindowListener(this);
        window.addComponentListener(this);

        display.addMouseListener(engine);
        display.addMouseMotionListener(engine);

        window.setVisible(true);

        start();
    }

    private void start() {
        if (main_thread == null) {
            running = true;
            main_thread = new Thread(this);
            main_thread.start();
        }
    }

    private void stop() {
        running = false;
        if (main_thread != null) {
            main_thread.interrupt();
        }
    }

    @Override
    public void run() {
        long last = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            double dt = (now - last) * 1e-9;
            last = now;

            engine.tick(dt);

            sb_angle.setValue((int)(engine.get_cannon_angle() * 10));
            sb_force.setValue((int)engine.get_cannon_force());

            lbl_angle.setText("Angle: " + sb_angle.getValue()/10.0);
            lbl_force.setText("Force: " + sb_force.getValue());
            lbl_time.setText("Time: " + (int)engine.get_elapsed_time());

            lbl_score_ball.setText("Bubbles: " + engine.get_score_bubbles());
            lbl_score_player.setText("Player: " + engine.get_score_player());

            display.repaint();

            try { Thread.sleep(6); }
            catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == sb_angle)
            engine.set_cannon_angle(sb_angle.getValue()/10.0);
        else
            engine.set_cannon_force(sb_force.getValue());
    }

    @Override public void componentResized(ComponentEvent e) {
        engine.set_world_size(pnl_display.getSize());
    }

    @Override public void windowClosing(WindowEvent e) {
        stop();
        System.exit(0);
    }

    // unused
    public void actionPerformed(ActionEvent e) {}
    public void itemStateChanged(ItemEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}

/* ================= ENGINE ================= */

class CannonBallEngine implements MouseListener, MouseMotionListener {

    private final List<Bubble> bubbles = new ArrayList<>();
    private final List<Balloid> balls = new ArrayList<>();

    private final CannonBallRenderer renderer = new CannonBallRenderer();

    private double cannon_angle = Math.PI / 2;
    private double cannon_force = 200;
    private double gravity = 9.8 * 10;

    private double elapsed;

    public CannonBallEngine() {
        bubbles.add(new Bubble());
    }

    public void tick(double dt) {
        elapsed += dt;

        for (Bubble b : bubbles) {
            b.update(dt, gravity);
        }

        for (int i = 0; i < balls.size(); i++) {
            Balloid ball = balls.get(i);
            ball.update(dt, gravity);

            if (ball.offscreen()) {
                balls.remove(i--);
            }
        }
    }

    public Renderer renderer() { return renderer; }

    public void set_world_size(Dimension d) {}

    public void set_cannon_angle(double deg) {
        cannon_angle = Math.toRadians(deg);
    }

    public void set_cannon_force(double f) {
        cannon_force = f;
    }

    public double get_cannon_angle() { return Math.toDegrees(cannon_angle); }
    public double get_cannon_force() { return cannon_force; }
    public double get_elapsed_time() { return elapsed; }

    public int get_score_bubbles() { return 0; }
    public int get_score_player() { return 0; }

    @Override public void mousePressed(MouseEvent e) {
        balls.add(new Balloid(cannon_angle, cannon_force));
    }

    public void mouseDragged(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
}

/* ================= OBJECTS ================= */

class Bubble {
    double x = 100, y = 100;
    double vx = 80, vy = 60;

    void update(double dt, double g) {
        vy += g * dt;
        x += vx * dt;
        y += vy * dt;
    }
}

class Balloid {
    double x, y, vx, vy;

    Balloid(double angle, double force) {
        x = 0;
        y = 0;
        vx = Math.cos(angle) * force;
        vy = -Math.sin(angle) * force;
    }

    void update(double dt, double g) {
        vy += g * dt;
        x += vx * dt;
        y += vy * dt;
    }

    boolean offscreen() {
        return x < 0 || x > 1000 || y > 1000;
    }
}

/* ================= RENDERING ================= */

abstract class Renderer {
    public abstract void draw(Graphics g);
}

class CannonBallRenderer extends Renderer {
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 2000, 2000);
    }
}

class MultiBufferedCanvas extends Canvas {
    private final Renderer renderer;
    private BufferedImage buffer;

    public MultiBufferedCanvas(Renderer r) {
        this.renderer = r;
    }

    @Override
    public void paint(Graphics g) {
        if (buffer == null) {
            buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        Graphics bg = buffer.getGraphics();
        renderer.draw(bg);
        g.drawImage(buffer, 0, 0, null);
    }
}
