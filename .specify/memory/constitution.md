# speckit Constitution
<!--
Sync Impact Report:
- Version change: none -> 1.0.0
- Modified principles: none
- Added sections:
    - Principle: Strict Type Safety & Immutability (The Foundation)
    - Principle: The "Integration-First" Testing Strategy
    - Principle: User Experience & Consistency (The "Polite Application" Rule)
    - Principle: Clean Architecture & Separation of Concerns
    - Principle: Performance & Resource Hygiene
    - Principle: Dependency & Governance
- Removed sections: none
- Templates requiring updates:
    - ✅ .specify/templates/plan-template.md
    - ✅ .specify/templates/spec-template.md
    - ✅ .specify/templates/tasks-template.md
- Follow-up TODOs: none
-->

## Core Principles

### I. Strict Type Safety & Immutability (The Foundation)
- Enforce strict typing (no `any`).
- Data structures must be immutable by default (read-only across boundaries).
- State mutations are strictly isolated to specific layers; data passed between layers must be pure.

### II. The "Integration-First" Testing Strategy
- **Tests as Specs**: Tests must describe user behavior/outcomes, not implementation details.
- **The Red-Green-Refactor Rule**: No implementation code can be written without a preceding failing test.
- **Boundary Strategy**: Use real instances for internal logic (integration) but mock external volatile systems (Network/APIs) to ensure determinism.

### III. User Experience & Consistency (The "Polite Application" Rule)
- **Predictability**: Interfaces (CLI flags, API endpoints) must follow consistent naming patterns (e.g., standard casing, verb-noun structures).
- **Helpful Failure**: Error messages must be actionable. Never return raw stack traces to the user; provide a summary and a suggested fix.
- **Responsiveness**: Long-running operations must provide feedback (progress indicators). The main thread/UI must never block.

### IV. Clean Architecture & Separation of Concerns
- **Dependency Rule**: High-level business rules must never depend on low-level infrastructure (DB, UI, Frameworks).
- **Port/Adapter Pattern**: External tools are plugins. Changing a database or UI framework should not require refactoring business logic.

### V. Performance & Resource Hygiene
- **Lazy by Default**: Defer heavy module loading or processing until strict execution time.
- **Resource Respect**: Clean up file handles, connections, and memory immediately after use.
- **Streaming**: Prefer streaming data processing over loading large datasets entirely into memory.

### VI. Dependency & Governance
- **Standard Library First**: Do not import third-party packages for trivial tasks (e.g., `left-pad`).
- **Security First**: Input validation must occur at the system entry point. Trust no input.
- **Documentation**: Complex logic requires "Why" comments explaining the rationale, not just "What" the code does.

## Governance
This Constitution is the immutable "Source of Truth" for all code generation and project development. It supersedes all other practices. Amendments require a formal proposal, review, and an approved migration plan for existing code. All pull requests and code reviews must verify compliance with these principles. Complexity or deviation from these principles must be explicitly justified and approved.

**Version**: 1.0.0 | **Ratified**: 2026-01-06 | **Last Amended**: 2026-01-06