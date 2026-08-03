# Backlog کیفیت معماری و نگهداشت

**Priority:** P1/P2 پس از گیت صحت مالی  
**Planned at:** `51c80fb9` — 2026-07-29  
**نوع سند:** Epic Backlog؛ هر AQ به Task Card کوچک تبدیل شود.  
**قاعده:** refactor بدون characterization test و outcome قابل اندازه‌گیری ممنوع.

## چیزی که نباید انجام شود

- کل graph ماژول‌ها فقط برای «Clean Architecture خالص‌تر» بازنویسی نشود؛ cycle ماژولی اثبات نشد.
- فایل بزرگ صرفاً بر اساس تعداد خط تکه‌تکه نشود؛ split باید ownership و test seam بسازد.
- dependencyها فقط به‌دلیل alpha/RC بودن یکجا upgrade نشوند؛ هر upgrade جدا با matrix build.
- همه literalهای `dp` کورکورانه با `LocalSpacing` جایگزین نشوند.

## AQ-001 — Quality gate و CI

**Evidence:**

- ۰ فایل تست Kotlin؛
- ۰ workflow CI؛
- ۰ config برای detekt/ktlint/spotless؛
- task تجمیعی verify وجود ندارد.

**خروجی:**

- root `verify` task؛
- Presubmit: common/JVM tests، migration verification، Android lint/compile، static analysis؛
- Nightly: Desktop/Web package، migration matrix و UI smoke؛
- macOS: iOS simulator compile/test؛
- cache و build-duration dashboard.

**Acceptance:** merge بدون عبور از Presubmit ممکن نباشد؛ release manifest فقط از commit سبز ساخته شود.

## AQ-002 — MVI/Effect/Snackbar واحد

**Evidence:**

- global Snackbar host در `FinTrackHost.kt` موجود است.
- Fixed Expense و چند سطح دیگر host محلی یا singleton UI call دارند.
- بعضی ViewModelها Effect می‌فرستند ولی Screen collector ندارد؛ Channel می‌تواند معلق یا پیام گم کند.
- تعداد قابل توجهی `collectAsState` به‌جای lifecycle-aware collection دیده شد.

**طرح:**

```text
Screen Route
  collect state with lifecycle
  CollectEffects(effect)
    -> navigation
    -> global message bus (UiText)
```

- ViewModel فقط Effect typed؛ هیچ singleton UI.
- host محلی فقط برای scope امنیتی مستقل مثل Lock Gate، با دلیل مستند.
- template/lint پروژه قرارداد State/Intent/Effect را enforce کند.

**Acceptance:** هر producer یک consumer تست‌شده؛ recreate/background پیام یا coroutine leak ایجاد نکند.

## AQ-003 — Pagination واقعی

**Evidence:**

- Transaction و Report در load-more، `limit` را بزرگ و `offset` را صفر نگه می‌دارند.
- هر صفحه کل داده قبلی را دوباره query/map می‌کند؛ هزینه تجمعی O(n²).

**طرح:**

- keyset pagination بر اساس `(timeStamp, id)`؛
- first page reactive؛ older pages append snapshot؛
- filter change pager را atomically reset کند؛
- insert/delete هم‌زمان duplicate/missing row ندهد.

**Acceptance:**

- برای ۱۰هزار تراکنش زمان/حافظه در هر append تقریباً مستقل از تعداد صفحات قبلی بماند.
- تست duplicate، delete و filter change.

## AQ-004 — Flow/subscription lifecycle

**Evidence:**

- Init تکراری در Dashboard/Transaction می‌تواند collector تازه بسازد.
- چند widget یک ViewModel را init می‌کنند.
- Asset history در هر load collector دائمی جدید ایجاد می‌کند.
- Search خالی queryهایی می‌سازد که همه ردیف‌ها را match می‌کنند و UI آن‌ها را پنهان می‌کند.

**طرح:**

- stream ثابت در `init` + `stateIn`؛
- job guard برای intent idempotency؛
- `flatMapLatest` برای انتخاب asset/filter؛
- VM در route root inject و state/action به child داده شود؛
- query خالی پیش از repository short-circuit.

**Acceptance:** instrumentation تعداد observer/SQL query با بازکردن تکراری صفحه رشد نکند.

## AQ-005 — مرز Transaction God contract

**Evidence:**

- `TransactionRepository` مسئول Transaction و CRUD Category/Tag/Person/Source/Search/Statistics است.
- LocalDataSource backup/sync/physical delete را نیز در همان قرارداد دارد.
- implementation حدود ۸۰۱ خط و چند query family دارد.
- گروه UseCase مانند service locator بزرگ تزریق می‌شود.

**ترتیب امن split:**

1. characterization tests؛
2. تعریف `TransactionStore`, `CategoryStore`, `SourceStore`, `TagStore`,
   `PersonStore`, `SearchHistoryStore`؛
