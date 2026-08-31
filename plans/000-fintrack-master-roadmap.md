# نقشه‌راه جامع محصول و مهندسی FinTrack

**وضعیت مبنا:** انتشار Android 4.5.0 طبق گزارش مالک محصول و `versionName` repository؛ iOS/Web/Desktop در سطح یک codebase مشترک اما نه همگی release-ready  
**مبنای ممیزی:** commit `51c80fb9` در تاریخ 2026-07-29  
**قاعده این سند:** فازهای ۱ تا ۱۷ پایین «قفل‌شده» هستند و متن/ترتیب/تیک آن‌ها تغییر نکرده است. گیت‌ها و ترک‌های جدید مکمل آن‌ها هستند.

## نتیجه اجرایی

FinTrack از نظر گستره قابلیت‌ها جلوتر از یک MVP ساده است، اما اکنون «تعداد فیچر» از «اعتمادپذیری هسته مالی و آمادگی چندسکویی» جلو زده است. بهترین مسیر رشد، اضافه‌کردن فیچر بیشتر در کوتاه‌مدت نیست؛ ابتدا باید داده مالی قابل اعتماد، Migration قابل اثبات، تست خودکار، Feature Gate واقعی و خروجی قابل نصب هر پلتفرم ساخته شود.

سه تصمیم اصلی:

1. قبل از ادامه Feature Development، یک Hotfix با نام 4.5.1 برای خطاهای مانده/انتقال/ویرایش و خطر حذف دیتابیس منتشر شود؛ تاریخ آن پس از spike و fixture baseline تعیین شود.
2. نسخه 5.0.0 فقط وقتی منتشر شود که «گیت صحت مالی» و تست‌های Phase 4 واقعاً خودکار و سبز باشند.
3. طراحی بزرگ‌صفحه یک پروژه آخرِ کار نیست؛ از Phase 4 به‌صورت زیرساخت مشترک شروع و تا قبل از Closed Beta تکمیل شود.

## تصویر واقعی پروژه

- ۴۳ ماژول Gradle، حدود ۵۶۹ فایل Kotlin و حدود ۶۲ هزار خط Kotlin وجود دارد.
- هیچ تست Kotlin واقعی در repository پیدا نشد؛ `commonTest` موجود خالی است.
- CI، Detekt/Ktlint/Spotless و سیاست Release Gate وجود ندارد.
- بیشترین ریسک اکنون در ledger و مانده حساب، Migration، Backup/Sync، امنیت داده و تفاوت ادعا با قابلیت واقعی پلتفرم‌هاست.
- چند قابلیت ظاهراً «آینده» هم‌اکنون بخشی از کد را دارند: Quick-add notification، Android Glance widget، theme/accent/glass، time picker، FAQ/news، asset/FX/gold/converter، AI، goals و achievements. این‌ها باید inventory و harden شوند، نه اینکه دوباره از صفر ساخته شوند.
- Desktop entry point، Web resource packaging، iOS host app و چند `actual` پلتفرمی کامل نیستند؛ بنابراین README فعلی بیش از سطح آمادگی واقعی ادعا می‌کند.
- طراحی مرجع فعلی عملاً phone-first و نزدیک 412×892 است؛ navigation، dashboard، tools، sheets و detail pages برای پنجره بزرگ تطبیق ساختاری ندارند.

## ثبت ریسک اولویت‌دار

| اولویت | ریسک | اثر | تصمیم |
|---|---|---|---|
| P0 | انتقال عادی می‌تواند دوبرابر از مبدأ کم کند؛ مبدأ=مقصد نیز معتبر است | مانده نادرست | freeze + FCR-002 |
| P0 | Edit Transaction delta را محاسبه ولی در DB اعمال نمی‌کند | drift دائمی مانده | FCR-003 + audit |
| P0 | Android دیتابیس قدیمی را حذف می‌کند | از دست رفتن کل تاریخچه | DBS-002 |
| P0 | schema upgrade پلتفرم‌ها همگرا/تأییدشده نیست | crash یا داده غیرقابل دسترس | DBS-003/004 |
| P0 | تست خودکار و CI وجود ندارد | regression بدون مانع انتشار | AQ-001 |
| P0 | Sync credential/ownership/storage امن ندارد | افشا/overwrite داده مالی | quarantine + plan 006 |
| P1 | Backup/Restore graph کامل و اتمیک نیست | backup ظاهراً موفق اما ناقص | DBS-005/006 |
| P1 | Platform actualهای no-op موفقیت نشان می‌دهند | از دست رفتن داده/اعتماد | FGC-003 |
| P1 | Dashboard preference را روی init overwrite می‌کند | Feature Toggle و انتخاب کاربر خراب | FGC-005 |
| P1 | UI فقط phone shell دارد | تجربه ضعیف tablet/desktop/web | plan 004 |
| P1 | `Int` و `Double` در پول/دارایی | overflow و rounding | Money 64-bit |

