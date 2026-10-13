# Service Placement Optimization - Heuristic GA Solver

A Java-based Service Placement Optimization Engine using a **Genetic Algorithm (GA)** with a professional Swing dashboard for visualization.

## Course
**Optimization Methods** - Fourth-Year Software / Computer Engineering  
Salahaddin University-Erbil

## Problem
Assign **12 microservices** to **4 servers** while minimizing:
- Resource utilization cost
- Load balance variance
- Network latency
- Constraint violations (via exterior penalty method)

**Search space:** 4^12 = 16,777,216 possible assignments

## Features
- Heuristic Genetic Algorithm with dynamic convergence (no fixed iteration limit)
- Tournament Selection (k=3), Single-Point Crossover (rate=0.8), Mutation (rate=0.1)
- Elitism: top 2 candidates preserved
- Exterior Penalty Method (P=1000) for constraint handling
- Professional Swing Dashboard with:
  - Convergence chart (log-scale)
  - Multi-objective cost breakdown
  - Server infrastructure gauges
  - Service allocation audit table
  - Mathematical formulation reference
  - Live solver console log
  - Re-run optimization button

## How to Run

```bash
# Compile
javac Main.java Dashboard.java

# Run
java Main
```

## Files
| File | Description |
|------|-------------|
| `Main.java` | GA optimization algorithm, data model, evaluation function |
| `Dashboard.java` | Professional Swing GUI with 4-tab dashboard |

## Authors
- hedeshawqiomer
