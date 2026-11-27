# SpaceINC Agent Instructions

## CodeScene Code Health Gate
After changing code, run `code_health_review` and `code_health_score` (via `/opt/homebrew/bin/cs-mcp`) on all modified or newly created Java classes.
Every touched Java class must achieve a **Code Health score of 9.0+** before finishing or committing. If any touched class scores below `9.0`, refactor to resolve the reported issues and re-verify with `code_health_score`.
