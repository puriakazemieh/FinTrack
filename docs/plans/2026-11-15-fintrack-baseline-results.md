# نتیجهٔ آغاز اجرای زمان‌بندی‌شده

۱۵ نوامبر ۲۰۲۶، ساعت ۱۸:۰۵ به وقت تهران · مبنای checkout `23c21e5388408e1e8c45cd40a22f31629e84ff99` · شاخهٔ develop.

## محیط و آزمون پایه

AVD موجود Pixel_9_Pro_XL با SDK در D:/Android/sdk اجرا شد. serial آن emulator-5554 و API آن 35 است. فرمان sys.boot_completed مقدار ۱ و pm path android مسیر framework-res.apk را برگرداند. اپ روی این امولاتور نصب نبود. تم و زبان داخل اپ هنوز قابل ثبت نیست.

بیلد اولیه با `gradlew.bat :app:assembleDirectDebug :core:domain:jvmTest --console=plain` به دریافت AAPT2 و migration-env وابسته شد و پس از ۷ دقیقه و ۴۷ ثانیه شکست خورد. دریافت Java از Maven Central با connection timeout روبه‌رو شد. curl اولیه نیز revocation offline گزارش کرد. دریافت دوباره با اعتبارسنجی TLS و گزینهٔ ssl-revoke-best-effort، برای migration-env پس از ادامهٔ دانلود موفق شد. SHA1 فایل برابر 9859de1b553a764ddfefb275a626ff369dd014fd و اندازهٔ آن 15302951 بایت است.

اجرای مستقل `gradlew.bat :core:domain:jvmTest --offline --console=plain` موفق شد. ۱۴ تست، صفر failure و صفر error ثبت شدند. BudgetTest سه تست، CheckUseCaseTest یک تست، DebtUseCaseTest یک تست، IdempotencyTest سه تست، InstallmentUseCaseTest یک تست و TransactionMatrixTest پنج تست داشتند.

تلاش offline با AAPT2 نصب‌شده در build-tools/36.0.0 مشکل AAPT2 را عبور داد. Gradle فایل migration-env را با کپی مستقیم به کش قابل resolve نمی‌دانست. init script موقت خارج repo، همان jar تأییدشده را در configuration مخصوص migration-env قرار داد. مرحلهٔ بعد با دو وابستگی cacheنشدهٔ sqlite-3-18-dialect:2.2.1 و kotlin-reflect:2.1.21 متوقف شد. این init script و override تغییری به تنظیمات repo نداده‌اند.

لاگ‌های بیلد اولیه، تست دامنه و سه تلاش بازیابی در `.artifacts/fintrack-baseline-2026-11-15/` نگهداری شده‌اند. نتایج XML آزمون در core/domain/build/test-results/jvmTest هستند. هنوز APK مبنا ساخته و نصب نشده و دادهٔ UI و ماتریس اعلان‌ها اجرا نشده‌اند. BASE-01 و BASE-02 باز می‌مانند.

## موجودی اعلان‌ها برای ادامهٔ N-01

| نوع | تولیدکننده و تحویل فعلی | مقصد/اقدام و شکاف |
|---|---|---|
| SMS | SmsReceiver → showStickyNotification، کانال sms_detection | URI داشبورد با smsDraftId؛ مدیریت/نادیده‌گرفتن. LOW و ongoing. |
| افزودن سریع | App و NotificationSettingsViewModel → showQuickAddNotification، quick_add | فرم خالی؛ LOW و ongoing. |
| بودجه | AddTransactionUseCase → showBudgetAlert → showNotification، budget_reminders | مسیر عمومی به داشبورد می‌رود. |
| قسط | Add/Update/MarkPaid در InstallmentUseCases → Scheduler → Worker | رشتهٔ installment_reminders با کانال ساخته‌شدهٔ installment_reminders_v4 فرق دارد؛ extraId قسط منتقل نمی‌شود. Worker مسیر عمومی نمایش را می‌خواند. |
| چک | CheckUseCases → Scheduler → Worker، cheque_reminders | extraId ارسال می‌شود؛ مدیریت به فهرست چک و نادیده‌گرفتن. |
| بدهی و طلب | DebtUseCases → Scheduler → Worker، debt_reminders | extraId ارسال می‌شود؛ مدیریت به فهرست بدهی و نادیده‌گرفتن. |
| خرید | ShoppingViewModel → Scheduler → Worker، shopping_reminders | extraId ارسال نمی‌شود؛ مسیر عمومی به داشبورد. |
| یادداشت | NoteEditViewModel → Scheduler → Worker، note_reminders | extraId ارسال نمی‌شود؛ مسیر عمومی به داشبورد. |
| هزینهٔ ثابت | AddFixedExpenseUseCase → Scheduler → Worker | کانال قسط؛ بدون extraId و مقصد مستقل. |