## گیت اضطراری R0 — قبل از هر Feature جدید

**نسخه پیشنهادی:** 4.5.1  
**زمان هدف:** پس از spike و fixture baseline؛ envelope اولیه ۲ تا ۴ هفته با confidence پایین  
**مالک:** Core/Data + QA

- تعریف قطعی semantics انتقال: مبلغ اصلی، مبلغ مقصد و کارمزد؛ ممنوعیت حساب مبدأ=مقصد.
- اعمال delta مانده در Update Transaction داخل همان SQL transaction.
- جلوگیری از حذف دیتابیس قدیمی Android و تهیه safety copy.
- شناسایی schemaهای واگرا و ساخت migration همگرا.
- خاموش نگه‌داشتن Sync و Cloud AI ناقص از طریق Product Gate.
- حذف/چرخاندن credential موجود در سورس server و جلوگیری از cleartext در release.
- ساخت حداقل regression suite برای add/edit/delete/transfer و upgrade fixture.
- افزودن یک Balance Audit غیرمخرب برای تشخیص حساب‌های drift کرده؛ اصلاح خودکار فقط وقتی semantics تاریخی قطعی است.

**شرط پایان:** containment مرحله صفر plan 006، `FCR-001..003`، حالت audit-only از `FCR-004`
و `DBS-001..004` سبز باشند. Repair خودکار/تأییدشده و Restore کامل به نسخه 5.0 منتقل می‌شوند.

## ترک‌های موازی که روی فازهای موجود سوار می‌شوند

### Track A — صحت مالی و داده

- Phase 4: invariant tests، Migration fixtures، Money 64-bit، idempotency اقساط/بدهی/چک و بودجه period-aware.
- Phase 5–8: ledger قابل rebuild، decimal دارایی و reconciliation عملیاتی.
- Phase 9 به بعد: reconciliation dashboard، backup round-trip و release migration drill.

### Track B — محصول چندسکویی و UI تطبیقی

- Phase 4: `AdaptiveLayoutInfo` و width-class مستقل از نوع دستگاه.
- Phase 5–6: adaptive shell؛ bottom bar در Compact، rail در Medium، rail/drawer و محدودیت عرض محتوا در Expanded/Large.
- Phase 6–8: grid برای Dashboard/Tools، master-detail برای transaction/source/person/debt/check/asset، two-pane برای settings/profile.
- قبل از Phase 9: keyboard/focus/hover، accessibility، resize state restoration و screenshot matrix.
- Phase 18: بسته‌بندی نهایی iOS/Web/Windows و انتشار.

### Track C — Feature Gate و Capability

- Phase 4: registry مرکزی و versioned defaults.
- هر Feature بعدی: ثبت Product Availability، Platform Capability، Entitlement/Rollout و User Visibility.
- تمام routeها، deep linkها، jobها، reminderها، widgetها و navigation itemها باید guard یکسان داشته باشند.

### Track D — Observability و Privacy

- Phase 5: قرارداد common برای analytics/crash/performance/log، platform actualها و consent.
- Phase 6 به بعد: event برای فیچرهای جدید، بدون اطلاعات مالی خام.
- Phase 9: داشبورد activation/retention/adoption/crash-free users و چرخه تصمیم‌گیری.

### Track E — Quality و Delivery

- Presubmit: common/domain/database/ViewModel tests + Android compile.
- Nightly: migration fixtures، Desktop/Web package، Android UI tests.
- Release: signed artifact، iOS build روی macOS، Web smoke، Desktop installers، backup/restore drill.
- هر refactor بزرگ بعد از characterization test انجام شود.

## برنامه قفل‌شده مالک محصول — بدون تغییر

# 🚀 فاز ۱ — انتشار نسخه 3.5.0 (پایدارسازی اولیه)
## هدف
داشتن یک نسخه پایدار که بتوان روی آن توسعه را ادامه داد.
## کارها
- [x]  انتشار نسخه 3.5.0

# 🔧 فاز ۲ — رفع باگ‌های اصلی
## هدف
پایدار کردن هسته برنامه.
## کارها
- [x]  رفع باگ هزینه ثابت
- [x]  رفع باگ بودجه
- [x]  رفع باگ یادداشت
- [x]  رفع باگ لیست
- [x]  انتشار نسخه 4.0.0
 
 
# 💰 فاز ۳ — تکمیل هسته مدیریت مالی
## هدف
تکمیل مهم‌ترین قابلیت‌های مالی.
## کارها
- [x]  اضافه کردن اقساط
- [x]  اضافه کردن بدهی
- [x]  اضافه کردن مدیریت چک
- [x]  انتشار نسخه 4.5.0 (بدون تست کامل)
 
