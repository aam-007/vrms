# Vehicle Rental Management System

A complete console application built with Java Standard Edition, with modular development sources and an independently runnable single-file release.

## 1. Abstract

This project models a small rental company managing a heterogeneous fleet of cars, bikes, and trucks. It supports fleet registration, availability queries, identifier searches, rental and return operations, subtype-specific charges, long-rental discounts, immutable transaction bills, and cumulative revenue. The implementation demonstrates abstraction, inheritance, dynamic method dispatch, controlled state transitions, and separation of presentation from business operations. Monetary values use exact decimal arithmetic with an explicit rounding policy. State remains in memory for the lifetime of one process. A modular source layout supports maintenance and academic study, while a mechanically assembled release preserves the same class architecture in one portable Java source file. Neither representation requires a framework, build system, third-party library, configuration file, or external service.

## 2. Problem statement

A rental company must distinguish vehicles with different charging rules while maintaining a consistent rental workflow. A car includes daily insurance, a bike has a basic daily charge, and a truck includes a daily heavy-load surcharge. Each rental must refer to a known, available vehicle and have a positive duration. A successful transaction must change availability, produce a consistent bill, and increase revenue by the actual discounted charge. Invalid operations must leave these values intact. Returns restore availability without charging again. Operators also need readable fleet listings, searches, and a way to add new vehicles.

The engineering problem is to support these operations without mixing terminal input, pricing, inventory mutation, and accounting in one class. The design must remain small enough to understand directly and structured enough to accommodate another vehicle type without rewriting the rental workflow.

## 3. Objectives

### Functional objectives

- Add cars, bikes, and trucks with unique IDs.
- List available vehicles in ascending base daily-rate order, with ID as a tie-breaker.
- Search all registered vehicles, including those currently rented.
- Rent a vehicle for a positive number of days and issue a bill immediately.
- Apply a 10% discount for durations strictly greater than seven days.
- Return rented vehicles and track revenue from successful rentals.
- Recover from ordinary invalid input and exit cleanly at end of input.

### Software engineering objectives

- Keep responsibilities modular, cohesive, and minimally coupled.
- Use abstraction, encapsulation, inheritance, and runtime polymorphism deliberately.
- Protect identity, monetary values, collection ownership, and state transitions.
- Support maintainability and extension through small, meaningful APIs.
- Preserve identical behavior across development and distribution representations.
- Compile, run, and test using only a standard JDK.

## 4. Technology and prerequisites

Use **JDK 11 or newer**, with `javac` and `java` on the command path. Java 11 is the source/API baseline because the application uses `String.isBlank()` and `String.strip()`. The delivered source was compiled with `--release 11` and executed on Eclipse Temurin OpenJDK 25.0.3; it was not separately executed on a Java 11 runtime.

All imports come from `java.*`. There are no Maven, Gradle, Ant, framework, JUnit, module descriptor, external JAR, or dependency-manifest requirements. The application neither reads nor writes persistent application state.

## 5. Project layout

```text
vehicle-rental/
|-- README.md
|-- src/
|   |-- Vehicle.java
|   |-- Car.java
|   |-- Bike.java
|   |-- Truck.java
|   |-- RentalBill.java
|   |-- RentalCompany.java
|   |-- ConsoleMenu.java
|   |-- Main.java
|   `-- RentalCompanyTest.java
`-- releases/
    `-- VehicleRentalApp.java
