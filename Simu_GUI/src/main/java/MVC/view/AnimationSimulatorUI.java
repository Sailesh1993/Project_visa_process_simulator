package MVC.view;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * SimulatorUI is a JavaFX Canvas-based visualization component that displays
 * a real-time simulation of a customer queue management system with multiple
 * service points.
 *
 * <p>This class implements the {@link IVisualisation} interface and provides
 * animated visualization of customers moving through a multi-stage service workflow.</p>
 *
 * <p>It features:
 * Six service points with queue status indicators
 * Real-time animation of customer movement between service points
 * Color-coded service points (green for available, red for busy)
 * Animated customer bubbles showing approval status
 * Flow diagram showing the customer journey through the system</p>
 *
 * <p>The visualization updates continuously using JavaFX's AnimationTimer, providing
 * smooth animations and real-time status updates.</p>
 *
 * See {@link IVisualisation}
 * See {@link AnimatedCustomer}
 */
public class AnimationSimulatorUI extends Canvas implements IVisualisation {
    private GraphicsContext gc;
    private int customerCount = 0;

    /** Array tracking all customers at each of the 6 service points. */
    private final List<AnimatedCustomer>[] customersAtSP = new ArrayList[6];

    /** List containing all active customers in the system. */
    private final List<AnimatedCustomer> allCustomers = new ArrayList<>();

    /** AnimationTimer that drives the continuous animation loop. */
    private AnimationTimer timer;

    /**
     * 2D array defining the pixel coordinates of the six service points.
     * Each row represents [x, y] coordinates for a service point.
     */
    private static final int[][] SP_POSITIONS = {
            {200, 200}, {450, 200}, {700, 100},
            {700, 300}, {950, 200}, {1200, 200}
    };

    /**
     * 2D array defining the pixel coordinates of the queue display areas
     * for each service point. Each row represents [x, y] coordinates.
     */
    private static final int[][] QUEUE_POSITIONS = {
            {120, 200}, {370, 200}, {620, 100},
            {620, 300}, {870, 200}, {1120, 200}
    };

    /**
     * Constructs a SimulatorUI canvas with the specified dimensions.
     *
     * <p>Initializes the graphics context, sets up customer tracking arrays,
     * and starts the animation timer that drives the continuous visualization.</p>
     *
     * @param width the width of the canvas in pixels
     * @param height the height of the canvas in pixels
     */
    public AnimationSimulatorUI(int width, int height) {
        super(width, height);
        gc = this.getGraphicsContext2D();
        for (int i = 0; i < 6; i++) {
            customersAtSP[i] = new ArrayList<>();
        }
        startAnimation();
    }

