// [CMSC3200] Technical Computing Using Java
// Program 6: Cannon VS Ball
//
//  ball 
//          cannon
//
// Group 2
// Brandon Schwartz, DaJuan Bowie, Joshua Staffen, Ravi Dressler
// SCH81594@pennwest.edu, BOW90126@pennwest.edu, STA79160@pennwest.edu, DRE44769@pennwest.edu

package CannonVSBall;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// ----------------
// The Main Class
// Creates the frame + UI elements, game engine and canvas display.
// Manages interaction between UI bits/controls and game state.
// ----------------
public class CannonVSBall implements ActionListener, AdjustmentListener, ComponentListener, ItemListener, Runnable, WindowListener {

    // Offsets for MenuItem and value arrays.
    private static final byte RUN = 0, PAUSE = 1, RESTART = 2, QUIT = 3, NUM_CONTROLS = 4;
    private static final byte XSMALL = 0, SMALL = 1, MEDIUM = 2, LARGE = 3, XLARGE = 4, NUM_SIZES = 5;
    private static final byte XSLOW = 0, SLOW = 1, NORMAL = 2, FAST = 3, XFAST = 4, NUM_SPEEDS = 5;
    private static final byte MERCURY = 0, VENUS = 1, EARTH = 2, MARS = 3, JUPITER = 4,
                               SATURN = 5, URANUS = 6, NEPTUNE = 7, PLUTO = 8, NUM_PLANETS = 9;
    private static final byte NODEBUG = 0, DB1 = 1, DB2 = 2, DB3 = 3, NUM_DEBUG_LEVELS = 4;
    private static final byte FERTILEBALLS = 0, DRAWHITBOXES = 1, BUBBLEGRAVITY = 2, NUM_EXTRAS = 3;

    // Parallel MenuItem value arrays.
    private static final double[] SIZES     = {.5, 1, 1.5, 2.5, 4};
    private static final double[] SPEEDS    = {1, 2, 3, 5, 8};
    private static final double[] GRAVITIES = {3.7, 8.87, 9.80665, 3.71, 24.79, 10.4, 8.87, 11.15, 0.620};

    private static final Dimension MIN_WINDOW_SIZE = new Dimension(640, 480);
    private static final int THREAD_SLEEP_MS = 4;

    // Frame and panels.
    private final Frame window;
    private final Panel pnl_display, pnl_controls;

    // The game logic, drawing system, thread.
    private final CannonBallEngine engine;
    private final MultiBufferedCanvas display;
    private Thread main_thread;
    private volatile boolean main_thread_running;

    // Elements of UI: MenuItems, ScrollBars, Labels.
    private final MenuBar menubar;
    private final Menu mnu_control, mnu_parameters, mnu_environment,
                       mnu_parameters_mnu_size, mnu_parameters_mnu_speed,
                       mnu_debuginfo, mnu_extras;
    private final MenuItem[]         mnu_control_itms;
    private final CheckboxMenuItem[] mnu_parameters_mnu_size_itms;
    private final CheckboxMenuItem[] mnu_parameters_mnu_speed_itms;
    private final CheckboxMenuItem[] mnu_environment_itms;
    private final CheckboxMenuItem[] mnu_debuginfo_itms;
    private final CheckboxMenuItem[] mnu_extras_itms;
    private final Label lbl_cannon_force, lbl_cannon_angle, lbl_score_ball, lbl_score_player, lbl_time;
    private final Scrollbar sb_cannon_force, sb_cannon_angle;

    public static void main(String[] args) { new CannonVSBall(); }

