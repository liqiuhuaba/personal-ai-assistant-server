# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workspace Overview

This workspace contains two related projects for a GOC (运维值班) intelligent alert system:

- **`jkgoc-agent/`** — Go service: real-time alert ingestion, noise filtering, chain-topology fault analysis, and auto-escalation decisions
- **`jk-goc-link-mange/`** — Java/Spring Boot service: link-chain metadata management, alert collection storage, GOC skill invocation, and LLM risk recognition

---

## jkgoc-agent (Go)

### Commands

```bash
# Start dev server (builds & restarts)
./restart.sh

# Build all binaries (local + Linux cross-compile)
./build.sh

# Compile only
go build ./...

# Run tests
go test ./pkg/... ./internal/...

# Run a single test package
go test ./pkg/faultanalyzer/...

# Manual start
go build -o bin/jkgoc-webserver ./cmd/webserver
./bin/jkgoc-webserver --config=config.dev.yaml
```

Config file: `config.dev.yaml`. API endpoints for alerts and chain topology are configured under `api.*`.

### Architecture

The pipeline is: raw alerts → fingerprint extraction → environment classification → noise suppression (vs. historical profile baseline) → chain subgraph construction → fault cluster analysis → severity decision (P0–P3) → optional LLM escalation report.

Key packages:
- `pkg/pipeline/` — orchestrates the processing stages end-to-end
- `pkg/faultanalyzer/` — builds alert-induced subgraph (`alert_subgraph.go`), diagnoses fault clusters (`fault_diagnoser.go`), and decides escalation level (`fault_decision_maker.go`)
- `pkg/fingerprint/` — normalizes raw alerts into canonical fingerprint strings (`{type}:{value}`)
- `pkg/alertfilter/` — pre-filter: AllowedApps whitelist and SuppressApps blacklist
- `internal/profile/` — stores and matches historical alert profiles (baseline for noise detection)
- `internal/suppression/` — applies suppression decisions based on profile comparison
- `internal/storage/` — MySQL persistence for alert history and profiles
- `webapp/` — HTTP server, diagnosis API handlers, and HTML pages
- `chain/` — DAG data structures for call-chain topology
- `llm/` — LLM client for generating escalation reports
- `goc/` — GOC platform API client

Fault level decision rules (chain dimension):
| Level | Trigger | Action |
|-------|---------|--------|
| P0 | Entry app alarmed + chain ≥5 apps + propagation ≥3 layers | Auto-escalate |
| P1 | Chain ≥4 apps + propagation ≥3 layers | Suggest escalate |
| P2 | Chain anomaly events ≥50 | Alert notification |
| P3 | Any alarm + any anomaly | Monitor |

Alert similarity uses weighted Jaro-Winkler: AppName 30%, RK 30%, MSG 40%, keyword overlap +10%.

**Documentation invariant**: when changing rules, pipeline stages, or API behavior, update `docs/DOCUMENTATION.md` in the same commit. It is the authoritative source for all concepts and rules.

---

## jk-goc-link-mange (Java/Spring Boot)

### Commands

```bash
# Build all modules
mvn clean package -DskipTests

# Run tests
mvn test

# Run a single test class
mvn test -pl jk-goc-link-mange-app -Dtest=LinkSyncSchedulerTest

# Local IDE startup requires extra JVM args (JDK 17):
# --add-opens java.base/java.lang=ALL-UNNAMED
# --add-opens java.base/java.util=ALL-UNNAMED
# --add-opens java.base/sun.util.calendar=ALL-UNNAMED
# --add-opens java.base/java.math=ALL-UNNAMED
# --add-opens java.base/sun.security.action=ALL-UNNAMED
# --add-exports=java.base/sun.net.util=ALL-UNNAMED
```

### Architecture

DDD layered architecture with a Gateway anti-corruption pattern:

```
jk-goc-link-mange-web        → HTTP Controllers (@RestController)
jk-goc-link-mange-app        → Application Services, Schedulers, Resource Impl
jk-goc-link-mange-client     → DTOs / API contracts (shared with callers)
jk-goc-link-mange-infra-api  → Gateway interfaces + BOs (dataobject layer)
jk-goc-link-mange-infra-impl → Mapper implementations, MyBatis XMLs, RPC clients
jk-goc-link-mange-main       → Spring Boot entry point + config files
```

Infra pattern: `infra-api` defines Gateway interfaces and BO classes; `infra-impl` provides `@Service`-annotated implementations injecting MyBatis `@Mapper` classes. Never call Mappers directly from the app layer — always go through Gateway interfaces.

Framework: **DongBoot** (JD internal Spring Boot scaffold, parent POM `dong-boot-dependencies`). Middleware (DUCC, JSF, JimDB, JMQ, DongDAL) requires registration in JD's Taishan portal before local use. Config split across `application.yml` and `src/main/resources/profile/` component-specific `.properties` files.

Core domains:
- **Link management**: collects call-chain metadata from Dayu platform → MySQL (`link_main_info`) + Elasticsearch
- **Alert collection**: scheduled ingestion of GOC alerts, stored in `goc_alarm_storage` and `goc_alert_collect_log`
- **GOC reporting**: `GocReportService` drives event escalation via JSF (`GocReportJsfService`)
- **LLM risk recognition**: records model inferences in `goc_llm_risk_recognize_record`
- **Skill invocation**: `GocSkillInvokeService` calls registered skills; logs tracked in `goc_skill_invoke_log`

JSF services (exposed): `LinkPullJsfService`, `GocReportJsfService`, `GocRecordJsfService` — defined in `infra-api/jsf/`, registered via Taishan.
