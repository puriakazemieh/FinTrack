# چک‌لیست جامع ایونت‌های ترکینگ (Event Taxonomy)

**مرتبط با فاز:** فاز ۵ از نقشه راه اصلی (مرحله ۴: تزریق ایونت‌ها در ViewModelها)

این سند شامل لیست جامع تمامی رویدادهایی (Events) است که برای بررسی دقیق رفتار کاربران در تمامی ماژول‌ها و فیچرهای FinTrack قابل پیاده‌سازی هستند.  
**نکته امنیتی بسیار مهم (حریم خصوصی):** در هیچ‌کدام از این ایونت‌ها نباید مبالغ مالی، نام اشخاص، توضیحات تراکنش، یا شناسه‌های دیتابیس (به‌جز UUIDهای گمنام) ارسال شود.

## ۱. چرخه عمر کاربر و امنیت (Onboarding & Security)
- [x] `onboarding_started` (شروع آنبوردینگ)
- [x] `onboarding_completed` (پایان آنبوردینگ)
- [x] `auth_pin_created` (تنظیم رمز عبور)
- [x] `auth_pin_changed` (تغییر رمز عبور)
- [x] `auth_biometric_enabled` (فعال‌سازی اثر انگشت/تشخیص چهره)
- [x] `auth_biometric_disabled`
- [x] `app_unlocked` (ورود موفق به اپلیکیشن)
- [x] `app_locked_timeout` (قفل شدن خودکار به دلیل عدم فعالیت)

## ۲. داشبورد (Dashboard)
- [x] `dashboard_viewed` (باز کردن داشبورد)
- [x] `dashboard_quick_add_clicked` (کلیک روی دکمه افزودن سریع)
- [x] `dashboard_recent_transaction_clicked` (کلیک روی یک تراکنش اخیر در داشبورد)
- [x] `dashboard_wallet_summary_viewed` (بررسی خلاصه کیف پول و موجودی‌ها در داشبورد)
- [x] `dashboard_widget_reordered` (تغییر چیدمان ویجت‌های داشبورد)
- [x] `dashboard_widget_toggled` (روشن یا خاموش کردن نمایش یک ویجت خاص)

## ۳. تراکنش‌ها (Transactions)
- [x] `transaction_list_viewed` (باز کردن لیست تراکنش‌ها)
- [x] `transaction_created` (ساخت تراکنش جدید - همراه با نوع تراکنش)
- [x] `transaction_updated` (ویرایش تراکنش)
- [x] `transaction_deleted` (حذف تراکنش)
- [x] `transaction_filter_applied` (استفاده از فیلتر در لیست تراکنش‌ها)
- [x] `transaction_search_used` (جستجو در تراکنش‌ها)
- [x] `transaction_duplicate_clicked` (تکرار یک تراکنش)
- [x] `transaction_sort_changed` (تغییر مرتب‌سازی لیست)
- [x] `transaction_search_performed` (جستجو در تراکنش‌ها)
- [x] `transaction_report_viewed` (مشاهده گزارش و نمودارهای گرافیکی)

## ۴. منابع مالی و حساب‌ها (Financial Sources)
- [x] `source_list_viewed`
- [x] `source_created` (پارامتر مجاز: نوع حساب مثلاً کارت، نقد، بانک)
- [x] `source_updated`
- [x] `source_deleted`
- [x] `source_transfer_initiated` (شروع انتقال وجه بین دو حساب)

## ۵. دسته‌بندی‌ها (Categories)
- [x] `category_list_viewed`
- [x] `category_created`
- [x] `category_updated`
- [x] `category_deleted`
- [x] `category_parent_changed` (تغییر والد یک دسته)
- [x] `category_icon_changed`
- [x] `category_reordered`

## ۶. تگ‌ها و اشخاص (Tags & Persons)
- [x] `tag_list_viewed`
- [x] `tag_created`
- [x] `tag_updated`
- [x] `tag_deleted`
- [x] `person_list_viewed`
- [x] `person_created`
- [x] `person_updated`
- [x] `person_deleted`



## ۸. بودجه‌بندی (Budgets)
- [x] `budget_list_viewed`
- [x] `budget_created` (پارامتر مجاز: `period` = monthly/yearly/etc)
- [x] `budget_updated`
- [x] `budget_deleted`
- [x] `budget_exceeded_warning` (نمایش هشدار رد شدن از سقف بودجه)

## ۹. دیون و تعهدات (Debts & Checks & Installments)
- [x] `debt_list_viewed`
- [x] `debt_created` (پارامتر مجاز: طلب یا بدهی)
- [x] `debt_settled` (تسویه کامل یا جزئی بدهی)
- [x] `debt_deleted`
- [x] `installment_list_viewed`
- [x] `installment_created`
- [x] `installment_paid` (پرداخت یک قسط)
- [x] `installment_deleted`
- [x] `check_list_viewed`
- [x] `check_created`
- [x] `check_status_changed` (تغییر وضعیت چک به پاس شده یا برگشتی)
- [x] `check_deleted`

## ۱۰. هزینه‌های ثابت (Fixed Expenses)
- [x] `fixed_expense_list_viewed`
- [x] `fixed_expense_created`
- [x] `fixed_expense_deleted`
- [x] `fixed_expense_auto_logged` (ثبت خودکار هزینه ثابت توسط سیستم)

## ۱۱. مدیریت دارایی‌ها (Assets & Gold/Crypto)
- [x] `asset_list_viewed`
- [x] `asset_created` (پارامتر مجاز: نوع دارایی مثلاً طلا/ارز)
- [x] `asset_updated`
- [x] `asset_deleted`
- [x] `fx_rates_viewed` (بررسی نرخ لحظه‌ای ارز و طلا)

## ۱۲. امکانات جانبی و ابزارها (Utilities & Tools)
- [x] `tools_hub_viewed` (باز کردن صفحه ابزارها)
- [x] `currency_converter_used` (استفاده از ماشین حساب تبدیل ارز)
- [x] `ai_advisor_opened` (باز کردن صفحه هوش مصنوعی)
- [x] `ai_insight_generated` (دریافت تحلیل از هوش مصنوعی)
- [x] `ai_insight_feedback_given` (لایک یا دیس‌لایک کردن پاسخ هوش مصنوعی)
- [x] `calendar_viewed` (مشاهده تقویم مالی)
- [x] `shopping_list_created`
- [x] `shopping_item_purchased` (تیک زدن یک آیتم در لیست خرید)
- [x] `note_created`

## ۱۳. پشتیبان‌گیری و همگام‌سازی (Backup & Sync)
- [x] `backup_exported_local` (خروجی گرفتن روی حافظه گوشی)
- [x] `backup_restored` (بازگردانی بکاپ)
- [x] `sync_started` (شروع سینک ابری)
- [x] `sync_completed`
- [x] `sync_failed`

## ۱۴. تنظیمات و پروفایل (Settings & Profile)
- [x] `profile_viewed` (مشاهده صفحه پروفایل)
- [x] `profile_edited` (ویرایش اطلاعات کاربری)
- [x] `theme_changed` (تغییر تم تاریک/روشن یا رنگ‌بندی)
- [x] `base_currency_changed` (تغییر واحد پول اصلی اپلیکیشن)
- [x] `notification_settings_changed` (روشن/خاموش کردن اعلان‌ها یا یادآورها)
- [x] `language_changed` (تغییر زبان اپلیکیشن)
