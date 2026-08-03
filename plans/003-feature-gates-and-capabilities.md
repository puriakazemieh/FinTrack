# رجیستری Feature Gate و Platform Capability

**Priority:** P0/P1  
**Planned at:** `51c80fb9` — 2026-07-29  
**Branch:** `codex/003-feature-gates-capabilities`  
**نوع سند:** Epic Plan؛ هر FGC پیش از اجرا به Task Card مستقل شکسته شود.

## مشکل فعلی

- `core/common/src/commonMain/kotlin/com/kazemieh/common/model/ToolFeature.kt:9-34`
  قابلیت غایب از CSV را enabled می‌داند.
- `feature-container/dashboard/src/commonMain/kotlin/com/kazemieh/dashboard/DashboardViewModel.kt:99-123`
  در هر ساخت ViewModel یک disabled set را روی همان preference می‌نویسد.
- `ManageToolsViewModel` انتخاب کاربر را روی همان کلید ذخیره می‌کند؛ بنابراین Dashboard آن را overwrite می‌کند.
- routeها و DI قابلیت‌های ناقص را ثبت می‌کنند.
- Notification/ImagePicker/Storage در چند پلتفرم no-op هستند اما UI یا permission نتیجه موفق می‌دهد.

## مدل هدف

```kotlin
enum class FeatureId
enum class ProductAvailability { Disabled, Internal, Preview, Beta, Stable }
enum class SupportStatus { Supported, Unsupported, Degraded }
enum class PermissionState { NotRequired, Unknown, Denied, CanRequest, Granted }
enum class EntitlementState { Unknown, NotEntitled, Entitled }
enum class RolloutState { Unknown, Excluded, Included }
enum class FeaturePresentation { Hidden, Disabled, RequiresAction, ReadOnly, Enabled }

data class FeatureDecision(
    val product: ProductAvailability,
    val support: SupportStatus,
    val permission: PermissionState,
    val entitlement: EntitlementState,
    val rollout: RolloutState,
    val userVisible: Boolean,
    val presentation: FeaturePresentation,
    val reasonCode: FeatureReasonCode?,
)
```

فرمول:

```text
usable = product allowed
      AND support == Supported
      AND permission in {NotRequired, Granted}
      AND entitlement == Entitled
      AND rollout == Included

visibleInDashboard = usable AND user preference
```

حالت `Unknown/Offline/Degraded` به success تبدیل نمی‌شود و به `Disabled/RequiresAction/ReadOnly`
نگاشت می‌شود. `userVisible` فقط customization است؛ اجازه محصول یا امنیت نیست.

## منابع تصمیم

1. **Build defaults:** امن و version-controlled.
2. **Product state:** Internal/Preview/Beta/Stable.
3. **Platform capability:** Android/iOS/JVM/JS actual.
4. **Build channel/distribution:** Play/Direct/App Store/Desktop/Web و region/legal eligibility.
5. **Entitlement:** Free/Premium/Admin/Workspace role.
6. **Remote rollout:** درصد/cohort با cache و default امن.
7. **User preference:** pin/hide/order.

## Task Cardها

### FGC-001 — Inventory

برای تمام routeها و ابزارها manifest بساز:

| Feature | Product state | Android | iOS | Desktop | Web | Distribution | Data migration | Owner |
|---|---|---|---|---|---|---|---|---|

هر قابلیت فعلی یکی از این تصمیم‌ها را بگیرد:

- Stable و قابل نمایش؛
- Preview پشت gate؛
- Disabled/quarantined؛
- dead/commented code برای حذف.

### FGC-002 — Registry common

- `FeatureRegistry` در لایه common و بدون Compose/Firebase.
- key پایدار؛ rename با alias/migration.
- schema version و migration افزایشی idempotent؛ user state برابر `Unset`, `UserSet` یا `Retired`.
- default جدید فقط روی `Unset` اعمال شود.
- decision observable برای UI.
- registry فقط reason enum/stable key؛ نگاشت به `UiText` در feature/UI.

### FGC-003 — PlatformCapabilities

