# Observability، Analytics و حریم خصوصی

**Priority:** P1 — Phase 5 / 5.1.0  
**Planned at:** `51c80fb9` — 2026-07-29  
**Branch:** `codex/005-observability-analytics`
**نوع سند:** Epic Plan؛ هر OBS پیش از اجرا به Task Card پلتفرمی مستقل شکسته شود.

## اصل

Analytics برای فهم رفتار محصول است، نه کپی‌کردن دیتای مالی کاربر. Logging برای تشخیص خطاست، نه ذخیره Prompt، پاسخ مدل یا تراکنش.

## ریسک فعلی

- logger عمومی همیشه Verbose است.
- AI context مانده، دارایی، بدهی، اقساط، بودجه و هدف را به سرویس cloud می‌دهد.
- پاسخ model در service log می‌شود.
- cleartext traffic در manifest release به‌طور کلی مجاز است.
- tokenها در preference عمومی نگه‌داری می‌شوند.

قبل از افزودن Firebase باید این مسیرها اصلاح شوند؛ وگرنه Crashlytics/Logging سطح خروج داده را بیشتر می‌کند.

## معماری هدف

```kotlin
interface Analytics {
    fun track(event: ProductEvent)
    fun setConsent(consent: AnalyticsConsent)
}

interface CrashReporter {
    fun record(error: Throwable, context: SafeCrashContext)
}

interface PerformanceTracer {
    fun start(trace: TraceName): Trace
}

interface AppLogger {
    fun log(level: LogLevel, event: SafeLogEvent)
}
```

- قراردادها در common؛ SDK vendor در platform source set.
- domain به SDK وابسته نیست.
- event مدل sealed/versioned؛ map رشته‌ای آزاد ممنوع.
- release logger redacted و severity-based.

## پشتیبانی پلتفرم

| قابلیت | Android | iOS | Web | Desktop |
|---|---|---|---|---|
| Firebase Analytics | Native | Native | Web SDK | Unsupported/adapter دیگر |
| Crashlytics | Native | Native | پشتیبانی رسمی مستقیم ندارد | پشتیبانی رسمی مستقیم ندارد |
| Firebase Performance | Native | Native | Web SDK (Beta) | adapter custom/unsupported |
| Product event contract | بله | بله | بله | بله |

Unsupported باید در `PlatformCapabilities` صریح باشد؛ adapter خالی نباید success جعلی بدهد.
Phase 5 release gate ابتدا Android production + common contract است. iOS/Web adapter تنها وقتی gate
انتشار است که host/bundle runnable همان پلتفرم وجود داشته باشد.

## OBS-001 — Privacy inventory

مسیرهای خروج را ثبت کن:

- Analytics؛
- Crash report؛
- app log؛
- AI prompt/response؛
- Sync/Backup؛
- Android system backup؛
- SMS/notification؛
- report/share/export.

برای هر فیلد: purpose، retention، destination، consent، deletion و masking.

## OBS-002 — Consent

- Analytics، crash reporting، performance و Cloud AI انتخاب‌های مستقل.
- collection خودکار در Manifest/Info.plist/Web config به‌صورت پیش‌فرض خاموش؛ enable/initialize فقط پس از consent پایدار.
- default مطابق سیاست حقوقی/بازار هدف؛ تغییر قابل دسترس.
- consent قبل از SDK collection؛ opt-out واقعی.
- opt-out، unsent crash reports و شناسه/queue محلی را مطابق policy پاک یا متوقف کند.
- Cloud AI consent از telemetry جدا و در لحظه ارسال context مالی گرفته شود.
- consent version و timestamp محلی؛ بدون fingerprinting.
- privacy screen: چه داده‌ای، چرا، کجا، مدت نگهداری و حذف.

## OBS-003 — Event taxonomy v1

### خودکار؛ custom duplicate نشود

- `first_open`
- `session_start`
- `app_update`
- `app_remove` فقط Android
- `screen_view` خودکار برای یک Activity/NavHost کافی نیست؛ automatic reporting خاموش و navigation observer مرکزی با route key ثابت و بدون ID ساخته شود.

«نصب برنامه» مستقیماً از خود app قابل log نیست؛ `first_open` بعد از اولین اجراست. «حذف برنامه» را app هنگام uninstall نمی‌تواند خودش ارسال کند؛ روی Android event خودکار SDK/گزارش store استفاده شود.

### Custom lifecycle/funnel

```text
onboarding_started
onboarding_completed
account_created
transaction_created
first_transaction_completed
budget_created
installment_created
debt_created
check_created
report_exported
backup_export_completed
restore_completed
```

### Feature usage

```text
feature_opened
feature_action_completed
feature_action_failed
sms_draft_detected
sms_draft_approved
sms_draft_rejected
sync_started
sync_completed
sync_conflict_shown
paywall_viewed
purchase_completed
referral_shared
referral_redeemed
```

