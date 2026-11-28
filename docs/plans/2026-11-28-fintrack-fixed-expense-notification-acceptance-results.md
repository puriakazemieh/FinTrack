# FIX-02d2 / QA-N9 — پذیرش اعلان هزینهٔ ثابت

۲۸ نوامبر۲۰۲۶؛ مبنا `f5258941258402c8c2ea0c50833aa3edadc9c92b`، شاخه `codex/fintrack-design-unification`. این مرحله ابزار آزمون اندروید و مدارک را اضافه کرد؛ کد محصول تغییر نکرد. FIX-02d2 و QA-N9 پذیرفته‌اند و همراه پذیرش‌های a/b/c/d1، FIX-02d و FIX-02 نیز بسته‌اند. کار دستی لازم نیست.

## محیط و پیش‌شرط

FinTrack_QA_API31 / API31، فارسی، GLASS_DARK، DirectDebug، serial=`127.0.0.1:5557` با سرورadb5038. alias کنسول `emulator-5556` روی5037 با نام AVD و boot_id همان مهمان تطبیق داده شد. هیچ گوشی شخصی هدف قرار نگرفت. APK محصول SHA256=`1f56e4f0e7148a962629eeee4220b09790934ccd44a4fc95c3e2227c05da6b71`؛ فقط APK ابزار آزمون در طول پذیرش تکمیل شد.

قبل از fixture، snapshot کامل `FTQA_N9_1795849334526` و DB/prefs ذخیره شدند. ابزار emulator-only، منبع۵ با10000تومان و دو هزینهٔ ماهانهٔ۵/۶ با مبلغ1000تومان و marker1795849447103 ساخت. A ثبت خودکار خاموش و B روشن بود. ثبت B از FixedExpenseWorker واقعی انجام شد: فقط یک تراکنش/رسید عادی، مانده9000. سپس هر دو یادآور از UpdateFixedExpenseUseCase و زمان‌بند واقعی با موعد نزدیک فعال شدند؛ ناشر مستقیماً فراخوانی نشد. آماده‌سازی فرم واقعی قبلاً در FIX-01b3/BASE-01c2 پذیرفته شده است؛ این fixture برای تکرار شرایط سیستم است.

کانال مبنا `fixed_expense_reminders`، importance4، صدای پیش‌فرض و bypassDnd=false؛ صفحه روشن، قفل باز، DND=0 و اعلان مجاز. شناسه‌های مستقل543178499/543178500، tagهای رکورد۵/۶. flags=0x18؛ autoCancel/onlyAlertOnce، بدون ongoing/noClear. اعلان هزینهٔ ثابت در سورس و کارت واقعی اکشن جداگانه ندارد؛ لمس بدنه و حذف آزموده شدند.

## سناریو، انتظار و نتیجهٔ واقعی

| سناریو/عمل | انتظار و نتیجهٔ پذیرفته‌شده | مدرک محلی با پیشوند fix02d2- |
|---|---|---|
| تحویل واقعی دو موعد هم‌زمان، اپ در پیش‌زمینه | بنر A دیده شد؛ دو کارت مستقل با عنوان هزینه، متن فارسی و۱,۰۰۰تومان در پنل؛ ثبت اضافه صفر | normal.mp4، normal-frame-05.png، normal-panel.png/xml، normal-notifications.txt |
| حذف A به چپ، سپس B به راست | فقط کارت هدف کنار رفت؛ حذف A، B را نگه داشت؛ مانده9000 و رسیدهای A=0/B=1 | normal-left/right.mp4، before/after تصاویر، normal-after-swipes.json |
| همان دو جهت روی قفل امن PIN | کارت‌ها بدون بازکردن قفل حذف شدند؛ deviceLocked=1 قبل/بعد؛ ثبت مالی اضافه صفر | locked.mp4، locked-panel.png، locked-left/right.mp4 و trust، locked-after-swipes.json |
| لمس A روی قفل، انصراف از PIN و سپس بازکردن قفل | PIN درخواست شد؛ انصراف قفل را حفظ کرد؛ پس از بازکردن قفل هیچ فرم معلقی باز نشد و Launcher فعال بود | cancel-clean-result.json و تصاویر pin/locked/unlocked، cancel-clean-finance.json |
| لمس A با فرایند سرد و سپس B در فرایند باز | نبود PID پیش از A و ایجاد فرایند تازه کنترل شد؛ فیلد قابل‌ویرایش دقیقاً عنوان رکورد۵، سپس رکورد۶ بود؛ حذف کارت A به B اثر نکرد | body-cold-editor-result.json، body-warm-editor-result.json، تصاویر editor، after-body.json |
| خاموش‌کردن All notifications از تنظیمات واقعی اندروید | هر دو job واقعاً SUCCEEDED؛ صفر کارت؛ شروع سرد انتخاب مسدود کاربر را تغییر نداد؛ مبلغ/رسیدها ثابت | global-off-settings.png، global-blocked-complete.json، global-blocked-dump.txt، global-blocked-after-cold.png |
| انتخاب Silent برای همین کانال از تنظیمات | importance2 با شروع سرد حفظ شد؛ هر دو کارت در بخش Silent، بدون بنر در بازهٔ ضبط؛ مبلغ/رسیدها ثابت | channel-silent.mp4، 31فریم و contact.png، channel-silent-panel.png/xml، channel-silent-complete.json |
| مسدودکردن Show notifications کانال | importance0 حفظ شد؛ هر دو Worker موفق و صفر کارت؛ بازکردن دوبارهٔ تنظیمات همچنان مسدود | channel-block-settings.png، channel-blocked-complete.json، channel-blocked-dump.txt، channel-blocked-after-cold.png |
| DND بدون وقفه با کانال HIGH و bypass=false | zen2، هر دو کارت در NMS intercepted و suppressedVisualEffects511؛ در ضبط بنر ظاهر نشد؛ ثبت اضافه صفر | dnd-high.mp4،31فریم/contact، dnd-live-dump.txt، dnd-high-complete.json |

