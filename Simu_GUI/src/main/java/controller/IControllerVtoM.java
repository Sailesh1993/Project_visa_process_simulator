package controller;

import simu.framework.IEngine;

/* interface for the UI */
public interface IControllerVtoM {
		public void startSimulation();
		public void increaseSpeed();
		public void decreaseSpeed();
        IEngine getEngine(); // for pause and stop button
}
