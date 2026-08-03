# گیت صحت هسته مالی — 4.5.1 و 5.0.0

**Priority:** P0  
**Planned at:** `51c80fb9` — 2026-07-29  
**Branch:** `codex/001-financial-core-release-gate`  
**نوع سند:** Epic Plan؛ هر FCR باید branch/Task Card مستقل و review ترتیبی داشته باشد.  
**هدف:** هیچ عملیات ثبت/ویرایش/حذف/انتقال یا تسویه‌ای مانده و تاریخچه مالی را ناسازگار نکند.

## چرا این کار اولین اولویت است

### انتقال

- `core/domain/src/commonMain/kotlin/com/kazemieh/domain/util/balanceImpact.kt:11-14`
  مبدأ را به اندازه `amount + amountTransfer` بدهکار و مقصد را فقط به اندازه `amount` بستانکار می‌کند.
- `feature-share/transaction/src/commonMain/kotlin/com/kazemieh/transaction/ui/add/AddTransactionViewModel.kt:318-327`
  اگر مقدار دوم خالی باشد آن را برابر اصل مبلغ ذخیره می‌کند.
- `core/designsystem/src/commonMain/composeResources/values/strings.xml:30`
  همان مقدار دوم را «کارمزد» معرفی می‌کند.
- اعتبارسنجی فعلی مبدأ و مقصد یکسان را رد نمی‌کند؛ در `buildMap` اثر دوم می‌تواند اثر اول را overwrite کند.

نتیجه: انتقال ۱۰۰ می‌تواند مبدأ را ۲۰۰ کم و مقصد را ۱۰۰ زیاد کند؛ انتقال به همان حساب حتی ممکن است موجودی را زیاد کند.

### ویرایش تراکنش

- `core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/UpdateTransactionUseCase.kt:16-27`
  delta قدیم/جدید را محاسبه می‌کند.
- `core/database/src/commonMain/kotlin/com/kazemieh/database/datasource/TransactionLocalDataSourceImpl.kt:682-725`
  delta را می‌گیرد ولی اعمال نمی‌کند.
- همان implementation در مسیر Add (`:674-676`) و Delete (`:736-738`) delta را اعمال می‌کند.

نتیجه: تغییر مبلغ، نوع، مبدأ، مقصد یا کارمزد رکورد را عوض می‌کند اما مانده حساب را stale نگه می‌دارد.

### ریسک‌های همراه

- مبلغ‌ها و مانده‌ها `Int` هستند؛ برای ریال/تومان سقف حدود ۲.۱ میلیارد عملی نیست.
- ثبت transaction و تغییر state قسط/بدهی/هزینه ثابت transaction اتمیک واحد ندارند.
- بودجه‌ها deleted transaction را می‌شمارند و periodهای مختلف را گاهی ماه جاری فرض می‌کنند.
- هیچ تست Kotlin در repo وجود ندارد.

## تصمیم دامنه‌ای لازم

مدل نهایی انتقال باید سه مفهوم مستقل داشته باشد:

```kotlin
principalAmount: Money
destinationAmount: Money // برای انتقال چندارزی؛ در همان ارز برابر principal
feeAmount: Money         // پیش‌فرض صفر
```

اثر همان‌ارز:

```text
source      -= principal + fee
destination += principal
reports classify fee once from the transfer record
```

در مدل فعلی، fee فقط یک بار داخل impact انتقال از مبدأ کم می‌شود. برای گزارش، همان فیلد به‌عنوان
هزینه طبقه‌بندی می‌شود و Expense/Transaction دوم با balance impact ساخته نمی‌شود. اگر بعداً ledger
double-entry اضافه شد، postingهای آن جای این فرمول را می‌گیرند و دوباره روی Source اعمال نمی‌شوند.

تا زمان ورود انتقال چندارزی:

- `principalAmount > 0`؛
- `destinationAmount == principalAmount`؛
- ارز fee برابر ارز مبدأ؛
- حساب‌های با ارز متفاوت برای انتقال رد شوند؛
- `feeAmount` پیش‌فرض صفر و غیرمنفی باشد.

**Stop condition:** داده تاریخی `amountTransfer` مبهم است. عامل حق ندارد همه ردیف‌های قبلی را خودکار fee یا destination amount فرض کند. ابتدا باید نمونه داده واقعی، نسخه ایجادکننده رکورد و الگوی مقادیر طبقه‌بندی شود.

## محدوده

### In scope

- منطق مرکزی اثر مالی؛
- Add/Edit/Delete Transaction؛
- انتقال و کارمزد؛
- reconciliation غیرمخرب؛
- قسط، بدهی، چک و هزینه ثابت از نظر atomicity/idempotency؛
- بودجه و حذف نرم؛
- تست‌های domain/database/ViewModel؛
- migration لازم برای semantics جدید.

### Out of scope

- UI بازطراحی کامل؛
- Sync چنددستگاهی؛
- خرید دارایی؛
- تبدیل همه مدل‌های پروژه به Money در همان PR اضطراری.

## Task Cardها

### FCR-001 — Characterization suite

