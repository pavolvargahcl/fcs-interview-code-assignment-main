# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

Key challenges:
- **Shared cost attribution**: When a warehouse serves multiple stores, how do we fairly split shared costs (rent, utilities, labor)? A warehouse with 3 stores needs allocation keys — by volume shipped, by revenue, by number of SKUs? The choice of key materially changes each store's P&L.
- **Cost granularity vs. complexity trade-off**: Tracking every cost at the warehouse-product-store level gives precision but creates data volume and processing overhead. We need to decide the right level of granularity.
- **Temporal cost variation**: Labor and transportation costs fluctuate seasonally (holiday peaks). A static allocation model will misrepresent costs during peak/off-peak periods.
- **Transportation cost modeling**: Costs between warehouses and stores depend on distance, shipment frequency, and carrier rates. A warehouse colocation change directly impacts transportation costs.

Questions I would ask:
- What is the current cost allocation methodology? Is it activity-based costing (ABC) or a simpler proportional model?
- Are there SLAs between warehouses and stores that affect cost (e.g., same-day delivery premium)?
- How frequently do cost reports need to be generated — real-time, daily, monthly?
- Which stakeholders consume cost data, and what decisions do they make with it?

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

Potential strategies (prioritized by impact and feasibility):

1. **Warehouse-Store proximity optimization**: Analyze whether products are being shipped from the closest warehouse. Reducing average shipping distance directly reduces transportation costs and delivery times. Expected: 10-20% reduction in logistics costs.

2. **Inventory consolidation**: If multiple warehouses store the same SKUs at low utilization, consolidating inventory into fewer warehouses reduces overhead while maintaining coverage. Expected: reduced holding costs and improved stock turnover.

3. **Capacity utilization analysis**: The system already tracks capacity and stock per warehouse. Identifying underutilized warehouses (low stock-to-capacity ratio) reveals opportunities to either fill unused space or downsize. Expected: better fixed-cost absorption.

4. **Demand-driven stock positioning**: Use store sales data to pre-position popular products in warehouses closest to high-demand stores. Reduces emergency transfers and expedited shipping.

Prioritization approach:
- Start with data analysis: which cost categories represent the largest spend?
- Focus on quick wins (rebalancing existing inventory) before structural changes (warehouse replacement).
- Measure baseline metrics first, then track improvement after each change.

Questions I would ask:
- What percentage of total fulfillment cost is transportation vs. storage vs. labor?
- Are there contractual constraints on warehouse leases that limit flexibility?
- What is the current service level (delivery time) and how much buffer exists?

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

Benefits of integration:
- **Single source of truth**: Eliminates discrepancies between operational and financial data. When the warehouse system records a stock movement, the financial system should reflect the corresponding cost journal entry.
- **Timely decision-making**: Real-time cost visibility enables proactive management rather than reacting to month-end reports.
- **Compliance and auditability**: Automated data flow between systems creates an audit trail, reducing manual reconciliation errors and satisfying regulatory requirements.
- **Automated reporting**: Integrated systems can generate P&L by warehouse, by store, or by product without manual data extraction and transformation.

Ensuring seamless integration:
- **Event-driven architecture**: Use domain events (similar to how we implemented the Store CDI events in this project) to publish cost-relevant operations. Financial systems subscribe to these events asynchronously, ensuring loose coupling.
- **Idempotency and reconciliation**: Every financial event should carry a unique correlation ID. The financial system should handle duplicate events gracefully. Periodic reconciliation jobs verify data consistency.
- **API contract stability**: Use OpenAPI specs (as done for the Warehouse API) to define the integration contract. Version the API to allow independent evolution.
- **Error handling and dead-letter queues**: Failed synchronization attempts should be captured, alerted on, and retried — never silently dropped.

Questions I would ask:
- What financial systems are in use (SAP, Oracle, custom)?
- Is there an existing integration middleware (ESB, message broker)?
- What is the acceptable latency for cost data synchronization?
- Are there regulatory requirements (e.g., IFRS, GAAP) that dictate how costs must be recorded?

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Importance:
- **Proactive cost management**: Without forecasting, cost overruns are only discovered after they occur. A budget-vs-actual dashboard enables early corrective action.
- **Capacity planning**: Forecasting demand helps determine when to expand, replace, or consolidate warehouses — directly tying into the warehouse replacement operation in our system.
- **Resource allocation**: Labor scheduling, inventory purchasing, and transportation contracts all depend on anticipated volumes.

Design considerations for a forecasting system:
- **Historical data as foundation**: The system already timestamps warehouse creation and archival. Extending this to track cost events over time builds the historical dataset needed for trend analysis.
- **Seasonality modeling**: Fulfillment costs are inherently seasonal. The system should support multi-year historical comparisons to identify patterns.
- **Scenario modeling**: Enable "what-if" analysis — e.g., "What if we replace warehouse X with a larger one at location Y? How does that affect total cost?"
- **Variance tracking**: Automatically flag when actual costs deviate from budget by a configurable threshold. Integrate alerts with the operational dashboard.
- **Granularity**: Budget at the warehouse level (fixed costs) and at the warehouse-product level (variable costs). Roll up to store and company-wide views.

Questions I would ask:
- What is the budget cycle (annual, quarterly, rolling)?
- How mature is the current forecasting capability — spreadsheets, or existing tooling?
- What external factors (fuel prices, labor market, lease renewals) most impact cost forecasts?
- Who owns the budget: operations, finance, or shared responsibility?

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

Why preserve cost history:
- **Baseline for the new warehouse**: The historical cost data of the old warehouse serves as the benchmark for the replacement. If the old warehouse's monthly operating cost was X, the new one should ideally come in at or below X (adjusted for the capacity change).
- **Business Unit Code continuity**: By reusing the BU code, the company maintains a continuous cost narrative for that business unit. Financial reports can show cost trends across the old and new warehouse under one code, making it easy to evaluate whether the replacement delivered the expected savings.
- **Audit and compliance**: Archived cost data must be retained for financial audits. Even though the old warehouse is archived, its cost records are part of the company's financial history.
- **ROI calculation**: The replacement decision was presumably justified by a business case (e.g., "the new warehouse will reduce operating costs by 15%"). Preserving the old costs allows post-implementation validation of that business case.

Cost control during replacement:
- **Transition cost budgeting**: The replacement period incurs one-time costs — stock transfer, dual operation overlap, setup costs. These should be budgeted separately from ongoing operations.
- **Stock matching validation**: Our implementation enforces that the new warehouse's stock matches the old one. This prevents inventory discrepancies that would create hidden costs (write-offs, emergency procurement).
- **Capacity validation**: We validate that the new capacity can handle existing stock. An undersized replacement would force overflow operations at higher cost.
- **Parallel cost tracking**: During the transition window, costs should be tracked for both the old (winding down) and new (ramping up) warehouse under the same BU code, with clear delineation.

Questions I would ask:
- What is the expected duration of the transition period?
- Are there contractual obligations on the old warehouse (lease exit costs)?
- How will in-flight shipments be handled during the transition?
- What is the budget allocated for the replacement beyond ongoing operations?

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
