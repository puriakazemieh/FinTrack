# آمادگی انتشار چندسکویی و طراحی تطبیقی

**Priority:** P1  
**Planned at:** `51c80fb9` — 2026-07-29  
**نوع سند:** Epic Plan؛ MPRها پیش از اجرا به Task Card کوچک با فرمان و rollback مستقل شکسته شوند.  
**Branch sequence:**

- `codex/004-platform-build-baseline`
- `codex/004-adaptive-shell`
- `codex/004-adaptive-finance-workspace`
- `codex/004-localization-foundation`

## وضعیت فعلی پلتفرم‌ها

| پلتفرم | وضعیت واقعی | Blocker اصلی |
|---|---|---|
| Android | منتشرشده 4.5.0 | صحت مالی، migration، امنیت backup/cleartext، تست |
| iOS | framework targets موجود | host/Xcode project غایب، actualهای ناقص، signing/CI |
| Desktop JVM | entry موجود | `mainClass` اشتباه، UI phone-stretched، migration/notifications/files |
| Web JS/Wasm | entry موجود | resource packaging، migration/persistence، responsive UI، Beta platform |
| Server | prototype | dependency graph و امنیت/auth/storage |

## Blockerهای build

- `composeApp/build.gradle.kts:137` main class برابر `com.kazemieh.composeApp.MainKt` است،
  ولی `composeApp/src/jvmMain/kotlin/com/kazemieh/fintrack/Main.kt` package دیگری دارد.
- Web copy task از `src/jsMain/resources/sql-wasm.wasm` می‌خواند؛ asset در source set دیگری track شده و production task پوشش روشن ندارد.
- repository یک iOS host app/Xcode project ندارد.
- server source از domain model استفاده می‌کند ولی dependency آن کامل نیست.
- بسیاری از library moduleها framework iOS جدا و تکراری تعریف کرده‌اند؛ فقط umbrella `composeApp` باید framework نهایی قابل اتصال بسازد.
- نسخه Android 4.5.0 با Desktop/Server 1.0.0 و README ناهماهنگ است.

## MPR-001 — Build baseline

1. یک version source of truth برای app/build number و artifact naming.
2. Desktop `mainClass` اصلاح و package/install smoke test.
3. Web source/resource set و production bundle یکسان؛ DB worker در artifact نهایی verify.
4. server dependency graph یا جداسازی صریح از release build.
5. iOS umbrella framework با نام معتبر `FinTrackKit`؛ libraryها فقط target/source set، نه frameworkهای متعدد.
6. README capability matrix بر اساس CI واقعی.
7. پس از سبز شدن baseline، convention pluginها در Task جدا:
   `fintrack.kmp.library`, `fintrack.compose.feature`, `fintrack.sqldelight`, `fintrack.android.app`.

سه سطح جدا ثبت شود:

1. `CompileBaseline`
2. `InternalRunnableArtifact`
3. `SignedDistributableRelease`

**Acceptance:**

- clean checkout برای Android/JVM/Web package قابل ساخت باشد.
- iOS روی macOS simulator compile و host launch شود.
- هر artifact version یکسان و release manifest داشته باشد.
- پس از سه اجرای baseline روی runner ثابت، threshold configuration/build و regression budget ثبت و رعایت شود.

## MPR-002 — iOS productization

- `iosApp` با SwiftUI/UIKit entry؛
- bundle ID، version، signing، privacy manifest و deep link؛
- database create/migrate/reopen؛
- image picker/storage/share واقعی؛
- notification capability صریح؛
- safe area/keyboard/back gesture؛
- Crashlytics/Analytics consented actual؛
- TestFlight checklist و rollback.

## MPR-003 — Desktop/Web productization

### Desktop

- ماتریس Windows/macOS/Linux با min version؛ Windows installer و app data location؛
- file picker/share/export؛
- keyboard shortcuts و menu؛
- update feed امضاشده؛
- multi-window فقط بعد از state ownership روشن؛
- DB lock/reopen/migration.

### Web

- ADR مستقل Kotlin/JS در برابر `wasmJs`؛ وجود `sql-wasm.wasm` موتور SQLite است و به معنی Kotlin/Wasm target نیست؛
- browser/min-version و compatibility matrix؛
- persistence quota/failure UX؛
- PWA/offline policy؛
- route refresh/deep link؛
- responsive/resizable layout؛
- browser download/import با size limits؛
- privacy و CSP/HTTPS.

