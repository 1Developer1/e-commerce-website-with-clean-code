# ADR 0004: Automated Architectural Enforcement

**Date:** 2026-02-13
**Status:** Accepted

## Context
Architectural rules (e.g., "Domain cannot depend on Infrastructure") are often forgotten as the team grows, leading to architectural erosion.

## Decision
We will enforce architecture via **Automated Tests** in the CI pipeline.
1.  **ArchUnit:** To enforce layer integrity, dependency directions, and naming conventions.
2.  **PMD & Checkstyle:** To enforce code quality, complexity limits, and coding standards.

## Consequences
**Positive:**
- Immediate feedback on architectural violations.
- Acts as living documentation.
- Prevents technical debt accumulation.

**Negative:**
- Initial setup cost.
- False positives may require tuning rulesets.

## Compliance
- `mvn test` must pass all ArchUnit rules.
- `mvn pmd:check` must meet defined thresholds.
