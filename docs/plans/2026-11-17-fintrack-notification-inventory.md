# سرشماری مسیرهای اعلان و استقلال کارت‌ها

مبنای بررسی: `cb75db5c4b2d9c5313ac1004a20211c734e52509`، ۱۷ نوامبر ۲۰۲۶. این فایل بخش اجرایی N-01 و پیش‌نیاز N-05/N-06/N-07 است. وضعیت آزمون و commit اصلاح استقلال کارت‌ها در گزارش پذیرش جدا ثبت می‌شود.

## مسیر واقعی تولید

تمام نام فایل‌های جدول در مخزن FinTrack هستند. `CHANNEL_*` ثابت‌های `core/domain/.../notification/NotificationManager.kt` هستند. برای کانالِ پیش‌فرض قدیمی، `postingChannelId` ممکن است پسوند `_alerts_v1` بسازد؛ انتخاب دستی سکوت یا مسدودسازی نباید دور زده شود.

| نوع | تولیدکننده و مسیر تحویل | کانال منطقی | شناسهٔ تولید و رکورد | بدنه و اکشن‌های فعلی |
|---|---|---|---|---|
| پیامک بانکی | `SmsReceiver.onReceive` → تشخیص/ذخیرهٔ SmsDraft → `showStickyNotification` | `sms_detection` | عدد کارت از `draftId.toInt()`؛ شناسهٔ کامل draft جدا موجود است | بدنه و ویرایش: `fintrack://dashboard?showAddTransaction=true&smsDraftId=...`؛ نادیده‌گرفتن: `SmsActionReceiver`، فقط مصرف همان draft؛ سوایپ ثبت مالی نمی‌کند |
| میان‌بر ثبت | `composeApp/App.kt` هنگام اجرا؛ تغییر تنظیم در `NotificationSettingsViewModel` → `showQuickAddNotification` | `quick_add` | عدد ثابت 9001؛ رکورد مالی ندارد | بدنه و اکشن، فرم خالی تراکنش؛ ردکردن، پرچم عدم نمایش مجدد را ثبت می‌کند؛ فعال‌سازی صریح می‌تواند دوباره آن را نشان دهد |
| بودجه | `AddTransactionUseCase.checkBudgetThreshold` → `showBudgetAlert` → `showNotification` | `budget_reminders` | عدد کارت از categoryId؛ **budgetId هنوز منتقل نمی‌شود** | فقط ورود به فهرست `fintrack://budget`؛ انتخاب بودجهٔ دقیق و رفتار عبور از آستانه در N-05/N-07 باقی است |
| قسط | Add/Update/MarkPaid در `InstallmentUseCases.kt` → Scheduler → WorkManager → `NotificationWorker` → `showNotification` | `installment_reminders_v4` | نام کار `installment_ID`؛ عدد کارت hash همین نام؛ `extraId=installment.id` | مسیر Worker فعلی فقط فهرست `fintrack://installment` را باز می‌کند؛ متد جداگانهٔ `showInstallmentNotification` دارای ثبت پرداخت/نادیده‌گرفتن است، اما تولیدکنندهٔ واقعی Worker هنوز از آن استفاده نمی‌کند |
| چک | Add/Update/Delete در `CheckUseCases.kt` → Scheduler/Worker → `showNotification` | `cheque_reminders` | `check_ID`، hash نام کار و extraId رکورد | بدنه و مدیریت: فهرست `fintrack://check`؛ نادیده‌گرفتن فقط کارت را می‌بندد؛ مقصد رکورد دقیق هنوز لازم است |
| بدهی و طلب | Add/Update در `DebtUseCases.kt` → Scheduler/Worker → `showNotification` | `debt_reminders` | `debt_ID`، hash نام کار و extraId رکورد | بدنه و مدیریت: فهرست `fintrack://debt`؛ نادیده‌گرفتن کارت؛ هر دو جهت پرداختنی و دریافتنی باید مستقل QA شوند |
| خرید | `ShoppingViewModel.scheduleReminder` → Scheduler/Worker → `showNotification` | `shopping_reminders` | `shopping_ID`، hash نام کار و extraId رکورد | بدنه: فهرست `fintrack://shopping`؛ پس از تیک‌زدن یا حذف، کار یادآور لغو می‌شود؛ انتخاب آیتم دقیق باز است |
| یادداشت | `NoteEditViewModel.syncReminder` → Scheduler/Worker → `showNotification` | `note_reminders` | `note_ID` و hash آن؛ **extraId هنوز ارسال نمی‌شود** | بدنه: فهرست `fintrack://notes`؛ حمل noteId، قفل یادداشت و محتوای حساس باید در N-05/N-07 اصلاح و تست شوند |
| هزینهٔ ثابت | `AddFixedExpenseUseCase` → Scheduler/Worker → `showNotification` | `fixed_expense_reminders` | `fixed_expense_ID`، hash نام کار و extraId رکورد | بدنه: فهرست `fintrack://fixed-expense`؛ ویرایش زمان‌بندی و ثبت خودکار، مسیرهای FIX-01/02 هستند |

## زیرساخت و مرزهای تحلیل

