# Immigration Visa Process Simulator (Java)

## 📌 Project Overview
This project is a **Discrete-Event Simulation (DES)** of the Immigration Visa application process, implemented in Java.  
It uses a provided **simulation framework** (engine, event handling, distributions) and a custom **model package** that represents the immigration process.

### 🔹 Simulation Flow
1. **Online System / Appointment Booking (SP1)**
    - Applications enter the system via the online portal and are scheduled for processing.

2. **Registration Desk (SP2)**
    - Officers check applicant ID and document submission.
    - Branching:
        - Renewal → skip biometrics → Doc Check
        - Extension → biometrics → Doc Check
        - Missing docs → sent to Missing Docs Queue until resolved

3. **Biometrics (SP3a, only for extensions)**
    - Biometrics are collected if required.

4. **Document Check (SP3b)**
    - Documents are verified using a service-time distribution.

5. **Decision Desk (SP4)**
    - Application outcome determined via a **Bernoulli distribution**:
        - Approved → Exit system
        - Rejected → Reapply (loop back to Registration Desk)

---

## 🛠️ Features
- Multiple **service points** with queues (registration, biometrics, doc check, decision).
- Probabilistic service times (Normal, Triangular, Gamma, Log-normal).
- **Approval/Rejection** outcomes using Bernoulli distribution.
- Statistics collected:
    - Average processing time per application
    - Queue lengths per desk
    - Officer utilization rates
    - Approval vs. rejection percentages

---

## 📂 Project Structure
```
src/

├── simu/framework/     # Generic simulation engine (do not modify)
│
│ ├── Engine.java
│ ├── Event.java
│ ├── EventList.java
│ ├── Clock.java
│ ├── ArrivalProcess.java
│ ├── IEventType.java
│ └── Trace.java

├── simu/model/ # Project-specific immigration visa model
│ ├── Application.java # Adapted from Customer
│ ├── ServicePoint.java # Immigration service desks
│ ├── EventType.java # Immigration-specific events
│ ├── MyEngine.java # Core simulation logic

├──eduni/distributions/ # Probability distributions

├── test/   # Main simulation runner
│ └── Simulator.java
  
```

## 🚀 Getting Started

## Process Diagram
[Link to process diagram] (https://lucid.app/lucidchart/a718b514-2885-440c-b49c-0978bccbd735/edit?invitationId=inv_ca18fbc7-496f-487b-bf73-3d2f65a2582f)

### 1. Clone the Repository
```bash
git clone https://github.com/SandipFromPokhara/Project_visa_process_simulator.git
cd Project_visa_process_simulator

2. Open in IntelliJ

File → New → Project from Version Control → Paste repository URL.

IntelliJ will download and set up the project.

3. Run the Simulation

Navigate to MyEngine.java.

Configure simulation parameters (duration, arrival rates, service distributions).

Run the simulation from IntelliJ.

🌳 Branching Strategy

We use GitHub Flow for collaboration:

main → stable branch (always working version).

Feature branches for each task:

feature-applicationascustomer-class

feature-service-points

feature-eventtypes

feature-myengine

Workflow

Create a new branch:

git checkout -b feature-xyz


Make your changes in IntelliJ.

Commit and push:

git add .
git commit -m "Implemented XYZ"
git push origin feature-xyz


Open a Pull Request (PR) on GitHub.

Team reviews → merge into main.

👥 Team Members

Member 1 – Project Manager + tester + coder + documentation

Member 2 – Coder + tester + documentation

Member 3 – Coder + tester + documentation

Member 4 – Coder + tester + documentation

📊 Future Improvements

Add support for priority queues (e.g., fast-track applications).

Extend with multiple decision outcomes (approve, reject, request more info).

Visualize simulation results (charts/graphs).
