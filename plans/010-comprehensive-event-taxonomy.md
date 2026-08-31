# چک‌لیست جامع ایونت‌های ترکینگ (Event Taxonomy)

**مرتبط با فاز:** فاز ۵ از نقشه راه اصلی (مرحله ۴: تزریق ایونت‌ها در ViewModelها)

این سند شامل لیست جامع تمامی رویدادهایی (Events) است که برای بررسی دقیق رفتار کاربران در تمامی ماژول‌ها و فیچرهای FinTrack قابل پیاده‌سازی هستند.  
**نکته امنیتی بسیار مهم (حریم خصوصی):** در هیچ‌کدام از این ایونت‌ها نباید مبالغ مالی، نام اشخاص، توضیحات تراکنش، یا شناسه‌های دیتابیس (به‌جز UUIDهای گمنام) ارسال شود.

## ۱. چرخه عمر کاربر و امنیت (Onboarding & Security)
- [x] `onboarding_started` (شروع آنبوردینگ)
- [x] `onboarding_completed` (پایان آنبوردینگ)
- [ ] `auth_pin_created` (تنظیم رمز عبور)
- [ ] `auth_pin_changed` (تغییر رمز عبور)
- [ ] `auth_biometric_enabled` (فعال‌سازی اثر انگشت/تشخیص چهره)
- [ ] `auth_biometric_disabled`
- [ ] `app_unlocked` (ورود موفق به اپلیکیشن)
- [ ] `app_locked_timeout` (قفل شدن خودکار به دلیل عدم فعالیت)

## ۲. داشبورد (Dashboard)
- [x] `dashboard_viewed` (باز کردن داشبورد)
- [ ] `dashboard_quick_add_clicked` (کلیک روی دکمه افزودن سریع)
- [ ] `dashboard_recent_transaction_clicked` (کلیک روی یک تراکنش اخیر در داشبورد)
- [ ] `dashboard_wallet_summary_viewed` (بررسی خلاصه کیف پول و موجودی‌ها در داشبورد)
- [x] `dashboard_widget_reordered` (تغییر چیدمان ویجت‌های داشبورد)
- [ ] `dashboard_widget_toggled` (روشن یا خاموش کردن نمایش یک ویجت خاص)

## ۳. تراکنش‌ها (Transactions)
- [x] `transaction_list_viewed` (باز کردن لیست تراکنش‌ها)
- [x] `transaction_created` (ساخت تراکنش جدید - همراه با نوع تراکنش)
- [x] `transaction_updated` (ویرایش تراکنش)
- [x] `transaction_deleted` (حذف تراکنش)
- [x] `transaction_filter_applied` (استفاده از فیلتر در لیست تراکنش‌ها)
- [ ] `transaction_search_used` (جستجو در تراکنش‌ها)
- [ ] `transaction_duplicate_clicked` (تکرار یک تراکنش)
- [ ] `transaction_sort_changed` (تغییر مرتب‌سازی لیست)
- [ ] `transaction_search_performed` (جستجو در تراکنش‌ها)
- [ ] `transaction_report_viewed` (مشاهده گزارش و نمودارهای گرافیکی)

## ۴. منابع مالی و حساب‌ها (Financial Sources)
- [x] `source_list_viewed`
- [x] `source_created` (پارامتر مجاز: نوع حساب مثلاً کارت، نقد، بانک)
- [x] `source_updated`
- [x] `source_deleted`
- [ ] `source_transfer_initiated` (شروع انتقال وجه بین دو حساب)

## ۵. دسته‌بندی‌ها (Categories)
- [ ] `category_list_viewed`
- [ ] `category_created` (پارامتر مجاز: نوع درآمد/هزینه)
- [ ] `category_updated`
- [ ] `category_deleted`
- [ ] `category_reordered` (تغییر ترتیب دسته‌بندی‌ها)