# ✅ فاز ۴ — تست کامل هسته مالی
## هدف
اطمینان از کیفیت Core Product.
## کارها
- [x]  تست کامل اقساط
- [x]  تست کامل بدهی
- [x]  تست کامل مدیریت چک
- [x]  انتشار نسخه 5.0.0
 
# 📊 فاز ۵ — زیرساخت محصول (Analytics)
## هدف
آماده شدن برای ورود کاربران واقعی.
## کارها
- [x] **فاز ۰: ساخت زیرساخت لایه Common**
  - [x] ساخت اینترفیس `AnalyticsService` و `CrashReporter`
  - [x] ساخت کلاس امن `ProductEvent` برای ایونت‌ها
  - [x] ساخت `NoOpAnalytics` برای پیش‌گیری از کرش سیستم
- [x] **فاز ۱: تنظیمات پنل فایربیس (توسط کاربر)**
  - [x] ساخت پروژه در Firebase Console
  - [x] ثبت اپلیکیشن Android و قرار دادن `google-services.json` در پوشه `app/`
  - [ ] ثبت اپلیکیشن Web و دریافت کلیدهای کانفیگ
  - [ ] ثبت یک اپلیکیشن موقت برای دریافت Secret Key بخش Measurement Protocol (برای دسکتاپ)
- [x] **فاز ۲: پیکربندی Gradle و وابستگی‌ها در KMP (توسط هوش مصنوعی)**
  - [x] اضافه کردن پلاگین‌ها و کتابخانه‌های `firebase-analytics`
  - [x] اعمال پلاگین‌ها در `build.gradle.kts` ماژول `:app` و `androidMain`
- [x] **فاز ۳: پیاده‌سازی لایه اندروید و DI (توسط هوش مصنوعی)**
  - [x] ساخت `AndroidAnalyticsService` و `AndroidCrashReporter`
  - [x] معرفی کلاس‌ها به Koin در `CommonModule.android.kt` یا `AppModule`
- [x] **فاز ۴: تزریق ایونت‌های رشد و جامع در ViewModelها (توسط هوش مصنوعی)**
  - [x] پیاده‌سازی و تزریق تمامی ایونت‌های ثبت‌شده در فایل `010-comprehensive-event-taxonomy.md` (بخش‌های اصلی مانند داشبورد اضافه شد، مابقی در روند توسعه تکمیل می‌شود)
  - [ ] اتصال سیستم Consent (ارسال اطلاعات فقط پس از تایید کاربر - در فازهای بعدی UI)
- [x] **فاز ۵: پیاده‌سازی لایه Web / جاوا اسکریپت (توسط هوش مصنوعی)**
  - [x] ساخت `NoOpAnalytics` به عنوان Fallback در لایه `jsMain` برای پایداری کامپایل
  - [ ] قرار دادن کدهای کانفیگ Firebase JS در `index.html` (نیاز به اکشن کاربر)
- [x] **فاز ۶: پیاده‌سازی لایه Desktop / JVM (توسط هوش مصنوعی)**
  - [x] ساخت `NoOpAnalytics` در لایه `jvmMain` تا زمان آماده‌سازی API Key
- [x] **فاز ۷: پیاده‌سازی لایه iOS (در آینده)**
  - [x] ساخت `NoOpAnalytics` در لایه `iosMain` برای پایداری کدهای مشترک KMP
- [ ] انتشار نسخه 5.1.0


# ⚙️ فاز ۶ — تکمیل تنظیمات
## هدف
تکمیل امکانات عمومی برنامه.
## کارها
- [ ]  تنظیمات (به‌جز پروفایل و سینک)
- [ ]  Event Tracking موارد اضافه شده
- [ ]  انتشار نسخه 5.5.0
 
 
# 💵 فاز ۷ — مدیریت دارایی
## هدف
افزایش ارزش واقعی برنامه.
## کارها
- [ ]  نرخ ارز
- [ ]  قیمت طلا
- [ ]  تبدیل ارز
- [ ]  مدیریت دارایی
- [ ] Event Tracking موارد اضافه شده
- [ ]  انتشار نسخه 6.0.0

# 🧪 فاز ۸ — تست مدیریت دارایی
## هدف
اطمینان از کیفیت امکانات جدید.
## کارها
- [ ]  تست کامل نرخ ارز
- [ ]  تست کامل طلا
- [ ]  تست کامل دارایی
- [ ]  تست کامل تبدیل ارز
- [ ] Event Tracking موارد اضافه شده
- [ ]  انتشار نسخه 6.1.0
 
