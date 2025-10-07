package MVC.view;

public interface IVisualisation {
	void clearDisplay();
	void newCustomer();

    void updateServicePointQueue(int servicePointId, int queueSize);

    void moveCustomer(int fromSP, int toSP, boolean isApproved);
}

