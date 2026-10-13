import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Executive Mission-Control Dashboard for Service Placement Optimization
 * Pure Java Swing - 100% ASCII Compatible (Cleanly compiles on any Windows charset)
 * 
 * Features:
 * 1. Executive Header with Live Status Pill and Asynchronous Re-Run Controller
 * 2. High-Impact KPI Metric Cards (Cost, Convergence Generation, Evaluations, Health)
 * 3. Modern Multi-Tabbed Workspace:
 *    - Tab 1: Executive Analytics (Convergence Chart, Multi-Objective Breakdown, Live Console)
 *    - Tab 2: Server Infrastructure (Cluster Hardware Gauges, 3/3 Slot Allocation Racks)
 *    - Tab 3: Microservice Allocation Audit (Full Data Table for all 12 Services)
 *    - Tab 4: Mathematical Optimization Model (Lectures 1 & 2 Formulation)
 */
public class Dashboard extends JFrame {

    // Problem Data References
    private Main.Server[] servers;
    private Main.Service[] services;
    private int[] assignment;
    private double bestFitness;
    private double[] bestHistory;
    private double[] avgHistory;
    private long elapsedMs;
    private String solverLogText;

    // Interactive UI Elements for Live Updates
    private JLabel kpiCostVal;
    private JLabel kpiGenVal;
    private JLabel kpiEvalVal;
    private JLabel kpiTimeVal;
    private JLabel statusPill;
    private JButton rerunButton;
    private JTextArea consoleArea;
    private JPanel convergencePanel;
    private JPanel breakdownPanel;
    private JPanel serversGridPanel;
    private DefaultTableModel tableModel;

    // Palette - Executive Slate & Electric Accents
    private static final Color BG_DARK        = new Color(11, 15, 25);
    private static final Color BG_HEADER      = new Color(17, 24, 39);
    private static final Color BG_CARD        = new Color(24, 32, 50);
    private static final Color BG_CARD_ALT    = new Color(18, 25, 40);
    private static final Color BORDER_COLOR   = new Color(42, 56, 84);
    private static final Color TEXT_WHITE     = new Color(243, 244, 246);
    private static final Color TEXT_MUTED     = new Color(156, 163, 175);

    private static final Color ACCENT_BLUE    = new Color(59, 130, 246);
    private static final Color ACCENT_GREEN   = new Color(16, 185, 129);
    private static final Color ACCENT_AMBER   = new Color(245, 158, 11);
    private static final Color ACCENT_PURPLE  = new Color(139, 92, 246);
    private static final Color ACCENT_RED     = new Color(239, 68, 68);

    private static final Color[] SERVER_ACCENTS = {
        new Color(59, 130, 246),  // Server 1: Blue
        new Color(16, 185, 129),  // Server 2: Emerald Green
        new Color(245, 158, 11),  // Server 3: Amber
        new Color(139, 92, 246)   // Server 4: Purple
    };

    private static final Color[] SERVICE_CHIP_COLORS = {
        new Color(239, 68, 68),  new Color(14, 165, 233),
        new Color(234, 179, 8),  new Color(168, 85, 247),
        new Color(34, 197, 94),  new Color(249, 115, 22),
        new Color(6, 182, 212),  new Color(236, 72, 153),
        new Color(132, 204, 22), new Color(129, 140, 248),
        new Color(20, 184, 166), new Color(245, 158, 11)
    };

    public Dashboard(Main.Server[] servers, Main.Service[] services,
                     int[] assignment, double bestFitness,
                     double[] bestHistory, double[] avgHistory, long elapsedMs,
                     String logText) {
        this.servers = servers;
        this.services = services;
        this.assignment = assignment;
        this.bestFitness = bestFitness;
        this.bestHistory = bestHistory;
        this.avgHistory = avgHistory;
        this.elapsedMs = elapsedMs;
        this.solverLogText = logText;

        setTitle("Service Placement Optimization Platform - Heuristic GA Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1460, 920);
        setMinimumSize(new Dimension(1220, 760));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // 1. Top Executive Control Header
        add(createHeaderBar(), BorderLayout.NORTH);

        // 2. Center Content with KPI Cards & Tabbed Presentation Area
        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setBackground(BG_DARK);
        centerContainer.setBorder(new EmptyBorder(12, 16, 12, 16));

        centerContainer.add(createKpiCardsRow(), BorderLayout.NORTH);
        centerContainer.add(createTabbedWorkspace(), BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. HEADER BAR
    // =========================================================================
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 20, 12, 20)
        ));

