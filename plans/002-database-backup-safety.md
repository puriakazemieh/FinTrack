# ایمنی دیتابیس، Migration، Backup و Attachment

**Priority:** P0/P1  
**Planned at:** `51c80fb9` — 2026-07-29  
**Branch:** `codex/002-database-backup-safety`
**نوع سند:** Epic Plan؛ هر DBS/SEC Task Card مستقل با fixture و rollback خود دارد.

## یافته‌های اثبات‌شده

- `core/database/src/androidMain/kotlin/com/kazemieh/database/DriverFactory.android.kt:13-20`
  دیتابیس قدیمی `fin_track.db` را حذف می‌کند.
- `core/database/src/jvmMain/kotlin/com/kazemieh/database/DriverFactory.jvm.kt:9-15`
  همیشه `Schema.create` دارد و lifecycle مهاجرت ندارد.
- `core/database/src/commonMain/kotlin/com/kazemieh/database/DatabaseInitializer.kt:10-42`
  خطاها را می‌بلعد و فقط یک repair دستی category انجام می‌دهد.
- `core/database/build.gradle.kts:9-16`
  schema output و migration verification غیرفعال است.
- schema Goal ستون‌هایی دارد که مسیر upgrade موجود آن‌ها را کامل اضافه نمی‌کند.
- BackupData تمام entityها، relationها، asset history و attachment bytes/manifest را ندارد.
- Restore چند حلقه جدا و غیراتمیک است و نتیجه واقعی insert/update را گزارش نمی‌کند.
- storage در Android/JVM path خارجی را مستقیم به `File(path)` می‌دهد؛ iOS ذخیره واقعی تصویر ندارد.
- foreign key enforcement بین driverها یکسان نیست.

## اصل طراحی

```text
Open database
  -> identify physical file and schema version
  -> create verified safety copy
  -> migrate/converge transactionally
  -> PRAGMA integrity_check + foreign_key_check
  -> open application
```

در failure:

```text
close new DB -> retain failed copy/log -> restore safety copy -> show actionable error
```

## Task Cardها

### DBS-001 — Fixture inventory `[P0 / 4.5.1]`

- از هر tag منتشرشده (`v_1.0.0`, `v_1.5.0`, `v_3.5.0`, `v_4.0.0`, `v_4.5.0`)
  حداقل یک دیتابیس بی‌داده و یک دیتابیس representative تهیه شود.
- fixtureها شامل transfer، debt، installment، check، budget، relation و attachment metadata باشند.
- fixture واقعی anonymize شود؛ هیچ داده شخصی وارد Git نشود.
- checksum و expected schema version برای هر fixture ثبت شود.

**Acceptance:** هر fixture در CI copy می‌شود، migrate می‌شود و fixture اصلی دست‌نخورده می‌ماند.

### DBS-002 — Android legacy rename بدون حذف `[P0 / 4.5.1]`

**مراحل:**

1. تمام نام‌های دیتابیس منتشرشده و sidecarهای `-wal`/`-shm` را inventory کن.
2. قبل از عملیات connectionها بسته و checkpoint انجام شود.
3. safety copy در app-owned storage با checksum بساز.
4. rename اتمیک در همان filesystem؛ در صورت نیاز copy+fsync+verify.
5. فقط پس از open/migrate/integrity success، legacy copy با retention policy پاک شود.
6. interruption در هر مرحله تست شود.

**ممنوع:** `deleteDatabase()` برای حل naming/migration.

### DBS-003 — یک lifecycle مشترک برای Android/iOS/JVM/JS `[P0 / 4.5.1]`

- driver API باید وضعیت `New`, `Existing(version)`, `Corrupt`, `UnsupportedFutureVersion` برگرداند.
- New → `Schema.create`.
- Existing older → `Schema.migrate`.
- Future version → fail closed، بدون downgrade/destructive create.
- JS/Web Worker باید migration callback و reopen test داشته باشد.
- catch-all silent حذف شود؛ خطا typed و user-safe باشد.