قابلیت‌ها نه Featureها:

```text
Notifications.Schedule
Notifications.PersistentQuickAdd
Images.PickCamera
Images.PickGallery
Images.Persist
Files.Share
Files.Export
Biometrics
BackgroundWork
Sms.ReadSuggestions
SecureStorage
CloudAnalytics
CrashReporting
PerformanceMonitoring
PlayServices
Billing
InAppUpdate
Ads
```

- no-op موفق حذف شود.
- Unsupported نتیجه typed بدهد.
- UI کنترل را disable/hide و علت را بگوید.
- permission state با support state یکی نباشد.

### FGC-004 — Enforcement surfaces

یک descriptor اجباری برای هر feature module، تصمیم registry را به این نقاط وصل کند:

- Tools/Dashboard/Profile cards؛
- bottom bar/rail/drawer؛
- route registration یا route guard؛
- deep link و app shortcut؛
- worker/scheduler/reminder؛
- widget/action receiver؛
- sync/analytics job؛
- settings و onboarding.

routeها ثابت ثبت و در boundary guard شوند تا restore/deep link پایدار بماند. ورود مستقیم به route
خاموش باید به safe destination + reason code localizable برود. تست/codegen یا lint باید descriptor،
route، worker، widget و registry را تطبیق دهد.

### FGC-005 — Preference migration

- `PREF_DISABLED_TOOLS` قدیمی فقط به `UserSet` visibility مهاجرت کند.
- Dashboard دیگر روی init preference ننویسد.
- order/pin/hide schema نسخه‌دار باشد.
- قابلیت product-disabled در preference کاربر حذف نشود؛ اگر بعداً فعال شد انتخاب قبلی قابل بازیابی باشد.

### FGC-006 — Remote rollout و entitlement

- Remote config نبود/خراب → default امن محلی.
- امنیت server-side فقط با server authorization؛ client gate کافی نیست.
- cohort assignment پایدار و privacy-safe.
- kill switch برای Sync/AI/automation.
- paywall فقط بعد از FeatureDecision؛ قیمت قبل از ورود داده سنگین شفاف.

## Featureهای اولیه پیشنهادی

| Feature | وضعیت پیشنهادی اکنون |
|---|---|
| Transactions/Categories/Sources/Tags/Persons | Stable پس از گیت مالی |
| Budget/Installment/Debt/Check | Beta تا پایان Phase 4 |
| Fixed expense automatic posting | Android Beta؛ سایر پلتفرم‌ها Unsupported |
| Cloud Sync/Profile | Disabled |
| Cloud AI Advisor | Production Disabled؛ فقط Internal در debug/internal build پس از secure storage، allowlist، redaction و consent موردی |
| Goals/Achievements | Internal تا تست |
| FX/Gold/Assets/Converter | Preview تا provider و تست |
| FAQ/News/Support | Inventory؛ content gate |
| SMS import | Android Beta با preview/dedupe |
| Quick notification/Glance widget | Android Beta |
| Attachment | Android Beta؛ دیگر پلتفرم‌ها طبق capability واقعی |

## Telemetry

فقط این اطلاعات مجاز:

- feature key؛
- decision reason enum؛
- platform/app version؛
- rollout cohort تصادفی؛
- action open/complete/fail با error class غیرحساس.

مبلغ، نام، توضیح، entity ID، SMS، path و content ممنوع.

## تست‌ها

- truth table تمام لایه‌ها؛
- migration preference؛
- cold start offline؛
- remote config timeout/corrupt؛
- route/deeplink bypass؛
- worker/widget وقتی gate خاموش است؛
- platform unsupported؛
- Play/Direct و region/legal channel؛
- recreation Dashboard بدون overwrite preference.

## Acceptance

- هیچ feature ناقص با absent-from-list فعال نشود.
- هیچ no-op platform success نماند.
- یک FeatureDecision بین تمام surfaceها سازگار باشد.
- هر feature جدید بدون registry entry در CI fail شود.
- schema migration مستقل از gate اجرا شود.
