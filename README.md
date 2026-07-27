# Smart Blood Bank Management System — Technical Documentation

## 1. Overview

The Smart Blood Bank Management System is a JavaFX desktop application supporting **SDG 3 (Good Health and Well-being)**. It simulates real hospital blood bank operations: automatic blood-type matching, emergency prioritization, expiry tracking, and low-stock alerting — not a simple CRUD application.

## 2. Architecture

```
ui/  ──uses──▶  service/  ──uses──▶  model/
(screens)       (business logic)     (data + rules)
```

| Layer | Responsibility | Classes |
|---|---|---|
| **model** | Data and rules for a single object | User, Donor, Patient, BloodBag, BloodType, EmergencyLevel |
| **service** | Logic that works across the whole system | BloodBank, BloodMatcher, InventoryManager, EmergencyRequest, FileManager, DemoDataSeeder |
| **ui** | Presentation only — no business logic | 8 JavaFX screens + shared infrastructure (AppContext, AppShell, ScreenManager, Screen, MainApp) |

## 3. Application Startup Sequence

1. `MainApp.start()` is the JavaFX entry point.
2. It creates a single `AppContext`, which instantiates one shared copy of every service class. Every screen receives this same `AppContext`, so all screens read and write the same underlying data.
3. `AppContext.loadData()` loads persisted data via `FileManager`. On a first run with no saved data, `DemoDataSeeder` populates realistic sample donors, patients, and blood bags.
4. `ScreenManager` opens the window on the Login screen.
5. On successful authentication, `ScreenManager.showApp()` builds the `AppShell` (sidebar + active screen).
6. On application close, `MainApp.stop()` calls `AppContext.saveData()`.

## 4. OOP Principles Applied

| Principle | Implementation |
|---|---|
| **Encapsulation** | All model fields are private; `BloodBag` status transitions only through `reserve()` / `markUsed()` / `markExpired()`, never a public setter |
| **Inheritance** | `User` → `Donor`, `Patient` |
| **Polymorphism** | `Donor` and `Patient` override `displayInfo()`/`getRoleDescription()` differently; all 8 screens override `Screen`'s abstract methods differently |
| **Abstraction** | `User` and `Screen` are abstract classes; `Patient` implements the `Comparable` interface |

## 5. Module Reference

### 5.1 model/

| Class | Responsibility |
|---|---|
| `User` (abstract) | Shared identity fields (id, name, contact, blood type) for every person in the system |
| `Donor` | Tracks donation history; enforces the 90-day minimum interval between donations |
| `Patient` | Tracks blood request details; implements `Comparable` so a priority queue can sort patients by urgency with no external logic |
| `BloodType` (enum) | The 8 blood types; encodes donor→recipient compatibility rules |
| `EmergencyLevel` (enum) | Four urgency levels with numeric priority |
| `BloodBag` | A single unit of blood; status changes only through controlled methods; auto-calculates its 42-day expiry |

### 5.2 service/

| Class | Responsibility |
|---|---|
| `BloodBank` | Single in-memory source of truth for all donors, patients, and blood bags |
| `BloodMatcher` | Finds blood-type-compatible stock and reserves the soonest-to-expire bags first (FIFO) |
| `InventoryManager` | Raises low-stock and near-expiry alerts by reading `BloodBank` |
| `EmergencyRequest` | Priority queue of patients waiting for blood, ordered automatically by urgency |
| `FileManager` | Persists all data to plain-text files and reloads it on startup |
| `DemoDataSeeder` | Populates a fresh install with realistic sample data |

### 5.3 ui/

| Screen | Purpose |
|---|---|
| Login | Single demo-credential gate (`admin` / `admin123`) |
| Dashboard | Read-only KPI overview: donor/patient counts, stock by type, active alerts |
| Donor Management | Register/edit/delete donors; record donations |
| Patient Management | Register/edit/delete patients; submit emergency requests |
| Blood Inventory | View and manage all blood bags, grouped by type |
| Emergency Requests | Priority-ordered queue; fulfill or remove requests |
| Reports | Live statistics: donations over time, fulfillment rate, stock breakdown |
| Settings | Manual save control; displays configured alert thresholds |

## 6. Data Flow Example — Fulfilling an Emergency Request

1. **UI:** `EmergencyRequestScreen` displays the queue from `EmergencyRequest.getPendingRequestsSorted()`.
2. **Service:** the Fulfill action calls `BloodMatcher.matchAndReserve(patient)`, which filters `BloodBank`'s available stock to compatible types via `BloodType.canDonateTo()`, sorts by expiry date, and reserves the oldest matching bags.
3. **Model:** each reserved `BloodBag` transitions `AVAILABLE → RESERVED → USED`; the `Patient` is marked fulfilled.
4. **Service:** `EmergencyRequest.removeRequest()` removes the patient from the queue.
5. **UI:** the screen refreshes to reflect the updated state.

---

## 7. Live Demonstration Script (Full Application Walkthrough)

Use this as a single continuous script for a short screen-recording of the whole application, in sequence:

> "This is the Smart Blood Bank Management System, built in Java and JavaFX. I'll sign in with the demo account. [Login]
>
> This is the Dashboard — every number here, donor count, patient count, available units, and active alerts, is calculated live from the underlying data, not hardcoded. [Dashboard]
>
> In Donor Management, I can register a new donor and log a donation for them. [Donor Management — add donor, record donation]
>
> In Patient Management, I'll register a new patient and mark them as a Critical emergency case, then submit their request. [Patient Management — add patient, submit request]
>
> Switching to Emergency Requests, notice this patient is automatically at the top of the queue because of their priority — no manual sorting. Clicking Fulfill reserves the correct compatible blood bag for them, closest to expiry first, and removes them from the queue. [Emergency Requests — fulfill]
>
> In Blood Inventory, you can see that reservation reflected here, along with every other bag grouped by type, with expiry tracking. [Inventory]
>
> Reports gives a live statistical summary of the whole system. [Reports]
>
> And Settings lets me save data manually and view the alert thresholds driving everything you just saw. [Settings]
>
> That's the full application — every action you saw calls real business logic in the service layer, not placeholder UI."

This single script is meant to be recorded as one take covering the entire app, rather than a separate video per screen — shorter, and shows the layers working together rather than in isolation.