# 👥 فاز ۹ — بتای بسته (Closed Beta)
## هدف
گرفتن بازخورد واقعی.
## کارها
- [ ]  انتشار برای ۳۰ تا ۱۰۰ کاربر
- [ ]  بررسی Crashها
- [ ]  بررسی Analytics
- [ ]  بررسی رفتار کاربران
- [ ]  اولویت‌بندی مشکلات
- [ ] رفع باگ ها و تست مواردی که توی اقدام هست مثل تست ماشین حساب و درست کردن ساعت در افزودن تراکنش
- [ ] معرفی به دوست و آشنا
- [ ] Event Tracking موارد اضافه شده
 
# 📈 فاز ۱۰ — بازاریابی اولیه
## هدف
جذب اولین کاربران واقعی.
## کارها
- [ ]  انتشار در کافه‌بازار
- [ ]  انتشار در مایکت
- [ ]  انتشار در Google Play
- [ ]  ساخت Landing Page
- [ ]  تولید محتوای اولیه
- [ ]  جذب اولین ۵۰۰ کاربر
- [ ] معرفی در گروه ها و کانال های تلگرامی
- [ ] Event Tracking موارد اضافه شده

❗ اینجا هنوز تبلیغات سنگین انجام نمی‌دهی؛ فقط جذب کاربران اولیه و یادگیری از رفتار آن‌ها.

# 🎯 فاز ۱۱ — اهداف مالی و دستاوردها
## هدف
افزایش تعامل کاربران.
## کارها
- [ ]  اهداف مالی
- [ ]  دستاوردها
- [ ] Event Tracking موارد اضافه شده
- [ ]  انتشار نسخه
 
# ✅ فاز ۱۲ — تست کامل اهداف مالی
## هدف
پایداری امکانات جدید.
## کارها
- [ ]  تست کامل اهداف مالی
- [ ]  تست کامل دستاوردها
- [ ] Event Tracking موارد اضافه شده
- [ ]  انتشار نسخه
 
 
# 💬 فاز ۱۳ — پشتیبانی
## هدف
آماده شدن برای کاربران بیشتر.
## کارها
- [ ]  رویدادهای مهم
- [ ]  سوالات متداول
- [ ]  سیستم پشتیبانی
- [ ] Event Tracking موارد اضافه شده
- [ ]  انتشار نسخه
 
# 🤖 فاز ۱۴ — مشاور هوشمند
## هدف
ایجاد مزیت رقابتی.
## کارها
- [ ]  اضافه کردن مشاور هوشمند
- [ ] Event Tracking موارد اضافه شده
- [ ]  انتشار نسخه
 
# ☁️ فاز ۱۵ — پروفایل و سینک
## هدف
تبدیل برنامه به محصول کامل.
## کارها
- [ ]  پروفایل
- [ ]  سینک و اکسپورت
- [ ] Event Tracking موارد اضافه شده
- [ ]  انتشار نسخه
 
# 🎨 فاز ۱۶ — پولیش نهایی
## هدف
آماده شدن برای رشد بزرگ.
## کارها
- [ ]  تکمیل Onboarding
- [ ]  رفع باگ‌های گزارش‌شده
- [ ]  بهبود Performance
- [ ]  بهبود UX
- [ ] Event Tracking موارد اضافه شده
- [ ]  انتشار نسخه پایدار
 
# 🚀 فاز ۱۷ — رشد محصول
## هدف
توسعه بر اساس داده.
## کارها
- [ ]  بررسی Analytics
- [ ]  بررسی Retention
- [ ]  بررسی Conversion
- [ ]  بررسی Feature Usage
- [ ] Event Tracking موارد اضافه شده
- [ ]  اولویت‌بندی Featureهای بعدی
- [ ]  برنامه‌ریزی نسخه‌های آینده

## تکمیل دقیق فازهای ۴ تا ۱۷

### Phase 4 / 5.0.0 — کیفیت هسته

علاوه بر checklist قفل‌شده:

- تست matrix برای Add/Edit/Delete/Transfer و fee؛
- تست idempotency برای پرداخت قسط، تسویه بدهی، ثبت/تغییر وضعیت چک و هزینه ثابت؛
- تست بودجه روزانه/هفتگی/ماهانه/سالانه و حذف نرم؛
- fixture دیتابیس تمام نسخه‌های منتشرشده؛
- تست restore روی backup ناقص/خراب و قطع عملیات؛
- قرارداد Money با `Long`، currency و checked arithmetic؛
- Android smoke build و حداقل سناریوی E2E آفلاین.

