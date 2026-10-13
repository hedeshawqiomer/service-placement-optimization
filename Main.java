import java.util.*;

/**
 * Service Placement Optimization using a Heuristic Algorithm
 * College Assignment - Optimization Methods (Software & Informatics Engineering)
 */
public class Main {
    static class Server {
        String name;
        double maxCPU, maxRAM, maxStorage, maxLatency;
        int capacity;
        Server(String n, double cpu, double ram, double st, double lat, int cap) {
            name = n; maxCPU = cpu; maxRAM = ram; maxStorage = st; maxLatency = lat; capacity = cap;
        }
    }
    static class Service {
        String name;
        double cpuDemand, ramDemand, storageDemand, latency;
        Service(String n, double cpu, double ram, double st, double lat) {
            name = n; cpuDemand = cpu; ramDemand = ram; storageDemand = st; latency = lat;
        }
    }
    
    /** Represents a single candidate solution in the search space */
    static class CandidateSolution implements Comparable<CandidateSolution> {
        int[] assignment; // Defines which service is mapped to which server
        double fitness;
        
        CandidateSolution(int numServices, int numServers, Random rand) {
            assignment = new int[numServices];
            for (int i = 0; i < numServices; i++) assignment[i] = rand.nextInt(numServers);
            fitness = Double.MAX_VALUE;
        }
        
        CandidateSolution(int[] assignment) {
            this.assignment = assignment.clone();
            fitness = Double.MAX_VALUE;
        }
        
        CandidateSolution copy() {
            CandidateSolution s = new CandidateSolution(assignment);
            s.fitness = fitness;
            return s;
        }
        
        @Override
        public int compareTo(CandidateSolution other) { return Double.compare(fitness, other.fitness); }
    }
    
    static final int NUM_SERVERS = 4, NUM_SERVICES = 12, POOL_SIZE = 50;
    static final int STAGNATION_PATIENCE = 500; // Heuristic plateau detection: stop when no improvement for 500 generations
    static final double EPSILON = 1e-5;         // Marginal improvement threshold
    static final double RECOMBINATION_RATE = 0.8, PERTURBATION_RATE = 0.1;
    static final int TOURNAMENT_SIZE = 3, ELITISM_COUNT = 2;
    static final double W_RESOURCE = 0.3, W_BALANCE = 0.3, W_LATENCY = 0.2, PENALTY = 1000.0;
    
    static Server[] servers;
    static Service[] services;
    static Random random = new Random();
    static double[] bestFitnessHistory, avgFitnessHistory;
    static int actualGenerations = 0;
    static long totalEvaluations = 0;
    static String lastSolverLog = "";


    static void initializeData() {
        servers = new Server[] {
            new Server("Server-1", 100, 64, 500, 50, 3), new Server("Server-2", 100, 64, 500, 50, 3),
            new Server("Server-3", 100, 64, 500, 50, 3), new Server("Server-4", 100, 64, 500, 50, 3)
        };
        services = new Service[] {
            new Service("Svc-1", 20, 8, 40, 10), new Service("Svc-2", 30, 16, 80, 15),
            new Service("Svc-3", 15, 4, 30, 5), new Service("Svc-4", 25, 12, 60, 20),
            new Service("Svc-5", 10, 6, 25, 8), new Service("Svc-6", 35, 20, 120, 25),
            new Service("Svc-7", 20, 10, 50, 12), new Service("Svc-8", 15, 8, 35, 7),
            new Service("Svc-9", 25, 14, 70, 18), new Service("Svc-10", 10, 4, 20, 6),
            new Service("Svc-11", 30, 16, 90, 22), new Service("Svc-12", 20, 10, 45, 10)
        };
    }

    // Evaluation function F(x) - Calculates the objective cost
    static double evaluate(CandidateSolution solution) {
        int[] assignment = solution.assignment;
        double[] cpuUsed = new double[NUM_SERVERS], ramUsed = new double[NUM_SERVERS];
        double[] storageUsed = new double[NUM_SERVERS], latencyUsed = new double[NUM_SERVERS];
        int[] serviceCount = new int[NUM_SERVERS];
        
        for (int i = 0; i < NUM_SERVICES; i++) {
            int sIdx = assignment[i];
            cpuUsed[sIdx] += services[i].cpuDemand; ramUsed[sIdx] += services[i].ramDemand;
            storageUsed[sIdx] += services[i].storageDemand; latencyUsed[sIdx] += services[i].latency;
            serviceCount[sIdx]++;
        }
        
        double fResource = 0.0, totalLoad = 0.0;
        double[] loads = new double[NUM_SERVERS];
        for (int j = 0; j < NUM_SERVERS; j++) {
            double cR = cpuUsed[j] / servers[j].maxCPU, rR = ramUsed[j] / servers[j].maxRAM, sR = storageUsed[j] / servers[j].maxStorage;
            fResource += (cR * cR) + (rR * rR) + (sR * sR);
            loads[j] = cR; totalLoad += cR;
        }
        
        double fBalance = 0.0, avgLoad = totalLoad / NUM_SERVERS;
        for (int j = 0; j < NUM_SERVERS; j++) {
            double diff = loads[j] - avgLoad;
            fBalance += diff * diff;
        }
        
        double totalLatency = 0.0;
        for (int j = 0; j < NUM_SERVERS; j++) totalLatency += latencyUsed[j];
        double fLatency = totalLatency / NUM_SERVICES;
        
        double fPenalty = 0.0;
        for (int j = 0; j < NUM_SERVERS; j++) {
            if (serviceCount[j] > servers[j].capacity) fPenalty += (serviceCount[j] - servers[j].capacity);
            if (cpuUsed[j] > servers[j].maxCPU) fPenalty += (cpuUsed[j] - servers[j].maxCPU);
            if (ramUsed[j] > servers[j].maxRAM) fPenalty += (ramUsed[j] - servers[j].maxRAM);
            if (storageUsed[j] > servers[j].maxStorage) fPenalty += (storageUsed[j] - servers[j].maxStorage);
            if (latencyUsed[j] > servers[j].maxLatency) fPenalty += (latencyUsed[j] - servers[j].maxLatency);
        }
        
        totalEvaluations++;
        double fitness = W_RESOURCE * fResource + W_BALANCE * fBalance + W_LATENCY * fLatency + PENALTY * fPenalty;
        solution.fitness = fitness;
        return fitness;
    }

