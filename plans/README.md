# مرکز برنامه‌ریزی FinTrack

این پوشه خروجی ممیزی عمیق پروژه در تاریخ ۱۴۰۵/۰۵/۰۷ (2026-07-29) و روی commit
`51c80fb9` است. در این ممیزی هیچ فایل سورس تغییر نکرده است.

## ترتیب مطالعه

1. [000-fintrack-master-roadmap.md](000-fintrack-master-roadmap.md) — نقشه‌راه محصول؛ فازهای ۱ تا ۱۷ مالک محصول را بدون تغییر نگه می‌دارد.
2. [001-financial-core-release-gate.md](001-financial-core-release-gate.md) — گیت فوری صحت مالی و نسخه 4.5.1/5.0.0.
3. [002-database-backup-safety.md](002-database-backup-safety.md) — مهاجرت، بازیابی، پیوست‌ها و ایمنی داده.
4. [003-feature-gates-and-capabilities.md](003-feature-gates-and-capabilities.md) — Feature Toggle، قابلیت واقعی هر پلتفرم و جلوگیری از نمایش قابلیت‌های ناقص.
5. [004-multiplatform-adaptive-release.md](004-multiplatform-adaptive-release.md) — Android/iOS/Web/Desktop و طراحی تطبیقی موبایل تا دسکتاپ.
6. [005-observability-analytics-privacy.md](005-observability-analytics-privacy.md) — Analytics، Crashlytics، Performance، Logging و حریم خصوصی.
7. [006-secure-sync-and-collaboration.md](006-secure-sync-and-collaboration.md) — بازطراحی Sync/Backup و پیش‌نیاز حساب خانوادگی، دنگ و اشتراک بدهی.
8. [007-competitor-benchmark.md](007-competitor-benchmark.md) — بنچمارک بازار و قواعد استفاده از آن در طراحی.
9. [008-architecture-quality-backlog.md](008-architecture-quality-backlog.md) — MVI، Performance، مرز ماژول‌ها، Build Logic و پاکسازی تدریجی.

این فایل‌ها **Epic Plan** هستند. شناسه‌های داخل آن‌ها باید پیش از پیاده‌سازی به Task Card کوچک با قالب پایین تبدیل شوند؛ هیچ عامل نباید یک فایل Epic را یکجا اجرا کند.

## ترتیب امن اجرا

1. containment مرحله صفر plan 006: خاموش‌کردن Sync ناامن، revoke/rotate credential و ممنوعیت cleartext در release.
2. `FCR-001` همراه `DBS-001`: characterization tests و inventory fixture.
3. `DBS-002..004`: حفظ دیتابیس قدیمی، lifecycle و schema convergence.
4. `FCR-002/003`: semantics انتقال و Update مبتنی بر row/revision پایدار داخل DB.
5. `FCR-004` فقط Audit Preview؛ هر repair واقعی بعد از Backup/Restore v2 اثبات‌شده.
6. انتشار 4.5.1 با Task IDهای دقیق.
7. Backup/Restore v2، Money 64-bit، idempotency تعهدات و بودجه.
8. انتشار 5.0.0.
9. Sync v2 فقط پس از global-ID backfill، round-trip backup و قرارداد ledger.

## قانون اجرای کارها با هوش مصنوعی

هر بار فقط یک Task Card مستقل را به عامل بدهید. عامل نباید چند Epic را هم‌زمان پیاده‌سازی کند.
هر Task Card باید این موارد را داشته باشد:

- شناسه، هدف و دلیل کسب‌وکاری؛
- پیش‌نیازها و فایل‌های در محدوده؛
- موارد صریحاً خارج از محدوده؛
- پلتفرم‌های هدف؛
- وضعیت Feature Toggle و رفتار در حالت خاموش؛
- تغییر دیتابیس، سازگاری عقب‌رو و Rollback؛
- رویدادهای مجاز و داده‌های ممنوع برای Telemetry؛
- تست‌های واحد، یکپارچه، UI و بسته‌بندی؛
- معیار پذیرش قابل اندازه‌گیری؛
- خروجی مورد انتظار: کد، تست، مستند و گزارش فرمان‌های اجراشده.

قالب پیشنهادی:

```text
Task ID:
Objective:
Why now:
Prerequisites:
In scope:
Out of scope:
Files/modules:
Implementation steps:
Feature gate:
Platform matrix:
Migration/rollback:
Telemetry/privacy:
Tests:
Acceptance criteria:
Verification commands:
Stop conditions:
```

## قواعد غیرقابل مذاکره

- هیچ نسخه‌ای با خطای شناخته‌شده در مانده حساب، مهاجرت دیتابیس یا Restore منتشر نشود.
- هر قابلیت جدید از روز اول در رجیستری Feature Gate ثبت شود.
- «نمایش/پنهان‌سازی توسط کاربر» با «آماده‌بودن محصول»، «پشتیبانی پلتفرم» و «اشتراک/دسترسی» یکی نباشد.
- Feature Toggle نباید جای Migration درست، کنترل دسترسی سرور یا تست را بگیرد.
- اطلاعات مالی خام، مبلغ، عنوان تراکنش، شماره حساب/کارت، متن SMS، Prompt و پاسخ AI وارد Analytics یا Log نشود.
- قابلیت Unsupported نباید موفقیت جعلی برگرداند؛ UI باید آن را صریحاً غیرفعال یا مخفی کند.
- هر تغییر مالی باید یک invariant قابل تست و مسیر reconciliation برای داده‌های قبلی داشته باشد.
- پیاده‌سازی هر Screen مطابق قرارداد پروژه باشد: فایل `*Screen.kt`، ریشه `Box`، MVI با State/Intent/Effect، `koinViewModel()`، `collectAsStateWithLifecycle()`، منابع رشته‌ای و `UiText`.

## تعریف پایان یک نسخه

نسخه تنها زمانی قابل انتشار است که:

- همه گیت‌های همان Milestone سبز باشند؛
- هیچ P0/P1 بازِ پذیرفته‌نشده وجود نداشته باشد؛
- Migration از تمام نسخه‌های منتشرشده روی fixture واقعی تست شده باشد؛
- Build/Package پلتفرم‌های ادعاشده ساخته و Smoke Test شده باشد؛
- Feature Gate، Analytics و حریم خصوصی برای قابلیت‌های جدید مرور شده باشند؛
- Release notes، مسیر Rollback و روش بازیابی داده آماده باشند؛
- release manifest نام CI job، runner OS، فرمان، artifact hash، threshold و approver را ثبت کند؛
- Worktree فقط شامل تغییرات همان Task باشد.