- `AndroidNotificationScheduler`، زمان محلی را به delay تبدیل می‌کند؛ موعد گذشته را صف نمی‌کند. کار یکتا با نام رشته‌ای و سیاست REPLACE ایجاد می‌شود. لغو، همان نام کار را به WorkManager می‌دهد.
- `NotificationWorker` عنوان، متن، channelId، notificationId و extraId را از Data می‌خواند. ورودی قدیمی `installment_reminders` را به `installment_reminders_v4` می‌برد و در همهٔ موارد فعلاً ناشر عمومی را صدا می‌زند. حمل extraId به‌تنهایی به معنی بازشدن رکورد درست نیست.
- Koin، رابط‌های NotificationManager و NotificationScheduler را در `notificationPlatformModule` به پیاده‌سازی Android وصل می‌کند. Worker و Receiverها از Koin استفاده می‌کنند؛ نبود caller در نمودار، دلیل بی‌استفاده بودن callback نیست.
- Receiver عمومی و `SmsActionReceiver` در Manifest ثبت و غیرقابل‌صدازدن از برنامهٔ دیگر هستند؛ `SmsReceiver` ورودی SMS سیستم را دریافت می‌کند. هویت اکشن و PendingIntent باید جدا از extras باشد. Intent دارای data باید گیرندهٔ صریح مناسب داشته باشد تا delivery به فیلتر فاقد data وابسته نماند.
- `MainActivity.handleIntent` برای شروع سرد و `onNewIntent` برای اپ باز اجرا می‌شوند. تحویل مسیر به میزبان مشترک در اصلاح SMS-03a انجام شده است؛ بستن کارت در Activity باید شناسهٔ همان کارت را نگه دارد.
- پیاده‌سازی‌های فعلی JVM/JS/iOS اعلان عملاً no-op هستند؛ آزمون Android، تأیید اعلان روی آن سکوها نیست.

## بازتولید نقص مشترک در مبنا

یک آزمون Android واقعی با `android.app.Instrumentation` به پروژه اضافه شد؛ وابستگی جدیدی دانلود نمی‌کند و در APK تولید قرار نمی‌گیرد. پس از آماده‌شدن Koin خود برنامه، ناشر واقعی را با دادهٔ مصنوعی صدا می‌زند. فقط شناسه‌های آزمایشیِ از پیش آزاد را می‌گیرد و در پایان همان کارت‌ها و تنظیم موقت خود را برمی‌گرداند.

روی DirectDebug/API 35، سه آزمون مبنا قرمز شدند؛ `notification-identity/baseline-red-ready.log`:

1. ۹ نوع با عدد مشترک 9001: انتظار ۹ کارت، نتیجهٔ واقعی فقط یک کارت.
2. دو SMS با شناسهٔ کامل متفاوت و عدد مشترک: یکی دیگری را جایگزین کرد.
3. callback ردکردن دو SMS با عدد مشترک، یک PendingIntent مشترک بود.

تلاش اول harness پیش از آماده‌شدن Koin شروع شد؛ خطای setup در `harness-crash.log` ثبت و انتظار آماده‌شدن برنامه اضافه شد. این خطای harness به عنوان بازتولید نقص اعلان حساب نشده است.

## اصلاح استقلال و کارهای پس از آن

اصلاح جاری، هویت کارت را به `(نوع منطقی، شناسهٔ کامل رکورد، عدد کارت)` وابسته می‌کند. نوع کانالِ ارتقایافته همان مالک منطقی را نگه می‌دارد. بدنه، ردکردن و نادیده‌گرفتن هم شناسهٔ مستقل دارند؛ لغوِ یک SMS یا میان‌بر نباید بودجه‌ای با همان عدد را بردارد. مصرف draft پس از ثبت مالی موفق و حذف تأییدشده باید فقط کارت‌های متناظر را لغو کند؛ شکست نوشتن، کارت و draft را نگه می‌دارد.

این اصلاح به معنی پذیرش کامل N-01 یا ماتریس ۹گانه نیست. ادامهٔ لازم:

- N-05: حمل budgetId/noteId و بازکردن همان رکورد در هفت مسیر فهرستی؛ شناسه‌های کار قدیمی هم حفظ شوند.
- N-06: مسیر واقعی Worker قسط، شناسهٔ معتبر، پرداخت دقیقاً یک‌بار، کار قدیمی و رکورد حذف‌شده/تسویه‌شده.
- N-07: یک‌بار هشدار، retry و بازسازی Activity، حریم خصوصی و یادداشت قفل‌شده.
- QA-N1 تا QA-N9: تحریک از SMS واقعی یا فرم همان فیچر و Scheduler/Worker واقعی؛ بنر، صدا، سوایپ باز/قفل، بدنه و تک‌تک اکشن‌ها. آزمون ناشر و callback جای این سناریوها را نمی‌گیرد.
- N-02/N-04/QA-N10: نسخه‌های قدیمی Android و ارتقا با کانال/تنظیم واقعی. کارت قدیمیِ بدون tag و بدون اطلاعات مالک در API 24/25 را نمی‌توان از روی عدد با اطمینان به نوع مشخص نسبت داد؛ حذف حدسی آن، پذیرش مهاجرت نیست.

مرجع رفتار سیستم: [هویت و لغو کارت Android](https://developer.android.com/reference/android/app/NotificationManager)، [هویت PendingIntent و Intent](https://developer.android.com/reference/android/app/PendingIntent).
