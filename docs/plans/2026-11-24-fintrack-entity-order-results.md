# ENT-01j — حفظ ترتیب منابع، برچسب‌ها و اشخاص در خواندن داده

سه mapper مشترک position دیتابیس را منتقل نمی‌کردند و مدل مقدار صفر می‌گرفت. اصلاح به انتقال position.toInt() در SourceDb.toSource، TagDb.toTag و PersonDb.toPerson محدود است؛ قرارداد مخزن و عملیات نوشتن تغییر نکردند.

شش آزمون SQLite واقعی قبل از اصلاح شکست خوردند و پس از آن پاس شدند: برای هر نوع، خواندن تکی و فهرست position=7 را حفظ می‌کنند؛ جابه‌جایی [2,3,1] و تغییر نام با مدل خوانده‌شده ترتیب را نگه می‌دارند و موجودی 900 و شمار تراکنش یک ثابت می‌مانند. مجموع ۱۶۱ تست JVM بدون failure/error و ساخت DirectDebug پاس شدند؛ ۱ دقیقه و ۱۱ ثانیه، ۶۵۷ وظیفه.

## پذیرش واقعی منبع

مبنا HEAD=0b2a63df1090977f6c54869d79ac0badb7295588 روی FinTrack_QA_API31، API 31، serial=127.0.0.1:5557، فارسی و تم فعلی نصب بود. در ابزارها ← منابع مالی، TestBank با دو اقدام پایین‌بردن به جایگاه سوم رفت و ترتیب [2,3,1,4] ذخیره شد. تغییر نام از فرم واقعی به QA Source edited در نسخهٔ قبلی، ترتیب را [1,2,3,4] و position را صفر کرد.

با APK جدید، همان مسیر واقعی اجرا شد؛ ترتیب [2,3,1,4] و position=2 پس از ذخیره و شروع سرد حفظ شدند. تصویر و XML صفحهٔ سرد هم ترتیب Blu Personal، Blu Work، نام ویرایش‌شده و USD QA را نشان می‌دهند. همهٔ فیلدهای منبع جز نام عمدی/position/updatedAt/syncStatus و همهٔ جدول‌های دیگر یکسان بودند؛ مانده، واحد و اطلاعات بانکی حفظ شدند. دیتابیس و ترجیحات هر دو اجرا دقیقاً بازیابی شدند.

APK جدید SHA-256: abc7c337276bad465d7c396f1490ba8be88e5f9ec26bb29aa8420df97ab3f162. در نتیجهٔ baseline مقدار hostBuiltApkSha مربوط به فایل build جدید روی میزبان است و هش APK نصب‌شدهٔ مبنا محسوب نمی‌شود.

## مرز پوشش و ادامه

نگاشت برچسب و شخص با تست دیتابیس پذیرفته شد؛ پذیرش فرم ویرایش این دو در این ردیف ادعا نمی‌شود. بررسی سورس نشان داد TagDraft و PersonDraft مستقلاً position را حذف می‌کنند؛ ENT-01k باید این مسیر فرم را اصلاح و پذیرش واقعی کند. ردیف مادر ENT-01 باز است. این ردیف کار دستی لازم ندارد.

GitNexus برای extensionها caller حل‌شده نداشت و UNKNOWN/lower-bound با receiver typing گزارش کرد (منبع ۳، برچسب ۱ و شخص ۱ محل حل‌نشده). جست‌وجوی سورس callerهای TransactionLocalDataSourceImpl و مصرف برچسب در NoteLocalDataSourceImpl/ShoppingLocalDataSourceImpl را مشخص کرد. صفر caller به معنی نبود اثر تعبیر نشد. پوشش زندهٔ همهٔ صفحات، backup یا sync ادعا نمی‌شود.

شواهد محلی .artifacts/fintrack-2026-11-22: entity-position-red.log، entity-position-green.log، source-position-native.cjs، source-position-baseline/fixed-result.json، restore.json، before/reordered/after.db، list/reorder/edited/cold.png/xml و source-position-verify.cjs. همهٔ داده‌ها مصنوعی و فرمان‌ها محدود به serial امولاتور بودند.