### Phase 5 / 5.1.0 — Observability امن

- common interfaces؛ Firebase فقط از platform adapter؛
- Android production adapter؛ iOS/Web فقط پس از runnable host/bundle و تا آن زمان capability صریح؛ Desktop با logger/crash backend سازگار یا unsupported؛
- consent و opt-out؛
- taxonomy نسخه‌دار؛
- `first_open`، `app_update` و `app_remove` را دوباره custom log نکنید اگر SDK آن‌ها را خودکار می‌فرستد؛ `app_remove` فقط Android است؛
- نرخ بازگشت یک event نیست، metric cohort از sessionهاست؛
- هیچ مبلغ، نام، توضیح، شماره حساب، متن SMS یا محتوای AI ارسال نشود.

### Phase 6 / 5.5.0 — تنظیمات + پوسته تطبیقی

- تنظیم زبان/اعداد/تقویم/واحد پول به‌صورت مفاهیم مستقل؛
- semantic tokens به‌جای جایگزینی کور تمام `dp`ها با `LocalSpacing`؛
- migration snackbarهای feature-level به Event Bus؛ Lock Gate در صورت نیاز host محدود خودش را نگه دارد؛
- رجیستری Feature و مدیریت user-visible tools؛
- foundation فیلترهای ذخیره‌شونده؛ rollout عمومی پس از تست Beta؛
- adaptive navigation و settings/profile two-pane.

### Phase 7–8 / 6.0.0–6.1.0 — دارایی

- provider abstraction، cache، timestamp و منبع نرخ؛
- Money/Decimal بدون `Double` مالی؛
- حالت stale/offline و هشدار زمان آخرین به‌روزرسانی؛
- دارایی، معامله و valuation از هم جدا؛
- تست rounding، تغییر ارز پایه و timezone؛
- خرید مستقیم دارایی در این فاز نیست.

### Phase 9 — Closed Beta

- cohortهای جدا برای build/platform/feature gate؛
- crash-free users، ANR، activation، D1/D7 retention و adoption؛
- channel داخلی برای feedback با شناسه build، بدون پیوست مالی پیش‌فرض؛
- reconciliation گزارش مانده؛
- validation فیلترهای ذخیره‌شونده و hardening quick-add notification/widget موجود؛
- matrix دستگاه: گوشی کوچک/بزرگ، tablet portrait/landscape، Desktop resize، Web.

### Phase 10 — Go-to-market

- Store assets بر مبنای پیام روشن: ثبت سریع، کنترل دارایی/تعهد، privacy و چندسکویی؛
- ASO آزمایشی و screenshotهای واقعی؛
- سازوکار in-app update هم‌زمان اولین Store rollout؛
- attribution contract از Phase 5، pilot یکتانت/Adivery در Phase 10 با consent و budget cap، optimization درآمدی در Phase 20؛
- قبل از تبلیغ سنگین، D7 retention و crash-free target تعریف شود.

### Phase 11–14

- goals/achievements/FAQ/news/AI موجود ابتدا inventory و gate شوند؛
- AI باید opt-in، redaction، secure secret storage و توضیح «مشاوره مالی حرفه‌ای نیست» داشته باشد؛
- پیشنهاد سرمایه‌گذاری فقط آموزشی/سناریویی تا قبل از بررسی حقوقی، نه توصیه شخصی قطعی.

### Phase 15

- Sync فعلی قابل rollout نیست؛ اجرای کامل [006-secure-sync-and-collaboration.md](006-secure-sync-and-collaboration.md) پیش‌نیاز است.
- delete cloud user، export، retention و recovery جزو Definition of Done پروفایل هستند.

### Phase 16–17

- polish بر اساس داده و task completion، نه صرفاً تغییر ظاهر؛
- cleanup ماژول‌ها در trancheهای کوچک با تست characterization و طبق
  [008-architecture-quality-backlog.md](008-architecture-quality-backlog.md)؛
- استخراج library فقط وقتی API پایدار، چند مصرف‌کننده واقعی و pipeline انتشار وجود دارد.

## فازهای پیشنهادی بعد از فاز ۱۷

### فاز ۱۸ — انتشار واقعی چندسکویی و بومی‌سازی

**هدف:** تبدیل «قابل کامپایل بودن مشترک» به محصول قابل نصب و پشتیبانی.

- iOS host/Xcode project، signing، notifications/share/image storage و App Store checklist؛
- Windows installer، اصلاح main class، update channel و file integration؛
- Web production bundle، persistence/migration، responsive shell و PWA/offline policy؛
- English + Gregorian + LTR؛ سپس چارچوب چندزبانه؛
- صفحه رسمی دانلود نسخه‌های Web/Windows/iOS؛
- تست Compact/Medium/Expanded/Large/XLarge.

