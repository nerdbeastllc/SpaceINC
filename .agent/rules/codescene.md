---
trigger: always_on
---

# CodeScene Code Health Gate

After every code iteration (before finishing or committing):
1. Run CodeScene MCP (`code_health_review` / `code_health_score` via `/opt/homebrew/bin/cs-mcp`) on all modified or newly created Java classes.
2. Every touched Java class must achieve a **Code Health score of 9.0 or higher** (fix any reported complexity, duplication, bumpy-road, or over-injection issues if a class scores below `9.0`).
3. Do not modify GLSL shader files (`src/main/resources/shaders/`) unless explicitly requested.