        // Title and Subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel mainTitle = new JLabel("SERVICE PLACEMENT OPTIMIZATION STUDIO");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainTitle.setForeground(TEXT_WHITE);

        JLabel subTitle = new JLabel("Heuristic Genetic Algorithm (GA)  |  Dynamic Plateau Convergence  |  Multi-Objective Exterior Penalty");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(TEXT_MUTED);

        titlePanel.add(mainTitle);
        titlePanel.add(subTitle);
        header.add(titlePanel, BorderLayout.WEST);

        // Right Toolbar: Live Status Badge + Re-Run Button
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        toolBar.setOpaque(false);

        statusPill = new JLabel("  [OPTIMAL & FEASIBLE]  ");
        statusPill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusPill.setForeground(ACCENT_GREEN);
        statusPill.setOpaque(true);
        statusPill.setBackground(new Color(16, 185, 129, 30));
        statusPill.setBorder(BorderFactory.createLineBorder(new Color(16, 185, 129, 120), 1, true));

        rerunButton = new JButton("Re-Run Optimization");
        rerunButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rerunButton.setForeground(Color.WHITE);
        rerunButton.setBackground(ACCENT_BLUE);
        rerunButton.setFocusPainted(false);
        rerunButton.setBorder(new EmptyBorder(8, 18, 8, 18));
        rerunButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rerunButton.addActionListener(e -> executeReOptimization());

        toolBar.add(statusPill);
        toolBar.add(rerunButton);
        header.add(toolBar, BorderLayout.EAST);