```

`src/` is authoritative: one important class per file makes navigation, editing, and responsibility boundaries clear. `RentalCompanyTest.java` is a development-only executable test source. `releases/VehicleRentalApp.java` is optimized for copying, submission, sharing, and direct compilation. It includes all eight application classes but excludes tests. No generated `.class` files are distributed.

### Compile and run the development version

From the project directory:

```sh
cd src
javac *.java
java Main
```

These commands work in a terminal without an IDE. Compilation creates `.class` files beside the sources. Compile the development and release directories separately because they intentionally contain classes with the same names in the unnamed package.

### Running the Standalone Release

From the project directory:

```sh
cd releases
javac VehicleRentalApp.java
java VehicleRentalApp
```

Alternatively, copy **only** `VehicleRentalApp.java` to an empty directory on another computer with a JDK, open a terminal in that directory, and run:

```sh
javac VehicleRentalApp.java
java VehicleRentalApp
```

There are no supporting source files or resources to copy. Compilation produces several class files because the release still contains multiple classes; single-file distribution refers to the source supplied to the recipient.

### Run the dependency-free tests

From `src/`, after compilation:

```sh
java RentalCompanyTest
```

Expected output:

```text
Passed 113 checks.
```

The test class uses ordinary checks that throw `AssertionError` on failure. It does not use the Java `assert` statement, so `-ea` is unnecessary. It checks domain behavior directly and runs the console against scripted `Scanner` input and captured output. Tests do not start from or alter a live operator session.

## 6. Operator interface and sample fleet

| Choice | Operation |
| --- | --- |
| 1 | Display available vehicles, sorted by base daily rate |
| 2 | Enter an ID and rental duration; complete rental and print bill |
| 3 | Enter an ID and return the vehicle |
| 4 | Display cumulative revenue |
| 5 | Choose Car/Bike/Truck and enter ID, model, and base daily rate |
| 6 | Search by ID and display the current availability state |
| 0 | Exit |

All amounts are denominated in **Indian Rupees (INR)**. Prices, fees, discounts, bills, rate entry, and revenue use INR. Existing sample amounts are illustrative INR prices; no currency conversion is performed. The fictional sample fleet is recreated at every startup:

| ID | Type | Fictional model | Base/day | With fees/day |
| --- | --- | --- | ---: | ---: |
| CAR-001 | Car | Aster Compact | 50.00 | 65.00 |
| CAR-002 | Car | Meridian Sedan | 75.00 | 90.00 |
| BIKE-001 | Bike | Swift City 125 | 20.00 | 20.00 |
| BIKE-002 | Bike | Trail Explorer 250 | 30.00 | 30.00 |
| TRUCK-001 | Truck | Atlas Cargo | 120.00 | 150.00 |
| TRUCK-002 | Truck | Titan Hauler | 160.00 | 190.00 |

The CLI uses a compact two-column menu, aligned vehicle tables, and receipt-style bills. Both base rates and fee-inclusive one-day prices are shown. The latter is obtained by calling `calculateRentalCost(1)`, so the UI does not duplicate surcharge formulas. Sorting uses the base rate, as stated in the listing heading. Neither fleet operations nor pricing depend on the sample fleet's size or IDs.

IDs are case-insensitive after normalization: leading/trailing Java whitespace is stripped and letters are uppercased with `Locale.ROOT`. For example, ` car-001 ` and `CAR-001` refer to the same vehicle. Internal spaces are preserved. Unicode normalization and visual-character equivalence are not implemented. Model names are stripped but retain their case.

## 7. Architecture

```text
Presentation: ConsoleMenu
           |
           v
Application/service: RentalCompany
           |
           +----> Domain: Vehicle hierarchy
           `----> Domain: RentalBill

Bootstrap: Main (development) / VehicleRentalApp (release)
           creates the company, sample fleet, Scanner, and menu
```

Dependencies point from the user interface toward business operations and domain objects. `RentalCompany` knows neither `Scanner` nor terminal formatting. `ConsoleMenu` requests rentals and formats the returned result, while `Vehicle` and its subclasses define vehicle behavior. The entry point constructs collaborators and starts the menu.

This direction allows the same business operations to be exercised by tests or a future different interface. Ordinary constructor parameters connect the objects; no container, service locator, framework wiring, repository facade, or additional service interface is needed.

### Class diagram

```text
                          Vehicle <<abstract>>
                          - id: String (final)
                          - model: String (final)
                          - dailyRate: BigDecimal (final)
                          - available: boolean
                          + calculateRentalCost(days)
                          + getType()
                          ~ rent() / returnToFleet()
                               ^
                 extends       |       extends
                 +-------------+-------------+
                 |             |             |
             Car final     Bike final    Truck final
             insurance     base only     surcharge

RentalCompany final ------ owns fleet List<Vehicle> (0..*)
       |                  owns totalRevenue
       `---- creates ----> RentalBill final (immutable values)

ConsoleMenu final -------> RentalCompany
       |-----------------> Vehicle (display queries)
       `-----------------> RentalBill (display queries)

Main / VehicleRentalApp -> RentalCompany, Car, Bike, Truck, ConsoleMenu

~ denotes package-private access; + denotes public access.
```

Bills copy descriptive and monetary values; they do not retain a live `Vehicle` reference. The test class is outside the production architecture.

## 8. Class responsibilities and APIs

### Vehicle

Owns immutable ID, model, and normalized base rate, plus mutable availability. Construction enforces nonblank identity/model and nonnegative rates. Final query methods expose the values needed by the service and presentation. Final, package-private `rent()` and `returnToFleet()` enforce legal transitions and cannot be overridden. `calculateRentalCost(int)` and `getType()` define the varying subtype behavior.

The protected `costWithDailyFee()` helper shares duration validation and daily multiplication. Package-private `normalizeId()`, `requireText()`, and `validateDays()` provide common validation to cooperating classes. These functions stay with the vehicle contract instead of creating a miscellaneous utility layer.

### Car, Bike, and Truck

Each final subclass supplies a display name and overrides rental subtotal calculation. Car owns `DAILY_INSURANCE_FEE` at 15.00; Truck owns `DAILY_HEAVY_LOAD_SURCHARGE` at 30.00. Bike has no fee or extra state. Final concrete classes communicate that extension should define another `Vehicle` subtype instead of changing the meaning of an existing category through further inheritance.

### RentalBill

Owns final snapshots of ID, model, type, days, subtotal, discount, and total. Its package-private constructor validates nonnegative, cent-exact amounts and ensures discount does not exceed subtotal. Total is derived by subtraction rather than independently supplied. Getters expose exactly the fields the console prints. No setter, lookup, input handling, availability mutation, or revenue accounting belongs here.

