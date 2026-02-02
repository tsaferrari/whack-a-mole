
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author tejonidhi
 */
public class Game {

    /**
     * Number of "Holes" for the moles.
     */
    private final int numButtons = 40;
    /**
     * The start button for game commencement.
     */
    private JButton start;
    /**
     * Prints the text for timer.
     */
    private JTextArea printTimer;
    /**
     * Prints the text for score.
     */
    private JTextArea printScore;
    /**
     * Value of timer.
     */
    private JTextField timerValue;
    /**
     * Value of score.
     */
    private JTextField scoreValue;
    /**
     * Time after which game will stop.
     */
    private final int gameTime = 20;
    /**
     * Flag for checking if game time has expired.
     */
    private boolean finished = false;
    /**
     * Array for storing button variables.
     */
    private JButton[] buttons;
    /**
     * Keeps a track of the number of moles hit when in up state. Notice that
     * the variable is atomic and can be accessed by one thread at a time.
     */
    private AtomicInteger score = new AtomicInteger(0);

    /**
     * The constructor for the whack-a-mole game.
     */
    public Game() {

        Font font = new Font(Font.MONOSPACED, Font.BOLD, 14);

        JFrame frame = new JFrame("Whack-A-Mole");
        frame.setSize(650, 400);
        JPanel main = new JPanel(new BorderLayout());
        JPanel buttonPane = new JPanel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel pane = new JPanel();

        start = new JButton("Start");
        printTimer = new JTextArea("Time Left: ");
        printTimer.setEditable(false);

        printScore = new JTextArea("Score: ");
        printScore.setEditable(false);

        timerValue = new JTextField(7);
        timerValue.setEditable(false);

        scoreValue = new JTextField(7);
        scoreValue.setEditable(false);

        pane.add(start);
        pane.add(printTimer);
        pane.add(timerValue);
        pane.add(printScore);
        pane.add(scoreValue);

        buttons = new JButton[numButtons];

        for (int i = 0; i < buttons.length; i++) {

            buttons[i] = new JButton("   ");
            buttons[i].setBackground(Color.LIGHT_GRAY);
            buttons[i].setFont(font);
            buttons[i].setOpaque(true);
            buttonPane.add(buttons[i]);
        }

        main.add(pane, BorderLayout.NORTH);
        main.add(buttonPane, BorderLayout.CENTER);
        frame.setContentPane(main);
        frame.setVisible(true);

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start.setEnabled(false);
                finished = false;
                score.set(0);
                scoreValue.setEditable(false);
                scoreValue.setText(String.valueOf(score));
                Runnable runTimer = new RunnableTimer(gameTime, timerValue);
                Thread t = new Thread(runTimer);
                t.start();

                for (JButton button : buttons) {

                    Runnable r = new RunnableButton(button);
                    Thread threadButton = new Thread(r);
                    threadButton.start();

                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (button.getText().equals(":-)")) {
                                threadButton.interrupt();
                            }
                        }

                    });
                }
            }

        });

    }

    /**
     * Runnable method for the timer thread that runs in the background.
     */
    private class RunnableTimer implements Runnable {

        /**
         * The field for storing the game time.
         */
        private int myTotalTime;
        /**
         * Area for printing the timer value.
         */
        private JTextField area;

        /**
         * Constructor for the runnable method for the timer thread.
         *
         * @param totalTime the total game time in seconds.
         * @param a the text field where the time left will be printed.
         */
        private RunnableTimer(int totalTime, JTextField a) {
            myTotalTime = totalTime;
            area = a;
        }

        @Override
        public void run() {

            while (myTotalTime >= 0) {
                try {
                    timerValue.setText(String.valueOf(myTotalTime));
                    timerValue.setForeground(Color.red);
                    Thread.sleep(1000);
                    myTotalTime--;

                } catch (InterruptedException ex) {
                    timerValue.setText("Timer thread was interrupted!");
                    return;
                }

            }
            try {
                finished = true;
                for (JButton b : buttons) {
                    b.doClick();
                }
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                timerValue.setText("Timer thread was interrupted!");
            }
            start.setEnabled(true);

        }
    }

    /**
     * The runnable class for the every thread controlling every button.
     */
    private class RunnableButton implements Runnable {

        /**
         * Text to display on button when mole is hit.
         */
        private final String offString = ":-(";
        /**
         * Text to display when the mole pops out.
         */
        private final String onString = ":-)";
        /**
         * Color of the button when the mole is hit.
         */
        private final Color offColor = Color.RED;
        /**
         * Color of the button when the mole pops out.
         */
        private final Color onColor = Color.GREEN;
        /**
         * Random number generator for mole timing.
         */
        private Random random = new Random();
        /**
         * Store the random numbers output.
         */
        private int randomLightTime;
        /**
         * The current button accessed by the thread.
         */
        private JButton myButton;

        /**
         * The constructor for the runnable method for threads controlling the
         * buttons.
         *
         * @param button the button controlled by the current thread.
         */
        private RunnableButton(JButton button) {
            myButton = button;
        }

        @Override
        public void run() {

            while (!finished) {
                try {
                    randomLightTime = random.nextInt(4) + 1;
                    myButton.setText(onString);
                    myButton.setBackground(onColor);
                    Thread.sleep(randomLightTime * 1000);

                    myButton.setText("   ");
                    myButton.setBackground(Color.LIGHT_GRAY);
                    myButton.setOpaque(true);
                    Thread.sleep(3000);

                } catch (InterruptedException ex) {

                    if (!finished) {
                        if (myButton.getText().equals(onString)) {
                            myButton.setText(offString);
                            myButton.setBackground(offColor);
                            scoreValue.setText(String.valueOf(score.incrementAndGet()));
                        }
                        try {
                            //delay before down state
                            Thread.sleep(2000);
                            myButton.setText("   ");
                            myButton.setBackground(Color.LIGHT_GRAY);
                            myButton.setOpaque(true);
                            Thread.sleep(3000);
                        } catch (InterruptedException ex1) {

                        }
                    } else {
                        myButton.setText("   ");
                        myButton.setBackground(Color.LIGHT_GRAY);
                        myButton.setOpaque(true);
                        return;
                    }
                }
            }
            myButton.setText("   ");
            myButton.setBackground(Color.LIGHT_GRAY);
            myButton.setOpaque(true);
        }

    }

    /**
     * The main method for the whack-a-mole game.
     *
     * @param args the command line parameters(not used in this program).
     */
    public static void main(String[] args) {
        new Game();
    }
}