3. `TransactionMutationCoordinator` تنها مالک atomic balance+relations؛
4. adapter موقت برای call siteهای قدیمی؛
5. migration تدریجی featureها؛
6. حذف adapter.

**Stop condition:** atomicity بین transaction و source balance نباید میان repositoryهای جدا از بین برود.

## AQ-006 — کاهش coupling concrete feature

**Evidence:**

- transaction و budget به چند feature picker concrete وابسته‌اند.
- Dashboard به تعداد زیادی feature-share dependency مستقیم دارد.

**طرح:**

- model/callback انتخاب entity در یک API خنثی یا `entity-selection` کوچک؛
- composition root مالک sheet/route concrete؛
- stateless selection content؛
- split `api/impl` فقط برای edgeهای پرتکرار، نه تمام ۴۳ ماژول.

**Acceptance:** transaction form برای Compact و Expanded بدون import کردن چند route implementation قابل compose باشد.

## AQ-007 — شکستن فایل‌های پرریسک

اولویت بر اساس size + churn + responsibility:

- `AddTransactionBottomSheet.kt`
- `GoalScreen.kt`
- `ShoppingListScreen.kt`
- `AIAdvisorScreen.kt`
- `ProfileScreen.kt`
- `DashboardViewModel.kt`
- `TransactionLocalDataSourceImpl.kt`

Pattern:

```text
FeatureRoute.kt       // VM/effect/navigation
FeatureScreen.kt      // root Box + adaptive composition
FeatureContent.kt     // stateless
FeatureSections.kt    // focused components
FeatureInteractor.kt  // orchestration قابل تست
```

هر tranche حداکثر ۱–۳ ماژول و بدون تغییر رفتار، مگر Task جدا داشته باشد.

## AQ-008 — SQL relation mapping

`GROUP_CONCAT`های موازی برای id/name/color/description ترتیب/cardinality تضمین‌شده مشترک ندارند و comma/null داده را خراب می‌کند.

- relation rows typed با batch query؛
- groupBy در Kotlin؛
- جلوگیری از N+1؛
- تست نام تکراری، null، comma و tag×person.

این Task با DBS-007 در plan 002 هماهنگ و فقط یک بار اجرا شود.

## AQ-009 — Build logic و dependency hygiene

- convention pluginها؛
- framework iOS فقط umbrella؛
- dependency تکراری تاریخ در transaction حذف؛
- Jackson constraint فقط scope لازم؛
- version catalog + compatibility matrix؛
- قبل/بعد configuration/build benchmark؛
- warning Gradle 10 و unused source setها به backlog نسخه‌دار.

## AQ-010 — Design system debt

- semantic tokens مطابق plan 004؛
- component catalog و interactive states؛
- accessibility semantics؛
- string hard-code به resources؛
- theme/accent/glass موجود inventory و harden؛
- snapshot tests برای تم/RTL/LTR/width.

## AQ-011 — Dead/partial code

نمونه‌ها:

- دو abstraction Sync که یکی type-erased/خالی و inject‌شده اما استفاده‌نشده است؛
- sections کامنت‌شده Dashboard/Profile؛
- قابلیت‌های فعلیِ خاموش یا نیمه‌پیاده.

برای هر مورد یکی از این تصمیم‌ها:

- Ship behind gate؛
- Keep as internal experiment با owner/deadline؛
- Delete همراه با test/DI cleanup.

کد کامنت‌شده به‌عنوان feature flag نگه‌داری نشود.

## AQ-012 — Documentation و Release truth

- `README.md` با capability matrix واقعی؛
- version source واحد؛
- LICENSE واقعاً track شود یا ادعا حذف؛
- `RTK.md` و `HANDOFF-v2.md` پیدا/بازیابی یا reference حذف؛
- module map از `settings.gradle.kts` تولید/به‌روز؛
- ADR برای Ledger، Migration، Feature Gate، Adaptive و Sync؛
- release checklist و changelog خودکار.

## اولویت و وابستگی

| موج | Taskها | پیش‌نیاز |
|---|---|---|
| 1 | AQ-001، AQ-002، AQ-004 | گیت مالی در حال اجرا |
| 2 | AQ-003، AQ-008، AQ-010 | تست/CI اولیه |
| 3 | AQ-005، AQ-006، AQ-007 | characterization سبز |
| 4 | AQ-009، AQ-011، AQ-012 | build baseline و registry |

## معیار پایان

- کاهش زمان query/load-more با benchmark؛
- collector/observer ثابت؛
- ۱۰۰٪ Screen state collection مطابق قرارداد؛
- صفر Effect بدون consumer؛
- build graph بدون framework تکراری؛
- documentation با artifact واقعی منطبق؛
- هر cleanup یک تست یا metric برای اثبات عدم regression داشته باشد.
