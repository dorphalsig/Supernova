# [TASK TITLE]

## Purpose
Add or modify Room entities, relationships, and DAOs with full test coverage.

## Definitions
- [Define @Entity fields and relations]
- [Explain DAO methods (inserts, queries, joins, FTS4 matches)]

## Rules
- Read Agents.md
- All data access must go through DAO (no raw SQL for FTS-backed tables)
- FTS4 + shadow table pattern required for search

## Test Harness
- Use `BaseRoomTest`, `DbAssertionHelpers`, and `TestEntityFactory`
- Use `CoroutineTestUtils` for dispatchers and scope control
- No migration tests — `fallbackToDestructiveMigration()` is enforced for MVP
- Minimum 70% test coverage required
- Extend / Reuse Test Harness components. Do NOT modify them.