**فایل‌ها/ماژول‌ها:**

- `core/domain/src/commonTest/`
- `core/database/src/commonTest/` یا `jvmTest/`
- fixture builders جدید زیر test source set

**مراحل:**

1. Source، Transaction و relation fixture بساز.
2. invariantهای زیر را برای Income/Expense/Transfer تست کن:
   - مجموع deltaها با semantics تعریف‌شده برابر است؛
   - Add سپس Delete مانده را دقیقاً به حالت اول برمی‌گرداند؛
   - Edit A→B معادل rollback(A)+apply(B) است؛
   - source=destination خطای دامنه‌ای می‌دهد؛
   - fee صفر پیش‌فرض است.
3. matrix تغییرات را پوشش بده: amount، type، source، destination، fee، tag/person.
4. تست overflow مرز `Int.MAX_VALUE` را قبل از migration ثبت کن تا شکست فعلی مستند شود.
5. تست concurrent edit، edit-vs-delete و delete تکراری اضافه کن.

**Acceptance:**

- تستی وجود داشته باشد که روی رفتار فعلی double-debit و stale balance شکست بخورد.
- test name سناریوی مالی را بیان کند، نه جزئیات implementation.

### FCR-002 — مدل انتقال و Migration

**فایل‌های اصلی:**

- `core/common/.../model/Transaction.kt`
- `core/domain/.../util/balanceImpact.kt`
- `feature-share/transaction/.../AddTransactionViewModel.kt`
- SQLDelight transaction schema و migration جدید
- `core/database/.../mapper/Mappers.kt`
- row/detail/report composableهای انتقال

**مراحل:**

1. semantic decision record کوتاه بنویس.
2. فیلدهای صریح اضافه کن؛ legacy field را در یک نسخه transitional فقط برای import نگه دار.
3. اعتبارسنجی مبدأ ≠ مقصد و fee ≥ 0.
4. labels، detail، copy/share و report را از یک formatter/domain model تغذیه کن.
5. migration داده قدیمی:
   - ردیف‌های واضح را تبدیل کن؛
   - `financialSemanticsVersion` و در مدل هدف posting/impact اعمال‌شده را ذخیره کن؛
   - ردیف‌های مبهم را `needsReview` و تا review از Edit/Delete دارای اثر مانده read-only کن؛
   - هیچ مانده‌ای را بدون audit خودکار overwrite نکن.
   - نبود metadata نسخه‌سازنده را فرض کن؛ inference از app version فقط وقتی evidence واقعی وجود دارد.
6. telemetry فقط count/status ناشناسِ migration؛ مبلغ یا شناسه حساب ممنوع.

**Acceptance:**

- انتقال ۱۰۰ بدون fee فقط ۱۰۰ از مبدأ و ۱۰۰ به مقصد اعمال کند.
- انتقال ۱۰۰ با fee ۵ دقیقاً ۱۰۵ از مبدأ و ۱۰۰ به مقصد اعمال کند.
- fee در report یک بار دیده شود و transaction مالی دوم مانده را کم نکند.
- principal مثبت، fee غیرمنفی، destinationAmount برابر principal در همان ارز و انتقال چندارزی فعلاً رد شود.
- source=destination ذخیره نشود.
- UI همیشه اصل مبلغ و fee را با label صحیح نشان دهد.
- Edit/Delete legacy مبهم بدون review نتواند impact جدید را برای rollback رکورد قدیمی حدس بزند.

### FCR-003 — Update delta اتمیک

**فایل اصلی:**  
`core/database/src/commonMain/kotlin/com/kazemieh/database/datasource/TransactionLocalDataSourceImpl.kt`

**مراحل:**

1. current transaction row و posting/semantics version داخل همان `database.transaction` خوانده شود؛ `oldTransaction` ارسالی caller منبع حقیقت نباشد.
2. revision/updatedAt مقایسه شود؛ stale edit با conflict typed رد شود.
3. rollback-old/apply-new از row پایدارشده داخل DB محاسبه شود.
4. transaction، relationها و balance changes در همان transaction اعمال شوند.
5. وجود Source و affected row count بررسی شود؛ missing source کل عملیات را rollback کند.
6. checked arithmetic استفاده شود؛ overflow کل عملیات را rollback کند.
7. پس از commit، observerها یک state سازگار ببینند.

**Acceptance:**

- failure وسط operation نه transaction و نه مانده را تغییر دهد.
- matrix تست FCR-001 سبز شود.
- دو Edit هم‌زمان هر دو از revision قدیمی commit نشوند.

### FCR-004 — Balance Audit و داده‌های drift کرده

**طرح:**

- `BalanceAuditUseCase` در 4.5.1 فقط اختلاف/قابلیت محاسبه را گزارش کند.
- مدل فعلی opening balance تاریخی ندارد؛ بیشتر حساب‌های legacy احتمالاً `Indeterminate` هستند.
- برای آینده یک checkpoint شامل user-confirmed balance، ledger high-water mark و revision بساز.
- expected balance فقط از checkpoint معتبر + postingهای بعد از آن محاسبه شود.
- در نسخه اول هیچ auto-fix silent وجود نداشته باشد.
- repair پس از Backup/Restore v2: safety snapshot → preview نسخه‌دار → تأیید → recheck revision → adjustment entry مستقل.
- adjustment نوعی مستقل از income/expense/budget داشته باشد تا گزارش‌ها را جعلی تغییر ندهد.

