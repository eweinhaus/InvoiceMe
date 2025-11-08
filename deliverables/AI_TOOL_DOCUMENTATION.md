# AI Tool Documentation: InvoiceMe Development

## Overview

This document outlines the AI tools used during the development of InvoiceMe and how they accelerated development while maintaining architectural quality.

---

**Primary AI Tool Used** : Cursor
**Models**: Auto (for plannning and general tasks), Sonnet 4.5 and Codex (for difficult tasks).
**MCPs**: Context7 (for up to date tool documentation), Render (when deploying a project to Render), Playwright (UX/UI testing)

---

## AI Workflow:

## Planning
- Import directions document into repo
- Have discussion with AI on what the requirements are, what needs to be built, what features to implement, potential tech stack choices, etc. Ask questions, get reccomendations, answer questions AI has for me.
- Ask AI to create PRD and a mermaid architecture file
- Ask AI to shard PRD into sub PRDs (anywhere from 4-10, depending on project size and requirement complexity)

---

## Prompts Used

#### 1: Creating a task list (saved as a Cursor Rule)

**Prompt**:
```
#How to create a task list
If user says something like '!Create a task list for @file', your job is to follow these steps in order:

1. Read memory bank
2. Read any related PRD files in planning folder
3. Talk to the user about the high level goals of this task list. What needs to be accomplished? Describe to the user the high level overview of the success criteria
4. Then tell the user about potential pitfalls. What are some likely issues that may arise? Is anything overly complex? Is anything confusing?
5. Finally, create the task list (a .md file in '/planning/tasks/' folder) with well-defined tasks to complete

```

#### 2: Planning exactly how to execute task list (Saved as a Cursor Rule)

**Prompt**:
```
#How to create a task list
If user says something like '!Create a task list for @file', your job is to follow these steps in order:

1. Read memory bank
2. Read any related PRD files in planning folder
3. Talk to the user about the high level goals of this task list. What needs to be accomplished? Describe to the user the high level overview of the success criteria
4. Then tell the user about potential pitfalls. What are some likely issues that may arise? Is anything overly complex? Is anything confusing?
5. Finally, create the task list (a .md file in '/planning/tasks/' folder) with well-defined tasks to complete
```


#### 3: Executing plan

**Prompt**:
```
[I list any adjustments I want to make to the plan here]
Execute this plan accordingly.

```