    /**
     * Starts the animation timer that continuously updates and renders
     * the simulation visualization.
     *
     * <p>The animation loop runs at the system's refresh rate and performs:
     * Updates customer positions and states
     * Updates customer location tracking
     * Redraws the entire visualization</p>
     */
    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                updateCustomers();
                updateCustomerLocations();
                draw();
            }
        };
        timer.start();
    }

    /**
     * Renders the complete visualization including background, flow diagram,
     * queues, service points, customers, and statistics.
     *
     * <p>This method is called repeatedly by the animation timer to update
     * the display. It clears the canvas and redraws all elements in order.</p>
     */
    private void draw() {
        gc.setFill(Color.web("#ECF0F1"));
        gc.fillRect(0, 0, getWidth(), getHeight());
        drawFlowLines();
        drawQueueBoxes();
        drawServicePoints();
        drawCustomers();
        drawStats();
    }

    /**
     * Draws the flow diagram showing the path customers take through
     * the service points.
     *
     * <p>Creates a directed graph with arrows connecting service points
     * and showing the workflow progression. Dashed lines indicate alternative
     * or feedback paths in the process.</p>
     */
    private void drawFlowLines() {
        gc.setStroke(Color.web("#95A5A6"));
        gc.setLineWidth(2);
        drawArrow(50, 200, 80, 200);
        drawArrow(160, 200, 170, 200);
        drawArrow(230, 200, 330, 200);
        drawArrow(410, 200, 420, 200);
        drawArrow(480, 180, 580, 120);
        drawArrow(660, 100, 670, 100);
        drawArrow(480, 220, 580, 280);
        drawArrow(660, 300, 670, 300);
        gc.setLineDashes(5, 5);
        drawArrow(480, 200, 830, 200);
        gc.setLineDashes();
        drawArrow(910, 200, 920, 200);
        drawArrow(730, 120, 830, 180);
        drawArrow(730, 280, 830, 220);
        drawArrow(980, 200, 1080, 200);
        drawArrow(1160, 200, 1170, 200);
        drawArrow(1230, 200, 1320, 200);
    }

    /**
     * Draws a directional arrow from point (x1, y1) to (x2, y2).
     *
     * <p>The arrow consists of a line segment with an arrowhead at the end point.
     * The arrowhead is calculated dynamically based on the arrow's angle.</p>
     *
     * @param x1 the x-coordinate of the starting point
     * @param y1 the y-coordinate of the starting point
     * @param x2 the x-coordinate of the ending point
     * @param y2 the y-coordinate of the ending point
     */
    private void drawArrow(double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);
        double a = Math.atan2(y2 - y1, x2 - x1), s = 8;
        gc.strokeLine(x2, y2, x2 - s*Math.cos(a - Math.PI/6), y2 - s*Math.sin(a - Math.PI/6));
        gc.strokeLine(x2, y2, x2 - s*Math.cos(a + Math.PI/6), y2 - s*Math.sin(a + Math.PI/6));
    }

    /**
     * Draws queue display boxes for each of the six service points.
     *
     * <p>Each queue box is an oval showing the current number of customers
     * waiting at that service point. The label indicates this is a queue area.</p>
     */
    private void drawQueueBoxes() {
        gc.setFont(new Font("Arial", 10));
        for (int i = 0; i < 6; i++) {
            int x = QUEUE_POSITIONS[i][0], y = QUEUE_POSITIONS[i][1];
            gc.setFill(Color.web("#BDC3C7"));
            gc.fillOval(x - 40, y - 25, 80, 50);
            gc.setStroke(Color.web("#7F8C8D"));
            gc.setLineWidth(2);
            gc.strokeOval(x - 40, y - 25, 80, 50);
            gc.setFill(Color.web("#2C3E50"));
            gc.setFont(new Font("Arial Bold", 14));
            int queueCount = customersAtSP[i].size();
            gc.fillText(String.valueOf(queueCount), x - 8, y + 5);
            gc.setFont(new Font("Arial", 9));
            gc.fillText("Queue", x - 18, y - 30);
        }
    }

    /**
     * Draws all six service points with status indicators.
     *
     * <p>Each service point is displayed as a square with:
     * Green background if queue size ≤ 10 (available)
     * Red background if queue size > 10 (busy)
     * Service point identifier and function label
     * Current queue count</p>
     */
    private void drawServicePoints() {
        String[] names = {"SP1: Entry","SP2: Docs","SP3: Bio","SP4: Missing","SP5: Verify","SP6: Decision"};
        gc.setFont(new Font("Arial", 11));
        for (int i = 0; i < 6; i++) {
            int x = SP_POSITIONS[i][0], y = SP_POSITIONS[i][1];

            // Count actual customers at this SP - 100% accuracy
            int queueCount = customersAtSP[i].size();

            // GREEN if queue <= 10, RED if queue > 10
            Color color = (queueCount <= 10) ? Color.web("#27AE60") : Color.web("#E74C3C");

            gc.setFill(color);
            gc.fillRect(x - 30, y - 30, 60, 60);
            gc.setStroke(Color.web("#2C3E50"));
            gc.setLineWidth(2);
            gc.strokeRect(x - 30, y - 30, 60, 60);

            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial Bold", 10));
            String[] parts = names[i].split(": ");
            gc.fillText(parts[0], x - 20, y - 10);
            if (parts.length > 1) gc.fillText(parts[1], x - 20, y + 5);

            gc.setFont(new Font("Arial", 9));
            gc.fillText("Q:" + queueCount, x - 18, y - 36);
        }
    }

    /**
     * Draws all currently moving customer bubbles.
     *
     * <p>Only customers actively moving between service points are displayed as bubbles.
     * The bubble color indicates the customer's status:
     * Green bubble: approved customer
     * Orange bubble: non-approved customer</p>
     */
    private void drawCustomers() {
        for (AnimatedCustomer c : allCustomers) {
            // draw bubbles that are moving
            if (c.moving) {
                gc.setFill(c.approved ? Color.web("#27AE60") : Color.web("#F39C12"));
                gc.fillOval(c.x - 6, c.y - 6, 12, 12);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeOval(c.x - 6, c.y - 6, 12, 12);
            }
        }
    }

    /**
     * Draws statistics and legend information on the canvas.
     *
     * <p>Displays the total number of applications processed and provides
     * a color-coded legend explaining the visualization elements.</p>
     */
    private void drawStats() {
        gc.setFill(Color.web("#2C3E50"));
        gc.setFont(new Font("Arial Bold", 16));
        gc.fillText("Total Applications: " + customerCount, 20, 30);
        drawLegend();
    }

    /**
     * Draws the legend box explaining the visualization color coding.
     *
     * <p>The legend displays:
     * Green service point: Available (queue ≤ 10)
     * Red service point: Busy (queue > 10)
     * Orange bubble: Application in transit</p>
     */
    private void drawLegend() {
        double centerX = getWidth() / 2;
        double legendY = getHeight() - 60;

        // Background box
        gc.setFill(Color.web("#FFFFFF"));
        gc.fillRect(centerX - 190, legendY - 15, 440, 50);
        gc.setStroke(Color.web("#2C3E50"));
        gc.setLineWidth(2);
        gc.strokeRect(centerX - 190, legendY - 15, 440, 50);

        // Green service point
        gc.setFill(Color.web("#27AE60"));
        gc.fillRect(centerX - 150, legendY - 5, 25, 25);
        gc.setStroke(Color.web("#2C3E50"));
        gc.setLineWidth(1);
        gc.strokeRect(centerX - 150, legendY - 5, 25, 25);
        gc.setFont(new Font("Arial", 11));
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("Service Point (Free)", centerX - 120, legendY + 12);

        // Red service point
        gc.setFill(Color.web("#E74C3C"));
        gc.fillRect(centerX - 10, legendY - 5, 25, 25);
        gc.setStroke(Color.web("#2C3E50"));
        gc.setLineWidth(1);
        gc.strokeRect(centerX - 10, legendY - 5, 25, 25);
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("Service Point (Busy)", centerX + 20, legendY + 12);

        // Application bubble
        gc.setFill(Color.web("#F39C12"));
        gc.fillOval(centerX + 130, legendY + 2, 12, 12);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(centerX + 130, legendY + 2, 12, 12);
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("= Application", centerX + 145, legendY + 12);
    }

    /**
     * Updates the state of all customers in the system.
     *
     * <p>This method:
     * Updates each customer's position based on their target
     * Checks if customers have reached their destination
     * Stops animation for customers reaching a service point
     * Removes customers exiting the system</p>
     */
    private void updateCustomers() {
        List<AnimatedCustomer> toRemove = new ArrayList<>();

        for (AnimatedCustomer c : allCustomers) {
            c.update();

            // Check if customer reached their target
            if (c.reachedTarget()) {
                if (c.toSp == -1) {
                    // Exiting system
                    toRemove.add(c);
                } else if (c.toSp >= 0 && c.toSp < 6) {
                    // Arrived at service point - no longer moving
                    c.moving = false;
                }
            }
        }

        allCustomers.removeAll(toRemove);
    }

    /**
     * Updates the tracking of which customers are at each service point.
     *
     * <p>Rebuilds the customersAtSP arrays to reflect the current position
     * of all non-moving customers. This information is used for queue display
     * and for routing decisions.</p>
     */
    private void updateCustomerLocations() {
        // Clear all SP lists
        for (int i = 0; i < 6; i++) {
            customersAtSP[i].clear();
        }

        // Count customers at each SP
        for (AnimatedCustomer c : allCustomers) {
            if (!c.moving && c.toSp >= 0 && c.toSp < 6) {
                customersAtSP[c.toSp].add(c);
            }
        }
    }

    /**
     * Creates a new customer entering the system at service point 0 (Entry).
     *
     * <p>This method is called from the model and schedules the customer creation
     * on the JavaFX Application Thread to ensure thread safety. The customer
     * is animated moving from the entry point to service point 0.</p>
     *
     * @see IVisualisation#newCustomer()
     */
    @Override
    public void newCustomer() {
        Platform.runLater(() -> {
            customerCount++;
            AnimatedCustomer c = new AnimatedCustomer(30, 200);
            c.toSp = 0;
            c.setTarget(SP_POSITIONS[0][0], SP_POSITIONS[0][1]);
            allCustomers.add(c);
        });
    }

    /**
     * Updates the queue display for a specific service point.
     *
     * <p>This method is required by the {@link IVisualisation} interface but is not used
     * in this implementation. The actual queue sizes are computed directly based on customer positions.</p>
     *
     * @param spId the service point identifier (0-5)
     * @param size the queue size (ignored in this implementation)
     * @see IVisualisation#updateServicePointQueue(int, int)
     */
    @Override
    public void updateServicePointQueue(int spId, int size) {}

    /**
     * Moves a customer from one service point to another.
     *
     * <p>If a customer is currently at the source service point, that customer
     * is moved. Otherwise, a new customer is created at the source location.
     * The customer's approval status is updated to reflect the outcome.</p>
     *
     * The method is thread-safe and schedules the operation on the
     * JavaFX Application Thread.
     *
     * @param fromSP the source service point ID (0-5, or -1 for entry)
     * @param toSP the destination service point ID (0-5, or -1 for exit)
     * @param isApproved true if the customer was approved, false otherwise
     * @see IVisualisation#moveCustomer(int, int, boolean)
     */
    @Override
    public void moveCustomer(int fromSP, int toSP, boolean isApproved) {
        // Find a customer at fromSP and move them
        Platform.runLater(() -> {
            AnimatedCustomer customerToMove = null;

            if (fromSP >= 0 && fromSP < 6) {
                // Find customer at this SP
                for (AnimatedCustomer c : allCustomers) {
                    if (!c.moving && c.toSp == fromSP) {
                        customerToMove = c;
                        break;
                    }
                }
            }

            // If didn't find customer, create a new customer
            if (customerToMove == null) {
                double startX = (fromSP >= 0 && fromSP < 6) ? SP_POSITIONS[fromSP][0] : 30;
                double startY = (fromSP >= 0 && fromSP < 6) ? SP_POSITIONS[fromSP][1] : 200;
                customerToMove = new AnimatedCustomer(startX, startY);
                allCustomers.add(customerToMove);
            }

            customerToMove.approved = isApproved;
            customerToMove.fromSp = fromSP;
            customerToMove.toSp = toSP;
            customerToMove.moving = true;

            if (toSP == -1) {
                // Exiting system
                customerToMove.setTarget(1350, 200);
            } else if (toSP >= 0 && toSP < 6) {
                // Moving to next service point
                customerToMove.setTarget(SP_POSITIONS[toSP][0], SP_POSITIONS[toSP][1]);
            }
        });
    }

    /**
     * Clears all display data and resets the visualization to its initial state.
     *
     * <p>Removes all customers from the system and resets the customer count.
     * This method is typically called when starting a new simulation.</p>
     *
     * @see IVisualisation#clearDisplay()
     */
    @Override
    public void clearDisplay() {
        gc.setFill(Color.web("#ECF0F1"));
        gc.fillRect(0, 0, getWidth(), getHeight());
        allCustomers.clear();
        for (int i = 0; i < 6; i++) {
            customersAtSP[i].clear();
        }
        customerCount = 0;
    }

    /**
     * AnimatedCustomer is a private inner class representing a customer entity
     * in the simulation with position tracking and animation capabilities.
     *
     * <p>Each customer has:
     * Current and target positions for smooth animation
     * Approval status determining visual representation
     * Service point tracking for routing decisions
     * Movement state management</p>
     */
    private static class AnimatedCustomer {
        /** Current x-coordinate of the customer. */
        double x,
        /** Current y-coordinate of the customer. */
        y,
        /** Target x-coordinate for animation. */
        targetX,
        /** Target y-coordinate for animation. */
        targetY;

        /** Movement speed in pixels per frame. */
        double speed = 2.5;

        /** True if the customer was approved at their current service point. */
        boolean approved = false;

        /** True if the customer is actively moving between service points. */
        boolean moving = true;

        /** ID of the service point the customer is moving from (-1 if unknown). */
        int fromSp = -1,
        /** ID of the service point the customer is moving to (-1 if exiting). */
        toSp = -1;

        /**
         * Constructs an AnimatedCustomer at the specified coordinates.
         *
         * @param x the initial x-coordinate
         * @param y the initial y-coordinate
         */
        AnimatedCustomer(double x, double y) {
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
        }

        /**
         * Sets the target position for this customer and starts animation.
         *
         * @param tx the target x-coordinate
         * @param ty the target y-coordinate
         */
        void setTarget(double tx, double ty) {
            targetX = tx;
            targetY = ty;
            moving = true;
        }

        /**
         * Updates the customer's position based on their current location
         * and target destination.
         *
         * <p>If the customer has not reached the target, moves them incrementally
         * towards it at the configured speed. Once close enough, snaps the customer
         * to the exact target position.</p>
         */
        void update() {
            if (!moving) return;

            double dx = targetX - x, dy = targetY - y;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d > speed) {
                x += (dx / d) * speed;
                y += (dy / d) * speed;
            } else {
                x = targetX;
                y = targetY;
            }
        }

        /**
         * Checks if the customer has reached their target position.
         *
         * <p>Returns true if the customer is within 1 pixel of the target
         * in both x and y coordinates.</p>
         *
         * @return true if the customer has reached the target, false otherwise
         */
        boolean reachedTarget() {
            return Math.abs(x - targetX) < 1 && Math.abs(y - targetY) < 1;
        }
    }
}