## معماری تطبیقی مشترک

مستندات رسمی JetBrains درباره artifact adaptive در حال گذار/ناسازگارند: راهنمای adaptive یک artifact
JetBrains برای `commonMain` معرفی می‌کند، درحالی‌که فهرست جدیدتر Android-only، کتابخانه AndroidX
adaptive را common نمی‌داند. بنابراین اولین Task یک compatibility spike روی نسخه‌های دقیق catalog و
تمام targetهاست.

- اگر `org.jetbrains.compose.material3.adaptive` با Compose 1.10 و تمام targetها compile شد،
  `WindowSizeClass/currentWindowAdaptiveInfo()` منبع استاندارد باشد.
- API breakpoint موازی ساخته نشود.
- فقط policy محصول مشتق شود:

```kotlin
data class FinTrackPanePolicy(
    val navigation: NavigationMode,
    val panes: PaneMode,
    val sheetPresentation: SheetPresentation,
    val inputModality: InputModality,
)
```

- اگر artifact سازگار نبود، adapter موقت پشت همین boundary ساخته و حذف آن با issue/version pin ثبت شود.
- policy علاوه بر width/height class، posture/hinge/occlusion، safe area، font scale، input modality و
  hover capability را مصرف کند.

Breakpoints پیشنهادی:

- Compact: کمتر از 600dp
- Medium: 600 تا کمتر از 840dp
- Expanded: 840 تا کمتر از 1200dp
- Large: 1200 تا کمتر از 1600dp
- ExtraLarge: 1600dp و بیشتر

این طبقه‌بندی بر اساس فضای موجود پنجره است، نه نوع دستگاه؛ باید هنگام resize تغییر کند.

## MPR-004 — `FinTrackAdaptiveShell`

| Width | Navigation | Content |
|---|---|---|
| Compact | bottom bar | تک‌ستونه، bottom sheet |
| Medium | rail در ارتفاع کافی؛ bottom bar در compact-height | یک pane عریض یا supporting pane در صورت فضای واقعی |
| Expanded | rail/permanent drawer | list-detail یا grid 2 ستونه |
| Large/XLarge | permanent drawer | workspace چندpane با max widths |

جایگزینی هدف:

- `composeApp/.../FinTrackHost.kt`
- `composeApp/.../navigationBar/FintrackNavigationBar.kt`

قواعد:

- state و ViewModel با resize از بین نرود.
- یک coordinator ViewModel در سطح canonical layout مالک selected ID، draft و back behavior باشد؛
  `Route/Screen wrapper` فقط آن را به contentهای stateless می‌دهد.
- system/navigation insets semantic باشند؛ `padding(bottom = 100.dp)` حذف شود.
- bottom sheet در Expanded به dialog/side sheet/detail pane تبدیل شود.
- عرض form محدود، table/report قابل کشش.

## MPR-005 — Layoutهای canonical

### Dashboard و Tools

- Compact: کارت‌های اولویت‌دار تک/دوستونه.
- Medium: grid دو ستونه.
- Expanded+: summary rail + configurable grid؛ اطلاعات پرتکرار بالا، ابزارها جدا.
- density رقیب‌های شلوغ تکرار نشود؛ progressive disclosure.
- stack/grid یکنواخت از cardهای هم‌اندازه ممنوع؛ اندازه و جایگاه از اولویت کار و داده واقعی بیاید.
- Glass فقط وقتی depth/overlay واقعی را توضیح می‌دهد؛ blur روی surface تقریباً مات decoration و هزینه اضافی است.

### Transactions/Source/Person/Debt/Check/Asset

- Compact: list → full screen detail.
- Expanded: list-detail؛ انتخاب حفظ شود؛ افزودن/ویرایش side panel.
- Desktop: keyboard navigation، context menu، sortable columns در report.

### Settings/Profile

- Compact: list → screen.
- Expanded: category pane + settings detail.

### Reports

- chart + filter + result table هم‌زمان در Large؛
- saved views؛
- export دقیقاً filter snapshot قابل مشاهده را استفاده کند.

## MPR-006 — Semantic tokens

جایگزینی کور `dp` با `LocalSpacing` ممنوع. Tokenها:

