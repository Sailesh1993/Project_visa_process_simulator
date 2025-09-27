package controller;

/* interface for the engine */
public interface IControllerMtoV {
    void showEndTime(double time);

    void visualiseCustomer();

    void updateQueueStatus(int servicePointId, int queueSize);       //NEW(to update queue status for each service point)

    void displayResults(String resultsText);
}
