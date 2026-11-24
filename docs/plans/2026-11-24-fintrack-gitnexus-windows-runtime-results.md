# GN-02a — محیط DLL برای GitNexus در ویندوز

FTS به DLLهای OpenSSL 3 وابسته است. پس از بارگذاری تازهٔ ابزارها، LOAD fts با Windows error 126 شکست خورد؛ بازنصب extension لازم نبود. راهنمای خود GitNexus مسیر mingw64 را پیشنهاد می‌داد، ولی Git نصب‌شدهٔ این سیستم DLLهای libcrypto-3-x64.dll و libssl-3-x64.dll را در C:/Program Files/Git/ucrt64/bin دارد. افزودن همین مسیر به PATH فرایند، repair-fts را موفق کرد. علت خطای قبلی WAL یا UIDهای incremental با این تغییر رفع‌شده فرض نمی‌شود؛ دستور بازسازی کامل GN-02 همچنان لازم است.

## فرمان قابل تکرار

[scripts/Invoke-GitNexus.ps1](../../scripts/Invoke-GitNexus.ps1) محل git.exe را از Get-Command پیدا می‌کند، مسیرهای ucrt64/bin و mingw64/bin و bin در والدهای مربوط را برای هر دو DLL بررسی می‌کند و در صورت وجود آن‌ها فقط PATH همین اجرا/فرزند را کامل می‌کند. PATH در finally بازگردانده و کد خروج CLI عیناً منتقل می‌شود. executable همان gitnexus.cmd نصب‌شده است؛ ابزار یا نسخهٔ دیگری دانلود نمی‌شود. اگر DLLها در Git پیدا نشوند، CLI با محیط موجود اجرا می‌شود تا نصب معتبر دیگر هم قابل استفاده باشد؛ سلامت FTS باید با status/query تأیید شود.

از ریشهٔ پروژه در PowerShell:

~~~powershell
.\scripts\Invoke-GitNexus.ps1 analyze --index-only --pdg --force --wal-checkpoint-threshold 1073741824
.\scripts\Invoke-GitNexus.ps1 status --json
.\scripts\Invoke-GitNexus.ps1 query AddPersonViewModel --repo FinTrack
~~~

قبل از CLI/MCP بعدی تا پایان فرایند analyze صبر کن. پس از هر تغییر HEAD یا سورس، بازسازی کامل، list_repos و تطبیق commit/runner و UID/caller لازم‌اند. پرامپت اجرا به همین فرمان تغییر کرد. این تغییر کد اپ یا برنامهٔ آزمون Android را تغییر نمی‌دهد؛ build دوبارهٔ اپ برای این بسته لازم نیست.

## تنظیم محلی MCP و آزمون

تنها env.PATH سرور gitnexus در C:/Users/p.kazemiyeh/.codex/config.toml با مسیر DLL تکمیل شد. CLI خود Codex تنظیم را parse کرد و مقایسهٔ ماشینی تأیید کرد command، args و تمام گزینه‌های قبلی حفظ شدند. نسخهٔ پشتیبان مجاور فایل تنظیم ساخته شد؛ مسیر دقیق در gn-windows-config-proof.json محلی ثبت است. این تنظیم محلی در Git commit نمی‌شود؛ launcher و روش استفاده قابل انتقال‌اند. محیط Windows کاربر یا سیستم تغییر نکرد.

launcher بدون profile اجرا شد: status --json با receipt schema 4 و HEAD=a5792b7a تطبیق داشت؛ فرمان نامعتبر exit=1 را حفظ کرد؛ analyze --repair-fts موفق بود. سپس یک MCP تازه با Client/Transport رسمی SDK و دقیقاً command/args/env تنظیم‌شده راه‌اندازی شد. query مربوط به AddPersonViewModel بدون warning/error و با ۶ نماد مرتبط و UID معتبر برگشت؛ سرور آزمایشی پس از آزمون بسته شد. هویت analyzer CLI 1.6.12 / Node 24.16.0 و digest نسخهٔ distribution/dependency با receiptهای قبلی یکسان ماندند.

## GN-02b — وضعیت اولیهٔ اتصال قدیمی

MCP در حال اجرای همین جلسه هنوز PATH قدیمی را دارد؛ query آن پس از ذخیرهٔ تنظیم همچنان error 126 گزارش کرد. این سرور قدیمی بدون درخواست، متوقف نشد. پس از پایان کارهای فعال، اتصال GitNexus در Codex را دوباره برقرار کن؛ اگر راه اتصال مجدد در دسترس نبود، Codex را ببند و باز کن. سپس در همین پروژه list_repos و query AddPersonViewModel با repo=FinTrack را اجرا کن و نبود هشدار FTS و وجود نماد مرتبط را ثبت کن. تا آن پذیرش، GN-02b باز می‌ماند. graph/context/impact فعلی و query از CLI/سرور تازه مسیر ادامهٔ کارند؛ این مورد توقف کارهای مستقل را لازم نمی‌کند.

شواهد محلی .artifacts/fintrack-2026-11-22: edit-position-final-index.log (error 126)، edit-position-fts-ucrt-repair.log، gn-windows-pilot-status.json، gn-windows-invalid-exit.json/log، gn-windows-pilot-repair.log، gn-windows-config-proof.json، gn-windows-fresh-mcp-query.json/log و stderr. هیچ کلید یا مقدار احراز هویت به گزارش منتقل نشد.


## پذیرش GN-02b روی اتصال فعال همین جلسه

۲۴ نوامبر ۲۰۲۶، پس از ثبت اصلاح انتخابگر در HEAD fa9d1cc1، analyze کامل با runner نصب‌شده و PDG تا exit0 پایان یافت. list_repos اتصال فعلی تازه شد؛ context و status --json دقیقاً HEAD فعلی و receipt schema4 یکسان، runnerIdentityStatus=current و incompleteReasons=[] داشتند. query AddPersonViewModel از خود MCP فعال همین جلسه با repo=FinTrack موفق شد؛ خطای FTS/126 یا warning نصب extension نداشت. تعریف Class در feature-share/person/src/commonMain/kotlin/com/kazemieh/person/ui/add/AddPersonViewModel.kt با UID معتبر و بدون NUL و محدودهٔ20تا110 مطابق سورس برگشت. جست‌وجوی انتخابگر و impact/context نیز در همین اتصال کار کردند.

بنابراین پذیرش اتصال فعال انجام شد و GN-02b بسته است؛ دستور دستیِ بخش قبل فقط سابقهٔ وضعیت اولیه است و اکنون لازم نیست. باز و بسته شدن Codex توسط این آزمون انجام نشد و دربارهٔ علت زمان فعال شدن اتصال ادعایی نمی‌شود. هیچ تنظیم یا کتابخانهٔ دیگری برای این پذیرش تغییر نکرد. شواهد جدید: gn02b-live-query.json و ds04a-postcommit-index.log/status.json/context.json در همان پوشهٔ محلی. این بسته فقط مدرک پذیرش و پرامپت را به‌روز می‌کند؛ build اپ نیاز ندارد.