**Acceptance:**

- audit idempotent و read-only باشد.
- repair فقط برای `Determinate`، یک ledger adjustment صریح با metadata/version ایجاد کند.
- امکان export گزارش قبل از repair وجود داشته باشد.
- تراکنش جدید بین preview و confirm باعث repair روی revision قدیمی نشود.

### FCR-005 — تعهدات اتمیک و idempotent

**فایل‌های اصلی:**

- `core/domain/.../InstallmentUseCases.kt`
- `core/domain/.../DebtUseCases.kt`
- `core/domain/.../FixedExpenseUseCases.kt`
- `feature-share/fixed-expense/.../FixedExpenseWorker.kt`
- database contracts/queries مربوط

**مراحل:**

1. قبل از Sync یک `originStableId` محلیِ globally unique برای obligationها اضافه و شناسه occurrence بساز:
   `(originType, originStableId, dueCivilDateOrInstant)`.
2. insert transaction و advance/settle state را یک database command و یک transaction کن.
3. retry همان occurrence نباید transaction دوم ایجاد کند.
4. زمان تراکنش خودکار برابر due time باشد، نه زمان اجرای Worker.
5. حساب ماه/سال جلالی با end-of-month policy صریح تست شود.
6. update/cancel reminder هم‌زمان با state entity reconcile شود.

**Acceptance:**

- crash/retry/simultaneous tap duplicate ایجاد نکند.
- partial payment بدهی تاریخچه و مانده درست داشته باشد.
- check statusهای پاس/برگشت/ابطال reminder فعال نداشته باشند.

### FCR-006 — بودجه period-aware

**فایل‌های اصلی:**

- `core/database/.../BudgetLocalDataSourceImpl.kt`
- `core/database/.../Transaction.sq`
- domain budget use cases

**مراحل:**

1. برای هر Budget بازه `[start, end)` از period و `startAt` خودش محاسبه شود.
2. query با budget/filter identity باشد، نه «اولین بودجه category».
3. soft-deleted transaction حذف شود.
4. coexistence یا uniqueness بودجه‌های هم‌پوشان تصمیم‌گیری و validate شود.
5. مرز روز/هفته/ماه/سال جلالی و تغییر timezone تست شود.

## مهاجرت Money به 64-bit

در 4.5.1 حداقل checked arithmetic، input limit صریح و rollback روی overflow اجباری است. قرارداد کامل
زیر قبل از 5.0.0 و پیش از ادعای «هسته مالی معتبر» اجرا شود:

- `Money(minorUnits: Long, currency: CurrencyCode)` در common؛
- هیچ `.toInt()` روی amount/balance؛
- عملیات با overflow check؛
- quantity طلا/ارز با decimal deterministic، نه `Double`؛
- serializer و JS precision با string یا safe representation؛
- schema migration و backup protocol version؛
- `Source.balance` در نهایت cache قابل rebuild باشد.

## Feature Gate

- `financial_core` قابل خاموش‌کردن نیست.
- `transfer_fee_v2` برای rollout و migration UI gate دارد، اما schema migration همیشه اجرا می‌شود.
- `balance_audit` ابتدا Preview و فقط برای cohort داخلی.
- `automatic_obligation_posting` تا سبز شدن idempotency test برای هر platform Disabled باشد.

## Verification

فرمان‌های نهایی باید با wrapper پروژه اجرا شوند:

```powershell
.\gradlew.bat :core:domain:jvmTest :core:database:jvmTest --no-daemon --console=plain
.\gradlew.bat :app:compileDirectDebugSources --no-daemon --console=plain
.\gradlew.bat check --no-daemon --console=plain
git status --short
```

نام دقیق test taskها پس از افزودن source setها با `:core:domain:tasks` و
`:core:database:tasks` تأیید شود. در baseline ممیزی، `:app:compileDirectDebugSources`
task معتبری بود اما اجرای کامل در configuration timeout شد؛ سبز بودن آن هنوز اثبات نشده است.

## Release Gate

### 4.5.1

- containment مرحله صفر plan 006؛
- `FCR-001`، `FCR-002` و `FCR-003`؛
- `FCR-004` فقط audit preview، بدون repair؛
- `DBS-001..004` از plan 002؛
- checked arithmetic و input guard؛
- Sync/AI cloud production برابر Disabled؛
- recovery notes و safety copy دیتابیس.

### 5.0.0

- Money 64-bit، Backup/Restore v2 و امکان recovery قبل از repair؛
- repair فقط برای داده Determinate؛
- FCR-005 و FCR-006؛
- migration fixtures تمام نسخه‌ها؛
- صفر P0/P1 صحت مالی؛
- manual QA اقساط/بدهی/چک روی backup کپی، نه دیتای اصلی.
