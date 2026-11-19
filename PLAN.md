# Ticket Management System — LLD Plan

A small support-desk system that runs in the terminal. Customers raise tickets, the system assigns
each one to a support agent, and agents move tickets through their lifecycle.

Goal: learn **OOP, SOLID, a few design patterns and LLD thinking** in under 1 hour.
Language: **Java 17**, no frameworks, no database, no build tool. Just `javac` + `java`.

---

## 1. How to approach any LLD problem (the 6-step recipe)

Interviewers want to see this order. Follow it every time.

1. **Clarify requirements**: what must the system do? (functional) What qualities must it have? (non-functional)
2. **Find the nouns**: these become your **entities / classes** (Ticket, Agent, Customer).
3. **Find the verbs**: these become **methods / services** (create ticket, assign, resolve).
4. **Define relationships**: has-a, is-a, who owns whom.
5. **Spot what can change**: put those behind **interfaces** (storage, assignment rule, notifications).
6. **Write code layer by layer**: model → repository → service → UI.

---

## 2. Requirements

### Functional
| # | Requirement |
|---|-------------|
| F1 | Register customers and support agents |
| F2 | A customer can create a ticket (title, description, priority) |
| F3 | A new ticket is **auto-assigned** to an agent |
| F4 | An agent can move a ticket: `OPEN → IN_PROGRESS → RESOLVED → CLOSED` |
| F5 | Invalid moves are rejected (you can't close an OPEN ticket) |
| F6 | View all tickets, or tickets for one agent |
| F7 | Notify the customer whenever their ticket changes |

### Non-functional
- **Extensible**: swapping the assignment rule or the notification channel should not touch existing classes.
- **Testable**: business logic does not depend on the console or on a concrete storage class.
- **Simple**: in-memory storage, single user at the terminal.

### Out of scope (say this in interviews; it shows maturity)
Login and auth, persistence to a DB, concurrency, SLA timers, comments on tickets.

---

## 3. Entities (nouns → classes)

| Class | Type | Responsibility |
|-------|------|----------------|
| `User` | abstract class | Common fields: `id`, `name`, `email` |
| `Customer` | extends `User` | Raises tickets |
| `Agent` | extends `User` | Works on tickets; tracks how many are active |
| `Ticket` | class | id, title, description, priority, status, customer, assigned agent, timestamps |
| `TicketStatus` | enum | `OPEN, IN_PROGRESS, RESOLVED, CLOSED` + **which moves are allowed** |
| `Priority` | enum | `LOW, MEDIUM, HIGH` |

---

## 4. Class diagram

```
                 ┌──────────────┐
                 │  <<abstract>>│
                 │     User     │
                 │ id,name,email│
                 └──────┬───────┘
              is-a      │      is-a
          ┌─────────────┴─────────────┐
   ┌──────┴─────┐              ┌──────┴──────┐
   │  Customer  │              │    Agent    │
   └──────┬─────┘              │ activeCount │
          │ 1                  └──────┬──────┘
          │ raises                    │ 1  works on
          │ *                         │ *
        ┌─┴───────────────────────────┴─┐
        │            Ticket             │
        │ id, title, priority, status   │
        │ updateStatus(next)            │
        └───────────────────────────────┘

  ┌───────────────────────── TicketService ──────────────────────────┐
  │  depends on INTERFACES only (Dependency Inversion)                │
  │                                                                   │
  │   TicketRepository ◄── InMemoryTicketRepository                   │
  │   UserRepository   ◄── InMemoryUserRepository                     │
  │   AssignmentStrategy ◄── RoundRobinAssignmentStrategy             │
  │                      ◄── LeastLoadedAssignmentStrategy (you!)     │
  │   NotificationService ◄── ConsoleNotificationService              │
  └───────────────────────────────────────────────────────────────────┘

  ConsoleApp (menu, reads input) ──calls──► TicketService / UserService
  Main ── creates everything and wires it together (manual DI)
```

---

## 5. Ticket lifecycle (state diagram)

```
   OPEN ──► IN_PROGRESS ──► RESOLVED ──► CLOSED
                ▲               │
                └── (reopen) ───┘
```

The rules live **inside the `TicketStatus` enum** (`canTransitionTo`). The status knows where it can go
next, so no service class needs a big `if/else` chain. This is a lightweight form of the **State pattern**.

---

## 6. SOLID: where each principle lives in this code

| Principle | Meaning in one line | Where you'll see it |
|-----------|---------------------|---------------------|
| **S**ingle Responsibility | One class, one reason to change | `ConsoleApp` only does I/O, `TicketService` only does business rules, repositories only store data |
| **O**pen/Closed | Add behaviour by adding classes, not by editing old ones | New assignment rule = new class implementing `AssignmentStrategy`. `TicketService` doesn't change |
| **L**iskov Substitution | A subclass must work wherever its parent is expected | `Customer` and `Agent` can both be used as a `User`; any `TicketRepository` impl works in the service |
| **I**nterface Segregation | Small, focused interfaces | `NotificationService` has one method; `AssignmentStrategy` has one method. Nobody implements methods they don't need |
| **D**ependency Inversion | Depend on abstractions, not concrete classes | `TicketService` constructor takes interfaces; `Main` decides the concrete classes |

---

## 7. Design patterns used (only 4, kept simple)

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `AssignmentStrategy` | Swap "how to pick an agent" at runtime |
| **Repository** | `TicketRepository`, `UserRepository` | Hide storage details; easy to swap for a DB later |
| **Observer (simplified)** | `NotificationService` called on every change | Decouple "something happened" from "who gets told" |
| **State (lightweight)** | `TicketStatus.canTransitionTo` | Each status knows its valid next states |

Also used: **manual Dependency Injection** in `Main` (no Spring needed to understand DI).

---

## 8. Other principles worth naming

- **Encapsulation**: `Ticket` fields are private; status only changes via `updateStatus()`, which validates.
- **Fail fast**: custom exceptions (`TicketNotFoundException`, `InvalidStatusTransitionException`) instead of silent `null`s.
- **DRY**: common user fields live once in `User`.
- **KISS / YAGNI**: no DB, no threads, no frameworks. Add them only when needed.
- **Composition over inheritance**: `TicketService` *has* a strategy, a repo and a notifier. It doesn't extend them.

---

## 9. Folder structure

```
src/com/ticketsystem/
├── Main.java                         # wiring (composition root)
├── model/
│   ├── User.java                     # abstract
│   ├── Customer.java
│   ├── Agent.java
│   ├── Ticket.java
│   ├── TicketStatus.java             # enum + transition rules
│   └── Priority.java                 # enum
├── exception/
│   ├── TicketNotFoundException.java
│   ├── UserNotFoundException.java
│   └── InvalidStatusTransitionException.java
├── repository/
│   ├── TicketRepository.java         # interface
│   ├── InMemoryTicketRepository.java
│   ├── UserRepository.java           # interface
│   └── InMemoryUserRepository.java
├── strategy/
│   ├── AssignmentStrategy.java       # interface
│   ├── RoundRobinAssignmentStrategy.java
│   └── LeastLoadedAssignmentStrategy.java   # ← your exercise
├── notification/
│   ├── NotificationService.java      # interface
│   └── ConsoleNotificationService.java
├── service/
│   ├── UserService.java
│   └── TicketService.java
└── cli/
    └── ConsoleApp.java               # menu loop
```

**Layers (dependencies only point downwards):**
```
cli  →  service  →  repository / strategy / notification  →  model
```

---

## 10. Walkthrough: what happens when a customer creates a ticket

1. `ConsoleApp` reads title, description, priority, and customer id from the keyboard.
2. Calls `ticketService.createTicket(customerId, title, desc, priority)`.
3. `TicketService` fetches the customer from `UserRepository` (throws if not found).
4. Asks `AssignmentStrategy.assign(agents)` → gets an `Agent`.
5. Builds the `Ticket`, links the agent, increments the agent's active count.
6. Saves via `TicketRepository.save(ticket)`.
7. Calls `notificationService.notify(customer, "Ticket #1 created...")`.
8. Returns the ticket to `ConsoleApp`, which prints it.

Notice that `TicketService` never knows the data is in a `HashMap`, never knows we print to the console,
and never knows the agent was chosen round-robin. That is **DIP + OCP** in practice.

---

## 11. Build order (≈ 50 minutes)

| Step | Time | What | Concept to focus on |
|------|------|------|---------------------|
| 1 | 10 min | Enums + model classes | Encapsulation, inheritance, enum with behaviour |
| 2 | 5 min | Custom exceptions | Fail fast |
| 3 | 10 min | Repository interfaces + in-memory impl | Repository pattern, DIP |
| 4 | 5 min | Strategy + notifier | Strategy, OCP, ISP |
| 5 | 10 min | Services | SRP, composition |
| 6 | 5 min | ConsoleApp + Main | Separation of UI, manual DI |
| 7 | 5 min | Run it, then write `LeastLoadedAssignmentStrategy` | Proving OCP: new feature, zero edits to service |

---

## 12. How to run

From the project folder (the one containing `src/`):

```bash
javac -d out $(find src -name "*.java")
java -cp out com.ticketsystem.Main
```

The app starts with 2 agents and 2 customers already loaded so you can try it immediately.

---

## 13. Interview questions you can now answer

1. **Why is `TicketStatus` an enum with logic instead of a String?** Type safety, and the transition rules live in one place.
2. **How would you add email notifications?** Write `EmailNotificationService implements NotificationService` and change one line in `Main`.
3. **How would you move to a database?** Write `JdbcTicketRepository implements TicketRepository`. The service is untouched.
4. **How would you make this thread-safe?** Use `ConcurrentHashMap`, `AtomicInteger` for IDs, and synchronize the assign-and-save step.
5. **Why is `User` abstract?** A plain "User" never exists on its own; it is always a Customer or an Agent.
6. **Where would you add SLA / escalation?** A new `EscalationPolicy` interface checked by a scheduler. The existing classes stay closed for modification.

---

## 14. Extensions to try after (practice)

- `LeastLoadedAssignmentStrategy` (started for you, see `TODO(human)`)
- Priority-based assignment: HIGH tickets go to senior agents
- Add comments to a ticket (`List<Comment>` inside `Ticket`)
- Search tickets by status or priority
- Save and load tickets from a CSV file (a new repository implementation!)