Scheduler از OneTimeWorkRequest با تأخیر و ExistingWorkPolicy.REPLACE استفاده می‌کند. زمان گذشته را نادیده می‌گیرد. NotificationWorker متد عمومی showNotification را فراخوانی می‌کند. متد تخصصی اعلان قسط اکشن پرداخت دارد ولی مسیر Scheduler به آن متصل نیست. اکشن‌های بازکردن صفحه از Receiver به startActivity می‌رسند؛ محدودیت trampoline باید در N-05 پوشش داده شود. N-01 تا ثبت مسیرهای لغو، ماتریس UI و پذیرش محرک‌های واقعی باز می‌ماند.

## ادامه

ابتدا دسترسی Gradle به وابستگی‌های مفقود یا repository محلی موقتِ حاوی artifactها و metadata معتبر آماده شود. سپس بیلد مبنا، نصب روی emulator-5554 و ایجاد دادهٔ مصنوعی BASE-01 انجام شود. آزمون‌های اعلان، بنر، صدا و سوایپ هنوز شاهد اجرایی ندارند.

## تلاش مجدد و رفع مانع بیلد

۱۵ نوامبر ۲۰۲۶ · دریافت sqlite-3-18-dialect:2.2.1 و kotlin-reflect:2.1.21 از Maven Central این بار موفق شد. checksum هر سه jar با SHA1 منتشرشدهٔ همان مخزن تطبیق داده شد. dialect برابر 8171c978caa0133bad7a00069361f98ffa0ab3df و reflect برابر 4929da533b3f04a101e4bb9ae0ee51a1d8107614 است.

jarها به همراه POM اصلی در مخزن محلی آزمایشی قرار گرفتند. init script فقط مخزن پروژهٔ core:database را پس از configuration با این سه ماژول تکمیل می‌کند؛ برای reflect فقط نسخهٔ 2.1.21 فیلتر شده تا نسخهٔ موردنیاز کامپایلر نیز از مخزن اصلی قابل resolve باشد. AAPT2 از نسخهٔ نصب‌شدهٔ SDK در build-tools/36.0.0 گرفته شد.

بیلد DirectDebug در ۲۲ ثانیه با موفقیت تمام شد؛ ۵۹۱ task شامل ۸۵ اجرا و ۵۰۶ up-to-date. APK در app/build/outputs/apk/direct/debug/app-direct-debug.apk ساخته شد. نصب با adb روی emulator-5554 موفق و شروع سرد MainActivity با Status: ok و زمان 3373ms ثبت شد. تصویر صفحهٔ خوشامد فارسی با تم تیره مشاهده شد؛ دکمهٔ «بعدی» و مراحل آنبوردینگ دیده می‌شوند.

برای تکرار همین بیلد در این checkout:

```powershell
.\gradlew.bat :app:assembleDirectDebug --offline "-Pandroid.aapt2FromMavenOverride=D:\Android\sdk\build-tools\36.0.0\aapt2.exe" --init-script .artifacts/fintrack-baseline-2026-11-15/fintrack-maven-retry.init.gradle --console=plain
```

مخزن محلی، init script، تصویر start.png و لاگ fintrack-dependency-retry-build.log در .artifacts/fintrack-baseline-2026-11-15 نگهداری شده‌اند. برای بازتولید، این فایل‌های محلی باید روی دستگاه موجود باشند. اجرای معمول Gradle بدون این script هنوز تأیید نشده است. BASE-01 برای ساخت دادهٔ مصنوعی و بازتولید ایرادها، و BASE-02 برای سناریوی ارتقا همچنان باز هستند. قدم بعد تکمیل آنبوردینگ روی امولاتور و آماده‌سازی دادهٔ تست است.

بازآزمایی با init script منتقل‌شده به پوشهٔ شواهد، این بار به قفل caches-jvm در core:data-contract برخورد کرد. اجرای بعدی با یک worker، compiler in-process و incremental=false از دریافت وابستگی‌ها عبور کرد اما در composeApp به unresolved reference برای dataModule رسید. هر دو لاگ fintrack-repro-check.log و fintrack-repro-check2.log ذخیره شدند. بنابراین موفقیت نصب و اجرای APK نخست تأیید شده است؛ تکرارپذیری بیلد کامل پس از کامپایل مجدد به بررسی این خطای مستقل نیاز دارد. نسخه‌های کد و تنظیمات repo برای حل آن تغییر نکردند.
