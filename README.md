# Smart Blood Bank — Documentation

## Architecture in one picture

ui/  ──uses──▶  service/  ──uses──▶  model/
(screens)       (business logic)     (data + rules)

- model/ — the "nouns": User, Donor, Patient, BloodBag, BloodType, EmergencyLevel
- service/ — the "verbs": BloodBank, BloodMatcher, InventoryManager, EmergencyRequest, FileManager, DemoDataSeeder
- ui/ — the 8 JavaFX screens plus shared infrastructure

## How the app starts up

1. MainApp.start() runs first.
2. It creates one AppContext, which creates one shared copy of every
   service class. Every screen is handed this same AppContext.
3. AppContext.loadData() tries to load saved data via FileManager.
   If nothing has ever been saved, DemoDataSeeder fills the app with
   sample donors, patients, and blood bags instead.
4. ScreenManager opens the window on the Login screen.
5. On successful login, ScreenManager.showApp() builds the AppShell.
6. On exit, MainApp.stop() calls AppContext.saveData().

## OOP principles — where they live in this code

| Principle | Where |
|---|---|
| Encapsulation | Every model class (private fields, validated setters); BloodBag's status only changes through reserve()/markUsed()/markExpired() |
| Inheritance | User → Donor, Patient |
| Polymorphism | Donor.displayInfo() / Patient.displayInfo() override User's abstract method differently |
| Abstraction | User is abstract; Patient implements Comparable; Screen is an abstract base for every UI screen |

## Data flow example: fulfilling an emergency request

1. UI: EmergencyRequestScreen shows the pending queue.
2. Service: clicking Fulfill calls BloodMatcher.matchAndReserve(patient).
3. Model: each reserved BloodBag transitions AVAILABLE → RESERVED → USED.
4. Service: EmergencyRequest.removeRequest() takes the patient out of the queue.
5. UI: the screen refreshes and the patient disappears from the list.
