package view;

public interface IVisualisation {
	void clearDisplay();

    void newCustomer();

    void updateServicePointQueue(int servicePointId, int queueSize);
}

