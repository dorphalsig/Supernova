# [TASK NAME]

## Purpose
Extend the testing-harness module with new shared testing utilities that will be reused across multiple test suites. Only create when existing harness components are insufficient.

## Definitions
- [Define new harness component and its testing domain]
- [Explain integration with existing BaseRoomTest, BaseSyncTest, etc.]
- [Specify which test scenarios this component will support]

## Rules
- Must be genuinely reusable across 3+ different test classes
- Cannot break existing harness functionality or API contracts
- Follow established patterns from BaseRoomTest, UiStateTestHelpers, etc.
- Place in testing-harness/src/main/kotlin/com/supernova/testing/
- Must extend/compose existing harness, not replace it
- Prefer moshi for anything JSON 

## Test Harness
- Test new harness components with simple inline test data
- Avoid creating fixtures for infrastructure tests
- Use existing harness components where possible
- Verify integration with BaseRoomTest, BaseSyncTest patterns
- Minimum 90% test coverage required