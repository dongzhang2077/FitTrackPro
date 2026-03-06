# Recommendation Strategy (V1)

## Intent and Decision

This document captures the product strategy decision discussed with the project owner:

- Current AI recommendation implementation is acceptable as a baseline.
- The next step should prioritize practical quality gains with low cost.
- V1 focuses on improving recommendation quality through a richer plan pool and lightweight rules.
- Heavy ML infrastructure (for example, dedicated cloud recommender pipelines) is out of scope for V1.

## Why This Direction

The current recommendation path already works and is stable:

- It can call an AI API with prompt-built context.
- It falls back to deterministic local logic when AI is unavailable.

Given time/token constraints, the fastest quality improvement is:

1. Better candidate plans (template coverage), and
2. Better ranking logic (goal/frequency-aware lightweight scoring).

## V1 Scope

### 1) Plan Pool Depth (Templates)

Expand starter templates so users have meaningful choices across:

- Primary goal: general fitness, muscle gain, fat loss, strength.
- Weekly frequency tiers: low (1-3), medium (4-5), high (6-7).
- Muscle focus/intensity tracks: chest, back, legs, full-body; light/medium/heavy.
- Equipment-friendly variants (for example, dumbbell-focused beginner options).

### 2) Lightweight Recommendation Logic

Keep recommendation deterministic and explainable:

- Use onboarding preferences and user history as ranking inputs.
- Keep AI as optional ranking/explanation enhancer over existing plan pool.
- Preserve robust fallback behavior at all times.

### 3) User Experience

- Show recommendation source (AI vs fallback) and reason.
- Keep recommendation explainable with short, human-readable rationale.

## Non-Goals (V1)

- No fully generated custom plans from raw exercise library.
- No advanced scientific modeling pipeline.
- No large cloud ML integration as a hard dependency.

## Staged Roadmap

- Now (V1): template expansion + preference-aware lightweight scoring.
- Next (V1.5): AI reranking/explanation over shortlisted plans.
- Later (V2): optional standalone advanced recommender project.

## Acceptance Criteria (V1)

- Onboarding goal/frequency data is persisted and consumed by recommendation.
- Seed/template plan coverage supports goal/frequency/muscle-tier starter scenarios.
- Fallback ranking is preference-aware and history-aware.
- Tests and CI checks remain green.