The company constructs a candidate bill before committing a rental, but exposes it to its caller only after success. A caller cannot observe a successful return value for a rejected rental.

### RentalCompany

Owns `List<Vehicle> fleet`, backed by `ArrayList`, and `BigDecimal totalRevenue`. Its six public operations are `addVehicle`, `findVehicleById`, `listAvailableVehicles`, `rentVehicle`, `returnVehicle`, and `getTotalRevenue`. The private `requireVehicle` converts an absent search into a useful exception for operations requiring a match. The service owns the named discount threshold and percentage, coordinates transactions, and maintains collection ownership.

### ConsoleMenu

Owns references to the company, one `Scanner`, and a `PrintStream`. `run()` is its only public behavior. Private methods handle each menu operation, line parsing, fleet output, and bill formatting. It converts expected argument/state exceptions into concise messages. A creation switch maps an operator's chosen category to a constructor; it is not used for pricing.

### Main / VehicleRentalApp

These alternative entry points have no instance state. Each creates the company, calls a private sample-fleet population method, creates one scanner, and starts `ConsoleMenu`. A private constructor prevents unnecessary entry-point instances. The scanner is closed by the entry point that owns it. The release entry point differs only in its name.

### RentalCompanyTest

Contains development-only scenarios and small check helpers. Its only mutable state is a count of completed checks. It has no production role and is absent from the release.

## 9. Pricing and monetary policy

Let `r` be the normalized base daily rate, `d` rental days, `i = 15.00` daily car insurance, and `s = 30.00` the daily truck surcharge:

```text
Bike:  C_bike  = r * d
Car:   C_car   = (r * d) + (i * d) = (r + i) * d
Truck: C_truck = (r * d) + (s * d) = (r + s) * d
```

Each subtype returns subtotal `S`, including any daily fees but excluding company discounts. In `RentalCompany`:

```text
D = roundToCents(0.10 * S), if d > 7
D = 0.00,                 otherwise
T = S - D
new revenue = previous revenue + T
```

Seven days receive no discount; eight days do. The percentage applies to the entire subtotal, including fees. Centralizing this rule in the service means a discount change does not require edits to three subclasses.

### Exact amounts and rounding

All monetary arithmetic uses `BigDecimal`. Decimal strings construct rates and constants; integer durations use `BigDecimal.valueOf(days)`. Binary floating-point representations cannot exactly represent many ordinary decimal fractions. Decimal arithmetic and an explicit rounding rule make this project's results reproducible. `BigDecimal` itself does not automatically impose a currency scale. See the [Oracle BigDecimal API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html).

The policy is:

1. Reject a negative input rate **before** rounding, including a value such as `-0.001`.
2. Normalize a valid rate to two decimal places using `HALF_UP` at construction.
3. Add cent-exact daily fees and multiply by integer days without intermediate rounding.
4. Round the computed discount to two decimal places using `HALF_UP`.
5. Subtract that rounded discount; add the exact final total to revenue.
6. Validate bill amounts with `UNNECESSARY`: malformed fractional-cent subtotals from a future subtype are rejected rather than silently repaired.

For example, a rate of `1.005` becomes `1.01`. An eight-day bike subtotal is `8.08`, its raw discount `0.808` becomes `0.81`, and its total is `7.27`. Zero base rates are valid; cars and trucks still include their applicable fees. The console formats `BigDecimal` amounts directly with two decimal places and comma grouping under `Locale.ROOT`. Bills, searches, and revenue use the `INR` prefix; the fleet table states INR once above its right-aligned numeric columns. Formatting never converts money to binary floating point.

## 10. Rental workflow and transaction boundary

```text
Operator selects Rent
          |
Console reads ID and whole-number duration
          |
RentalCompany validates positive duration
          |
Normalize ID -> linear lookup -> require known vehicle
          |
Check availability
          |
Vehicle.calculateRentalCost(days) -> dynamic dispatch -> subtotal
          |
Company computes discount
          |
Construct and validate candidate RentalBill -> final total
          |
Compute candidate updated revenue
          |
vehicle.rent() -> assign updated revenue
          |
Return bill -> ConsoleMenu displays result
```

All ordinary validation and monetary computation precede mutation. Bill construction intentionally occurs before availability and revenue changes, even though the bill is delivered afterward. An invalid subtotal, discount, ID, duration, or state cannot leave a partially charged rental in the delivered single-threaded application. The vehicle's own transition guard remains authoritative even though the service checks availability early to avoid unnecessary calculation.

Revenue means the sum of accepted rental charges at rental time. It is not proof of a payment settlement. Returning a vehicle performs lookup and `returnToFleet()` only; it does not recalculate a bill, refund a charge, or increase revenue.

## 11. State transitions, invariants, and exceptions

```text
                  rent()
       AVAILABLE ---------> RENTED
           ^                  |
           |                  |
           +------------------+
              returnToFleet()
```

Renting from RENTED or returning from AVAILABLE throws `IllegalStateException` before mutation.

