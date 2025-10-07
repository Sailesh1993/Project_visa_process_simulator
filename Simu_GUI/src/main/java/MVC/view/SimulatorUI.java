package MVC.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;

public class SimulatorUI extends Canvas implements IVisualisation {
    private GraphicsContext gc;
    private int customerCount = 0;

    // Track all customers by their current queue/service point
    private final List<AnimatedCustomer>[] customersAtSP = new ArrayList[6];
    private final List<AnimatedCustomer> allCustomers = new ArrayList<>();

    private AnimationTimer timer;

    // Layout
    private static final int[][] SP_POSITIONS = {
            {200, 200}, {450, 200}, {700, 100},
            {700, 300}, {950, 200}, {1200, 200}
    };
    private static final int[][] QUEUE_POSITIONS = {
            {120, 200}, {370, 200}, {620, 100},
            {620, 300}, {870, 200}, {1120, 200}
    };

    public SimulatorUI(int width, int height) {
        super(width, height);
        gc = this.getGraphicsContext2D();
        for (int i = 0; i < 6; i++) {
            customersAtSP[i] = new ArrayList<>();
        }
        startAnimation();
    }

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

    // ---------- Drawing ----------
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

    private void drawArrow(double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);
        double a = Math.atan2(y2 - y1, x2 - x1), s = 8;
        gc.strokeLine(x2, y2, x2 - s*Math.cos(a - Math.PI/6), y2 - s*Math.sin(a - Math.PI/6));
        gc.strokeLine(x2, y2, x2 - s*Math.cos(a + Math.PI/6), y2 - s*Math.sin(a + Math.PI/6));
    }

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

    private void drawServicePoints() {
        String[] names = {"SP1: Entry","SP2: Docs","SP3: Bio","SP4: Missing","SP5: Verify","SP6: Decision"};
        gc.setFont(new Font("Arial", 11));
        for (int i = 0; i < 6; i++) {
            int x = SP_POSITIONS[i][0], y = SP_POSITIONS[i][1];

            // Count actual customers at this SP - 100% accuracy
            int queueCount = customersAtSP[i].size();

            // GREEN if queue is 0, RED if queue >= 1
            Color color = (queueCount == 0) ? Color.web("#27AE60") : Color.web("#E74C3C");

            gc.setFill(color);
            gc.fillRect(x - 30, y - 30, 60, 60);
            gc.setStroke(Color.web("#2C3E50"));
            gc.setLineWidth(2);
            gc.strokeRect(x - 30, y - 30, 60, 60);

            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial Bold", 10));
            String[] parts = names[i].split(": ");
            gc.fillText(parts[0], x - 20, y - 5);
            if (parts.length > 1) gc.fillText(parts[1], x - 20, y + 10);

            gc.setFont(new Font("Arial", 8));
            gc.fillText(queueCount > 0 ? "BUSY" : "FREE", x - 18, y + 25);

            gc.setFont(new Font("Arial", 9));
            gc.fillText("Q:" + queueCount, x - 18, y - 36);
        }
    }

    private void drawCustomers() {
        for (AnimatedCustomer c : allCustomers) {
            gc.setFill(c.approved ? Color.web("#27AE60") : Color.web("#F39C12"));
            gc.fillOval(c.x - 6, c.y - 6, 12, 12);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeOval(c.x - 6, c.y - 6, 12, 12);
        }
    }

    private void drawStats() {
        gc.setFill(Color.web("#2C3E50"));
        gc.setFont(new Font("Arial Bold", 16));
        gc.fillText("Total Applications: " + customerCount, 20, 30);

        drawLegend();
    }

    private void drawLegend() {
        double centerX = getWidth() / 2;
        double legendY = getHeight() - 60;

        // Background box
        gc.setFill(Color.web("#FFFFFF"));
        gc.fillRect(centerX - 220, legendY - 10, 440, 50);
        gc.setStroke(Color.web("#2C3E50"));
        gc.setLineWidth(2);
        gc.strokeRect(centerX - 220, legendY - 10, 440, 50);

        // Green service point
        gc.setFill(Color.web("#27AE60"));
        gc.fillRect(centerX - 150, legendY - 5, 25, 25);
        gc.setStroke(Color.web("#2C3E50"));
        gc.setLineWidth(1);
        gc.strokeRect(centerX - 150, legendY - 5, 25, 25);
        gc.setFont(new Font("Arial", 11));
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("Queue (Free)", centerX - 120, legendY + 12);

        // Red service point
        gc.setFill(Color.web("#E74C3C"));
        gc.fillRect(centerX - 10, legendY - 5, 25, 25);
        gc.setStroke(Color.web("#2C3E50"));
        gc.setLineWidth(1);
        gc.strokeRect(centerX - 10, legendY - 5, 25, 25);
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("Queue (Busy)", centerX + 20, legendY + 12);

        // Application bubble
        gc.setFill(Color.web("#F39C12"));
        gc.fillOval(centerX + 130, legendY + 2, 12, 12);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(centerX + 130, legendY + 2, 12, 12);
        gc.setFill(Color.web("#2C3E50"));
        gc.fillText("= Application", centerX + 145, legendY + 12);
    }

    // ---------- State maintenance ----------
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

    private void updateCustomerLocations() {
        // Clear all SP lists
        for (int i = 0; i < 6; i++) {
            customersAtSP[i].clear();
        }

        // Count customers at each SP (those that have arrived and stopped moving)
        for (AnimatedCustomer c : allCustomers) {
            if (!c.moving && c.toSp >= 0 && c.toSp < 6) {
                customersAtSP[c.toSp].add(c);
            }
        }
    }

    // ---------- IVisualisation ----------
    @Override
    public void newCustomer() {
        customerCount++;
        AnimatedCustomer c = new AnimatedCustomer(30, 200);
        c.toSp = 0;
        c.setTarget(SP_POSITIONS[0][0], SP_POSITIONS[0][1]);
        allCustomers.add(c);
    }

    @Override
    public void updateServicePointQueue(int spId, int size) {
        // Not needed - we count actual bubbles
    }

    @Override
    public void moveCustomer(int fromSP, int toSP, boolean isApproved) {
        // Find a customer at fromSP and move them
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

        // If we didn't find one, create a new one (shouldn't happen normally)
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
    }

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

    // ---------- Moving bubble ----------
    private static class AnimatedCustomer {
        double x, y, targetX, targetY;
        double speed = 2.5;
        boolean approved = false;
        boolean moving = true;
        int fromSp = -1, toSp = -1;

        AnimatedCustomer(double x, double y) {
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
        }

        void setTarget(double tx, double ty) {
            targetX = tx;
            targetY = ty;
            moving = true;
        }

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

        boolean reachedTarget() {
            return Math.abs(x - targetX) < 1 && Math.abs(y - targetY) < 1;
        }
    }
}