### فاز ۱۹ — همکاری مالی

**هدف:** family/shared workspace پس از Sync امن.

- حساب مشترک خانواده؛
- دنگ و سفر؛
- تن‌خواه شرکت؛
- بدهی/طلب اشتراکی و invitation؛
- roles، audit log، conflict resolution و leave/delete workspace؛
- person photo با attachment امن.

### فاز ۲۰ — درآمد و رشد پایدار

**هدف:** monetization بدون تخریب اعتماد.

- entitlement و subscription؛
- free tier شفاف؛
- referral و اشتراک رایگان با قواعد ضدتقلب؛
- in-app update alert؛
- تبلیغات محدود و opt-out/premium؛
- attribution با Adivery/متریکس و campaign taxonomy؛
- بررسی market size/TAM-SAM-SOM بعد از داده Beta.

### فاز ۲۱ — داده ورودی، خروجی و Integration

**هدف:** FinTrack به هاب مالی کاربر تبدیل شود.

- custom saved filters مشابه custom list؛
- copy transaction و create-from-copy؛
- share transaction با masking؛
- report export با filter snapshot؛
- import adapter و mapping UI؛
- API با OAuth/scope/rate limit/audit؛
- بررسی TickTick/منابع دیگر فقط با API رسمی و رضایت کاربر؛
- balance-after-transaction به‌صورت ledger running balance.

### فاز ۲۲ — افزونه‌های Android و کتابخانه‌ها

**هدف:** capture سریع و استخراج دارایی‌های reusable.

- quick actions notification موجود: تست و hardening؛
- Glance widget موجود: توسعه محدود به ۲–۳ widget با ارزش بالا، نه کپی همه dashboard cards؛
- widget ثبت سریع، خلاصه ماه و سررسیدها؛
- Message Bar و Jalali Calendar موجود: harden/extract پس از تثبیت API؛
- Number-to-Words جدید: ابتدا design/build و فقط پس از مصرف‌کننده دوم library شود؛
- تقویم مستقل از UI و با Gregorian/Jalali strategy.

### فاز ۲۳ — تحقیق، محتوا و برند فنی

**هدف:** ایجاد مزیت بازاری و اعتبار فنی.

- بنچمارک دوره‌ای ایران/جهان؛
- design study از Figma با رعایت حق مؤلف؛
- استخراج داستان migration به KMP از تاریخ Git؛
- مقاله Medium، متن LinkedIn و مستند معماری؛
- store screenshot experiments و content calendar.

### فاز ۲۴ — اکوسیستم و B2B

**هدف:** ارزیابی محصول‌های مجاور بدون آلوده‌کردن هسته.

- discovery برای اپ صندوق/فروشگاه/FinTrack؛
- API contract و tenant isolation؛
- cashbox/petty-cash pilot؛
- تصمیم build/buy/partner؛
- پروژه خرید مستقیم ارز دیجیتال/طلا/بورس تنها پس از بررسی مجوز، KYC/AML، custody، liability و شریک دارای مجوز.

## نقشه تمام ایده‌های اضافه به برنامه