| Invariant | Enforcement |
| --- | --- |
| ID and model are non-null and nonblank | `Vehicle` constructor and shared text validation |
| Stored identity and base rate cannot be reassigned | Private final fields; no setters |
| Rate is non-null and nonnegative | `Vehicle` constructor, before rounding |
| ID matching is consistent | `Vehicle.normalizeId` used by construction and lookup |
| IDs are unique within a company | `RentalCompany.addVehicle` performs a normalized search |
| Null or already-rented additions are rejected | `RentalCompany.addVehicle` |
| Rental days are positive | `Vehicle.validateDays` in the service and pricing helper |
| Requested rental/return vehicle exists | `RentalCompany.requireVehicle` |
| Unavailable vehicles cannot be rented | Service precheck and `Vehicle.rent` |
| Available vehicles cannot be returned | `Vehicle.returnToFleet` |
| Bills contain valid, consistent money | `RentalBill` constructor |
| Only successful rentals increase revenue | Commit section of `rentVehicle` |
| Returns and failures preserve revenue | Return path has no accounting; checks precede commit |
| Successful rental results are exposed only on success | Bill returned after both state changes |

Unknown IDs and invalid business arguments produce `IllegalArgumentException`; invalid transitions produce `IllegalStateException`. Constructor collaborators such as a null scanner are programming errors and rejected with `NullPointerException` through `Objects.requireNonNull`. A malformed fractional-cent subtotal produces `ArithmeticException` and signals a broken pricing implementation. The console does not hide arbitrary programming defects with `catch (Exception)`.

## 12. Input handling

The interface reads whole lines and parses integers using `Integer.parseInt`; it never mixes `nextInt()` and `nextLine()`. There is exactly one scanner over `System.in`.

| Input/problem | Behavior and responsible layer |
| --- | --- |
| Letters, decimals, or integer overflow where a number is expected | UI explains and repeats the same numeric prompt |
| Unlisted numeric menu option | UI explains and returns to the menu |
| Invalid numeric vehicle category | UI explains and returns to the menu |
| Blank or null ID | Domain/service validation rejects it |
| Unknown ID in search | Empty `Optional`; UI prints no match |
| Unknown ID in rental/return | Service rejects it with a useful message |
| Zero or negative days | Domain/service rejects it; UI returns to menu |
| Duplicate rental or duplicate return | Vehicle/service rejects it; UI remains usable |
| Invalid rate text | UI repeats the rate prompt; plain decimals such as `50.00` are accepted |
| Negative rate | Constructor rejects it; no vehicle is added |
| Duplicate added ID | Service rejects it; original vehicle remains |
| End of input at menu or any prompt | UI closes cleanly; incomplete operation does not commit |

The rate prompt intentionally accepts a decimal point and digits, with an optional sign; it does not accept grouping commas, currency symbols, or exponent notation. Input-format checks improve interaction, while the lower layers independently enforce business rules for every caller.

## 13. Object-oriented design

### Abstraction and inheritance

`Vehicle` is abstract because it represents shared identity, rates, availability, and a pricing contract without declaring one universal pricing formula. All three categories are rentable vehicles and share the same state model, making inheritance appropriate. The base class centralizes common validation and transitions; subclasses supply the charge variation and display name.

### Runtime polymorphism

Consider:

```java
Vehicle vehicle = new Truck("T-1", "Atlas Cargo", new BigDecimal("120.00"));
BigDecimal subtotal = vehicle.calculateRentalCost(3);
```

The reference's compile-time type is `Vehicle`, so that type defines the available method contract. The object's runtime type is `Truck`, so its overriding pricing method is selected when invoked. This dynamic lookup is described in [JLS 15.12.4.4](https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.12.4.4).

Here the result is `450.00`. The service executes that same method call for a bike or car and receives the corresponding subtotal. It has no `instanceof` pricing branches or type-code switches. `getType()` is used for descriptive output, never to choose a pricing rule. A new valid subtype can therefore participate in rentals and receive the company discount through the existing workflow.

### Encapsulation

Private fields alone would be insufficient if arbitrary setters could create invalid state. Vehicle identity and rates remain immutable; availability changes only through guarded behavior. `RentalCompany` returns a new filtered list, so callers may sort or clear the result without editing the master collection. Revenue is an immutable decimal value, and bill snapshots cannot be rewritten after return or a later rental.

Collection protection is intentionally shallow: listed and searched vehicles are the actual domain objects, not immutable copies. Their public queries cannot change availability, but trusted code in the same unnamed package can invoke the package-private transitions. Application callers should use `RentalCompany` for rental use cases and register each vehicle object with one company. This is an application boundary for cooperating code, not an adversarial isolation mechanism or a cross-company ownership system.

## 14. SOLID analysis

