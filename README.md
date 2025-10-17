# Immigration Visa Process Simulator (Java)

## 📌 Project Overview
The Immigration Visa Process Simulator is a **Discrete-Event Simulation (DES)** system 
developed in Java to model and analyze the workflow of immigration visa applications. 
The simulator reproduces real-world scenarios of applicant arrivals, document submission, 
biometric verification, and final decision-making, while collecting detailed performance 
metrics such as queue lengths, service utilization, and processing times.

The project is implemented using Object-Oriented Programming (OOP) principles in Java, 
following the Model–View–Controller (MVC) design pattern. The system integrates JavaFX 
for the graphical user interface, SceneBuilder for GUI layout design, JUnit5 for testing, 
and JPA (Jakarta Persistence API) with MariaDB as the database layer for storing simulation results and configurations.


### 🏃‍♂️ Simulation Flow Overview
```
🏢 SP1 – Application Entry & Appointment Booking
    └─ Applicants arrival to the system
        (Arrival intervals determined by configured probability distribution, e.g., Negative Exponential, Normal, Gamma)
        ↓
🏢 SP2 – Document Submission Department
    └─ Officers verify identity and documents
        ├─ New applications → proceed to SP3 (Biometrics)
        ├─ Renewals → skip SP3
        └─ Incomplete → routed to SP4 (Missing Documents Queue)
        ↓
🏢 SP3 – Biometrics Department (Optional)
    └─ Required for new applicants only
        (Processing time follows user-defined distribution, e.g., Normal or Gamma)
        ↓
🏢 SP4 – Missing Docs Waiting Department
    └─ Applicants with missing documents wait until issues are resolved
        ↓
🏢 SP5 – Document Check Department
    └─ Verification of completeness and correctness of all documents
        ↓
🏢 SP6 – Decision Department
    └─ Probabilistic outcome:
        ├─ Approved → Exit system
        └─ Rejected → May reapply up to 3 times before permanent removal from the system
```  
```  
             [SP1 🏢 Application Entry & Appointment Booking]
                                  │
                                  ▼
                [SP2 🏢 Document Submission Department]
                                  │
                     ┌────────────┴───────────┐
                     │                        │
                     ▼                        ▼
[SP3 🏢 Biometrics Department]           [SP4 🏢 Missing Documents Queue]
                     │                            │
                     ▼                            ▼
                    [SP5 🏢 Document Check Department]
                                  │
                                  ▼
                    [SP6 🏢 Decision Department]
                                  │
                       ┌──────────┴───────────┐
                       │                      │
                       ▼                      ▼
                   Approved ✔             Rejected ❌
                     (Exit)               (Reapply ≤3)

```
---

## ⚙️ Key Features
- **Multiple service points (SP1-SP6)** with independent queues and staff capacity.
- **Probabilistic distributions** for service and arrival times (Normal, Gamma, Negexp, etc.).
- **Event-driven simulation engine** with A–B–C phase logic (advance time, process events, trigger conditional events)..
- **Real-time statistics updates** on:
    - Average processing time per application
    - Queue lengths per desk
    - Service point utilization rates
    - Approval vs. rejection
- **Dynamic visualization** of the simulation process using JavaFX animations.
- **Bottleneck detection** based on service point utilization rates.
- **Automatic data persistence** via JPA ORM into a MariaDB database, storing:
  - Simulation configurations,
  - Service point performance results, and
  - Individual application logs.
---

## 🏗️ Project Architecture
The project follows a modular MVC + ORM architecture:
```
src/main/java/
├── eduni/ 
│   ├── distributions/ ⚛️                        # Probability distribution classes
│   └── project_distributionconfiguration/ 📊    # Contains configurations for specific statistical distribution (Normal, Exponential, or Gamma)
│ 
├── MVC/ 🖥️
│   ├── controller/ 🎛️                           # Handles user interaction & simulation control
│   ├── simu/framework/ ⚙️                       # Generic simulation engine classes (Event, Clock, Engine)
│   ├── simu/model/ 🧩                           # Immigration process logic (ApplicationAsCustomer, EventType, MyEngine, ServicePoint)
│   └── view/ 🖼️                                 # JavaFX GUI layouts and animations
│
├── ORM/
│   ├── dao/ 📝                                  # DAO class for database operations
│   ├── datasource/ 💾                           # Singleton for managing JPA connections to MariaDB database
│   └── entity/ 🧱                               # JPA entity mappings (SimulationRun, SPResult, DistConfig, ApplicationLog)
│
├── Main.java 🚀                                 # Main class to launch the simulation
│ 
├── resources/
│   ├── FXML/ 🗂️                                 # JavaFX FXML files for Scenebuilder
│   ├── META-INF/persistence.xml 📁              # JPA persistence configuration
│   └── logback.xml/ 📜                          # Logging configuration
│
├── test/
│   ├── controller/                              # Controller classes for testing
│   ├── simu/framework/                          # Engine class test
│   ├── simu/model/                              # MyEngine class test
│   └── view/                                    # GUI animation test
│ 
└── pom.xml                                      # Maven dependencies
```
---

## 💻 Development Environment

- **IDE**: IntelliJ IDEA (Ultimate Edition)
- **Programming Language**: Java (OpenJDK 1.8.0_462)
- **GUI Builder**: SceneBuilder
- **Database**: MariaDB (via HeidiSQL for schema verification)
- **ORM Framework**: JPA (Hibernate provider)
- **Build Tool**: Maven
- **Version Control**: Git + GitHub (collaborative development with feature branches)
- **Testing**: JUnit5
---

## 🚀 Getting Started

### 1. Clone the Repository
```
git clone https://github.com/SandipFromPokhara/Project_visa_process_simulator.git
cd Project_visa_process_simulator
```
### 2. Open in IntelliJ
```
File → New → Project from Version Control → Paste repository URL.

IntelliJ will download and set up the project.
```
### 3. Run the Simulation
```
Navigate to MyEngine.java.

Configure simulation parameters (duration, arrival rates, service distributions).

Run the simulation from IntelliJ.
```
---

## 🌳 Version Control Workflow 

A collaborative GitHub Flow model was followed:
- main → stable branch (always working version).
- Each team member worked on a dedicated feature branch.
- The project manager merged tested branches into a pre-production branch (feature-applicationascustomer).
- Once all components were validated, they were merged into the main branch.

Example branch naming conventions:
```
feature-applicationascustomer
feature-servicepoints
feature-engine-logic
feature-database-integration
```

### Workflow
```
Create a new branch:

git checkout -b feature-xyz

Make your changes in IntelliJ.

Commit and push:

git add .
git commit -m "Implemented XYZ"
git push origin feature-xyz

Open a Pull Request (PR) on GitHub.

Team reviews → merge into main.
```
---

## 👥 Team Members

Sandip Ranjit – Project Manager + code + test + documentation

Dinal Maha Vidanelage – Code + test + documentation

Sailesh Karki – Code + test + documentation

Twe He Gam Aung – Code + test + documentation

---

## 📊 Future Enhancements
- Incorporate priority queues (e.g., fast-track visa applications).
- Expand the decision logic to include additional outcomes such as “request additional documents.”
- Add visual analytics dashboards to compare multiple simulation runs.
- Allow parameter saving and loading for reproducible simulation experiments.