    public CannonVSBall() {
        engine       = new CannonBallEngine();
        display      = new MultiBufferedCanvas(engine.renderer());
        main_thread  = null;
        main_thread_running = false;

        // Frame
        window = new Frame("CannonBubbles");
        window.setMinimumSize(MIN_WINDOW_SIZE);
        window.setBackground(Color.black);
        window.setLayout(new BorderLayout());

        // Panels
        pnl_display = new Panel(new BorderLayout());
        pnl_display.setBackground(Color.gray);
        pnl_controls = new Panel(new GridBagLayout());
        pnl_controls.setBackground(new Color(158, 137, 79));

        // Menubar
        menubar                              = new MenuBar();
        mnu_control                          = menubar.add(new Menu("Control"));
        mnu_control_itms                     = new MenuItem[NUM_CONTROLS];
        mnu_control_itms[RUN]                = mnu_control.add(new MenuItem("Run",     new MenuShortcut(KeyEvent.VK_R)));
        mnu_control_itms[PAUSE]              = mnu_control.add(new MenuItem("Pause",   new MenuShortcut(KeyEvent.VK_P)));
        mnu_control_itms[RESTART]            = mnu_control.add(new MenuItem("Restart", new MenuShortcut(KeyEvent.VK_P, true)));
        mnu_control.addSeparator();
        mnu_control_itms[QUIT]               = mnu_control.add(new MenuItem("Quit",    new MenuShortcut(KeyEvent.VK_Q, true)));

        mnu_parameters                       = menubar.add(new Menu("Parameters"));
        mnu_parameters_mnu_size              = (Menu) mnu_parameters.add(new Menu("Size"));
        mnu_parameters_mnu_size_itms         = new CheckboxMenuItem[NUM_SIZES];
        mnu_parameters_mnu_size_itms[XSMALL] = (CheckboxMenuItem) mnu_parameters_mnu_size.add(new CheckboxMenuItem("xsmall"));
        mnu_parameters_mnu_size_itms[SMALL]  = (CheckboxMenuItem) mnu_parameters_mnu_size.add(new CheckboxMenuItem("small"));
        mnu_parameters_mnu_size_itms[MEDIUM] = (CheckboxMenuItem) mnu_parameters_mnu_size.add(new CheckboxMenuItem("medium"));
        mnu_parameters_mnu_size_itms[LARGE]  = (CheckboxMenuItem) mnu_parameters_mnu_size.add(new CheckboxMenuItem("large"));
        mnu_parameters_mnu_size_itms[XLARGE] = (CheckboxMenuItem) mnu_parameters_mnu_size.add(new CheckboxMenuItem("xlarge"));

        mnu_parameters_mnu_speed             = (Menu) mnu_parameters.add(new Menu("Speed"));
        mnu_parameters_mnu_speed_itms        = new CheckboxMenuItem[NUM_SPEEDS];
        mnu_parameters_mnu_speed_itms[XSLOW] = (CheckboxMenuItem) mnu_parameters_mnu_speed.add(new CheckboxMenuItem("xslow"));
        mnu_parameters_mnu_speed_itms[SLOW]  = (CheckboxMenuItem) mnu_parameters_mnu_speed.add(new CheckboxMenuItem("slow"));
        mnu_parameters_mnu_speed_itms[NORMAL]= (CheckboxMenuItem) mnu_parameters_mnu_speed.add(new CheckboxMenuItem("normal"));
        mnu_parameters_mnu_speed_itms[FAST]  = (CheckboxMenuItem) mnu_parameters_mnu_speed.add(new CheckboxMenuItem("fast"));
        mnu_parameters_mnu_speed_itms[XFAST] = (CheckboxMenuItem) mnu_parameters_mnu_speed.add(new CheckboxMenuItem("xfast"));

        mnu_environment                         = menubar.add(new Menu("Environment"));
        mnu_environment_itms                    = new CheckboxMenuItem[NUM_PLANETS];
        mnu_environment_itms[MERCURY]           = (CheckboxMenuItem) mnu_environment.add(new CheckboxMenuItem("Mercury"));
        mnu_environment_itms[VENUS]             = (CheckboxMenuItem) mnu_environment.add(new CheckboxMenuItem("Venus"));
        mnu_environment_itms[EARTH]             = (CheckboxMenuItem) mnu_environment.add(new CheckboxMenuItem("Earth"));
        mnu_environment_itms[MARS]              = (CheckboxMenuItem) mnu_environment.add(new CheckboxMenuItem("Mars"));
        mnu_environment_itms[JUPITER]           = (CheckboxMenuItem) mnu_environment.add(new CheckboxMenuItem("Jupiter"));
        mnu_environment_itms[SATURN]            = (CheckboxMenuItem) mnu_environment.add(new CheckboxMenuItem("Saturn"));
        mnu_environment_itms[URANUS]            = (CheckboxMenuItem) mnu_environment.add(new CheckboxMenuItem("Uranus"));
        mnu_environment_itms[NEPTUNE]           = (CheckboxMenuItem) mnu_environment.add(new CheckboxMenuItem("Neptune"));
        mnu_environment_itms[PLUTO]             = (CheckboxMenuItem) mnu_environment.add(new CheckboxMenuItem("PLUTO"));

        mnu_debuginfo                           = menubar.add(new Menu("Info"));
        mnu_debuginfo_itms                      = new CheckboxMenuItem[NUM_DEBUG_LEVELS];
        mnu_debuginfo_itms[NODEBUG]             = (CheckboxMenuItem) mnu_debuginfo.add(new CheckboxMenuItem("none"));
        mnu_debuginfo_itms[DB1]                 = (CheckboxMenuItem) mnu_debuginfo.add(new CheckboxMenuItem("level 1"));
        mnu_debuginfo_itms[DB2]                 = (CheckboxMenuItem) mnu_debuginfo.add(new CheckboxMenuItem("level 2"));
        mnu_debuginfo_itms[DB3]                 = (CheckboxMenuItem) mnu_debuginfo.add(new CheckboxMenuItem("level 3"));

        mnu_extras                              = menubar.add(new Menu("Extras"));
        mnu_extras_itms                         = new CheckboxMenuItem[NUM_EXTRAS];
        mnu_extras_itms[FERTILEBALLS]           = (CheckboxMenuItem) mnu_extras.add(new CheckboxMenuItem("fertile balls"));
        mnu_extras_itms[DRAWHITBOXES]           = (CheckboxMenuItem) mnu_extras.add(new CheckboxMenuItem("draw hitboxes"));
        mnu_extras_itms[BUBBLEGRAVITY]          = (CheckboxMenuItem) mnu_extras.add(new CheckboxMenuItem("bubble gravity"));

        // Control panel — scrollbars and labels
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=1; gbc.weightx=1;   gbc.ipady=2; gbc.insets=new Insets(10,10,0,10); sb_cannon_force = new Scrollbar(Scrollbar.HORIZONTAL);     pnl_controls.add(sb_cannon_force, gbc);
        gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=1; gbc.weightx=1;   gbc.ipady=1; gbc.insets=new Insets(0,10,5,10);  lbl_cannon_force = new Label("Force: ?px/s", Label.CENTER); pnl_controls.add(lbl_cannon_force, gbc);
        gbc.gridx=1; gbc.gridy=0; gbc.gridwidth=1; gbc.weightx=.5;  gbc.ipady=1; gbc.insets=new Insets(5,10,0,0);   lbl_score_ball   = new Label("Bubble: ",     Label.CENTER); pnl_controls.add(lbl_score_ball, gbc);
        gbc.gridx=1; gbc.gridy=1; gbc.gridwidth=2; gbc.weightx=.75; gbc.ipady=1; gbc.insets=new Insets(0,10,5,10);  lbl_time         = new Label("Time: ?s",     Label.CENTER); pnl_controls.add(lbl_time, gbc);
        gbc.gridx=2; gbc.gridy=0; gbc.gridwidth=1; gbc.weightx=.5;  gbc.ipady=1; gbc.insets=new Insets(5,0,0,10);   lbl_score_player = new Label("Player: ",     Label.CENTER); pnl_controls.add(lbl_score_player, gbc);
        gbc.gridx=3; gbc.gridy=0; gbc.gridwidth=1; gbc.weightx=1;   gbc.ipady=2; gbc.insets=new Insets(10,10,0,10); sb_cannon_angle  = new Scrollbar(Scrollbar.HORIZONTAL);     pnl_controls.add(sb_cannon_angle, gbc);
        gbc.gridx=3; gbc.gridy=1; gbc.gridwidth=1; gbc.weightx=1;   gbc.ipady=1; gbc.insets=new Insets(0,10,5,10);  lbl_cannon_angle = new Label("Angle: ?deg",  Label.CENTER); pnl_controls.add(lbl_cannon_angle, gbc);

        sb_cannon_force.setBackground(pnl_controls.getBackground().darker());
        sb_cannon_angle.setBackground(pnl_controls.getBackground().darker());

        // Angle: range 0..900, visible 100 → effective range 0..900 degrees×0.1 = 0..90 degrees
        sb_cannon_angle.setMinimum(0); sb_cannon_angle.setMaximum(1000); sb_cannon_angle.setVisibleAmount(100);
        sb_cannon_angle.setBlockIncrement(45); sb_cannon_angle.setUnitIncrement(9);

        sb_cannon_force.setMinimum(0); sb_cannon_force.setMaximum(800); sb_cannon_force.setVisibleAmount(100);
        sb_cannon_force.setBlockIncrement(25); sb_cannon_force.setUnitIncrement(10);
        sb_cannon_force.setValue(250);

        // Assemble window
        window.setMenuBar(menubar);
        window.add("Center", pnl_display);
        window.add("South",  pnl_controls);
        pnl_display.add("Center", display);

        // Radio defaults
        mnu_control_itms[PAUSE].setEnabled(false);
        set_mradio(mnu_parameters_mnu_size_itms,  NUM_SIZES,        MEDIUM);
        set_mradio(mnu_parameters_mnu_speed_itms, NUM_SPEEDS,       NORMAL);
        set_mradio(mnu_environment_itms,          NUM_PLANETS,      EARTH);
        set_mradio(mnu_debuginfo_itms,            NUM_DEBUG_LEVELS, NODEBUG);

        engine.set_gravity(GRAVITIES[EARTH]);
        engine.set_bubble_size(SIZES[MEDIUM]);
        engine.set_bubble_speed(SPEEDS[NORMAL]);
        engine.set_cannon_force(sb_cannon_force.getValue());
        display.debug_lvl = 0;

        // Attach listeners
        for (MenuItem mi : mnu_control_itms)               mi.addActionListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_size_itms)  mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_speed_itms) mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_environment_itms)   mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_debuginfo_itms)     mi.addItemListener(this);
        for (CheckboxMenuItem mi : mnu_extras_itms)        mi.addItemListener(this);
        sb_cannon_angle.addAdjustmentListener(this);
        sb_cannon_force.addAdjustmentListener(this);
        window.addWindowListener(this);
        window.addComponentListener(this);
        display.addMouseListener(engine);
        display.addMouseMotionListener(engine);

        window.validate();
        window.setVisible(true);
        start_thread();
    }

    private void exit() {
        display.removeMouseListener(engine);
        display.removeMouseMotionListener(engine);
        sb_cannon_angle.removeAdjustmentListener(this);
        sb_cannon_force.removeAdjustmentListener(this);
        for (MenuItem mi : mnu_control_itms)               mi.removeActionListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_size_itms)  mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_parameters_mnu_speed_itms) mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_environment_itms)   mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_debuginfo_itms)     mi.removeItemListener(this);
        for (CheckboxMenuItem mi : mnu_extras_itms)        mi.removeItemListener(this);
        window.removeWindowListener(this);
        window.removeComponentListener(this);
        stop_thread();
        window.dispose();
        System.exit(0);
    }

    private void start_thread() {
        if (main_thread == null) {
            main_thread_running = true;
            main_thread = new Thread(this, "CannonVSBall-main");
            main_thread.setDaemon(true);
            main_thread.start();
        }
    }

    private void stop_thread() {
        if (main_thread != null) {
            main_thread_running = false;
            main_thread.interrupt();
            main_thread = null;
        }
    }

    @Override
    public void run() {
        int paintlimiter = 0;
        long frame_start_t = System.nanoTime();
        while (main_thread_running) {
            final long now = System.nanoTime();
            final double delta_t = (now - frame_start_t) / 1_000_000_000.0;
            frame_start_t = now;

            // Sync scrollbars and labels
            final int angleVal = (int)(engine.get_cannon_angle() * 10);
            sb_cannon_angle.setValue(angleVal);
            lbl_cannon_angle.setText("Angle: " + (angleVal / 10.0) + "deg");

            final int forceVal = (int) engine.get_cannon_force();
            sb_cannon_force.setValue(forceVal);
            lbl_cannon_force.setText("Force: " + forceVal + "px/s");

            lbl_time.setText("Time: " + ((int)(engine.get_elapsed_time() * 10)) / 10.0);
            lbl_score_ball.setText("Bubbles: " + engine.get_score_bubbles());
            lbl_score_player.setText("Player: " + engine.get_score_player());

            engine.tick(delta_t);
            display.debug_inform_ticktime(delta_t);

            if (++paintlimiter > 2) {
                display.repaint();
                paintlimiter = 0;
            }

            try { Thread.sleep(THREAD_SLEEP_MS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        final Object bar = e.getSource();
        if (bar == sb_cannon_angle) {
            engine.set_cannon_angle(sb_cannon_angle.getValue() / 10.0);
        } else if (bar == sb_cannon_force) {
            engine.set_cannon_force(sb_cannon_force.getValue());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Object item = e.getSource();
        final int idx = find_mitem(mnu_control_itms, NUM_CONTROLS, item);
        switch (idx) {
            case RUN:
            case PAUSE:
                engine.set_pause(!engine.is_paused());
                mnu_control_itms[RUN].setEnabled(engine.is_paused());
                mnu_control_itms[PAUSE].setEnabled(!engine.is_paused());
                break;
            case RESTART:
                engine.restart();
                mnu_control_itms[RUN].setEnabled(true);
                mnu_control_itms[PAUSE].setEnabled(false);
                break;
            case QUIT:
                window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                break;
            default:
                if (idx != -1) System.out.println("err: unmatched control menu item: " + item);
        }
    }

    /** Returns index of {@code item} in {@code items[0..size)}, or -1 if absent. */
    private int find_mitem(MenuItem[] items, int size, Object item) {
        for (int i = 0; i < size; i++) if (item == items[i]) return i;
        return -1;
    }

    /** Sets {@code radios[radio]} checked and clears all others. */
    private void set_mradio(CheckboxMenuItem[] radios, int size, int radio) {
        for (int i = 0; i < size; i++) radios[i].setState(i == radio);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        final Object item = e.getSource();
        int radio;
        if ((radio = find_mitem(mnu_parameters_mnu_size_itms, NUM_SIZES, item)) >= 0) {
            set_mradio(mnu_parameters_mnu_size_itms, NUM_SIZES, radio);
            engine.set_bubble_size(SIZES[radio]);
        } else if ((radio = find_mitem(mnu_parameters_mnu_speed_itms, NUM_SPEEDS, item)) >= 0) {
            set_mradio(mnu_parameters_mnu_speed_itms, NUM_SPEEDS, radio);
            engine.set_bubble_speed(SPEEDS[radio]);
        } else if ((radio = find_mitem(mnu_environment_itms, NUM_PLANETS, item)) >= 0) {
            set_mradio(mnu_environment_itms, NUM_PLANETS, radio);
            engine.set_gravity(GRAVITIES[radio]);
        } else if ((radio = find_mitem(mnu_debuginfo_itms, NUM_DEBUG_LEVELS, item)) >= 0) {
            set_mradio(mnu_debuginfo_itms, NUM_DEBUG_LEVELS, radio);
            display.debug_lvl = radio;
        } else if ((radio = find_mitem(mnu_extras_itms, NUM_EXTRAS, item)) >= 0) {
            switch (radio) {
                case FERTILEBALLS:  engine.set_m_fertileballs(mnu_extras_itms[radio].getState());  break;
                case DRAWHITBOXES:  engine.set_m_drawhitboxes(mnu_extras_itms[radio].getState());  break;
                case BUBBLEGRAVITY: engine.set_m_bubblegravity(mnu_extras_itms[radio].getState()); break;
            }
        }
    }

    // Greetings on open/close — constrained to [0,3] so default is never reached accidentally.
    private static final String[] OPEN_MSGS  = { "Would you like to play a game?", "Life? Don't talk to me about life.", "Hello there.", ":-)" };
    private static final String[] CLOSE_MSGS = { "So soon?", "We'd only just begun...", "Come back!", "Daisy..  Daisy..." };

    @Override public void windowOpened(WindowEvent e)   { System.out.println(OPEN_MSGS[(int)(Math.random() * OPEN_MSGS.length)]); }
    @Override public void windowClosing(WindowEvent e)  { System.out.println(CLOSE_MSGS[(int)(Math.random() * CLOSE_MSGS.length)]); exit(); }
    @Override public void windowActivated(WindowEvent e)   {}
    @Override public void windowDeactivated(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e)   {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowClosed(WindowEvent e)      {}

    @Override
    public void componentResized(ComponentEvent e) {
        engine.set_world_size(new Dimension(pnl_display.getWidth() - 2, pnl_display.getHeight() - 2));
    }
    @Override public void componentHidden(ComponentEvent e)  {}
    @Override public void componentShown(ComponentEvent e)   {}
    @Override public void componentMoved(ComponentEvent e)   {}
}


// ----------------
// The Game Engine
// Holds world data, runs physics and collision, handles mouse input.
// ----------------
class CannonBallEngine implements MouseListener, MouseMotionListener {

    private static final double PIXELS_PER_METER = 10.0;
    private static final Dimension MIN_WORLD_SIZE = new Dimension(256, 256);

    private final CannonBallRenderer r;

    // Game control flags
    private boolean physics_paused;
    private boolean restart_game;

    // Mode flags
    private boolean m_fertileballs;
    private boolean m_drawhitboxes;
    private boolean m_bubblegravity;

    // Pending values (written from UI thread, applied at tick start)
    // [0] = current value used in tick(), [1] = pending value set by UI
    private final double[] cannon_force;
    private final double[] cannon_angle;   // radians internally; degrees on the setter interface
    private final double[] bubble_size;    // pixels
    private final double[] bubble_speed;   // pixels/second
    private final double[] world_gravity;  // pixels/s²
    private final Dimension[] world_size;  // pixels

    private final Rectangle world_perim;

    private int score_player;
    private int score_bubbles;
    private double elapsed_time;
    private double timer_fertileballs;
    private boolean fire_cannon;

    // Game objects  (ArrayList is unsynchronized → no per-call locking overhead vs Vector)
    private final Cannon can;
    private ArrayList<Bubble>    bubbs;
    private ArrayList<Balloid>   balls;
    private ArrayList<Rectangle> rects;

    // Mouse state
    private Point m1, m2;
    private boolean dragoff;
    private Rectangle dragbox;

    // Pending events — written from AWT thread, consumed in tick()
    private volatile Rectangle addrect_pending;
    private Rectangle addrect_applied;
    private volatile MouseEvent mouseclick_pending;
    private MouseEvent mouseclick_applied;

    // Scratch rectangles reused every tick to avoid per-frame allocation
    private final Rectangle scratch_intersection = new Rectangle();

    public CannonBallEngine() {
        cannon_angle  = new double[]{1, Math.PI - Math.random() * Math.PI / 2};
        cannon_force  = new double[]{1, PIXELS_PER_METER * 2};
        bubble_size   = new double[]{1, PIXELS_PER_METER / 2 + 1};
        bubble_speed  = new double[]{1, PIXELS_PER_METER};
        world_gravity = new double[]{1, 9.8 * PIXELS_PER_METER};
        world_size    = new Dimension[]{new Dimension(MIN_WORLD_SIZE), new Dimension(MIN_WORLD_SIZE)};
        world_perim   = new Rectangle(0, 0, world_size[0].width, world_size[0].height);

        can = new Cannon((int)(PIXELS_PER_METER * 12), (int)(PIXELS_PER_METER * 3),
                         new Point((int) PIXELS_PER_METER, (int) PIXELS_PER_METER));

        m1 = null; m2 = null;
        dragoff = false; dragbox = null;
        addrect_pending = null; addrect_applied = null;
        mouseclick_pending = null; mouseclick_applied = null;

        physics_paused = true;
        restart_game = false;
        m_fertileballs = false;
        m_drawhitboxes = false;
        m_bubblegravity = false;
        fire_cannon = false;

        r = new CannonBallRenderer(MIN_WORLD_SIZE);
        init_game_state();
    }

    /** Resets scores, timers, and spawns the initial bubble. */
    private void init_game_state() {
        score_player = 0;
        score_bubbles = 0;
        elapsed_time = 0;
        timer_fertileballs = 0;
        set_cannon_angle(Math.random() * 90);
        rects = new ArrayList<>();
        bubbs = new ArrayList<>();
        balls = new ArrayList<>();
        bubbs.add(new Bubble(null));
    }

    // ---- Getters / Setters (called from UI thread) ----
    public void set_world_size(Dimension dim)   { world_size[1].setSize(Math.max(MIN_WORLD_SIZE.width, dim.width), Math.max(MIN_WORLD_SIZE.height, dim.height)); }
    public void set_bubble_size(double m)        { bubble_size[1]   = m * PIXELS_PER_METER; }
    public void set_bubble_speed(double mps)     { bubble_speed[1]  = mps * PIXELS_PER_METER; }
    public void set_cannon_angle(double degrees) { cannon_angle[1]  = Math.toRadians(180 - Math.min(90, Math.max(0, degrees))); }
    public void set_cannon_force(double force)   { cannon_force[1]  = force; }
    public void set_gravity(double mpsps)        { world_gravity[1] = mpsps * PIXELS_PER_METER; }
    public void set_m_fertileballs(boolean m)    { m_fertileballs   = m; }
    public void set_m_drawhitboxes(boolean m)    { m_drawhitboxes   = m; r.redrawAll(); }
    public void set_m_bubblegravity(boolean m)   { m_bubblegravity  = m; }
    public void set_pause(boolean p)             { physics_paused   = p; }
    public void restart()                        { set_pause(true); restart_game = true; }
    public boolean is_paused()                   { return physics_paused; }
    public Renderer renderer()                   { return r; }
    public double get_cannon_angle()             { return Math.toDegrees(Math.PI - cannon_angle[0]); }
    public double get_cannon_force()             { return cannon_force[0]; }
    public double get_elapsed_time()             { return elapsed_time; }
    public int    get_score_bubbles()            { return score_bubbles; }
    public int    get_score_player()             { return score_player; }
    public void   fire_cannon()                  { fire_cannon = true; }

    // ---- Tick ----
    public void tick(double delta_t) {

        // Apply pending engine values
        cannon_force[0]  = cannon_force[1];
        world_gravity[0] = world_gravity[1];

        // Pending rectangle add
        final Rectangle pending_rect = addrect_pending;
        if (pending_rect != addrect_applied) {
            addrect_applied = pending_rect;
            if (pending_rect != null && !pending_rect.isEmpty() && !pending_rect.intersects(can.hitbox())) {
                boolean ok = true;
                for (int i = 0; ok && i < bubbs.size(); i++) ok = !pending_rect.intersects(bubbs.get(i).hitbox());
                for (int i = 0; ok && i < balls.size(); i++) ok = !pending_rect.intersects(balls.get(i).hitbox());
                for (int i = 0; ok && i < rects.size(); i++) {
                    if (pending_rect.contains(rects.get(i))) { rects.remove(i--); }
                    else if (rects.get(i).contains(pending_rect)) { ok = false; }
                }
                if (ok) { rects.add(pending_rect); r.redraw(r.l_statics); }
            }
        }

        // Pending mouse click
        final MouseEvent pending_click = mouseclick_pending;
        if (pending_click != mouseclick_applied) {
            mouseclick_applied = pending_click;
            if (pending_click != null) {
                Point p = pending_click.getPoint();
                p = new Point(p.x - r.BORDER, p.y - r.BORDER);
                if (pending_click.getButton() == MouseEvent.BUTTON3) {
                    for (int i = 0; i < rects.size(); i++) {
                        if (rects.get(i).contains(p)) { rects.remove(i); r.redraw(r.l_statics); break; }
                    }
                } else if (pending_click.getButton() == MouseEvent.BUTTON1) {
                    if (can.hitbox().contains(p)) fire_cannon();
                }
            }
        }

        // Cannon angle change
        if (cannon_angle[0] != cannon_angle[1]) {
            cannon_angle[0] = cannon_angle[1];
            can.aim(cannon_angle[0]);
            r.redraw(r.l_cannon);
        }

        // Bubble speed change
        if (bubble_speed[0] != bubble_speed[1]) {
            bubble_speed[0] = bubble_speed[1];
            for (Bubble bubb : bubbs) bubb.refresh_speed();
        }

        // Bubble size change
        if (bubble_size[0] != bubble_size[1]) {
            bubble_size[0] = bubble_size[1];
            for (Bubble bubb : bubbs) bubb.refresh_size();
            r.redraw(r.l_bubbles);
        }

        // World size change
        if (!world_size[0].equals(world_size[1])) {
            world_size[0].setSize(world_size[1]);
            world_perim.setSize(world_size[0].width, world_size[0].height);
            can.refresh_hitbox();
            for (Bubble bubb : bubbs) {
                if      (bubb.tl_x() <= world_perim.x)          bubb.set_pos_x(bubb.radius + 1);
                else if (bubb.br_x() >= world_perim.width)      bubb.set_pos_x(world_perim.width  - bubb.radius - 1);
                if      (bubb.tl_y() <= world_perim.y)          bubb.set_pos_y(bubb.radius + 1);
                else if (bubb.br_y() >= world_perim.height)     bubb.set_pos_y(world_perim.height - bubb.radius - 1);
            }
            r.set_resolution(world_size[0]);
            r.redrawAll();
        }

        // Physics
        if (!physics_paused) {
            // Fertile balls mode: spawn a new bubble every 3 seconds
            if (m_fertileballs) {
                timer_fertileballs += delta_t;
                if (timer_fertileballs > 3) {
                    bubbs.add(new Bubble(bubbs.isEmpty() ? null : bubbs.get(0).pos()));
                    timer_fertileballs = 0;
                }
            }

            // Fire cannon
            if (fire_cannon) {
                balls.add(new Balloid());
                r.redraw(r.l_balloids);
                fire_cannon = false;
            }

            // Bubble physics
            final Vec2 normal = new Vec2(0, 0);
            final Vec2 forces = new Vec2(0, m_bubblegravity ? world_gravity[0] * delta_t : 0);

            for (Bubble bubb : bubbs) {
                normal.x = 0; normal.y = 0;
                final Rectangle hb   = bubb.next_hitbox(delta_t);
                final Rectangle isec = hb.intersection(world_perim);

                // Wall bounces
                if (hb.width != isec.width) {
                    normal.x = bubb.vel_x() * -2;
                    bubb.set_pos_x(hb.x <= world_perim.x ? bubb.radius + 1 : world_perim.width - bubb.radius - 1);
                }
                if (hb.height != isec.height) {
                    normal.y = bubb.vel_y() * -2;
                    bubb.set_pos_y(hb.y <= world_perim.y ? bubb.radius + 1 : world_perim.height - bubb.radius - 1);
                }

                if (bubb.nocollide) {
                    // Still in unsafe spawn; clear flag once clear of everything
                    bubb.nocollide = false;
                    for (Rectangle rect : rects) bubb.nocollide |= hb.intersects(rect);
                    bubb.nocollide |= hb.intersects(can.hitbox());
                } else {
                    for (Rectangle rect : rects) {
                        scratch_intersection.setBounds(hb.intersection(rect));
                        if (!scratch_intersection.isEmpty()) {
                            if (scratch_intersection.height > scratch_intersection.width) normal.x = bubb.vel_x() * -2;
                            else                                                          normal.y = bubb.vel_y() * -2;
                        }
                    }
                    if (hb.intersects(can.hitbox())) {
                        score_bubbles++;
                        bubb.nocollide = true;
                        bubb.set_pos_x(bubb.radius() + 1 + Math.random() * world_size[0].width  * 0.6 - bubb.radius() - 1);
                        bubb.set_pos_y(bubb.radius() + 1 + Math.random() * world_size[0].height - bubb.radius() - 1);
                    }
                }

                bubb.accelerate(normal);
                bubb.accelerate(forces);
                bubb.advance(delta_t);
            }

            // Balloid physics
            final Vec2 ball_normal = new Vec2(0, 0);
            final Vec2 ball_forces = new Vec2(0, world_gravity[0] * delta_t);

            for (int i = 0; i < balls.size(); i++) {
                final Balloid ball = balls.get(i);
                ball_normal.x = 0; ball_normal.y = 0;
                final Rectangle bhb = ball.next_hitbox(delta_t);

                // Bounce off / destroy rects
                for (int j = 0; j < rects.size(); j++) {
                    scratch_intersection.setBounds(bhb.intersection(rects.get(j)));
                    if (!scratch_intersection.isEmpty()) {
                        if (scratch_intersection.height > scratch_intersection.width) ball_normal.x = ball.vel_x() * -2;
                        else                                                          ball_normal.y = ball.vel_y() * -2;
                        rects.remove(j);
                        r.redraw(r.l_statics);
                        break;
                    }
                }

                // Collide with bubbles
                boolean removed = false;
                for (int j = 0; j < bubbs.size() && !removed; j++) {
                    if (!bhb.intersection(bubbs.get(j).hitbox()).isEmpty()) {
                        bubbs.remove(j);
                        bubbs.add(new Bubble(null));
                        score_player++;
                        balls.remove(i--);
                        removed = true;
                        r.redraw(r.l_bubbles);
                    }
                }

                // Collide with cannon (after 1 second of lifetime — invincibility frames)
                if (!removed && ball.lifetime > 1 && bhb.intersects(can.hitbox())) {
                    balls.remove(i--);
                    removed = true;
                    score_bubbles++;
                }

                if (!removed) {
                    ball.accelerate(ball_normal);
                    ball.accelerate(ball_forces);
                    ball.advance(delta_t);
                    ball.lifetime += delta_t;
                    if (ball.offscreen()) {
                        balls.remove(i--);
                        System.out.println("Balloid has left the building.");
                    }
                }
            }

            r.redraw(r.l_balloids);
            r.redraw(r.l_bubbles);
            elapsed_time += delta_t;

        } else if (restart_game) {
            init_game_state();
            r.redraw(r.l_bubbles);
            r.redraw(r.l_cannon);
            r.redraw(r.l_statics);
            r.redraw(r.l_balloids);
            restart_game = false;
        }
    }

    // ---- Mouse Listeners ----
    @Override public void mousePressed(MouseEvent e)  { m1 = e.getPoint(); dragbox = new Rectangle(); }
    @Override public void mouseDragged(MouseEvent e)  {
        m2 = e.getPoint();
        if (!dragoff) {
            dragbox.setLocation(
                Math.max(r.BORDER, Math.min(Math.min(m1.x, m2.x), r.res_x() - r.BORDER * 2)),
                Math.max(r.BORDER, Math.min(Math.min(m1.y, m2.y), r.res_y() - r.BORDER * 2)));
            dragbox.setSize(
                Math.min(Math.abs(m1.x - m2.x), r.res_x() - r.BORDER * 2 - dragbox.x),
                Math.min(Math.abs(m1.y - m2.y), r.res_y() - r.BORDER * 2 - dragbox.y));
        }
        r.redraw(r.l_dragbox);
    }
    @Override public void mouseReleased(MouseEvent e) {
        if (m2 == null) mouseclick_pending = e;  // No drag occurred → treat as click
        addrect_pending = dragbox;
        dragbox = null; m1 = null; m2 = null;
        r.redraw(r.l_dragbox);
    }
    @Override public void mouseExited(MouseEvent e)  { dragoff = true; }
    @Override public void mouseEntered(MouseEvent e) { dragoff = false; }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e)   {}


    // ============================================================
    // Inner: Renderer
    // ============================================================
    class CannonBallRenderer extends Renderer {
        public final int l_background, l_statics, l_bubbles, l_balloids, l_cannon, l_dragbox;
        public final int BORDER = 1;
        public Point tl, br;

        public CannonBallRenderer(Dimension resolution) {
            super(6, new Dimension(resolution.width + 2, resolution.height + 2));
            l_background = LAYERS[0];
            l_statics    = LAYERS[1];
            l_bubbles    = LAYERS[2];
            l_balloids   = LAYERS[3];
            l_cannon     = LAYERS[4];
            l_dragbox    = LAYERS[5];
            tl = new Point(BORDER, BORDER);
            br = new Point(res_x() - BORDER, res_y() - BORDER);
        }

        @Override
        public void draw(int layer, Graphics g) {
            try {
                if      (layer == l_background) draw_background(g);
                else if (layer == l_statics)    draw_statics(g);
                else if (layer == l_bubbles)    draw_bubbles(g);
                else if (layer == l_balloids)   draw_balloids(g);
                else if (layer == l_cannon)     draw_cannon(g);
                else if (layer == l_dragbox)    draw_dragbox(g);
                else System.out.println("err: bad layer code in draw");
            } catch (java.util.ConcurrentModificationException e) {
                System.out.println("Everything is fine.");
            }
        }

        private void draw_background(Graphics g) {
            g.setColor(Color.darkGray);
            g.fillRect(0, 0, res_x(), res_y());
            g.setColor(Color.blue);
            g.drawRect(0, 0, res_x() - 1, res_y() - 1);
        }

        private void draw_statics(Graphics g) {
            g.setColor(Color.orange.darker());
            for (Rectangle rect : rects) {
                g.fillRect(rect.x, rect.y, rect.width, rect.height);
                if (m_drawhitboxes) {
                    g.setColor(Color.red);
                    g.drawRect(rect.x, rect.y, rect.width, rect.height);
                    g.setColor(Color.orange.darker());
                }
            }
        }

        private void draw_bubbles(Graphics g) {
            g.setColor(new Color(155, 255, 255, 50));
            for (Bubble bubb : bubbs) {
                g.fillOval(BORDER + bubb.tl_x(), BORDER + bubb.tl_y(),
                           (int)(bubb.radius() * 2 + 1), (int)(bubb.radius() * 2 + 1));
                if (m_drawhitboxes) {
                    final Rectangle hb = bubb.hitbox();
                    g.drawRect(hb.x, hb.y, hb.width, hb.height);
                }
            }
        }

        private void draw_balloids(Graphics g) {
            g.setColor(new Color(180, 255, 180, 180));
            for (Balloid ball : balls) {
                g.fillOval(BORDER + ball.tl_x(), BORDER + ball.tl_y(),
                           (int)(ball.radius() * 2 + 1), (int)(ball.radius() * 2 + 1));
                if (m_drawhitboxes) {
                    g.setColor(Color.green);
                    final Rectangle hb = ball.hitbox();
                    g.drawRect(hb.x, hb.y, hb.width, hb.height);
                    g.setColor(new Color(180, 255, 180, 180));
                }
            }
        }

        private void draw_cannon(Graphics g) {
            final Point a = can.tip();
            final Point b = can.tail();
            final Point z = can.sidediff();

            g.setColor(new Color(48, 39, 14, 255));
            g.fillOval((int)(br.x - PIXELS_PER_METER * 7.5), (int)(br.y - PIXELS_PER_METER * 6),
                       (int)(PIXELS_PER_METER * 7.5), (int)(PIXELS_PER_METER * 6));
            g.setColor(new Color(36, 38, 17));
            g.fillPolygon(new int[]{a.x+z.x, b.x+z.x, b.x-z.x, a.x-z.x},
                          new int[]{a.y-z.y, b.y-z.y, b.y+z.y, a.y+z.y}, 4);
            g.setColor(new Color(74, 60, 20, 120));
            g.fillOval((int)(br.x - PIXELS_PER_METER * 8), (int)(br.y - PIXELS_PER_METER * 5.5),
                       (int)(PIXELS_PER_METER * 7.5), (int)(PIXELS_PER_METER * 6));

            if (m_drawhitboxes) {
                g.setColor(Color.red);
                g.drawPolygon(new int[]{a.x+z.x, b.x+z.x, b.x-z.x, a.x-z.x},
                              new int[]{a.y-z.y, b.y-z.y, b.y+z.y, a.y+z.y}, 4);
                g.setColor(Color.blue);
                g.drawLine(a.x, a.y, b.x, b.y);
                g.setColor(Color.magenta);
                final Rectangle hb = can.hitbox();
                g.drawRect(hb.x, hb.y, hb.width, hb.height);
            }
        }

        private void draw_dragbox(Graphics g) {
            if (dragbox != null) {
                g.setColor(Color.white);
                g.drawRect(dragbox.x, dragbox.y, dragbox.width, dragbox.height);
            }
        }

        @Override
        public void set_resolution(Dimension res) {
            super.set_resolution(new Dimension(res.width + 2, res.height + 2));
            br = new Point(res_x() - BORDER, res_y() - BORDER);
        }
    }


    // ============================================================
    // Inner: Cannon
    // ============================================================
    class Cannon {
        private final Point corner_offset;
        private final int length, diameter;
        private double angle;
        private final Rectangle hitbox;

        public Cannon(int l, int d, Point offset) {
            diameter      = d;
            length        = l;
            angle         = Math.PI / 2 + Math.PI / 4 + Math.PI / 8;
            corner_offset = new Point(d / 2 + 1 + offset.x, d / 2 + 1 + offset.y);
            hitbox        = new Rectangle();
            refresh_hitbox();
        }

        public void aim(double rad) {
            angle = Math.min(Math.PI, Math.max(Math.PI / 2, rad));
            refresh_hitbox();
        }

        public void refresh_hitbox() {
            final Point a = tip(), b = tail(), z = sidediff();
            final int x1 = Math.min(a.x - z.x, b.x - z.x);
            final int y1 = Math.min(a.y - z.y, b.y - z.y);
            final int x2 = Math.max(a.x + z.x, b.x + z.x);
            final int y2 = Math.max(a.y + z.y, b.y + z.y);
            hitbox.setBounds(x1, y1, x2 - x1, y2 - y1);
        }

        public double    angle()   { return angle; }
        public Rectangle hitbox()  { return hitbox; }

        public Point tip() {
            final Point t = tail();
            t.x += (int)(Math.cos(angle) * length);
            t.y -= (int)(Math.sin(angle) * length);
            return t;
        }
        public Point tail() {
            return new Point(world_size[0].width - corner_offset.x, world_size[0].height - corner_offset.y);
        }
        public Point sidediff() {
            return new Point(
                (int) Math.abs(Math.cos(angle - Math.PI / 2) * diameter / 2),
                (int) Math.abs(Math.sin(angle - Math.PI / 2) * diameter / 2));
        }
    }


    // ============================================================
    // Inner: Ball (abstract base)
    // ============================================================
    abstract class Ball {
        protected Vec2 pos, vel;
        protected double radius;
        protected Rectangle hitbox;

        protected Rectangle gen_hitbox(Vec2 p) {
            return new Rectangle((int)(p.x - radius - 1), (int)(p.y - radius - 1),
                                 (int)(radius * 2 + 1),   (int)(radius * 2 + 1));
        }
        protected void refresh_hitbox() {
            hitbox.setBounds((int)(pos.x - radius - 1), (int)(pos.y - radius - 1),
                             (int)(radius * 2 + 1),     (int)(radius * 2 + 1));
        }

        public Rectangle hitbox()                    { return hitbox; }
        public Rectangle next_hitbox(double delta_t) { return gen_hitbox(Vec2.add(pos, Vec2.mul(vel, delta_t))); }

        public void accelerate(Vec2 accel) { vel.add(accel); }
        public void advance(double delta_t) {
            pos.add(Vec2.mul(vel, delta_t));
            hitbox.setLocation((int)(pos.x - radius - 1), (int)(pos.y - radius - 1));
        }

        public double radius()  { return radius; }
        public Vec2   vel()     { return new Vec2(vel); }
        public Vec2   pos()     { return new Vec2(pos); }
        public double vel_x()   { return vel.x; }
        public double vel_y()   { return vel.y; }
        public double pos_x()   { return pos.x; }
        public double pos_y()   { return pos.y; }
        public int    tl_x()    { return (int)(pos.x - 1 - radius); }
        public int    tl_y()    { return (int)(pos.y - 1 - radius); }
        public int    br_x()    { return (int)(pos.x + radius); }
        public int    br_y()    { return (int)(pos.y + radius); }
    }


    // ============================================================
    // Inner: Bubble (target)
    // ============================================================
    class Bubble extends Ball {
        public boolean nocollide;

        public Bubble(Vec2 position) {
            nocollide = true;
            radius    = bubble_size[0];
            pos       = (position != null)
                ? new Vec2(position)
                : new Vec2(radius + 1 + Math.random() * world_size[0].width  * 0.75 - radius - 1,
                           radius + 1 + Math.random() * world_size[0].height - radius - 1);
            vel    = new Vec2(Math.random() < .5 ? 1 : -1, Math.random() < .5 ? 1 : -1);
            hitbox = gen_hitbox(pos);
            refresh_speed();
        }

        public void refresh_size() {
            final Rectangle new_hb = new Rectangle(hitbox);
            final int delta = (int)(bubble_size[0] - radius);
            new_hb.grow(delta, delta);
            final Rectangle isec = new_hb.intersection(world_perim);
            boolean ok = (new_hb.width == isec.width && new_hb.height == isec.height);
            for (int i = 0; ok && i < rects.size(); i++) ok = new_hb.intersection(rects.get(i)).isEmpty();
            if (ok) { radius = bubble_size[0]; refresh_hitbox(); }
        }

        public void refresh_speed() {
            final double sign_x = Math.signum(vel.x), sign_y = Math.signum(vel.y);
            final double speed  = bubble_speed[0] + Math.random() * bubble_speed[0] + 1;
            vel = new Vec2(sign_x * speed, sign_y * speed);
        }

        public void set_pos_x(double x) { pos.x = x; nocollide = true; refresh_hitbox(); }
        public void set_pos_y(double y) { pos.y = y; nocollide = true; refresh_hitbox(); }
    }


    // ============================================================
    // Inner: Balloid (cannonball)
    // ============================================================
    class Balloid extends Ball {
        public double lifetime;

        public Balloid() {
            radius   = 12.0;
            lifetime = 0;
            final Point tip = can.tip();
            pos = new Vec2(tip.x, tip.y);
            vel = new Vec2(Math.cos(can.angle()) * cannon_force[0],
                          -Math.sin(can.angle()) * cannon_force[0]);
            hitbox = gen_hitbox(pos);
        }

        public boolean offscreen() {
            return br_x() < 0 || tl_x() > world_size[0].width || tl_y() > world_size[0].height;
        }
    }
}


// ============================================================
// MultiBufferedCanvas — displays the composed render image
// ============================================================
class MultiBufferedCanvas extends Canvas {

    private final RenderComposer composer;
    private BufferedImage backbuff;

    public int debug_lvl;
    private String debug_msg;
    private long   debug_data_last_frame_t;
    private double debug_data_frametime;
    private double debug_data_ticktime;

    public MultiBufferedCanvas(Renderer r) {
        setBackground(new Color(10, 10, 10));
        composer = new RenderComposer(r);
        debug_lvl = 0;
        debug_msg = "";
        debug_data_last_frame_t = System.nanoTime();
        debug_data_frametime = 0;
        debug_data_ticktime  = 0;
    }

    /** Fallback constructor with a placeholder renderer. */
    public MultiBufferedCanvas() {
        this(new Renderer(1, new Dimension(256, 256)) {
            { redraw(1); }
            @Override public void draw(int layer, Graphics g) {
                g.setColor(Color.magenta); g.fillRect(0, 0, res_x()-1, res_y()-1);
                g.setColor(Color.black);   g.fillRect(3, 3, res_x()-7, res_y()-7);
                g.setColor(Color.red);     g.drawString("there's nothing", 7, res_y()/2);
            }
        });
    }

    public void set_renderer(Renderer r) { composer.set_renderer(r); }

    @Override
    public void update(Graphics g) {
        backbuff = composer.recompose();
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        if (backbuff != null) g.drawImage(backbuff, 0, 0, null);
        switch (debug_lvl) {
            case 3:
                debug_data_frametime = System.nanoTime() - debug_data_last_frame_t;
                debug_data_last_frame_t += (long) debug_data_frametime;
                debug_msg += "Ticktime: ~"  + (int)(debug_data_ticktime  * 1000)     + "ms | ";
                debug_msg += "Frametime: ~" + (int)(debug_data_frametime / 1_000_000) + "ms | ";
                debug_msg += "Framerate: ~" + (int)(1_000_000_000 / debug_data_frametime) + "fps | ";
                // fall through
            case 2:
                debug_msg += "Layer Count: "  + composer.info_layer_count() + " | ";
                debug_msg += "Draw Status: "  + Integer.toBinaryString(composer.info_redraw_status()) + " | ";
                // fall through
            case 1:
                debug_msg += "Renderer Resolution: " + composer.info_res_x() + "x" + composer.info_res_y() + " | ";
                debug_msg += "Canvas Resolution: "   + getWidth() + "x" + getHeight();
                g.setColor(Color.red);
                g.drawString("[" + debug_msg + "]", 12, 15);
                debug_msg = "";
                break;
        }
    }

    public void debug_inform_ticktime(double tt) { debug_data_ticktime = tt; }
}


// ============================================================
// Renderer — abstract layer manager
// ============================================================
abstract class Renderer {
    public final int   LAYER_COUNT;
    public final int[] LAYERS;

    private int       redraw;
    private Dimension resolution;
    private boolean   resolution_changed;

    public Renderer(int layer_count, Dimension res) {
        LAYER_COUNT = Math.min(8, Math.max(1, layer_count));
        LAYERS = new int[LAYER_COUNT];
        for (int i = 0; i < LAYER_COUNT; i++) LAYERS[i] = 1 << i;
        redraw = 0;
        resolution = res.getSize();
        resolution_changed = true;
    }

    public abstract void draw(int layer, Graphics g);

    /** Flag a specific layer for redraw. */
    public void redraw(int layer)   { redraw |= layer; }
    /** Flag all layers for redraw. */
    public void redrawAll()         { redraw = ~0; }
    public void redraw_clear()      { redraw = 0; }
    public int  redraw_status()     { return redraw; }

    public int  res_x()             { return resolution.width; }
    public int  res_y()             { return resolution.height; }
    protected void set_resolution(Dimension res) {
        resolution = res.getSize();
        resolution_changed = true;
    }

    public boolean reschange_status() { return resolution_changed; }
    public void    reschange_clear()  { resolution_changed = false; }
}


// ============================================================
// RenderComposer — manages layer BufferedImages, bakes final frame
// ============================================================
class RenderComposer {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    int draws;
    private Renderer      r;
    private BufferedImage composedbuff;
    private Graphics2D    composedbuff_gfx;
    private BufferedImage[] layerbuffs;
    private Graphics2D[]    layerbuffs_gfx;

    public RenderComposer(Renderer renderer) { set_renderer(renderer); }

    public void set_renderer(Renderer renderer) {
        r = renderer;
        layerbuffs     = new BufferedImage[r.LAYER_COUNT];
        layerbuffs_gfx = new Graphics2D[r.LAYER_COUNT];
        generate_buffers();
    }

    private void generate_buffers() {
        composedbuff     = new BufferedImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
        composedbuff_gfx = composedbuff.createGraphics();
        composedbuff_gfx.setBackground(TRANSPARENT);
        for (int i = 0; i < r.LAYER_COUNT; i++) {
            layerbuffs[i]     = new BufferedImage(r.res_x(), r.res_y(), BufferedImage.TYPE_INT_ARGB);
            layerbuffs_gfx[i] = layerbuffs[i].createGraphics();
            layerbuffs_gfx[i].setBackground(TRANSPARENT);
        }
    }

    public void dispose_buffers() {
        composedbuff_gfx.dispose(); composedbuff_gfx = null; composedbuff = null;
        for (int i = 0; i < r.LAYER_COUNT; i++) {
            layerbuffs_gfx[i].dispose(); layerbuffs_gfx[i] = null; layerbuffs[i] = null;
        }
    }

    private void update() {
        draws = r.redraw_status(); r.redraw_clear();
        if (r.reschange_status()) { dispose_buffers(); generate_buffers(); r.reschange_clear(); }
        for (int i = 0; i < r.LAYER_COUNT; i++) {
            if ((draws & r.LAYERS[i]) != 0) {
                layerbuffs_gfx[i].clearRect(0, 0, r.res_x(), r.res_y());
                r.draw(r.LAYERS[i], layerbuffs_gfx[i]);
            }
        }
        if (draws != 0) compose();
    }

    private void compose() {
        composedbuff_gfx.clearRect(0, 0, r.res_x(), r.res_y());
        for (BufferedImage layer : layerbuffs) composedbuff_gfx.drawImage(layer, 0, 0, null);
    }

    public BufferedImage recompose() { update(); return composedbuff; }

    public int info_res_x()          { return r.res_x(); }
    public int info_res_y()          { return r.res_y(); }
    public int info_layer_count()    { return r.LAYER_COUNT; }
    public int info_redraw_status()  { return draws; }
}


// ============================================================
// Vec2 — 2D double-precision vector
// ============================================================
class Vec2 {
    public double x, y;

    public Vec2(double x, double y) { this.x = x; this.y = y; }
    public Vec2(Vec2 copy)          { this.x = copy.x; this.y = copy.y; }

    public void add(Vec2 v)         { x += v.x; y += v.y; }
    public void mul(double s)       { x *= s;   y *= s; }

    public static Vec2 add(Vec2 a, Vec2 b) { return new Vec2(a.x + b.x, a.y + b.y); }
    public static Vec2 mul(Vec2 v, double s) { return new Vec2(v.x * s, v.y * s); }
    public static double magnitude(Vec2 v) { return Math.sqrt(v.x * v.x + v.y * v.y); }
}