| Principle | Application to this implementation | Deliberate boundary |
| --- | --- | --- |
| Single Responsibility (SRP) | Vehicle protects common state; each subtype owns its pricing; company coordinates fleet transactions; bill represents a result; menu owns interaction; entry point bootstraps. A formatting change stays in the menu, while a discount change stays in the service. | No separate classes for each trivial menu action or arithmetic operation. |
| Open/Closed (OCP) | A new `Vehicle` subclass can supply pricing and display type without editing `rentVehicle`, billing arithmetic, or revenue logic. The test suite exercises a custom subtype through the unchanged service. | Bootstrap and the menu's category-construction choices must be extended to make a new category available to users. OCP does not mean the entire application never changes. |
| Liskov Substitution (LSP) | All supplied subtypes accept positive durations, reject non-positive durations, return nonnegative cent-exact subtotals, and leave availability untouched while pricing. Shared final state operations have identical meaning for all vehicles. | Future subtypes must preserve that contract. A subtype rejecting ordinary positive durations or changing state while quoting would violate it. |
| Interface Segregation (ISP) | Collaborators use a compact vehicle abstraction and focused company operations. Bills provide only immutable result data. | No artificial `Rentable`, `Billable`, or one-method service interfaces are added merely to display the principle. |
| Dependency Inversion (DIP) | Company pricing depends on the `Vehicle` abstraction, and collection declarations depend on `List`. Business logic has no dependency on terminal input or output. | The UI calls a concrete `RentalCompany`; the design does not claim universal interface-based inversion. An extra service interface would not solve a present requirement. |

## 15. Other design principles

- **DRY:** ID normalization, duration validation, state transitions, subtype fees, the discount calculation, and revenue accumulation each have a defined owner. UI daily totals call the pricing API. The release is assembled from the same class bodies rather than independently developed.
- **KISS:** Ordinary objects, one list, linear searches, standard exceptions, and a readable switch-based menu are sufficient. There is no registry, factory hierarchy, reflection, annotation processing, or persistence facade.
- **YAGNI:** No accounts, reservations, payment gateways, database, concurrency machinery, or network API is implemented without a requirement.
- **Tell, Don't Ask:** The company tells a vehicle to `rent()` or `returnToFleet()`; the vehicle guards the transition. Queries remain appropriate for display and filtering. This use follows the behavior-and-data focus discussed in [Fowler's Tell Don't Ask](https://martinfowler.com/bliki/TellDontAsk.html).
- **High cohesion:** Related pricing knowledge stays in each subtype, fleet accounting stays in the company, and terminal concerns stay in the menu.
- **Low coupling:** The service communicates through domain methods rather than editing fields or learning about menu numbers. Bill data does not depend on a live UI or vehicle.
- **Information hiding:** The master list and accounting assignments stay private. Display sorting operates on a derived list. No arbitrary setters expose identity or money mutation.
- **Composition of responsibilities:** The menu uses a company and an input/output pair; the company contains vehicles and creates results. Inheritance is limited to the genuine vehicle relationship.

## 16. Complexity and scalability

Let `n` be fleet size and `a` the number of available vehicles. Costs below treat typical bounded-length IDs and monetary values as constant-size inputs.

| Operation | Time | Additional space | Explanation |
| --- | --- | --- | --- |
| Add vehicle | O(n) | O(1) amortized per added reference | Duplicate lookup dominates append; occasional backing-array growth is O(n) |
| Find by ID | O(n) | O(1) | One linear traversal |
| Filter available vehicles | O(n) | O(a) | Build a separate result list |
| Sort available results | O(a log a) | O(a) worst case | Standard comparator-based list sort |
| Rent | O(n) | O(1) | Lookup followed by validation, arithmetic, and one bill |
| Return | O(n) | O(1) | Lookup followed by a guarded transition |
| Read revenue | O(1) | O(1) | Return the immutable accumulated value |

