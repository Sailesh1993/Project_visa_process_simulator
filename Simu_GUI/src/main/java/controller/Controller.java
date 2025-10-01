package controller;

import distributionconfiguration.DistributionConfig;
import javafx.application.Platform;
import simu.framework.IEngine;
import simu.model.MyEngine;
import view.ISimulatorUI;

public class Controller implements IControllerVtoM, IControllerMtoV {   // NEW
	private IEngine engine;
	private ISimulatorUI ui;

    private DistributionConfig[] configs;
    private Long seed;
	
	public Controller(ISimulatorUI ui, DistributionConfig[] configs, Long seed) {
        this.ui = ui;
        this.configs = configs;
        this.seed = seed;
	}

	/* Engine control: */
	@Override
	public void startSimulation() {
		engine = new MyEngine(this, configs, seed); // new Engine thread is created for every simulation
		engine.setSimulationTime(ui.getTime());
		engine.setDelay(ui.getDelay());
		ui.getVisualisation().clearDisplay();
		((Thread) engine).start();
		//((Thread)engine).run(); // Never like this, why?
	}
	
	@Override
	public void decreaseSpeed() { // hidastetaan moottorisäiettä
		engine.setDelay((long)(engine.getDelay()*1.10));
	}

	@Override
	public void increaseSpeed() { // nopeutetaan moottorisäiettä
		engine.setDelay((long)(engine.getDelay()*0.9));
	}


	/* Simulation results passing to the UI
	 * Because FX-UI updates come from engine thread, they need to be directed to the JavaFX thread
	 */
	@Override
	public void showEndTime(double time) {
		Platform.runLater(()->ui.setEndingTime(time));
	}

	@Override
	public void visualiseCustomer() {
		Platform.runLater(() -> ui.getVisualisation().newCustomer());
	}

    @Override
    public void updateQueueStatus(int servicePointId, int queueSize) {
        Platform.runLater(() -> ui.getVisualisation().updateServicePointQueue(servicePointId, queueSize));
    }

    @Override
    public void displayResults(String resultsText) {
        Platform.runLater(() -> {
            ui.displayResults(resultsText);                 //Calling method in SimulatorGUI to update results Label
        });
    }
}
