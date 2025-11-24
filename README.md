 Functional Requirements
3.1 Nightly High-Value Transaction Monitor (Scheduled Job)
Use Case: Every night, the bank must scan the previous day’s transactions and identify high-
value transactions for risk and compliance.
Trigger:
• Implement as a scheduled job using @Scheduled (e.g., 01:00 AM), and optionally expose a
manual trigger endpoint:
POST /jobs/run-high-value-monitor?date=YYYY-MM-DD
Behavior:
• For the given date (or yesterday by default), stream all transactions from the
TRANSACTION table.
• Use a streaming JDBC ResultSet (forward-only, read-only) with an appropriate fetchSize to
simulate a large data set.
• Implement a producer–consumer pattern:
– Producer: reads rows in batches (e.g., 500 rows) from the ResultSet and pushes each
batch into a bounded BlockingQueue.
– Consumers: a fixed-size ExecutorService with worker threads that poll batches from the
queue.
• For each batch, use Java Streams to:
– Filter transactions above a configurable threshold (risk.highValueThreshold from
application.yaml).
– Create entries in HIGH_VALUE_TXN table with TXN_ID, ACCOUNT_ID, AMOUNT,

TXN_TIMESTAMP, and REASON.
• Log total transactions processed, high-value count, and total runtime of the job.


Use rthe following endpojnts for metrices 
/actuator/metrics/highValue.monitor.time
/jobs/run-high-value-monitor?date=2024-01-24


Needs oracle wallet to connect to oracle db  
Place the folder in resources directory
