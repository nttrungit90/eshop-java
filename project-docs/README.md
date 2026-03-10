# eShop Project Docs

Web portal for project documentation — migration progress, architecture, notes, and more.

## Quick Start

```bash
cd project-docs
npm install
npm run dev
```

Open **http://localhost:3333**

## Via Docker Compose

```bash
docker compose up project-docs -d
```

Open **http://localhost:3333**

## How It Works

The server reads all `.md` files from `data/` and renders them as tabs alongside the migration dashboard. To add a new page, just create a `.md` file in `data/`:

```
project-docs/data/
├── architecture.md          → "Architecture" tab
├── claude-instructions.md   → "Claude Instructions" tab
├── migration-plan.md        → "Migration Plan" tab
├── migration-progress.md    → "Migration Progress" tab
├── notes.md                 → "Notes" tab
├── tasks.yaml               → Powers the Dashboard tab
└── <any-name>.md            → Becomes a new tab automatically
```

Refresh the browser to see changes — data is loaded on each request.