### DBS-004 — Schema convergence و SQLDelight verification `[P0 / 4.5.1]`

1. `schemaOutputDirectory` و `verifyMigrations` فعال شود.
2. snapshot پایه `.db` commit شود.
3. fingerprint شامل table/column/type/nullability/index/foreign-key برای تمام variantهای شناخته‌شده ساخته شود؛ `user_version` به‌تنهایی کافی نیست.
4. برای `fresh-current`، هر مسیر `upgraded-current` و schemaهای نیمه‌مهاجرت‌یافته fixture مستقل باشد.
5. برای هر fingerprint شناخته‌شده مسیر همگرایی مشخص؛ fingerprint ناشناخته fail-closed و safety export.
6. blind `ALTER TABLE` روی schema جاری ممنوع؛ وجود واقعی ستون/variant قبل از convergence تعیین شود.
7. migration جدید append-only باشد؛ migration قبلی منتشرشده rewrite نشود.
8. `verifySqlDelightMigration` بخشی از `check` و CI شود.

**Acceptance:**

- fresh create و upgrade از هر fixture schema معادل داشته باشند.
- migration دوباره اجراشدنی یا به‌درستی version-guarded باشد.
- integrity و foreign key check سبز.

### DBS-005 — Backup envelope نسخه‌دار `[P1 / 5.0.0]`

```text
BackupEnvelope
  formatVersion
  appVersion
  createdAt
  sourcePlatform
  locale/calendar metadata
  entities[]
  relations[]
  attachmentsManifest[]
  checksums
  encryptionMetadata
```

- تمام entityها و edgeها explicitly DTO شوند؛ model دامنه مستقیماً wire format نباشد.
- registry ماشین‌خوان برای هر table/edge: `Included`, `Derived` یا `Excluded(reason)`؛ table جدید بدون classification در CI fail شود.
- attachment با content hash و blob جدا؛ absolute path ممنوع.
- checksum قبل از restore.
- import قدیمی adapter نسخه‌دار داشته باشد.
- export ادعای encryption نکند مگر authenticated encryption واقعی استفاده شود.
- دو mode جدا:
  - local safety snapshot با platform-secured key و غیرقابل انتقال؛
  - portable backup با passphrase/recovery user و KDF پارامترمند نسخه‌دار.
- هر دو mode: AEAD، salt/nonce تصادفی، tamper/wrong-key rejection، size/parser limit و بدون fallback خاموش به plaintext.
- XOR قدیمی فقط import صریح و قرنطینه‌ای؛ plaintext fallback خودکار ممنوع.

### DBS-006 — Restore اتمیک و قابل پیش‌نمایش `[P1 / 5.0.0]`

مراحل:

1. parse + schema validation؛
2. checksum؛
3. compatibility plan؛
4. dry-run و نمایش counts/conflicts/missing attachments؛
5. safety snapshot؛
6. ساخت کامل database و attachment namespace در staging؛
7. graph/integrity/foreign-key/blob validation پیش از فعال‌سازی؛
8. atomic activation/swap در صورت پشتیبانی filesystem؛ در غیر این صورت recovery journal با startup finalize/rollback؛
9. نتیجه دقیق inserted/updated/skipped/failed.

Mode restore صریح باشد:

- `Replace/NewWorkspace` تا قبل از global ID امن؛
- `Merge` فقط پس از global ID، collision policy و relation/tombstone tests.

**Failure:** دیتابیس قبلی و فایل‌ها باید قابل بازیابی باشند.

### DBS-007 — Relation mapping و referential deletion `[P1]`

