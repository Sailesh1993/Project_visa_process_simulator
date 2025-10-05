package controller;

import view.IVisualisation;

public interface IControllerMtoV {
    void showEndTime(double time);

    void visualiseCustomer();

    void updateQueueStatus(int servicePointId, int queueSize);

    void displayResults(String resultsText);

    // NEW: Add this method for real-time stats
    void updateStatistics(int totalApps, int approved, int rejected, double avgTime, double currentTime);
    IVisualisation getVisualisation();
}