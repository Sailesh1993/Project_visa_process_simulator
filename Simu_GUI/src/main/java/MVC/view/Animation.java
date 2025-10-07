package MVC.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;

public class Animation extends Canvas implements IVisualisation {
    private GraphicsContext gc;
    private int customerCount = 0;
    private int[] queueSizes = new int[6];
    private boolean[] servicePointBusy = new boolean[6];
    private List<AnimatedCustomer> customers = new ArrayList<>();
    private AnimationTimer timer;
    // Service point positions (horizontal layout)
    private static final int[][] SP_POSITIONS = {
            {200, 200},   // SP1
            {450, 200},   // SP2
            {700, 100},   // SP3
            {700, 300},   // SP4
            {950, 200},   // SP5
            {1200, 200}   // SP6
    };

    // Queue box positions (before each service point)
    private static final int[][] QUEUE_POSITIONS = {
            {120, 200},   // Queue for SP1
            {370, 200},   // Queue for SP2
            {620, 100},   // Queue for SP3
            {620, 300},   // Queue for SP4
            {870, 200},   // Queue for SP5
            {1120, 200}   // Queue for SP6
    };

    public Animation(int width, int height) {
        super(width, height);
        gc = this.getGraphicsContext2D();
        startAnimation();
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
                updateCustomers();
                updateServicePointStatus();
            }
        };
        timer.start();
    }

    private void draw() {
        gc.setFill(Color.web("#ECF0F1"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        drawFlowLines();
        drawQueueBoxes();
        drawServicePoints();
        drawCustomers();
        drawStats();
    }

    private void drawFlowLines() {
        gc.setStroke(Color.web("#95A5A6"));
        gc.setLineWidth(2);

        // Entry to Queue1
        drawArrow(50, 200, 80, 200);
        // Queue1 to SP1
        drawArrow(160, 200, 170, 200);
        // SP1 to Queue2
        drawArrow(230, 200, 330, 200);
        // Queue2 to SP2
        drawArrow(410, 200, 420, 200);

        // SP2 to Queue3 (biometrics - up)
        drawArrow(480, 180, 580, 120);
        // Queue3 to SP3
        drawArrow(660, 100, 670, 100);

        // SP2 to Queue4 (missing docs - down)
        drawArrow(480, 220, 580, 280);
        // Queue4 to SP4
        drawArrow(660, 300, 670, 300);

        // SP2 to Queue5 (direct)
        gc.setLineDashes(5, 5);
        drawArrow(480, 200, 830, 200);
        gc.setLineDashes();
        // Queue5 to SP5
        drawArrow(910, 200, 920, 200);

        // SP3 to Queue5
        drawArrow(730, 120, 830, 180);

        // SP4 to Queue5
        drawArrow(730, 280, 830, 220);

        // SP5 to Queue6
        drawArrow(980, 200, 1080, 200);
        // Queue6 to SP6
        drawArrow(1160, 200, 1170, 200);

        // SP6 to Exit
        drawArrow(1230, 200, 1320, 200);
    }

    private void drawArrow(double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowSize = 8;
        gc.strokeLine(x2, y2, x2 - arrowSize * Math.cos(angle - Math.PI / 6),
                y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        gc.strokeLine(x2, y2, x2 - arrowSize * Math.cos(angle + Math.PI / 6),
                y2 - arrowSize * Math.sin(angle + Math.PI / 6));
    }

    private void drawQueueBoxes() {
        gc.setFont(new Font("Arial", 10));

        for (int i = 0; i < 6; i++) {
            int x = QUEUE_POSITIONS[i][0];
            int y = QUEUE_POSITIONS[i][1];

            // Draw queue cylinder/drum shape
            gc.setFill(Color.web("#BDC3C7"));
            gc.fillOval(x - 40, y - 25, 80, 50);

            gc.setStroke(Color.web("#7F8C8D"));
            gc.setLineWidth(2);
            gc.strokeOval(x - 40, y - 25, 80, 50);

            // Display queue count
            gc.setFill(Color.web("#2C3E50"));
            gc.setFont(new Font("Arial Bold", 14));
            String queueText = String.valueOf(queueSizes[i]);
            gc.fillText(queueText, x - 8, y + 5);

            // Label
            gc.setFont(new Font("Arial", 9));
            gc.fillText("Queue", x - 18, y - 30);
        }
    }

    private void drawServicePoints() {
        String[] names = {
                "SP1: Entry",
                "SP2: Docs",
                "SP3: Bio",
                "SP4: Missing",
                "SP5: Verify",
                "SP6: Decision"
        };

        gc.setFont(new Font("Arial", 11));

        for (int i = 0; i < 6; i++) {
            int x = SP_POSITIONS[i][0];
            int y = SP_POSITIONS[i][1];

            // Change color based on occupancy
            Color boxColor;
            if (servicePointBusy[i]) {
                boxColor = Color.web("#E74C3C"); // Red when busy
            } else {
                boxColor = Color.web("#27AE60"); // Green when vacant
            }

            // Draw service point box
            gc.setFill(boxColor);
            gc.fillRect(x - 30, y - 30, 60, 60);

            gc.setStroke(Color.web("#2C3E50"));
            gc.setLineWidth(2);
            gc.strokeRect(x - 30, y - 30, 60, 60);

            // Draw service point label
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial Bold", 10));

            // Split name for two lines
            String[] parts = names[i].split(": ");
            gc.fillText(parts[0], x - 20, y - 5);
            if (parts.length > 1) {
                gc.fillText(parts[1], x - 20, y + 10);
            }

            // Status indicator
            gc.setFont(new Font("Arial", 8));
            gc.fillText(servicePointBusy[i] ? "BUSY" : "FREE", x - 18, y + 25);
        }
    }

    private void drawCustomers() {
        for (AnimatedCustomer customer : customers) {
            gc.setFill(customer.approved ? Color.web("#27AE60") : Color.web("#F39C12"));
            gc.fillOval(customer.x - 6, customer.y - 6, 12, 12);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeOval(customer.x - 6, customer.y - 6, 12, 12);
        }
    }

    private void drawStats() {
        gc.setFill(Color.web("#2C3E50"));
        gc.setFont(new Font("Arial Bold", 16));
        gc.fillText("Total Applications: " + customerCount, 20, 30);

        // Legend
        gc.setFont(new Font("Arial", 11));
        gc.setFill(Color.web("#F39C12"));
        gc.fillOval(20, 450, 12, 12);
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("Processing", 38, 460);

        gc.setFill(Color.web("#27AE60"));
        gc.fillOval(120, 450, 12, 12);
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("Approved", 138, 460);

        gc.setFill(Color.web("#E74C3C"));
        gc.fillRect(220, 445, 15, 15);
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("SP Busy", 242, 460);

        gc.setFill(Color.web("#27AE60"));
        gc.fillRect(320, 445, 15, 15);
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("SP Free", 342, 460);
    }

    private void updateServicePointStatus() {
        // Check if any customer is at a service point position
        for (int i = 0; i < 6; i++) {
            servicePointBusy[i] = false; // Reset

            int spX = SP_POSITIONS[i][0];
            int spY = SP_POSITIONS[i][1];

            // Check if any customer is near this service point
            for (AnimatedCustomer customer : customers) {
                double distance = Math.sqrt(Math.pow(customer.x - spX, 2) + Math.pow(customer.y - spY, 2));
                if (distance < 30) { // Within service point area
                    servicePointBusy[i] = true;
                    break;
                }
            }
        }
    }

    private void updateCustomers() {
        List<AnimatedCustomer> toRemove = new ArrayList<>();
        for (AnimatedCustomer customer : customers) {
            customer.update();
            if (customer.reachedTarget()) {
                toRemove.add(customer);
            }
        }
        customers.removeAll(toRemove);
    }

    @Override
    public void newCustomer() {
        customerCount++;
        AnimatedCustomer customer = new AnimatedCustomer(30, 200);
        customer.setTarget(QUEUE_POSITIONS[0][0], QUEUE_POSITIONS[0][1]);
        customers.add(customer);
    }

    @Override
    public void updateServicePointQueue(int servicePointId, int queueSize) {
        if (servicePointId >= 0 && servicePointId < 6) {
            queueSizes[servicePointId] = queueSize;
        }
    }

    @Override
    public void moveCustomer(int fromSP, int toSP, boolean isApproved) {
        AnimatedCustomer customer = new AnimatedCustomer(
                SP_POSITIONS[fromSP][0],
                SP_POSITIONS[fromSP][1]
        );
        customer.approved = isApproved;

        if (toSP == -1) {
            customer.setTarget(1350, 200); // Exit
        } else {
            customer.setTarget(SP_POSITIONS[toSP][0], SP_POSITIONS[toSP][1]);
        }
        customers.add(customer);
    }

    @Override
    public void clearDisplay() {
        gc.setFill(Color.web("#ECF0F1"));
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    private class AnimatedCustomer {
        double x, y;
        double targetX, targetY;
        double speed = 2.5;
        boolean approved = false;

        AnimatedCustomer(double x, double y) {
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
        }

        void setTarget(double tx, double ty) {
            this.targetX = tx;
            this.targetY = ty;
        }

        void update() {
            double dx = targetX - x;
            double dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > speed) {
                x += (dx / distance) * speed;
                y += (dy / distance) * speed;
            } else {
                x = targetX;
                y = targetY;
            }
        }

        boolean reachedTarget() {
            return Math.abs(x - targetX) < 1 && Math.abs(y - targetY) < 1;
        }
    }
}