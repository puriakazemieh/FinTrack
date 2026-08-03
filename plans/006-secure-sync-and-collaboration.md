# Sync امن، Backup ابری و Collaboration

**Priority:** P0 برای quarantine؛ P2 برای بازطراحی و Phase 15/19  
**Planned at:** `51c80fb9` — 2026-07-29  
**نوع سند:** Epic Plan؛ هر مرحله پروتکل/client/server/workspace Task Card مستقل است.  
**Branch sequence:**

- `codex/006-disable-unsafe-sync`
- `codex/006-sync-protocol-v2`
- `codex/006-sync-server-v2`
- `codex/006-shared-workspaces`

## تصمیم فوری

Sync فعلی برای داده واقعی production مناسب نیست و باید با Product Gate برابر `Disabled` بماند.

علت‌های اثبات‌شده:

- credential مشترک داخل source server وجود دارد؛ باید حذف و rotate شود.
- caller-controlled `userId` مالکیت فایل را تعیین می‌کند.
- نام فایل از input ساخته می‌شود؛ path traversal/collision ممکن است.
- فایل JSON plaintext، write غیراتمیک و merge بدون lock است.
- IDهای autoincrement محلی دستگاه merge key هستند.
- BackupData entity/relation/attachment کامل ندارد.
- restore غیراتمیک است.
- client تغییر برخی entityها را اصلاً trigger upload نمی‌داند.
- HTTP non-2xx و exceptionها می‌توانند به موفقیت/`0 to 0` تبدیل شوند.
- UI restore تاریخچه timestamp را می‌فرستد ولی آخرین داده restore می‌شود.

**قاعده:** هیچ عامل نباید با patch موضعی این backend را «production-ready» اعلام کند.

## مرحله صفر — Incident containment

1. secret موجود حذف و در مقصد مربوط rotate/revoke شود؛ مقدار آن هرگز در issue/log/doc تکرار نشود.
2. Sync route از deployment عمومی خارج یا auth fail-closed شود.
3. client feature gate خاموش و UI با پیام «در حال آماده‌سازی» صادقانه.
4. cleartext release خاموش.
5. هر remote data موجود snapshot و access review شود.
6. security incident note و rotation record.

## مدل هویت و workspace

```text
User
  id: global immutable

Workspace
  id
  type: Personal | Family | Trip | PettyCash | Business

Membership
  userId
  workspaceId
  role: Owner | Admin | Editor | Viewer
```

- owner از token verified server-side؛ query `userId` منبع اعتماد نیست.
- delete user شامل export، ownership transfer، retention window و permanent deletion job.
- هر entity `globalId`, `workspaceId`, `revision`, `createdAt`, `updatedAt`, `deletedAt`.
- local numeric ID فقط index داخلی.
- global ID پیش از rollout Sync روی device تولید و تمام relationها backfill شود؛ collision report و rollback لازم است.

## پروتکل Sync v2

```text
ClientChangeBatch
  protocolVersion
  deviceId
  baseCursor
  operations[]

Operation
  operationId       // idempotency
  entityType
  entityGlobalId
  workspaceId
  baseRevision
  mutation/tombstone

ServerResponse
  acknowledgedOperationIds
  nextCursor
  remoteOperations[]
  conflicts[]
```

- server cursor، نه wall-clock دستگاه.
- tombstone و relation operation.
- ledger operation immutable/idempotent؛ balance LWW merge نشود.
- attachment blob جدا با hash/signed upload.
- retry همان operation duplicate نسازد.

Semantics batch:

- auth/schema/size validation failure کل batch را بدون cursor جدید رد کند؛
- mutationهای acknowledged و ثبت conflictها در یک server transaction commit شوند؛
- outcome هر operation یکی از `Acknowledged`, `Conflict`, `Rejected` باشد؛
- cursor فقط همراه همان commit جلو برود؛
- client فقط `Acknowledged`ها را از outbox خارج کند.

## Conflict policy

| داده | سیاست |
|---|---|
| Ledger transaction | immutable operation؛ edit به revision/new operation |
| Balance | derived؛ merge مستقیم ممنوع |
| Category/Tag label | revision + user conflict UI |
| Ordering/preferences | deterministic per-user merge |
| Relation | set operation/tombstone |
| Attachment | content hash + ownership |
| Delete vs edit | conflict صریح؛ silent resurrection ممنوع |

قرارداد ledger مشترک plan 001/006:

- Transaction فعلی projection قابل نمایش/ویرایش است.
- هر mutation مالی postingهای immutable تولید می‌کند؛ Edit یعنی reversal posting قبلی + replacement posting جدید در یک commit.
- Sync فقط operation/posting immutable و projection revision را منتقل می‌کند؛ `Source.balance` مستقیم LWW نمی‌شود.
- legacy row بدون posting/semantics version تا reconciliation در مسیر قرنطینه است.

## Server v2

- OIDC/OAuth یا auth provider معتبر؛
- storage تراکنشی با unique constraints؛
- optimistic revision؛
- body/rate/time limits؛
- TLS only؛
- idempotency key؛
- atomic commit؛
- tombstone retention طولانی‌تر از maximum offline window؛ device retirement و full-resync protocol؛
- audit log بدون payload مالی؛
- object key server-generated؛
- encryption at rest و key rotation؛
- backup/restore جدا از live sync.

## Client v2

- outbox محلی durable؛
- local mutation و enqueue outbox در یک database transaction؛
- تغییر تمام entityها و relationها؛
- mark synced فقط بعد از ack؛
- retry/backoff؛
- foreground/manual/background policy؛
- وضعیت صریح: Offline, Pending, Syncing, Conflict, Error, UpToDate؛
- never swallow transport errors؛
- non-2xx failure؛
- conflict center و recovery.
- apply remote operations و advance local cursor در یک database transaction؛
- stale device بعد از tombstone retention مجبور به full resync شود.

## Google Drive

در نسخه اول یکی از این دو نقش را انتخاب کند:

- export/import snapshot شخصی، یا
- transport رسمی Sync.

Drive و backend اختصاصی هم‌زمان دو source of truth نباشند. پیشنهاد: ابتدا backend canonical برای live sync؛ Drive فقط backup export نسخه‌دار.

## رمزنگاری

- Backup envelope مطابق دو mode مجزای plan 002: local device snapshot با platform key و portable backup با user passphrase/recovery؛
- authenticated encryption، KDF/versioning و بدون plaintext fallback؛
- server-side encryption مکمل است، نه جای end-to-end claim؛
- recovery/key loss policy؛
- old XOR backup فقط import adapter با هشدار؛
- CVV هرگز sync/backup نشود.

## پیش‌نیاز Collaboration

پس از سبز شدن Sync v2:

### Family account

- invitation و membership؛
- privacy per account/category؛
- shared vs private source؛
- audit trail و notification؛
- leave/transfer ownership.

### Dang/Trip

- participant shares؛
- چند ارز و نرخ snapshot؛
- settlement پیشنهادشده؛
- rounding invariant؛
- پرداخت جزئی؛
- share link محدود/منقضی.

### Petty cash

- request/approval؛
- receipt attachment؛
- limit و period؛
- role separation؛
- immutable audit trail؛
- export/report.

### Shared debt/receivable

- طرفین تأییدشده؛
- partial settlement history؛
- dispute/comment بدون تغییر silent ledger؛
- reminder consent؛
- share/masking.

## Test matrix

- دو device هم‌زمان ID مشابه؛
- retry همان batch؛
- offline edits؛
- clock skew؛
- edit/delete conflict؛
- relation tombstone؛
- partial network failure؛
- attachment upload/download corruption؛
- unauthorized workspace access؛
- path traversal payload؛
- account deletion؛
- backup restore then sync؛
- crash بین local mutation/outbox و crash بین remote apply/cursor؛
- protocol version old/new coexistence؛
- load/race on same workspace.

## Rollout

1. Internal با داده مصنوعی؛
2. Preview با workspace تازه و export اجباری؛
3. Beta cohort کوچک؛
4. remote v1 فقط با ownership proof، collision report و import قرنطینه‌ای؛
5. Stable پس از SLO و incident drill.

SLO پیشنهادی:

- acknowledged operation loss = 0؛
- duplicate ledger operation = 0؛
- unauthorized access = 0؛
- sync success > 99.5% برای شبکه سالم؛
- conflict قابل مشاهده، نه silent overwrite.

## Acceptance Phase 15

- auth/ownership server-side؛
- full graph round-trip؛
- delete/export user؛
- conflict UI؛
- encrypted backup؛
- no shared secret in client/source؛
- no raw financial payload in log؛
- disaster recovery و restore drill؛
- privacy policy و retention.

ابزار validate به‌تنهایی مجوز merge داده v1 به workspace v2 نیست.
