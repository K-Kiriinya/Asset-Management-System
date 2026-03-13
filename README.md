# Asset Management System
This is a learning experience creating a website using html and java 

# Database Schema

The Asset Management System uses a MariaDB database with three main tables: `users`, `assets`, and `asset_assignments`. Below is a detailed description of each table.

---

## Table: `users`

Stores all system users (both administrators and regular users).

| Column      | Type                      | Null | Key | Default | Extra          | Description                             |
|-------------|---------------------------|------|-----|---------|----------------|-----------------------------------------|
| `userid`    | int(11)                   | NO   | PRI | NULL    | auto_increment | Unique user identifier                  |
| `full_name` | varchar(120)              | NO   |     | NULL    |                | User's full name                        |
| `username`  | varchar(60)               | NO   | UNI | NULL    |                | Unique login username                   |
| `email`     | varchar(120)              | NO   | UNI | NULL    |                | Unique email address                    |
| `password`  | varchar(255)              | NO   |     | NULL    |                | Hashed password (plain for demo)        |
| `role`      | enum('ADMIN','USER')      | NO   |     | USER    |                | User role – determines access level     |
| `status`    | enum('ACTIVE','DISABLED') | NO   |     | ACTIVE  |                | Account status                          |
| `created_at`| timestamp                 | YES  |     | CURRENT_TIMESTAMP | | Timestamp of account creation           |

---

## Table: `assets`

Contains all assets managed in the system.

| Column         | Type                                          | Null | Key | Default             | Extra          | Description                             |
|----------------|-----------------------------------------------|------|-----|---------------------|----------------|-----------------------------------------|
| `asset_id`     | int(11)                                       | NO   | PRI | NULL                | auto_increment | Unique asset identifier                 |
| `asset_tag`    | varchar(40)                                   | NO   | UNI | NULL                |                | Unique asset tag (e.g., barcode)        |
| `asset_name`   | varchar(120)                                  | NO   |     | NULL                |                | Descriptive name of the asset           |
| `category`     | varchar(80)                                   | NO   |     | NULL                |                | Asset category (e.g., Laptop, Printer)  |
| `asset_status` | enum('AVAILABLE','ASSIGNED','DECOMMISSIONED') | NO   |     | AVAILABLE           |                | Current status                          |
| `created_at`   | timestamp                                     | YES  |     | CURRENT_TIMESTAMP |                | Timestamp when asset was added           |

---

## Table: `asset_assignments`

Tracks the assignment history of assets to users. This is a junction table linking `users` and `assets`.

| Column          | Type      | Null | Key | Default             | Extra          | Description                                 |
|-----------------|-----------|------|-----|---------------------|----------------|---------------------------------------------|
| `assignment_id` | int(11)   | NO   | PRI | NULL                | auto_increment | Unique assignment identifier                 |
| `asset_id`      | int(11)   | NO   | MUL | NULL                |                | Foreign key referencing `assets(asset_id)`   |
| `user_id`       | int(11)   | NO   | MUL | NULL                |                | Foreign key referencing `users(userid)`      |
| `assigned_at`   | timestamp | YES  |     | CURRENT_TIMESTAMP |                | Date and time when asset was assigned        |
| `returned_at`   | timestamp | YES  |     | NULL                |                | Date and time when asset was returned (if any) |
| `notes`         | text      | YES  |     | NULL                |                | Optional notes added by admin during assignment |

**Relationships:**
- `asset_assignments.asset_id` → `assets.asset_id` (foreign key)
- `asset_assignments.user_id` → `users.userid` (foreign key)

---

## Summary Diagram