پارامترهای عمومی مجاز:

```text
schema_version
platform
app_version
build_number
feature_key
entry_point
result
safe_error_code
rollout_cohort
```

Event model باید safe-by-construction باشد: enum/bucket محدود، سقف طول/cardinality و بدون arbitrary
string. allowlist نام کلید به‌تنهایی کافی نیست.

### ممنوع

- amount/balance/value؛
- transaction/category/person/source text؛
- local/global entity ID؛
- card/account number؛
- SMS body/sender؛
- photo/path؛
- search query؛
- AI prompt/response؛
- exact due date/location.

## OBS-004 — Metrics

Event و metric را جدا نگه دار:

- Activation: account_created → first_transaction_completed.
- Retention: D1/D7/D30 cohort از session/user activity.
- Adoption: کاربران فعال هر feature / کاربران فعال.
- Reliability: crash-free users/sessions، ANR، safe failure code.
- Performance: cold start p50/p95، screen-ready p95، DB query p95، sync duration.
- Data quality: migration success، restore success، balance audit mismatch count.

یک Metric Dictionary اجباری:

- فرمول numerator/denominator؛
- eligible population و telemetry coverage rate؛
- installation/user identity و رفتار reinstall/restore/delete؛
- cohort clock/timezone و observation window؛
- bias جمعیت opt-in؛
- منبع مکمل مانند Play Console/OS vitals برای release health.

## OBS-005 — Performance traces

```text
app_cold_start
database_open_and_migrate
dashboard_ready
transaction_save
transaction_query_first_page
report_generate
backup_export
restore_validate
sync_round_trip
asset_rates_refresh
```

هیچ trace attribute حساس نباشد. Web علاوه بر traceهای محصول، LCP/INP/CLS پایش شود.

## OBS-006 — Crash و log hardening

- prompt/response AI log حذف؛
- error message server خام به client برنگردد؛
- release log سطح Info/Warn/Error و sampling؛
- stack trace + safe error code، بدون payload؛
- secure storage برای token؛
- Performance network trace فقط allowlist host/path bucket غیرحساس؛ Sync/AI URL کامل به telemetry نرود یا instrumentation آن‌ها خاموش شود؛
- debug data تنها در debug build و با banner واضح؛
- user action breadcrumb فقط enum عمومی.

## OBS-007 — Event validation

- schema unit test؛
- event name/parameter allowlist؛
- forbidden-key scan؛
- DebugView برای Android/iOS/Web؛
- duplicate automatic/custom test؛
- consent off test؛
- proxy/packet test که قبل/بعد opt-out هیچ request telemetry غیرمجاز نبیند؛
- offline queue size/retention؛
- feature gate cohort consistency.

## OBS-008 — Release plumbing و عملیات

- R8/ProGuard mapping upload و verification؛
- Apple dSYM upload و synthetic crash؛
- Web source map policy و environment separation؛
- dev/staging/prod project جدا؛
- release marker/build mapping؛
- alert threshold، incident owner و runbook؛
- dashboard/retention owner و data-quality monitor.

## Dashboardهای Phase 9

1. Release health: crash-free, ANR, migration/restore failure.
2. Activation funnel.
3. D1/D7/D30 retention.
4. Feature adoption و completion/failure.
5. Platform/build/locale split.
6. Campaign attribution پس از consent.

## Feature Gate

- `analytics_collection`
- `crash_reporting`
- `performance_monitoring`
- `cloud_ai_data_sharing`

این‌ها user consent را override نمی‌کنند. Kill switch برای incident وجود داشته باشد.

## Acceptance

- event contract مشترک روی چهار پلتفرم compile شود.
- برای platformهای release-ready adapter واقعی؛ بقیه capability صریح.
- قبل از consent و پس از opt-out هیچ request به endpointهای telemetry طبق proxy test ارسال نشود.
- forbidden data test سبز.
- first_open/app_remove duplicate custom نداشته باشد.
- همه Eventهای Feature جدید در همان PR schema و test داشته باشند.

## مراجع رسمی

- [Firebase Analytics event logging](https://firebase.google.com/docs/analytics/events)
- [Automatically collected Analytics events](https://support.google.com/analytics/answer/9234069)
- [Firebase Crashlytics supported platforms](https://firebase.google.com/docs/crashlytics)
- [Firebase Performance Monitoring supported platforms](https://firebase.google.com/docs/perf-mon)
- [Configure Analytics collection](https://firebase.google.com/docs/analytics/ios/configure-data-collection)
- [Disable Performance Monitoring](https://firebase.google.com/docs/perf-mon/disable-sdk)
- [Crashlytics opt-in reporting](https://firebase.google.com/docs/crashlytics/android/customize-crash-reports)
- [Manual screen view reporting](https://firebase.google.com/docs/analytics/screenviews)