    // Selects a solution from the pool using tournament selection
    static CandidateSolution selectSolution(CandidateSolution[] pool) {
        CandidateSolution best = null;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            CandidateSolution candidate = pool[random.nextInt(pool.length)];
            if (best == null || candidate.fitness < best.fitness) best = candidate;
        }
        return best.copy();
    }

    // Recombines two parent solutions to create new variations
    static CandidateSolution[] recombine(CandidateSolution parent1, CandidateSolution parent2) {
        if (random.nextDouble() > RECOMBINATION_RATE) return new CandidateSolution[] { parent1.copy(), parent2.copy() };
        int point = 1 + random.nextInt(NUM_SERVICES - 2);
        int[] child1Map = new int[NUM_SERVICES], child2Map = new int[NUM_SERVICES];
        for (int i = 0; i < NUM_SERVICES; i++) {
            if (i < point) { child1Map[i] = parent1.assignment[i]; child2Map[i] = parent2.assignment[i]; }
            else { child1Map[i] = parent2.assignment[i]; child2Map[i] = parent1.assignment[i]; }
        }
        return new CandidateSolution[] { new CandidateSolution(child1Map), new CandidateSolution(child2Map) };
    }

    // Perturbs a solution randomly to explore new search space areas
    static void perturb(CandidateSolution solution) {
        for (int i = 0; i < NUM_SERVICES; i++) if (random.nextDouble() < PERTURBATION_RATE) solution.assignment[i] = random.nextInt(NUM_SERVERS);
    }

    // Main optimization loop using Heuristic Stopping Criteria (Zero Violations + Stagnation Detection)
    static CandidateSolution runOptimization() {
        List<Double> bestHistoryList = new ArrayList<>();
        List<Double> avgHistoryList = new ArrayList<>();
        
        CandidateSolution[] pool = new CandidateSolution[POOL_SIZE];
        for (int i = 0; i < POOL_SIZE; i++) { 
            pool[i] = new CandidateSolution(NUM_SERVICES, NUM_SERVERS, random); 
            evaluate(pool[i]); 
        }
        
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(String.format("Starting GA Optimization Pool=%d, Servers=%d, Services=%d\n", POOL_SIZE, NUM_SERVERS, NUM_SERVICES));
        logBuilder.append("Stagnation Patience: 500 generations | Epsilon: 1e-5\n");
        logBuilder.append("----------------------------------------------------------------------\n");

        CandidateSolution globalBest = null;
        double previousBestFitness = Double.MAX_VALUE;
        int stagnationCounter = 0;
        int iter = 0;
        
        // PURE HEURISTIC OPTIMIZATION LOOP: No fixed iteration limit!
        // Loop runs dynamically until heuristic convergence conditions are satisfied.
        while (true) {
            actualGenerations = iter + 1;
            Arrays.sort(pool);
            if (globalBest == null || pool[0].fitness < globalBest.fitness) {
                globalBest = pool[0].copy();
            }
            
            double sumFitness = 0;
            for (CandidateSolution s : pool) sumFitness += s.fitness;
            double avgCost = sumFitness / POOL_SIZE;
            bestHistoryList.add(pool[0].fitness);
            avgHistoryList.add(avgCost);
            
            // Check heuristic convergence condition
            boolean isFeasible = globalBest.fitness < PENALTY;
            if (Math.abs(previousBestFitness - globalBest.fitness) < EPSILON) {
                if (isFeasible) {
                    stagnationCounter++;
                }
            } else {
                stagnationCounter = 0;
                previousBestFitness = globalBest.fitness;
            }
            
            if (iter % 100 == 0) {
                String line = String.format("Generation %5d: Best = %8.4f | Avg = %9.4f | Feasible: %-3s | Plateau: %d/%d\n", 
                        iter, pool[0].fitness, avgCost, isFeasible ? "YES" : "NO", stagnationCounter, STAGNATION_PATIENCE);
                System.out.print(line);
                logBuilder.append(line);
            }
            
            // PURE HEURISTIC TERMINATION CRITERIA:
            // Condition 1: Must be fully feasible (zero constraint violations, fitness < PENALTY)
            // Condition 2: Genetic Plateau Reached (no fitness improvement > EPSILON for STAGNATION_PATIENCE generations)
            if (isFeasible && stagnationCounter >= STAGNATION_PATIENCE) {
                String stopMsg = String.format("\n>>> CONVERGENCE MET at Generation %,d!\nReason: Zero violations + Fitness stabilized for %d generations.\nTotal Evals: %,d\n",
                        iter, STAGNATION_PATIENCE, totalEvaluations);
                System.out.println(stopMsg);
                logBuilder.append(stopMsg);
                break;
            }
            
            CandidateSolution[] nextPool = new CandidateSolution[POOL_SIZE];
            for (int i = 0; i < ELITISM_COUNT; i++) nextPool[i] = pool[i].copy(); // Keep the best solutions
            
            for (int i = ELITISM_COUNT; i < POOL_SIZE; i += 2) {
                CandidateSolution[] newSolutions = recombine(selectSolution(pool), selectSolution(pool));
                perturb(newSolutions[0]); perturb(newSolutions[1]);
                evaluate(newSolutions[0]); evaluate(newSolutions[1]);
                nextPool[i] = newSolutions[0];
                if (i + 1 < POOL_SIZE) nextPool[i + 1] = newSolutions[1];
            }
            pool = nextPool;
            iter++;
        }
        
        Arrays.sort(pool);
        if (pool[0].fitness < globalBest.fitness) globalBest = pool[0].copy();
        
        // Export dynamic history arrays to dashboard
        bestFitnessHistory = new double[bestHistoryList.size()];
        avgFitnessHistory = new double[avgHistoryList.size()];
        for (int i = 0; i < bestHistoryList.size(); i++) {
            bestFitnessHistory[i] = bestHistoryList.get(i);
            avgFitnessHistory[i] = avgHistoryList.get(i);
        }
        
        lastSolverLog = logBuilder.toString();
        return globalBest;
    }

    static void printResults(CandidateSolution best, long elapsedMs) {
        System.out.println("\n===========================================================\n               OPTIMAL ASSIGNMENT FOUND\n===========================================================");
        System.out.print("Mapping Matrix: [");
        for (int i = 0; i < NUM_SERVICES; i++) System.out.print((best.assignment[i] + 1) + (i < NUM_SERVICES - 1 ? ", " : "]\n\n"));
        
        double[] cpuUsed = new double[NUM_SERVERS], ramUsed = new double[NUM_SERVERS], storageUsed = new double[NUM_SERVERS], latencyUsed = new double[NUM_SERVERS];
        List<List<String>> serverServices = new ArrayList<>();
        for (int j = 0; j < NUM_SERVERS; j++) serverServices.add(new ArrayList<>());
        for (int i = 0; i < NUM_SERVICES; i++) {
            int s = best.assignment[i];
            cpuUsed[s] += services[i].cpuDemand; ramUsed[s] += services[i].ramDemand;
            storageUsed[s] += services[i].storageDemand; latencyUsed[s] += services[i].latency;
            serverServices.get(s).add(services[i].name);
        }
        for (int j = 0; j < NUM_SERVERS; j++) {
            System.out.printf("  %s: %-35s CPU: %3.0f/%.0f  RAM: %2.0f/%.0f  Storage: %3.0f/%.0f  Latency: %2.0f/%.0f%n",
                    servers[j].name, serverServices.get(j), cpuUsed[j], servers[j].maxCPU, ramUsed[j], servers[j].maxRAM, storageUsed[j], servers[j].maxStorage, latencyUsed[j], servers[j].maxLatency);
        }
        System.out.printf("\n  Objective Cost  : %.4f%n  Execution Time  : %.2f seconds%n  Generations Ran : %,d (Dynamic Stopping)%n  Pool Size       : %d%n  Total Evals     : %,d%n===========================================================%n",
                best.fitness, elapsedMs / 1000.0, actualGenerations, POOL_SIZE, totalEvaluations);
    }

    public static void main(String[] args) {
        System.out.println("+-----------------------------------------------------------+\n|   Service Placement Optimization - Heuristic Solver       |\n|   Servers: 4 | Services: 12 | Capacity: 3 each            |\n|   Stopping Mode: Dynamic Plateau Detection (Zero Violations)|\n+-----------------------------------------------------------+\n");
        initializeData();
        long startTime = System.currentTimeMillis();
        CandidateSolution optimalSolution = runOptimization();
        long elapsed = System.currentTimeMillis() - startTime;
        printResults(optimalSolution, elapsed);
        
        System.out.println("\nLaunching visualization dashboard...");
        Dashboard.show(servers, services, optimalSolution.assignment, optimalSolution.fitness, bestFitnessHistory, avgFitnessHistory, elapsed, lastSolverLog);
    }
}