- aggregateهای موازی `GROUP_CONCAT` در Transaction/FixedExpense/Shopping با relation row typed و batch query جایگزین شوند.
- policy هر entity: Archive، Replace Reference، Delete History.
- soft-deleted tag/person/source در joinها ظاهر نشود.
- unsafe `INSERT OR REPLACE` با UPSERT مشخص جایگزین شود.
- orphan repair قبل از فعال‌کردن foreign keys.
- سپس `foreign_keys=ON` روی همه connectionها و `foreign_key_check`.

### DBS-008 — Attachment Store امن `[P1]`

```text
AttachmentId -> metadata -> app-owned canonical path/content URI
```

- input path از backup/sync هیچ‌وقت مستقیم خوانده/حذف نشود.
- containment check، type/size validation و content hash.
- update ابتدا metadata commit؛ فایل قبلی با cleanup queue پس از موفقیت حذف شود.
- reference count یا ownership برای جلوگیری از orphan/delete مشترک.
- Android، iOS، Desktop و Web adapter و capability صریح.

### SEC-001 — App Lock و credential storage `[P1]`

**Evidence:**

- `feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/LockViewModel.kt:127-145`
  در ناسازگاری enabled/hash می‌تواند fail-open شود و شمارنده/cooldown تلاش ندارد.
- همان ViewModel و recovery seed از hash سفارشی استفاده می‌کنند.
- داده‌ها و tokenها در preference عمومی هستند.

**مراحل:**

- state ناسازگار fail-closed با recovery کنترل‌شده؛
- KDF استاندارد و secure platform storage؛
- constant-time verification؛
- attempt counter، cooldown افزایشی و audit محلی غیرحساس؛
- biometric فقط adapter پلتفرم و fallback روشن؛
- legacy credential فقط پس از verify موفق migrate شود؛
- security question به‌عنوان recovery ضعیف بازطراحی یا حذف شود.

**Acceptance:** brute-force آنلاین محدود، migration کاربر را قفل نکند و نبود hash هرگز app را بی‌صدا باز نکند.

## امنیت داده

- CVV/card security code هرگز persist نشود.
- migration قبل از 5.0 مقدارهای CVV موجود را پاک و ستون/فیلد را از model، DTO، backup و sync حذف کند.
- شماره‌ها حداقل‌سازی و mask شوند.
- Release: cleartext ممنوع؛ debug exception محدود به host توسعه.
- Android backup rules allowlist/exclude واقعی داشته باشند.
- token/API key/PIN hash در secure platform storage.
- هیچ secret یا نمونه داده مالی در fixture/log/Crashlytics.

## Feature Gate و Rollback

- `backup_export_v2`: Preview تا round-trip موفق همه fixtureهای نسخه‌های منتشرشده و variantهای schema.
- `restore_v2`: فقط پس از safety snapshot و dry-run.
- `cloud_sync`: Disabled تا تکمیل plan 006.
- Gate خاموش schema migration را متوقف نمی‌کند.
- هر migration release یک rollback عملیاتی دارد، نه SQL downgrade کور.

## Verification

```powershell
.\gradlew.bat :core:database:verifySqlDelightMigration --no-daemon --console=plain
.\gradlew.bat :core:database:jvmTest --no-daemon --console=plain
.\gradlew.bat check --no-daemon --console=plain
git status --short
```

نام دقیق task generated schema و migration پس از فعال‌کردن plugin با `:core:database:tasks --all`
ثبت شود. Test matrix پلتفرمی باید شامل create → write → close → reopen → upgrade → read باشد.

## خروجی‌های لازم

- schema/migration ADR؛
- fixture manifest؛
- Backup format specification؛
- Restore failure/recovery runbook؛
- attachment capability matrix؛
- گزارش upgrade هر نسخه منتشرشده.

## مرجع رسمی

- [SQLDelight migrations and verification](https://sqldelight.github.io/sqldelight/2.1.0/multiplatform_sqlite/migrations/)
  توضیح می‌دهد migrationها چگونه version می‌شوند و `verifySqlDelightMigration` چگونه fixtureهای schema قبلی را تا schema جاری اعتبارسنجی می‌کند.
