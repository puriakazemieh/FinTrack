# DEBT-01a — حفظ یادآور در ویرایش بدهی و طلب

۲۵ نوامبر ۲۰۲۶، مبنای05373b39؛ DirectDebug، FinTrack_QA_API31/API31، فارسی و GLASS_DARK، serial127.0.0.1:5557/server5038. این ردیف تکمیل است؛ DEBT-01 تسویه و سایر پذیرش‌های مالی مستقل بازند.

## بازتولید و اصلاح

`AddDebtViewModel.loadDebt` زمان سررسید را می‌خواند ولی reminderEnabled را در state کپی نمی‌کرد. در فرم جدید مقدار پیش‌فرض false بود و ذخیرهٔ فقط توضیحات همان false را به UpdateDebtUseCase می‌داد؛ زمان‌بند درخواست قبلی را لغو می‌کرد. فقط یک خط تولیدی برای خواندن reminderEnabled از رکورد به state افزوده شد. پیش‌فرض فرم جدید، زمان، قرارداد MVI، DI و مسیر ذخیره تغییر نکردند.

ابزار opt-in `DebtReminderFormInstrumentation` از repository، ViewModel و WorkManager واقعی استفاده می‌کند. وابستگی debt فقط در androidTestImplementation اپ افزوده شد؛ runner پیش‌فرض تغییر نکرد. چهار fixture موقت با روابط معتبر از دو رکورد BASE-01b ساخته شدند: هر دو نوع بدهی/طلب، هر کدام یادآور روشن و خاموش، موعد24ساعت بعد. همان ViewModel برای بارگذاری‌های پی‌درپی استفاده شد تا روشن بودن رکورد قبلی روی رکورد خاموش باقی نماند. فقط توضیحات ویرایش شد؛ تمام سایر فیلدها و tagها با رکورد قبل مقایسه شدند. برای روشن، وجود یک WorkInfo فعال ENQUEUED قبل و پس از ذخیره و برای خاموش، نبود کار فعال تأیید شد.

روی APK مبنا،4 سناریو اجرا شدند و دقیقاً2 حالت روشن شکست خوردند: `Loaded reminder flag differs from persisted true`. بعد از اصلاح، بدون تغییر assertionها همان4 سناریو پاس شدند. در تلاش اول harness، تغییر marker داخلی android_metadata از en_US به fa به‌عنوان دادهٔ غیرمنتظره رد شد؛ baseline در finally با بایت دقیق بازگردانده شد. سپس harness فقط همین انتقال داخلیِ دقیق را مجاز کرد و بازتولید کامل دوباره اجرا شد. این استثنا شامل هیچ جدول کسب‌وکار نیست.

یک بازآزمایی UI نیز روی رکورد اصلی1/QA BASE Debt انجام شد: منوی همان ردیف → ویرایش → اسکرول تا یادآور. سوییچ روشن و ساعت ذخیره‌شده12:45 دیده و تصویرش بررسی شد. فرم بدون ذخیره بسته شد؛ هیچ دادهٔ کسب‌وکار تغییر نکرد. موعد این fixture قبلی گذشته است؛ این مشاهده، ادعای تحویل اعلان تازه نیست.

## build و حفظ داده

فرمان build با runner اختیاری، `:core:domain:jvmTest :app:assembleDirectDebug :app:assembleDirectDebugAndroidTest`، offline، یک worker، Xmx3072m و compiler in-process موفق شد:20ثانیه،629task،47 اجرا و582 up-to-date. مجموعه104 تست دامنه up-to-date و سبز بود؛ اجرای تازه104تست ادعا نمی‌شود. SHA-256 APK نهایی `c950d1e4b0384d1c05a3d24f9f60b45660da8ba4076eecfbad7d769fe15d8de3` است. فرمان اولیهٔ red به‌دلیل تجزیهٔ آرگومان dotted در PowerShell شکست خورد؛ با نقل‌قول آرگومان‌ها اصلاح شد، سپس build قرمز32ثانیه/623task موفق بود. این خطای میزبان، شکست محصول محسوب نشد.

قبل از هر اجرا DB و prefs کامل ذخیره شدند. مقایسهٔ همهٔ جدول‌ها حفظ رکوردهای قبلی، مانده‌ها،4تراکنش،34draft، روابط و چهار هزینهٔ ثابت را تأیید کرد؛ فقط fixtureهای debt3..6 و sequence مربوط به آن‌ها موقتی بودند. jobهای متعلق به ابزار لغو و fixtureها حذف شدند، سپس DB/prefs با بایت دقیق بازگردانده شدند. UI اصلی نیز پس از مقایسه به snapshot پیش از اجرای سبز بازگشت. دادهٔ قبلی از جمله fixture4 هزینهٔ ثابت با یادآور خاموش و زمان1795616580000 حفظ شده است. هیچ کار دستی کاربر لازم نیست.

## شواهد و اثر

در `.artifacts/fintrack-2026-11-25/`: debt01a-red/green-{instrumentation,result,before,after,restored}، logهای build/run، native.cjs؛ ui-after.db، ui-result.json و ui-verify.cjs؛ تصاویر/XMLهای list، row-menu و reminder-controls. نتیجهٔ red با2شکست و green با0شکست مستقل ذخیره شده‌اند. اثبات ENQUEUED جای بنر، صدا یا سوایپ QA-N6 نیست.

GitNexus در05373b39 با receipt schema4/current و CLI/context تطبیق داده شد؛ بعد از افزودن تست و قبل از اصلاح تولیدی دوباره کامل بازسازی و impact گرفته شد. loadDebt ریسک LOW داشت؛ caller مستقیم onIntent و مصرف‌کنندگان BottomSheet/list با سورس بررسی شدند. APIها عوض نشدند؛ snapshot رسمی پیش از تغییر و gate staged پیش از کامیت ثبت می‌شوند. پلن اصلی تغییر نکرد. ادامهٔ اصلی، FIX-02 ثبت اتمی هر دوره و سپس QA-N9 است.