- `SpacingTokens`
- `ShapeTokens`
- `ControlSizeTokens`
- `TouchTargetTokens`
- `ContentWidthTokens`
- `PaneTokens`
- `NavigationInsets`

icon size، radius، touch target و gutter مفاهیم متفاوت‌اند. ابتدا فایل‌های پرچرخش و shell مهاجرت شوند.

## MPR-007 — Accessibility و input

- حداقل touch target 48dp؛
- semantics برای Switch، color swatch، chart و icon؛
- اکشن customize مسیر آشکار در UI داشته باشد؛ long-press تنها مسیر نباشد.
- focus traversal و visible focus؛
- hover/pressed/selected states؛
- default/hover/focus/active/disabled/loading/error/success برای componentهای تعاملی؛
- shortcutهای Add/Search/Save/Back؛
- screen reader labels و keyboard-only E2E؛
- contrast در theme شیشه‌ای/سفارشی.
- reduced-motion policy؛ motion اطلاعاتی حفظ و motion تزئینی حذف/کوتاه شود.
- اعداد مالی از tabular figures یا راه‌حل هم‌ترازی معادل استفاده کنند؛ Vazirmatn تک‌فونت فارسی حفظ شود مگر تست locale نیاز دیگری نشان دهد.

## MPR-008 — Locale و تقویم

```text
AppLocale
TextDirection
NumeralSystem
CalendarSystem
CurrencyPreference
TimeZone
TemporalValue = Instant | CivilDate | LocalSchedule
```

- رخداد واقعی ثبت‌شده = `Instant`؛
- تاریخ بدون ساعت مثل دوره بودجه = `LocalDate/CivilDate`؛
- سررسید/زمان‌بندی محلی = `LocalDateTime + TimeZoneId`؛
- Persian/Gregorian formatter strategy؛
- RTL/LTR از locale، نه hard-code؛
- seed data با stable key و label resource؛
- English resource کامل؛
- تغییر تاریخ تراکنش، ساعت انتخاب‌شده را به 00:00 reset نکند؛
- Jalali library با reference-date tests و Gregorian هم‌سطح.

## Test matrix

سه lane:

- PR smoke: چند viewport/locale/interaction بحرانی؛
- Nightly golden: matrix گسترده با baseline owner و tolerance مشخص؛
- Release manual/device: foldable، input و platform fidelity.

نقاط پایه و مرزی (dp-equivalent):

```text
360x800   Compact phone
599/600   Compact ↔ Medium boundary
600x960   Medium tablet portrait
839/840   Medium ↔ Expanded boundary
840x900   Expanded
1199/1200 Expanded ↔ Large boundary
1200x800  Large desktop/tablet
1599/1600 Large ↔ ExtraLarge boundary
1600x1000 ExtraLarge desktop
```

برای هرکدام:

- lane متناسب از LTR/RTL، light/dark/glass؛
- keyboard/touch/mouse و phone landscape/fold posture؛
- font scale عادی و بزرگ؛
- navigation resize؛
- list-detail selection؛
- dialog/sheet transformation؛
- screenshot/golden و smoke interaction.

## ترتیب اجرا

1. Build baseline؛
2. Capability registry؛
3. AdaptiveLayoutInfo + shell؛
4. semantic tokens/insets؛
5. Dashboard/Tools؛
6. Transactions master-detail؛
7. Settings/Profile؛
8. Reports/Assets/obligations؛
9. locale/a11y polish؛
10. internal runnable packaging؛
11. signed/distributable packaging در Phase 18.

## Definition of Done

- هیچ صفحه اصلی در Expanded یک ستون تمام‌عرض بی‌هدف نباشد.
- resize route/selection/form draft را حفظ کند.
- کنترل unsupported دیده نشود یا توضیح صریح داشته باشد.
- Android/iOS/Desktop/Web artifact هرکدام launch + DB reopen smoke داشته باشند.
- README سطح Compile/Internal/Distributable هر پلتفرم را جدا نشان دهد.

## مراجع رسمی

- [Window size classes](https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes)
- [Adaptive navigation](https://developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation)
- [Compose Multiplatform adaptive layouts](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)
- [Android-only Compose Multiplatform APIs](https://kotlinlang.org/docs/multiplatform/compose-android-only-components.html)
- [KMP umbrella framework guidance](https://kotlinlang.org/docs/multiplatform/multiplatform-project-configuration.html)
- [Platform-specific Compose behavior](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)
