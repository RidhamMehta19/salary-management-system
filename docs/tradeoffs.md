# Trade-offs

| Decision | Reason | Alternative | Trade-off |
|---|---|---|---|
| Modular monolith | One deployable unit is easy to understand and enough for 10,000 employees. | Microservices | Less independent scaling, but much lower operational complexity. |
| PostgreSQL | Relational constraints, grouping, indexes, and free-tier availability fit salary records. | SQLite | Easier local setup, but weaker production parity and concurrency. |
| PrimeNG | Mature accessible components cover tables, forms, paginator, cards, tags, and feedback quickly. | Angular Material or custom CSS | Adds bundle weight, but reduces custom interaction code and UI risk. |
| Server-side pagination | Keeps the browser responsive and makes database query intent explicit. | Load all employees | Simpler client state, but poor memory, latency, and filtering behavior. |
| Salary-history snapshots | A small append-only table gives useful change visibility without building payroll. | Overwrite current salary only | Simpler schema, but loses a valuable HR question: what changed and when? |
| Currency-grouped analytics | Avoids silently converting values without an exchange-rate requirement. | Convert to one currency | More comparable totals, but needs rate source, date semantics, and auditability. |
| No authentication in this assessment | Identity and roles are unspecified and not needed to demonstrate salary workflows. | Add SSO/RBAC now | Safer for production, but guessing roles would fabricate requirements. |
| Profile-based seed | Reproducible, fast, and avoids committing 10,000 SQL rows. | Giant SQL file or production startup seed | Easier to inspect SQL, but noisy, less maintainable, and risky if enabled accidentally. |
| Static frontend hosting + API + managed PostgreSQL | Fits free/low-cost deployment and separates frontend build from backend runtime. | Single VM or Kubernetes | Fewer moving parts and easier demos, with cold starts/provider limits as trade-offs. |
