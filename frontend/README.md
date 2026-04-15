# CPT202 Resource Contributor Frontend

## Overview

This project implements the Resource Contributor module for the CPT202 platform.

## Features

- **My Submissions** - View and manage submitted resources
- **Create / Edit Resource** - Form with file upload and draft saving
- **Submission Detail** - View details and review feedback

## Quick Start

```bash
cd frontend
npm install
npm run dev
```

Access at **http://localhost:5173**

## Pages

| Route | Page | Description |
|-------|------|-------------|
| `/` | Home | Feature overview |
| `/contributor/my-submissions` | My Submissions | Submission list |
| `/contributor/create` | Create Resource | New resource form |
| `/contributor/edit/:id` | Edit Resource | Edit existing |
| `/contributor/detail/:id` | Submission Detail | View details |

## Tech Stack

- Vue 3
- Vite
- Vue Router
- Pinia
- Axios

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/resources` | POST | Create resource |
| `/api/resources/my` | GET | Get submissions |
| `/api/resources/:id` | GET/PUT | View/Update |
| `/api/resources/:id/submit` | POST | Submit for review |
| `/api/resources/:id/upload` | POST | Upload file |
| `/api/resources/:id/files` | GET | List files |
| `/api/resources/:id/files/:fileId` | DELETE | Delete file |

## Workflow

```
Create Resource -> Fill Details -> Upload Files -> Save Draft -> Submit for Review
                                                              |
                                                    [Pending Review]
                                                              |
                                          +-------------------+-------------------+
                                          |                                       |
                                    [Approved]                             [Rejected]
                                                                                  |
                                                                   Edit & Resubmit
```

## Project Structure

```
frontend/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.js
    ├── App.vue
    ├── router/index.js
    ├── store/index.js
    ├── views/Home.vue
    └── components/Contributor/
        ├── MySubmissions.vue
        ├── ResourceForm.vue
        └── SubmissionDetail.vue
```