| ایده | محل برنامه | تصمیم |
|---|---|---|
| Rive/Rivium trace رفتار کاربر | Phase 5 discovery | نام ابزار و privacy ابتدا اعتبارسنجی؛ قرارداد vendor-neutral |
| quick transaction notification | Phase 22 | موجود است؛ harden و اندازه‌گیری |
| dashboard Android widgets | Phase 22 | فقط widgetهای پرتکرار و قابل نگهداری |
| Cloud و قابلیت خاص | Phase 15/19 | فقط روی Sync امن |
| تن‌خواه و دنگ | Phase 19 | workspace مشترک |
| بدهی/طلب حرفه‌ای و اشتراک | Phase 19 | بعد از identity/sync |
| صندوق + فروشگاه + FinTrack | Phase 24 | محصول/tenant جدا |
| market cap | Phase 20 | پس از Beta data |
| referral/free subscription | Phase 20 | entitlement + anti-fraud |
| update alert | Phase 20 | platform-aware |
| Metrics/Adivery/Firebase | Phase 5/10/20 | consent و attribution schema |
| snackbar event bus | Phase 6 | host امنیتی Lock مستثنا در صورت نیاز |
| LocalSpacing | Phase 6/16 | semantic-token migration، نه جایگزینی کور |
| deep module cleanup | Phase 16 | tranche کوچک با characterization test |
| transfer fee display | R0/Phase 4 | ابتدا semantics و migration |
| transaction date/time | Phase 4/6 | timestamp واحد؛ date change زمان را پاک نکند |
| server backup/sync/profile/Drive | Phase 15 | مطابق طرح امن Sync |
| دو/چندزبانه + English/Gregorian | Phase 18 | locale/calendar/number مستقل |
| تبلیغات/اشتراک/Yektanet | Phase 20 | بعد از retention |
| Web/Windows/iOS/download hub | Phase 18 | release-ready artifacts |
| person photo | Phase 19 | attachment ID امن |
| آموزش/بورس/converter/tools | Phase 7/13/14 | education/gate؛ معامله مستقیم جدا |
| FAQ/news | Phase 13 | کد موجود را inventory کنید |
| خرید مستقیم دارایی | Phase 24 discovery | فعلاً defer حقوقی |
| پیشنهاد سرمایه‌گذاری | Phase 14 | آموزشی، explainable و محدود |
| balance after transaction | Phase 21 | derived running balance |
| KMP Git story/Medium/LinkedIn | Phase 23 | از Git evidence |
| DB migrations | R0/Phase 4 | P0 |
| Message Bar/Jalali/calendar library | Phase 22 | پس از تثبیت و مصرف‌کننده دوم |
| copy/share transaction/report filters | Phase 21 | privacy-aware |
| custom theme palette/glass | Phase 6/16 | قابلیت موجود؛ harden |
| API for other apps | Phase 21/24 | scope/auth/audit |
| benchmark/Figma/manual app review | مداوم + Phase 23 | research repository |
| Play Store | Phase 10 | همان برنامه فعلی |
| number to words | Phase 22 | locale-aware |
| delete cloud user | Phase 15 | DoD اجباری |
| TickTick financial data | Phase 21 discovery | فقط API رسمی/قانونی |
| responsive large screens | Track B + Phase 18 | از Phase 4 شروع |
| custom filter lists | Phase 21 | saved query model |
| Feature Toggle برای تمام قابلیت‌های جدید | Track C + Definition of Done همه فازها | Availability، Capability، Entitlement/Rollout و User Visibility اجباری |

## تصمیم‌های لازم مالک محصول

ایده‌های زیر حذف نشده‌اند؛ پیشنهاد مهندسی اجرای مرحله‌ای یا تعویق است. پیش از تبدیل به Task اجرایی،
مالک محصول یکی از گزینه‌های «حفظ دقیق»، «اجرای مرحله‌ای» یا «رد» را تأیید کند.

| ایده اصلی | پیشنهاد فعلی | تصمیم مالک |
|---|---|---|
| همه dashboard widgetها به Android widget تبدیل شوند | ابتدا quick-add و summary/due؛ سپس بر اساس usage | باز |
| حذف همه SnackbarHostها | حذف feature-level؛ حفظ host مستقل Lock فقط اگر global host پشت gate است | باز |
| همه spacing/sizeها از `LocalSpacing` | semantic token بر اساس نقش، نه یک scale واحد | باز |
| خرید مستقیم ارز/طلا/بورس | discovery حقوقی/KYC/custody و شریک مجاز؛ فعلاً خارج core | باز |
| پیشنهاد سرمایه‌گذاری | آموزشی و explainable؛ توصیه شخصی قطعی فعلاً تعویق | باز |

## سؤال‌های باز محصول

- «Rivium/Rive trace» دقیقاً نام کدام ابزار یا SDK است؟
- «market cap پروژه» یعنی TAM/SAM/SOM بازار، valuation محصول، یا هر دو؟
- «قابلیت‌های خاص Cloud» کدام use caseها هستند؟
- «ابزار آموزش و بورس» محتوای آموزشی است، data dashboard است یا معامله؟
- «اخبار و امکانات اپ» یعنی news feed، changelog/What's New یا هر دو؟

تا پاسخ مالک محصول، عامل نباید برای این پنج مورد implementation بسازد.

## وابستگی و گیت فازهای ۱۸ تا ۲۴

| Epic | وابستگی | گیت خروج | پلن اجرایی |
|---|---|---|---|
| 18A iOS release | build baseline، capability registry | host launch + DB reopen + TestFlight checklist | plan 004 |
| 18B Desktop/Web release | build baseline، adaptive shell | installer/bundle + persistence smoke | plan 004 |
| 18C Locale | formatter/calendar policy | English/LTR/Gregorian matrix | plan 004 |
| 19A Sync identity/workspace | Phase 15 امن | auth/ownership/conflict SLO | plan 006 |
| 19B Family/Dang/Petty Cash | 19A | invariant + role/audit tests | plan 006 |
| 20 Monetization/Growth | entitlement + consent + Beta retention | pricing/referral/attribution metrics | plan 003/005 |
| 21A Saved views/copy/share/export | filter foundation | privacy + snapshot consistency | Task Card مستقل |
| 21B Public API/import | identity/workspace | OAuth scope/rate/audit | Task Card مستقل |
| 22 Android surfaces | Phase 9 usage data | widget reliability/adoption | Task Card مستقل |
| 22 Libraries | API پایدار + مصرف‌کننده دوم | package tests/docs/versioning | Task Card مستقل |
| 23 Research/content | موازی و مستقل | evidence/provenance/editorial review | Task Card مستقل |
| 24 B2B/trading discovery | Phase 17 data + legal review | go/no-go decision، نه build خودکار | Task Card مستقل |

