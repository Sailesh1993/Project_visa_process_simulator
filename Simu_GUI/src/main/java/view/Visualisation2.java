package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Visualisation2 extends Canvas implements IVisualisation {
	private GraphicsContext gc;
	int customerCount = 0;

    private int[] queueSizes = new int[6];              //Array for holding queue size

	public Visualisation2(int width, int height) {
		super(width, height);
		gc = this.getGraphicsContext2D();
		clearDisplay();
	}

	public void clearDisplay() {
		gc.setFill(Color.CYAN);
		gc.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
	
	public void newCustomer() {
		customerCount++;

        //Only for adjustment of displaying Application arrival count
        String applicationDisplay = "Applications " + customerCount;               //Creating application display text

        Text textLength = new Text(applicationDisplay);                       //Creating text object to measure width of string
        textLength.setFont(new Font(20));                                  //Setting same font as the text display
        double textWidth = textLength.getLayoutBounds().getWidth();           //Getting width of the text
        double textHeight = 20;

		gc.setFill(Color.CYAN);					// first erase old text
		gc.fillRect(100,80, textWidth + 20, textHeight);
		gc.setFill(Color.PURPLE);						// then write new text
		gc.setFont(new Font(20));
		gc.fillText(applicationDisplay, 100, 100);
	}

    @Override
    public void updateServicePointQueue(int servicePointId, int queueSize) {
        queueSizes[servicePointId] = queueSize;             // Update the queue size for the specific service point

        //Only for adjustment of displaying Application arrival count
        String queueDisplay = "[Service Point " + (servicePointId + 1) + "]" + " Queue: " + queueSize;      //Creating service points and queue display text

        Text queueDisplayLength = new Text(queueDisplay);
        queueDisplayLength.setFont(new Font(17));
        double queueDisplayLengthWidth = queueDisplayLength.getLayoutBounds().getWidth();
        double queueDisplayLengthHeight = 20;

        // Clear previous queue display and redraw the updated value
        gc.setFill(Color.CYAN);
        gc.fillRect(100, 120 + servicePointId * 40, queueDisplayLengthWidth + 20, queueDisplayLengthHeight);         // Erase old queue size text
        gc.setFill(Color.PURPLE);                                                      // Write new queue size text
        gc.setFont(new Font(17));
        gc.fillText(queueDisplay, 100, 120 + servicePointId * 40 + 15);
    }
}
