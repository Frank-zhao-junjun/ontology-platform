# VS Code Skill Index Workflow

This workspace includes a VS Code-friendly skill index workflow so you can use your local skill library without Cursor-native skill loading.

## What It Does

- Scans local skill folders (Cursor, Claude, and your repo skill library).
- Builds a searchable markdown index at `tools/skill-index/SKILL_INDEX.md`.
- Exposes commands in VS Code Command Palette via Tasks.

## Usage

1. Open Command Palette (`Ctrl+Shift+P`)
2. Run `Tasks: Run Task`
3. Choose one of:
   - `Skills: Rebuild Local Index`
   - `Skills: Open Local Index`

## Scanned Sources

- `%USERPROFILE%\.cursor\skills`
- `%USERPROFILE%\.cursor\skills-cursor`
- `%USERPROFILE%\.claude\skills`
- `E:\00 - AI\00 - Skills\SKILLS`

## Notes

- If you add new skills, rerun `Skills: Rebuild Local Index`.
- You can edit `tools/skill-index/rebuild-skill-index.ps1` to add/remove scan paths.
