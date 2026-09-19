# Performance Notes

The target workload is an HR manager working with about 10,000 employee records. The design uses server-side pagination with a maximum page size of 100, indexed directory filters, and a database allow-list for sorting. The browser never receives the full employee table.

Dashboard endpoints use grouped JPA projections for currency, department, country, and salary-band aggregates. Salary bands are selected per stored currency using fixed thresholds documented for the demo dataset; the query does not convert or combine currencies. Employee-to-department is a lazy relation and is mapped while the service transaction is open; salary history is fetched by an indexed employee/date query.

The current seed writes 250 employees and their history rows per batch. This is fast enough for the assessment dataset while keeping memory predictable. If workload grows materially, useful next steps are cursor pagination for very deep pages, explicit read models for analytics, database query-plan monitoring, caching of reference departments, and read replicas. Redis, Kafka, Elasticsearch, and microservices are not justified for this scope.