Storage for the fleet is O(n). String comparison and arbitrary-precision arithmetic also depend on operand size; the table describes scaling with fleet size rather than claiming unbounded money arithmetic is constant time. The backing list supports amortized constant-time append and efficient traversal; see [Oracle's ArrayList documentation](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/ArrayList.html).

A substantially larger fleet could replace or supplement the list with `Map<String, Vehicle>` for expected constant-time hash-based lookup. That would require consistent normalized keys, duplicate handling, and, if both structures were retained, synchronized updates between them. Sorting and filtering would still require derived results. For a small in-memory fleet, the current representation offers clearer ownership and fewer consistency obligations. A database-backed fleet would require a different persistence and transaction design instead of merely adding a map.

## 17. Design decisions

| Decision | Rationale and consequence |
| --- | --- |
| Abstract `Vehicle` | Expresses shared rentable state while requiring a pricing implementation; prevents instantiation of an underspecified generic category. |
| Final concrete vehicle classes | Stabilizes each existing category's behavior; new categories extend the deliberate base contract. |
| Immutable ID, model, and rate | Prevents identity drift after uniqueness checks and makes vehicle descriptions stable. Rate changes are outside this version's scope. |
| Behavior-based availability | Guards legal transitions at the state owner, avoiding unrestricted boolean mutation. Package visibility keeps transitions out of the public application API. |
| `BigDecimal` money | Supports exact decimal input and consistent cent-based output with explicit rounding; avoids binary floating-point artifacts. |
| `List<Vehicle>` declaration | Describes the operations the service needs without exposing a specific backing implementation in its field type. |
| `ArrayList` backing | Provides simple append and iteration with a small conceptual footprint; the workload has no linked-list-specific need. |
| Linear ID lookup | Keeps uniqueness, lookup, rental, and return behavior easy to inspect at the required scale. |
| Shared ID normalization | Makes case-insensitive addition, search, rental, and return agree, independent of the machine's default locale. |
| Service-level discount | The duration promotion applies to every category; duplicating it in subclasses would scatter one company policy. |
| Immutable bill snapshot | Preserves the completed rental's result even when the vehicle is returned or rented again. Deriving total prevents contradictory bill fields. |
| Candidate bill before mutation | Ensures bill validation and arithmetic cannot produce an ordinary partial transaction. Both changes occur only after preparation succeeds. |
| Separate console class | Allows input, formatting, and recovery to evolve without affecting domain rules; scripted tests use the same service API. |
| Derived list for display | Sorting and clearing display results cannot reorder or delete vehicles in the company. |
| No framework or build tool | The program requires only compilation and execution. Plain `javac` and `java` provide transparent, portable builds, a small footprint, and straightforward academic review. |
| Split development sources | Makes class ownership and navigation explicit, improving maintenance and study. |
| Flattened release source | Makes copying and submission convenient while preserving the class architecture and eliminating external-resource requirements. |
| In-memory accounting | Meets the requested single-process scope; restart deliberately resets fleet state and revenue. |

These choices keep the design proportional to its problem. Adding a repository, factory per subtype, dependency container, or pricing strategy hierarchy would introduce names and indirection without solving a current requirement.

## 18. Release design and source consistency

A conventional Java compilation unit may declare multiple top-level classes. With `javac`'s file-based compilation, the public class is declared in its matching source filename; other top-level classes in the file can be package-private. This arrangement is described in [JLS 7.6](https://docs.oracle.com/javase/specs/jls/se25/html/jls-7.html#jls-7.6).

The release contains `public final class VehicleRentalApp` followed by package-private `Vehicle`, `Car`, `Bike`, `Truck`, `RentalBill`, `RentalCompany`, and `ConsoleMenu`. There is no package declaration. Multiple classes retain inheritance, polymorphism, encapsulation, and separate responsibilities despite sharing one physical file.

### Maintenance procedure

Edit `src/` first. Assemble the release by collecting unique imports, renaming only the `Main` class and its constructor to `VehicleRentalApp`, removing the top-level `public` modifier from the other seven classes, and concatenating those unchanged bodies. Do not copy the test class. Then compile and test each representation separately.

For reproducibility, the following optional PowerShell snippet performs that assembly from the project root. It uses the shell only to combine source text; the application and its compilation still require only the JDK. It overwrites the derived release file:

```powershell
$classNames = @('Main', 'Vehicle', 'Car', 'Bike', 'Truck',
    'RentalBill', 'RentalCompany', 'ConsoleMenu')
$imports = @()
$bodies = @()
foreach ($className in $classNames) {
    $source = [IO.File]::ReadAllText((Join-Path $PWD "src/$className.java"))
    $imports += [regex]::Matches($source, '(?m)^import .+;$') |
        ForEach-Object { $_.Value }
    $body = [regex]::Replace($source, '(?m)^import .+;\r?\n', '').Trim()
    if ($className -eq 'Main') {
        $body = $body.Replace('Main', 'VehicleRentalApp')
    } else {
        $body = [regex]::Replace($body, '^public ', '')
    }
    $bodies += $body
}
$release = "// Generated from src/: edit the modular source first.`n" +
    (($imports | Sort-Object -Unique) -join "`n") + "`n`n" +
    ($bodies -join "`n`n") + "`n"
[IO.File]::WriteAllText((Join-Path $PWD 'releases/VehicleRentalApp.java'), $release)
```

This is a narrow assembly procedure for these named classes, not a general Java parser. If imports, top-level declarations, or the entry point are reorganized, review the transformation. Verification of this delivery confirmed that the assembled file matches the modular bodies exactly apart from these declared transformations.

## 19. Verification results

Verification performed on 15 September 2026 with Temurin OpenJDK 25.0.3. The development sources compiled for the Java 11 API/language baseline. Both representations passed the same **113 dependency-free checks**. Four complete console-input scenarios produced identical output across development and release entry points.

### Required cases

| Case | Expected and observed result | Status |
| --- | --- | --- |
| Bike at 20.00 for 3 days | Subtotal/total 60.00; discount 0.00 | PASS |
| Car at 50.00 for 3 days | 150.00 base + 45.00 insurance = 195.00; no discount | PASS |
| Truck at 120.00 for 3 days | 360.00 base + 90.00 surcharge = 450.00; no discount | PASS |
| Bike at 20.00 for exactly 7 days | Subtotal/total 140.00; no discount | PASS |
| Bike at 20.00 for 8 days | Subtotal 160.00; discount 16.00; total 144.00 | PASS |
| Unknown rental ID | Rejected; availability and revenue unchanged | PASS |
| Zero or negative duration | Rejected by service and each supplied subtype | PASS |
| Successful rental | Bill returned; vehicle unavailable; total added to revenue | PASS |
| Second rental without return | Rejected; vehicle remains rented; revenue unchanged | PASS |
| Return after rental | Vehicle available and listed again; revenue unchanged | PASS |
| Second return | Rejected; availability and revenue unchanged | PASS |
| Combined Car 3 days + Bike 8 days + Truck 3 days | Revenue 195.00 + 144.00 + 450.00 = 789.00 | PASS |
| Invalid menu and duration text | Prompt recovers; application remains usable | PASS |
| Unknown search ID | Clear no-match message | PASS |
| Mixed-case and padded IDs | Same vehicle found; duplicates rejected | PASS |
| List result sorted or cleared | Master fleet order and membership unchanged | PASS |
| Rounded rate 1.005; Bike 8 days | Rate 1.01; subtotal 8.08; discount 0.81; total 7.27 | PASS |
| Zero-rate bike | Valid 0.00 rental | PASS |
| Bill after return and later rental | Original snapshot unchanged | PASS |
| Future subtype through base reference | Its custom price used; common discount applied | PASS |
| Malformed subtype subtotal | Bill validation rejects before revenue/availability mutation | PASS |
| End of input during an incomplete operation | Clean exit; no partial rental | PASS |

The full application was run through scripted terminal sessions and the resulting output was inspected. This includes fleet display, a car rental, duplicate rejection, return, a discounted bike rental, a truck rental, revenue, invalid menu input, invalid durations, and an unknown search. The example below is an actual verified output sequence with the entered lines restored beside the prompts for readability.

### Standalone independence

Only `VehicleRentalApp.java` was copied to an otherwise empty directory. It compiled with `javac VehicleRentalApp.java` and ran with `java VehicleRentalApp`. Every generated `.class` file was then deleted and the same compilation and run succeeded again. No modular source or precompiled dependency was present for either clean build. After independence was established, the optional test source was separately compiled with the release classes; all 113 checks passed there too.

### Manual design review

Every production class was reviewed for its responsibility, state, operations, API, invariants, dependencies, duplicated knowledge, and whether removing it would improve the design. The eight-class architecture was retained: merging bills, pricing, or terminal code into the company would mix reasons for change. The review also removed duplicated fee/promotion values from console startup text; displayed daily prices come from polymorphic pricing. No factories, repositories, service interfaces, or framework layers were justified. Verification artifacts and generated classes are kept out of the distributed project.

## 20. Complete example session

Run `java Main` after compiling `src/`. The same interaction applies to `java VehicleRentalApp` in the release directory.

```text

  VEHICLE RENTAL MANAGEMENT SYSTEM
  ------------------------------------------------------------------------
  All amounts in Indian Rupees (INR).

  MENU
  1. View available vehicles      4. View total revenue
  2. Rent vehicle                 5. Add vehicle
  3. Return vehicle               6. Search by vehicle ID
  0. Exit

  Choose an option: 1

  AVAILABLE VEHICLES
  ------------------------------------------------------------------------
  Daily rates in INR, sorted by base rate (lowest first).

  ID         TYPE   MODEL               BASE / DAY  WITH FEES
  ---------  -----  ------------------  ----------  ---------
  BIKE-001   Bike   Swift City 125           20.00      20.00
  BIKE-002   Bike   Trail Explorer 250       30.00      30.00
  CAR-001    Car    Aster Compact            50.00      65.00
  CAR-002    Car    Meridian Sedan           75.00      90.00
  TRUCK-001  Truck  Atlas Cargo             120.00     150.00
  TRUCK-002  Truck  Titan Hauler            160.00     190.00

  6 vehicle(s) available. With fees excludes rental discounts.

  MENU
  1. View available vehicles      4. View total revenue
  2. Rent vehicle                 5. Add vehicle
  3. Return vehicle               6. Search by vehicle ID
  0. Exit

  Choose an option: 2

  RENT VEHICLE
  ------------------------------------------------------------------------
  Vehicle ID: CAR-001
  Rental days: 3

  RENTAL BILL
  ------------------------------------------------------------------------
  Vehicle ID:            CAR-001
  Model:                 Aster Compact
  Type:                  Car
  Rental days:           3

  Subtotal (with fees):             INR 195.00
  Discount:                           INR 0.00
  --------------------------------------------
  Total:                            INR 195.00

  Rental successful. Vehicle is now rented.

  MENU
  1. View available vehicles      4. View total revenue
  2. Rent vehicle                 5. Add vehicle
  3. Return vehicle               6. Search by vehicle ID
  0. Exit

  Choose an option: 1

  AVAILABLE VEHICLES
  ------------------------------------------------------------------------
  Daily rates in INR, sorted by base rate (lowest first).

  ID         TYPE   MODEL               BASE / DAY  WITH FEES
  ---------  -----  ------------------  ----------  ---------
  BIKE-001   Bike   Swift City 125           20.00      20.00
  BIKE-002   Bike   Trail Explorer 250       30.00      30.00
  CAR-002    Car    Meridian Sedan           75.00      90.00
  TRUCK-001  Truck  Atlas Cargo             120.00     150.00
  TRUCK-002  Truck  Titan Hauler            160.00     190.00

  5 vehicle(s) available. With fees excludes rental discounts.

  MENU
  1. View available vehicles      4. View total revenue
  2. Rent vehicle                 5. Add vehicle
  3. Return vehicle               6. Search by vehicle ID
  0. Exit

  Choose an option: 2

  RENT VEHICLE
  ------------------------------------------------------------------------
  Vehicle ID: CAR-001
  Rental days: 3

  Cannot complete operation: Vehicle CAR-001 is already rented.

  MENU
  1. View available vehicles      4. View total revenue
  2. Rent vehicle                 5. Add vehicle
  3. Return vehicle               6. Search by vehicle ID
  0. Exit

  Choose an option: 3

  RETURN VEHICLE
  ------------------------------------------------------------------------
  Vehicle ID: CAR-001

  Vehicle returned successfully.

  MENU
  1. View available vehicles      4. View total revenue
  2. Rent vehicle                 5. Add vehicle
  3. Return vehicle               6. Search by vehicle ID
  0. Exit

  Choose an option: 1

  AVAILABLE VEHICLES
  ------------------------------------------------------------------------
  Daily rates in INR, sorted by base rate (lowest first).

  ID         TYPE   MODEL               BASE / DAY  WITH FEES
  ---------  -----  ------------------  ----------  ---------
  BIKE-001   Bike   Swift City 125           20.00      20.00
  BIKE-002   Bike   Trail Explorer 250       30.00      30.00
  CAR-001    Car    Aster Compact            50.00      65.00
  CAR-002    Car    Meridian Sedan           75.00      90.00
  TRUCK-001  Truck  Atlas Cargo             120.00     150.00
  TRUCK-002  Truck  Titan Hauler            160.00     190.00

  6 vehicle(s) available. With fees excludes rental discounts.

  MENU
  1. View available vehicles      4. View total revenue
  2. Rent vehicle                 5. Add vehicle
  3. Return vehicle               6. Search by vehicle ID
  0. Exit

  Choose an option: 4

  REVENUE
  ------------------------------------------------------------------------
  Total revenue:         INR 195.00

  MENU
  1. View available vehicles      4. View total revenue
  2. Rent vehicle                 5. Add vehicle
  3. Return vehicle               6. Search by vehicle ID
  0. Exit

  Choose an option: 0

  Goodbye.
```

## 21. Limitations and future extensions

The system is **in-memory, single-process, console-based, single-user, and non-persistent**. Restart discards added vehicles, availability changes, and revenue. There are no customer accounts, real payments, rental dates/timestamps, persistent rental history, database storage, remote services, or authentication. Bills represent accepted rental charges, not legal invoices or payment receipts. These are intentional scope boundaries.

The classes assume one cooperating application flow. They are not thread-safe and do not provide multi-process transaction isolation or cross-company vehicle ownership. The application does not retain bill history after display, track overdue rentals, or calculate refunds. Very large inputs remain subject to normal integer, decimal-representation, memory, and terminal limits.

Possible extensions, without implementing them prematurely:

| Extension | Natural point of change |
| --- | --- |
| Additional vehicle categories | New `Vehicle` subclass, plus bootstrap/menu construction choices |
| Customer records and rental history | Service-level relationships and stored bill records |
| Persistent database | Explicit storage boundary and durable transaction handling |
| Reservation dates | New availability and overlap rules; positive duration alone is insufficient |
| Payment processing | Separate payment workflow and settlement state; redefine revenue timing deliberately |
| GUI or REST API | Replace the presentation adapter while reusing business operations |
| Configurable pricing policies | Extract policies when actual variability justifies the abstraction |

Separation of responsibilities helps localize these changes, but additions such as persistence and concurrent bookings would require new correctness guarantees, not just extra menu options.

## 22. References and style

Technical references consulted for this implementation and report:

1. [Oracle Java SE 25: BigDecimal](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html) — exact decimal representation, arithmetic, scale, and rounding APIs.
2. [Oracle Java SE 25: ArrayList](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/ArrayList.html) — collection behavior and operation costs.
3. [Java Language Specification, Section 7.6](https://docs.oracle.com/javase/specs/jls/se25/html/jls-7.html#jls-7.6) — top-level declarations and source-file organization.
4. [Java Language Specification, Section 15.12.4.4](https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.12.4.4) — runtime method selection.
5. [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) — naming, explicit imports, braces, and source organization. This project follows the requested **four-space indentation**, rather than Google's two-space block indentation, and intentionally makes the release a multiple-class source file.
6. [Martin Fowler: Tell Don't Ask](https://martinfowler.com/bliki/TellDontAsk.html) — placing behavior with its associated data without eliminating useful queries.

The source uses conventional class/method/constant naming, explicit imports, braces, minimal comments, and no generated accessor/setter boilerplate. Detailed explanation belongs in this README so that the Java code remains focused on executable responsibilities.