## ۶. اشخاص (Persons)
- [ ] `person_list_viewed`
- [ ] `person_detail_viewed` (باز کردن صفحه جزئیات و مانده حساب یک شخص)
- [ ] `person_created`
- [ ] `person_updated`
- [ ] `person_deleted`

## ۷. برچسب‌ها (Tags)
- [ ] `tag_list_viewed`
- [ ] `tag_created`
- [ ] `tag_updated`
- [ ] `tag_deleted`

## ۸. بودجه‌بندی (Budgets)
- [ ] `budget_list_viewed`
- [ ] `budget_created` (پارامتر مجاز: `period` = monthly/yearly/etc)
- [ ] `budget_updated`
- [ ] `budget_deleted`
- [ ] `budget_exceeded_warning` (نمایش هشدار رد شدن از سقف بودجه)

## ۹. دیون و تعهدات (Debts & Checks & Installments)
- [ ] `debt_list_viewed`
- [ ] `debt_created` (پارامتر مجاز: طلب یا بدهی)
- [ ] `debt_settled` (تسویه کامل یا جزئی بدهی)
- [ ] `debt_deleted`
- [ ] `installment_list_viewed`
- [ ] `installment_created`
- [ ] `installment_paid` (پرداخت یک قسط)
- [ ] `installment_deleted`
- [ ] `check_list_viewed`
- [ ] `check_created`
- [ ] `check_status_changed` (تغییر وضعیت چک به پاس شده یا برگشتی)
- [ ] `check_deleted`

## ۱۰. هزینه‌های ثابت (Fixed Expenses)
- [ ] `fixed_expense_list_viewed`
- [ ] `fixed_expense_created`
- [ ] `fixed_expense_deleted`
- [ ] `fixed_expense_auto_logged` (ثبت خودکار هزینه ثابت توسط سیستم)

## ۱۱. مدیریت دارایی‌ها (Assets & Gold/Crypto)
- [ ] `asset_list_viewed`
- [ ] `asset_created` (پارامتر مجاز: نوع دارایی مثلاً طلا/ارز)
- [ ] `asset_updated`
- [ ] `asset_deleted`
- [ ] `fx_rates_viewed` (بررسی نرخ لحظه‌ای ارز و طلا)

## ۱۲. امکانات جانبی و ابزارها (Utilities & Tools)
- [ ] `tools_hub_viewed` (باز کردن صفحه ابزارها)
- [ ] `currency_converter_used` (استفاده از ماشین حساب تبدیل ارز)
- [ ] `ai_advisor_opened` (باز کردن صفحه هوش مصنوعی)
- [ ] `ai_insight_generated` (دریافت تحلیل از هوش مصنوعی)
- [ ] `ai_insight_feedback_given` (لایک یا دیس‌لایک کردن پاسخ هوش مصنوعی)
- [ ] `calendar_viewed` (مشاهده تقویم مالی)
- [ ] `shopping_list_created`
- [ ] `shopping_item_purchased` (تیک زدن یک آیتم در لیست خرید)
- [ ] `note_created`

## ۱۳. پشتیبان‌گیری و همگام‌سازی (Backup & Sync)
- [ ] `backup_exported_local` (خروجی گرفتن روی حافظه گوشی)
- [ ] `backup_restored` (بازگردانی بکاپ)
- [ ] `sync_started` (شروع سینک ابری)
- [ ] `sync_completed`
- [ ] `sync_failed`

## ۱۴. تنظیمات و پروفایل (Settings & Profile)
- [ ] `profile_viewed` (مشاهده صفحه پروفایل)
- [ ] `profile_edited` (ویرایش اطلاعات کاربری)
- [ ] `theme_changed` (تغییر تم تاریک/روشن یا رنگ‌بندی)
- [ ] `base_currency_changed` (تغییر واحد پول اصلی اپلیکیشن)
- [ ] `notification_settings_changed` (روشن/خاموش کردن اعلان‌ها یا یادآورها)
- [ ] `language_changed` (تغییر زبان اپلیکیشن)