در هر inspect مالی، تعداد تراکنش منبع مصنوعی=1، رسید A=0 و B=1 و مانده9000 کنترل شد. بعد از سوایپ و لمس نیز موعد دوره‌ها تغییر نکرد. مقدار attempt=1 در گزارش کار موفق را retry نخوان؛ آزمون retry واقعی مالی در d1 ثبت شده است. تأیید صدای هر۹نوع همان QA-AUDIO قبلی کاربر است؛ این ضبط‌های صفحه شاهد شنیدن یا نشنیدن صدا نیستند. برای Silent/DND، حفظ تنظیم واقعی سیستم، رفتار بصری ضبط‌شده و وضعیت تحویل بررسی شدند.

## تلاش‌های ردشده و مرز پذیرش

اولین انتظار ثابتِ کوتاه برای لمس روی قفل، قبل از تحویل واقعی تمام شد؛ پیش‌شرط نامعتبر بود و به‌عنوان خطای محصول ثبت نشد. انتظار به مشاهدهٔ دو کارت واقعی تغییر کرد؛ تأخیر عملی WorkManager در چند تکرار حدود16ثانیه پس از موعد درخواستی بود. ضبط و سناریوهای معتبر دوباره اجرا شدند. لغو PIN نیز بدون اجرای instrumentation یا کشتن اپ بین لغو و بازکردن قفل تکرار شد تا فرم معلق واقعاً بررسی شود.

تغییر خام POST_NOTIFICATION با appops به‌تنهایی مسدودسازی قابل اتکای این AVD نبود: دو کارت باقی بودند. آن تلاش پذیرفته نشد؛ از کلید واقعی All notifications استفاده شد و پس از موفقیت دو Worker صفر کارت تأیید شد. این آزمون API31، مجوز runtime اندروید۱۳ نیست و پوشش حذف‌شدهٔ چندنسخه‌ای گزارش نمی‌شود.

اولین ضبط DND با importance3 بود؛ برای حذف این عامل، Pop on screen روشن، importance4 و bypass=false تأیید و سناریو با نام dnd-high دوباره اجرا شد. محتوای اصلی صفحه در ضبط DND سیاه است و نوارهای سیستم دیده می‌شوند؛ نتیجه فقط به نبود بنر در همین بازه و interception سیستم محدود است. هیچ ادعای شنیدن صدا از این ضبط وجود ندارد.

ارتقای کانال قدیمی در N-04/QA-N10، retry اعلان/به‌روزرسانی کارت و حریم خصوصی مشترک در N-07، و پذیرش هشت نوع دیگر مستقل‌اند. این پذیرش آن‌ها را نمی‌بندد.

## ساخت و بازگردانی

ساخت APK محصول و ابزار با runner=`com.kazemieh.fintrack.FixedExpenseNotificationAcceptanceInstrumentation` و همان گزینه‌های offline/max-workers1 پذیرفته شد؛ آخرین ساخت ابزار۶ثانیه بود (`fix02d2-jobs-build.log`). نتایج۲۲۷تایی JVM از c1/c2 محفوظ‌اند و در d2 دوباره اجرا نشده‌اند. این مرحله مجموعهٔ سناریوهای دستگاهی بالا را اجرا کرد، نه یک شمارش تازه از آزمون‌های واحد.

پس از لغو کارها/کارت‌های متعلق به fixture و خاموش‌کردن auto/reminder آن‌ها، snapshot کامل بارگذاری شد: همهٔ جدول‌های کسب‌وکار برابر، prefs بایت‌به‌بایت یکسان، zen0، lockDisabled=true، credential خالی و POST_NOTIFICATION=default. خطوط NotificationChannel قبل/بعد یکسان بودند؛ انتخاب‌های کانال و userLockedFields نیز برگشتند. manifest ابزار وجود ندارد؛ fixture مالی تازه باقی نماند. آخرین APK ابزار مجدداً نصب و صفحهٔ خانهٔ واقعی مشاهده شد. فقط snapshot متعلق به همین آزمون با فرمان کنسول حذف شد؛ receipt در fix02d2-snapshot-delete.txt و session.json است. چهار خروجی انتشار کاربر دست‌نخورده باقی ماندند.

شواهد در `.artifacts/fintrack-2026-11-25/`؛ گزارش‌های restore/session، before/restored DB/prefs و restoration-result.json مبنای حفظ داده‌اند. این مسیر نادیده‌گرفته‌شده عمداً وارد Git نمی‌شود؛ fixture تکرارپذیر در androidTest و این گزارش در Git ثبت می‌شوند. پیش از اجرای prepare دوباره، روی امولاتور منتخب snapshot کامل و مسیر بازیابی بساز؛ روی گوشی واقعی اجرا نکن.

GitNexus پس از افزودن ابزار کامل بازسازی شد. دور اول خروج صفر ولی FTS معیوب داشت و پذیرفته نشد؛ بازسازی کامل دوم FTSavailable، runner schema4/current و HEAD یکسان داد. uid متد schedule و caller مستقیم onStart با سورس تطبیق داده شدند؛ شاخه‌های چندپلتفرمی گراف با تزریق Android واقعی کنترل شدند. گیت staged و بازسازی پس از کامیت جدا اجرا می‌شوند.