## معماری هدف

```text
Compose Screen (stateless content + adaptive container)
  -> Feature MVI (State / Intent / Effect)
  -> Domain command/query
  -> Repository transaction boundary
  -> Data source / SQLDelight

Cross-cutting contracts:
  FeatureRegistry | PlatformCapabilities | Telemetry | LocaleSettings
  Money/Ledger    | AttachmentStore      | Auth/Workspace/Sync
```

قواعد:

- Domain هیچ Compose/Firebase/Android dependency ندارد.
- یک عملیات کسب‌وکاری چندجدولی فقط یک transaction boundary دارد.
- `Source.balance` در نهایت cache قابل rebuild از ledger است.
- شناسه Sync global و immutable است؛ ID محلی دیتابیس کلید داخلی می‌ماند.
- UI از capability نتیجه‌دار استفاده می‌کند؛ no-op موفق ممنوع است.
- adaptive decision بر اساس available window است، نه `isTablet`.
- route registration و navigation item از یک registry خوانده می‌شوند.

## ظرفیت پیشنهادی سه موج نخست

بازه‌های زیر planning envelope هستند، نه تعهد تاریخ. پس از spike ساخت، fixture inventory و اندازه‌گیری
configuration time باید با confidence range بازبرآورد شوند. فرض اولیه: تمرکز تقریباً تمام‌وقت یک
توسعه‌دهنده باتجربه، دستگاه تست و runner macOS؛ تاریخ انتشار نباید معیارهای گیت را حذف کند.

### موج ۱ — حدود ۲ تا ۴ هفته پس از baseline

- اجرای R0 و 4.5.1؛
- freeze فیچر؛
- backup نسخه کاربران آزمایشی؛
- ledger/migration regression suite؛
- rotate secret و gate کردن Sync/AI.

### موج ۲ — حدود ۳ تا ۵ هفته

- تکمیل Phase 4 و 5.0.0؛
- CI اولیه؛
- obligation idempotency؛
- بودجه period-aware؛
- Feature Registry v1.

### موج ۳ — حدود ۳ تا ۵ هفته

- Phase 5 و 5.1.0؛
- telemetry privacy contract؛
- Crashlytics Android/iOS؛
- Performance Android/iOS/Web؛
- event taxonomy و dashboard.

### موج ۴ — حدود ۴ تا ۸ هفته

- Phase 6 و adaptive shell؛
- locale foundation؛
- settings inventory؛
- Dashboard/Tools grid و transaction master-detail؛
- شروع asset provider hardening.

## شاخص‌های تصمیم

- Reliability: crash-free users، ANR، restore success، migration success، balance audit mismatch.
- Activation: first account → first transaction → second-day return.
- Engagement: weekly active transaction creators، report use، budget/obligation adoption.
- Retention: D1/D7/D30 cohort، نه event دست‌ساز «retention».
- Performance: cold start p50/p95، screen-ready p95، slow DB query count، Web LCP/INP.
- Quality: escaped defects، flaky tests، build duration، release rollback count.
- Business: referral conversion، paid conversion، churn، CAC فقط پس از attribution قابل اعتماد.

## محدودیت ممیزی

- آمار ساختار با `rg --files`، شمارش فایل‌های `*.kt` و `git ls-files` قابل بازتولید است؛ build scan رسمی هنوز وجود ندارد.
- build task باریک Android در Gradle شناسایی شد، اما compile کامل به‌دلیل ماندن طولانی در configuration در بازه ۱۰ دقیقه تمام نشد؛ این خود یک مسئله DX است ولی شکست compile اثبات‌شده نیست.
- تست دستی روی دستگاه، Xcode، مرورگرهای متعدد و Desktop installer انجام نشده است.
- فایل ارجاع‌شده `RTK.md` و سند اشاره‌شده `HANDOFF-v2.md` در workspace پیدا نشدند.
- آمار بازار snapshot زمانی است و باید پیش از تصمیم بازاری دوباره برداشت شود.