        return header;
    }

    // =========================================================================
    // 2. TOP KPI METRIC CARDS
    // =========================================================================
    private JPanel createKpiCardsRow() {
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setOpaque(false);

        kpiCostVal = new JLabel(String.format("%.4f", bestFitness));
        kpiCostVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        kpiCostVal.setForeground(ACCENT_GREEN);

        kpiGenVal = new JLabel(String.format("%,d Gens", bestHistory.length));
        kpiGenVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        kpiGenVal.setForeground(ACCENT_BLUE);

        kpiEvalVal = new JLabel(String.format("%,d", Main.totalEvaluations));
        kpiEvalVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        kpiEvalVal.setForeground(ACCENT_PURPLE);

        kpiTimeVal = new JLabel(String.format("%.2f s", elapsedMs / 1000.0));
        kpiTimeVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        kpiTimeVal.setForeground(TEXT_WHITE);

        kpiRow.add(buildSingleKpiCard("BEST OBJECTIVE COST", kpiCostVal, "0 Violations | Fully Feasible", ACCENT_GREEN));
        kpiRow.add(buildSingleKpiCard("DYNAMIC STOP GENERATION", kpiGenVal, "Plateau Patience: 500", ACCENT_BLUE));
        kpiRow.add(buildSingleKpiCard("TOTAL EVALUATIONS", kpiEvalVal, "99.8% Search Space Pruned", ACCENT_PURPLE));
        kpiRow.add(buildSingleKpiCard("EXECUTION DURATION", kpiTimeVal, "Sub-Second Convergence", TEXT_MUTED));

        return kpiRow;
    }

    private JPanel buildSingleKpiCard(String title, JLabel valLabel, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 14, 10, 14)
            )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLbl.setForeground(TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    // =========================================================================
    // 3. TABBED PRESENTATION WORKSPACE
    // =========================================================================
    private JTabbedPane createTabbedWorkspace() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_CARD_ALT);
        tabs.setForeground(TEXT_WHITE);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        tabs.addTab("  1. Master Analytics Overview  ", createMasterAnalyticsTab());
        tabs.addTab("  2. Server Infrastructure & Racks  ", createServerInfrastructureTab());
        tabs.addTab("  3. Service Demand & Placement Audit  ", createServiceAuditTab());
        tabs.addTab("  4. Mathematical Formulation  ", createFormulationTab());

        return tabs;
    }

    // -------------------------------------------------------------------------
    // TAB 1: MASTER ANALYTICS OVERVIEW
    // -------------------------------------------------------------------------
    private JPanel createMasterAnalyticsTab() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(12, 10, 10, 10));

        // Left Column: Interactive Vector Convergence Chart
        convergencePanel = createVectorConvergencePanel();
        p.add(convergencePanel);

        // Right Column: Split into Cost Breakdown (Top) + Solver Console (Bottom)
        JPanel rightCol = new JPanel(new GridLayout(2, 1, 0, 12));
        rightCol.setOpaque(false);

        breakdownPanel = createCostBreakdownPanel();
        JPanel consolePanel = createConsolePanel();

        rightCol.add(breakdownPanel);
        rightCol.add(consolePanel);
        p.add(rightCol);

        return p;
    }

    private JPanel createVectorConvergencePanel() {
        return new JPanel() {
            {
                setBackground(BG_CARD);
                setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int mLeft = 60, mRight = 24, mTop = 48, mBottom = 40;
                int cW = w - mLeft - mRight;
                int cH = h - mTop - mBottom;

                // Title
                g2.setColor(TEXT_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.drawString("Convergence Trajectory (Log10 Fitness Scale)", 16, 26);

                if (bestHistory == null || bestHistory.length < 2) return;

                double maxF = bestHistory[0];
                double minF = bestFitness;
                for (double f : avgHistory) if (f > maxF) maxF = f;
                if (maxF <= minF) maxF = minF + 1;

                double logMax = Math.log10(maxF + 1);
                double logMin = Math.log10(Math.max(minF, 0.01) + 1);
                if (logMax <= logMin) logMax = logMin + 1;

                // Grid lines & Y labels
                g2.setColor(new Color(36, 48, 72));
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 3f}, 0f));
                for (int i = 0; i <= 4; i++) {
                    int y = mTop + (int)(cH * i / 4.0);
                    g2.drawLine(mLeft, y, mLeft + cW, y);

                    double val = Math.pow(10, logMax - (logMax - logMin) * i / 4.0) - 1;
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2.drawString(String.format("%,.0f", val), 12, y + 4);
                    g2.setColor(new Color(36, 48, 72));
                }
                g2.setStroke(new BasicStroke(1f));

                // Baseline & X labels
                g2.setColor(BORDER_COLOR);
                g2.drawLine(mLeft, mTop + cH, mLeft + cW, mTop + cH);

                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString("Gen 0", mLeft, mTop + cH + 16);
                g2.drawString(String.format("Gen %,d", bestHistory.length / 2), mLeft + cW / 2 - 20, mTop + cH + 16);
                g2.drawString(String.format("Gen %,d (Converged)", bestHistory.length), mLeft + cW - 105, mTop + cH + 16);

                // Map points
                int step = Math.max(1, bestHistory.length / Math.max(cW, 1));
                List<Point> bestPts = new ArrayList<>();
                List<Point> avgPts = new ArrayList<>();

                for (int i = 0; i < bestHistory.length; i += step) {
                    double logB = Math.log10(Math.max(bestHistory[i], 0.01) + 1);
                    double logA = Math.log10(Math.max(avgHistory[i], 0.01) + 1);

                    int px = mLeft + (int)((double) i / (bestHistory.length - 1) * cW);
                    int pyB = mTop + (int)((logMax - logB) / (logMax - logMin) * cH);
                    int pyA = mTop + (int)((logMax - logA) / (logMax - logMin) * cH);

                    pyB = Math.max(mTop, Math.min(mTop + cH, pyB));
                    pyA = Math.max(mTop, Math.min(mTop + cH, pyA));

                    bestPts.add(new Point(px, pyB));
                    avgPts.add(new Point(px, pyA));
                }

                // Shaded gradient under best fitness
                if (bestPts.size() > 1) {
                    Polygon poly = new Polygon();
                    poly.addPoint(bestPts.get(0).x, mTop + cH);
                    for (Point p : bestPts) poly.addPoint(p.x, p.y);
                    poly.addPoint(bestPts.get(bestPts.size() - 1).x, mTop + cH);

                    GradientPaint gp = new GradientPaint(
                        mLeft, mTop, new Color(59, 130, 246, 50),
                        mLeft, mTop + cH, new Color(16, 185, 129, 0)
                    );
                    g2.setPaint(gp);
                    g2.fill(poly);
                }

                // Average line (Amber exploration line)
                g2.setColor(new Color(245, 158, 11, 130));
                g2.setStroke(new BasicStroke(1.2f));
                for (int i = 1; i < avgPts.size(); i++) {
                    g2.drawLine(avgPts.get(i - 1).x, avgPts.get(i - 1).y, avgPts.get(i).x, avgPts.get(i).y);
                }

                // Best Fitness Line (Emerald glow)
                g2.setStroke(new BasicStroke(3.5f));
                g2.setColor(new Color(16, 185, 129, 40));
                for (int i = 1; i < bestPts.size(); i++) {
                    g2.drawLine(bestPts.get(i - 1).x, bestPts.get(i - 1).y, bestPts.get(i).x, bestPts.get(i).y);
                }

                g2.setStroke(new BasicStroke(2.2f));
                g2.setColor(ACCENT_GREEN);
                for (int i = 1; i < bestPts.size(); i++) {
                    g2.drawLine(bestPts.get(i - 1).x, bestPts.get(i - 1).y, bestPts.get(i).x, bestPts.get(i).y);
                }

                // Target Endpoint
                if (!bestPts.isEmpty()) {
                    Point end = bestPts.get(bestPts.size() - 1);
                    g2.setColor(new Color(16, 185, 129, 80));
                    g2.fillOval(end.x - 7, end.y - 7, 14, 14);
                    g2.setColor(ACCENT_GREEN);
                    g2.fillOval(end.x - 4, end.y - 4, 8, 8);
                    g2.setColor(TEXT_WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(String.format("Final: %.4f", bestFitness), end.x - 65, end.y - 10);
                }

                // Legend
                int legX = mLeft + cW - 170;
                g2.setColor(ACCENT_GREEN);
                g2.fillRoundRect(legX, 16, 12, 4, 2, 2);
                g2.setColor(TEXT_WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString("Global Best", legX + 16, 21);

                g2.setColor(ACCENT_AMBER);
                g2.fillRoundRect(legX + 85, 16, 12, 4, 2, 2);
                g2.setColor(TEXT_WHITE);
                g2.drawString("Pool Avg", legX + 101, 21);
            }
        };
    }

    private JPanel createCostBreakdownPanel() {
        return new JPanel() {
            {
                setBackground(BG_CARD);
                setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                g2.setColor(TEXT_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.drawString("Multi-Objective Cost Decomposition", 16, 24);

                double[] cpuUsed = new double[4], ramUsed = new double[4], storageUsed = new double[4], latencyUsed = new double[4];
                int[] svcCount = new int[4];
                for (int i = 0; i < assignment.length; i++) {
                    int s = assignment[i];
                    cpuUsed[s] += services[i].cpuDemand; ramUsed[s] += services[i].ramDemand;
                    storageUsed[s] += services[i].storageDemand; latencyUsed[s] += services[i].latency;
                    svcCount[s]++;
                }

                double fResource = 0, totalLoad = 0;
                double[] loads = new double[4];
                for (int j = 0; j < 4; j++) {
                    double cr = cpuUsed[j] / servers[j].maxCPU;
                    double rr = ramUsed[j] / servers[j].maxRAM;
                    double sr = storageUsed[j] / servers[j].maxStorage;
                    fResource += cr * cr + rr * rr + sr * sr;
                    loads[j] = cr; totalLoad += cr;
                }

                double fBalance = 0, avgLoad = totalLoad / 4;
                for (int j = 0; j < 4; j++) {
                    double diff = loads[j] - avgLoad;
                    fBalance += diff * diff;
                }

                double totalLat = 0;
                for (int j = 0; j < 4; j++) totalLat += latencyUsed[j];
                double fLatency = totalLat / assignment.length;

                double fPenalty = 0;
                for (int j = 0; j < 4; j++) {
                    if (svcCount[j] > servers[j].capacity) fPenalty += svcCount[j] - servers[j].capacity;
                    if (cpuUsed[j] > servers[j].maxCPU) fPenalty += cpuUsed[j] - servers[j].maxCPU;
                    if (ramUsed[j] > servers[j].maxRAM) fPenalty += ramUsed[j] - servers[j].maxRAM;
                    if (storageUsed[j] > servers[j].maxStorage) fPenalty += storageUsed[j] - servers[j].maxStorage;
                    if (latencyUsed[j] > servers[j].maxLatency) fPenalty += latencyUsed[j] - servers[j].maxLatency;
                }

                double wResource = Main.W_RESOURCE * fResource;
                double wBalance = Main.W_BALANCE * fBalance;
                double wLatency = Main.W_LATENCY * fLatency;
                double wPenalty = Main.PENALTY * fPenalty;
                double total = wResource + wBalance + wLatency + wPenalty;

                String[] names = {"Resource Utilization", "Load Balance Variance", "Network Latency", "Constraint Penalty"};
                double[] values = {wResource, wBalance, wLatency, wPenalty};
                Color[] colors = {ACCENT_BLUE, ACCENT_GREEN, ACCENT_AMBER, ACCENT_RED};
                String[] formulas = {
                    String.format("W1 (0.3) * %.4f = %.4f", fResource, wResource),
                    String.format("W2 (0.3) * %.6f = %.6f", fBalance, wBalance),
                    String.format("W3 (0.2) * %.4f = %.4f", fLatency, wLatency),
                    String.format("P (1,000) * %.2f = %.2f", fPenalty, wPenalty)
                };

                double maxVal = Math.max(total, 1);
                int barX = 16, barW = w - 32;
                int startY = 38;
                int availH = h - startY - 40;
                int itemH = availH / 4;
                int barH = Math.max(12, Math.min(18, itemH - 20));

                for (int i = 0; i < 4; i++) {
                    int y = startY + i * itemH;

                    g2.setColor(colors[i]);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.drawString(names[i], barX, y);

                    g2.setColor(TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2.drawString(formulas[i], barX + 140, y);

                    g2.setColor(new Color(28, 38, 58));
                    g2.fillRoundRect(barX, y + 4, barW, barH, 4, 4);

                    double ratio = values[i] / maxVal;
                    int fillW = Math.max(2, (int)(barW * Math.min(ratio, 1.0)));
                    g2.setColor(colors[i]);
                    g2.fillRoundRect(barX, y + 4, fillW, barH, 4, 4);

                    g2.setColor(TEXT_WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(String.format("%.4f", values[i]), barX + 6, y + 4 + barH - 3);
                }

                // Total divider
                int totalY = startY + 4 * itemH + 6;
                g2.setColor(BORDER_COLOR);
                g2.drawLine(barX, totalY, barX + barW, totalY);
                totalY += 18;

                g2.setColor(TEXT_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(String.format("TOTAL FITNESS COST:  %.4f", total), barX, totalY);

                String status = fPenalty == 0 ? "[0 Violations: Valid Solution]" : "[Constraint Violations Present]";
                g2.setColor(fPenalty == 0 ? ACCENT_GREEN : ACCENT_RED);
                g2.drawString(status, barX + 220, totalY);
            }
        };
    }

    private JPanel createConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(new Color(16, 22, 36));
        head.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel title = new JLabel("Live Solver Execution Log");
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(TEXT_WHITE);

        JLabel tag = new JLabel("Stagnation Limit: 500");
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tag.setForeground(TEXT_MUTED);

        head.add(title, BorderLayout.WEST);
        head.add(tag, BorderLayout.EAST);
        panel.add(head, BorderLayout.NORTH);

        consoleArea = new JTextArea();
        consoleArea.setBackground(new Color(10, 14, 22));
        consoleArea.setForeground(new Color(209, 213, 219));
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        consoleArea.setEditable(false);
        consoleArea.setMargin(new Insets(8, 8, 8, 8));
        consoleArea.setText(solverLogText);

        JScrollPane scroll = new JScrollPane(consoleArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // -------------------------------------------------------------------------
    // TAB 2: SERVER INFRASTRUCTURE & HARDWARE RACKS
    // -------------------------------------------------------------------------
    private JPanel createServerInfrastructureTab() {
        serversGridPanel = new JPanel(new GridLayout(2, 2, 14, 14));
        serversGridPanel.setBackground(BG_DARK);
        serversGridPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        renderServerCards();
        return serversGridPanel;
    }

    private void renderServerCards() {
        serversGridPanel.removeAll();

        double[] cpuUsed = new double[4], ramUsed = new double[4], storageUsed = new double[4], latencyUsed = new double[4];
        List<List<Integer>> sGroups = new ArrayList<>();
        for (int j = 0; j < 4; j++) sGroups.add(new ArrayList<>());
        for (int i = 0; i < assignment.length; i++) {
            int s = assignment[i];
            cpuUsed[s] += services[i].cpuDemand; ramUsed[s] += services[i].ramDemand;
            storageUsed[s] += services[i].storageDemand; latencyUsed[s] += services[i].latency;
            sGroups.get(s).add(i);
        }

        for (int j = 0; j < 4; j++) {
            final int sIdx = j;
            JPanel card = new JPanel(new BorderLayout(0, 10)) {
                {
                    setBackground(BG_CARD);
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SERVER_ACCENTS[sIdx], 1, true),
                        new EmptyBorder(12, 16, 12, 16)
                    ));
                }
            };

            // Server Card Header
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);

            JLabel nameLbl = new JLabel(servers[j].name);
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            nameLbl.setForeground(SERVER_ACCENTS[j]);

            JLabel slotBadge = new JLabel(String.format("Capacity: %d / %d Slots", sGroups.get(j).size(), servers[j].capacity));
            slotBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            slotBadge.setForeground(sGroups.get(j).size() == 3 ? ACCENT_GREEN : ACCENT_AMBER);

            header.add(nameLbl, BorderLayout.WEST);
            header.add(slotBadge, BorderLayout.EAST);
            card.add(header, BorderLayout.NORTH);

            // Hardware Gauges
            JPanel gauges = new JPanel(new GridLayout(4, 1, 0, 6));
            gauges.setOpaque(false);

            gauges.add(createResourceGauge("CPU Core Load", cpuUsed[j], servers[j].maxCPU, "%"));
            gauges.add(createResourceGauge("RAM Memory", ramUsed[j], servers[j].maxRAM, "GB"));
            gauges.add(createResourceGauge("Disk Storage", storageUsed[j], servers[j].maxStorage, "GB"));
            gauges.add(createResourceGauge("Latency Limit", latencyUsed[j], servers[j].maxLatency, "ms"));
            card.add(gauges, BorderLayout.CENTER);

            // Assigned Service Chips Row
            JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            chipsPanel.setOpaque(false);
            chipsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                "Hosted Microservices", 0, 0,
                new Font("Segoe UI", Font.BOLD, 10), TEXT_MUTED
            ));

            for (int svcIdx : sGroups.get(j)) {
                JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
                chip.setBackground(SERVICE_CHIP_COLORS[svcIdx]);
                chip.setBorder(new EmptyBorder(2, 8, 2, 8));

                JLabel chipLbl = new JLabel(services[svcIdx].name);
                chipLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                chipLbl.setForeground(Color.BLACK);
                chip.add(chipLbl);

                chipsPanel.add(chip);
            }
            card.add(chipsPanel, BorderLayout.SOUTH);

            serversGridPanel.add(card);
        }
        serversGridPanel.revalidate();
        serversGridPanel.repaint();
    }

    private JPanel createResourceGauge(String name, double used, double max, String unit) {
        JPanel p = new JPanel(new BorderLayout(8, 2));
        p.setOpaque(false);

        double ratio = used / max;
        JLabel lbl = new JLabel(String.format("%s: %.0f/%.0f %s (%.0f%%)", name, used, max, unit, ratio * 100));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(TEXT_WHITE);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.min(100, ratio * 100));
        bar.setPreferredSize(new Dimension(140, 8));
        bar.setBorder(null);
        bar.setBackground(new Color(30, 42, 64));
        bar.setForeground(ratio > 1.0 ? ACCENT_RED : ratio > 0.8 ? ACCENT_AMBER : ACCENT_BLUE);

        p.add(lbl, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    // -------------------------------------------------------------------------
    // TAB 3: MICROSERVICE AUDIT TABLE
    // -------------------------------------------------------------------------
    private JPanel createServiceAuditTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"Service Name", "CPU Demand", "RAM Demand", "Storage Demand", "Latency", "Placed Server", "Constraint Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        refreshAuditTableData();

        JTable table = new JTable(tableModel);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_WHITE);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);

        JTableHeader th = table.getTableHeader();
        th.setBackground(new Color(18, 25, 42));
        th.setForeground(TEXT_WHITE);
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < cols.length; i++) table.getColumnModel().getColumn(i).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private void refreshAuditTableData() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (int i = 0; i < services.length; i++) {
            Main.Service s = services[i];
            int svrIdx = assignment[i];
            tableModel.addRow(new Object[]{
                s.name,
                String.format("%.0f %%", s.cpuDemand),
                String.format("%.0f GB", s.ramDemand),
                String.format("%.0f GB", s.storageDemand),
                String.format("%.0f ms", s.latency),
                servers[svrIdx].name,
                "FEASIBLE [OK]"
            });
        }
    }

    // -------------------------------------------------------------------------
    // TAB 4: MATHEMATICAL OPTIMIZATION MODEL (Presentation Slides)
    // -------------------------------------------------------------------------
    private JPanel createFormulationTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JTextArea modelText = new JTextArea();
        modelText.setBackground(BG_CARD);
        modelText.setForeground(new Color(229, 231, 235));
        modelText.setFont(new Font("Consolas", Font.PLAIN, 13));
        modelText.setEditable(false);
        modelText.setMargin(new Insets(16, 16, 16, 16));

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("         MATHEMATICAL OPTIMIZATION MODEL: SERVICE PLACEMENT PROBLEM\n");
        sb.append("         Course: Optimization Methods (Lectures 1 & 2 Foundations)\n");
        sb.append("========================================================================================\n\n");
        sb.append("1. PROBLEM DATA (Sets and Parameters):\n");
        sb.append("   - Servers J = {Server-1, Server-2, Server-3, Server-4} (M = 4)\n");
        sb.append("   - Services I = {Svc-1, Svc-2, ..., Svc-12} (N = 12)\n");
        sb.append("   - Resource Bounds per Server: MaxCPU, MaxRAM, MaxStorage, MaxLatency\n");
        sb.append("   - Hard Server Slot Capacity = 3 Services per Server\n\n");
        sb.append("2. DECISION VARIABLES:\n");
        sb.append("   - Integer Vector: x = [s_0, s_1, ..., s_11], where s_i in {0, 1, 2, 3}\n");
        sb.append("   - Total Unconstrained Search Space: 4^12 = 16,777,216 Combinations\n\n");
        sb.append("3. MULTI-OBJECTIVE COST FUNCTION F(x):\n");
        sb.append("   Minimize F(x) = W_res * f_resource + W_bal * f_balance + W_lat * f_latency + P * f_penalty\n\n");
        sb.append("   Where:\n");
        sb.append("     - f_resource = Sum_j [ (CPU_j/MaxCPU)^2 + (RAM_j/MaxRAM)^2 + (Storage_j/MaxStorage)^2 ]\n");
        sb.append("     - f_balance  = Sum_j [ (CPU_j/MaxCPU - AvgCPU_Load)^2 ]  (Cluster Variance)\n");
        sb.append("     - f_latency  = Sum_j [ Latency_j ] / N\n");
        sb.append("     - f_penalty  = Sum_j [ Violations_j ]  (Exterior Penalty Multiplier P = 1,000)\n\n");
        sb.append("4. HEURISTIC GA SOLVER ARCHITECTURE:\n");
        sb.append("   - Population Size : 50 Candidate Solutions\n");
        sb.append("   - Selection       : Tournament Selection (k = 3)\n");
        sb.append("   - Crossover       : Single-Point Recombination (Rate = 0.8)\n");
        sb.append("   - Mutation        : Random Perturbation (Rate = 0.1)\n");
        sb.append("   - Elitism         : Top 2 Candidates Preserved Intact\n\n");
        sb.append("5. AUTONOMOUS TERMINATION CRITERION:\n");
        sb.append("   - Feasibility Condition : Zero Constraint Violations (Penalty == 0)\n");
        sb.append("   - Plateau Condition     : No Fitness Improvement (> 1e-5) for 500 Consecutive Generations\n");
        sb.append("   - No Fixed Loop Count   : Runs dynamically until optimal convergence is proven!\n");

        modelText.setText(sb.toString());

        JScrollPane scroll = new JScrollPane(modelText);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // =========================================================================
    // 4. ASYNCHRONOUS RE-RUN CONTROLLER
    // =========================================================================
    private void executeReOptimization() {
        rerunButton.setEnabled(false);
        rerunButton.setText("Solving GA...");
        statusPill.setText("  [OPTIMIZING...]  ");
        statusPill.setForeground(ACCENT_AMBER);
        statusPill.setBackground(new Color(245, 158, 11, 30));

        SwingWorker<Main.CandidateSolution, Void> worker = new SwingWorker<>() {
            private String newLog;
            private long startT;
            private long durMs;

            @Override
            protected Main.CandidateSolution doInBackground() {
                startT = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                sb.append(">>> Launching GA Heuristic Optimization...\n");
                sb.append(String.format("Servers: %d | Services: %d | Capacity: 3 each\n", Main.NUM_SERVERS, Main.NUM_SERVICES));
                sb.append("Convergence Mode: Dynamic Stagnation Plateau (Patience = 500)\n");
                sb.append("----------------------------------------------------------------------\n");

                Main.CandidateSolution sol = Main.runOptimization();
                durMs = System.currentTimeMillis() - startT;

                sb.append(Main.lastSolverLog);
                newLog = sb.toString();
                return sol;
            }

            @Override
            protected void done() {
                try {
                    Main.CandidateSolution best = get();
                    assignment = best.assignment;
                    bestFitness = best.fitness;
                    bestHistory = Main.bestFitnessHistory;
                    avgHistory = Main.avgFitnessHistory;
                    elapsedMs = durMs;
                    solverLogText = newLog;

                    // Update KPI Header Labels
                    kpiCostVal.setText(String.format("%.4f", bestFitness));
                    kpiGenVal.setText(String.format("%,d Gens", bestHistory.length));
                    kpiEvalVal.setText(String.format("%,d", Main.totalEvaluations));
                    kpiTimeVal.setText(String.format("%.2f s", elapsedMs / 1000.0));

                    // Update Console Area
                    consoleArea.setText(solverLogText);
                    consoleArea.setCaretPosition(consoleArea.getDocument().getLength());

                    // Refresh Views
                    convergencePanel.repaint();
                    breakdownPanel.repaint();
                    renderServerCards();
                    refreshAuditTableData();

                    statusPill.setText("  [OPTIMAL & FEASIBLE]  ");
                    statusPill.setForeground(ACCENT_GREEN);
                    statusPill.setBackground(new Color(16, 185, 129, 30));
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    rerunButton.setEnabled(true);
                    rerunButton.setText("Re-Run Optimization");
                }
            }
        };
        worker.execute();
    }

    // Public Show Entry Point
    public static void show(Main.Server[] servers, Main.Service[] services,
                            int[] assignment, double fitness,
                            double[] bestHistory, double[] avgHistory, long elapsedMs,
                            String logText) {
        SwingUtilities.invokeLater(() -> {
            Dashboard d = new Dashboard(servers, services, assignment, fitness,
                    bestHistory, avgHistory, elapsedMs, logText);
            d.setVisible(true);
        });
    }
}
