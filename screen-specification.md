# RECRUITMENT MANAGEMENT SYSTEM
## SCREENS SPECIFICATION & TECHNICAL BLUEPRINT
> **Tech Stack:** Spring Boot 4.1.0, Thymeleaf, MySQL 8+, Custom `PasswordUtil` for hashing.
> **UI Tech Stack:** Bootstrap 5
> **Icon Libraries:** Phosphor Icons (Bold/Fill weights), Radix UI Icons.
> **System Language:** English

---

## 1. USER AUTHENTICATION & PROFILE MANAGEMENT
*   **Authorization Mechanism:** Custom `AuthInterceptor` via `HttpSession` (Spring Security is **NOT** used).
*   **Interceptor Exclusions:** URLs matching `/auth/**`, `/css/**`, `/js/**`, `/error/**`, and the root `/` are excluded via `WebConfig`.
*   **Password Cryptography:** All password handling must inject and use `PasswordUtil`:
    *   Hashing: `passwordUtil.hash(rawPassword)`
    *   Verification: `passwordUtil.matches(rawPassword, hashedPassword)`

### SCR-01: User Login
*   **URL:** `/auth/login`
*   **Description:** Entry point for system authentication.
*   **UI Components:** Form fields for `username`/`email`, `password`, a "Sign In" submit button, and hyperlinked navigation to `SCR-03` (`/auth/register`) and `SCR-02` (`/auth/forgot-password`).
*   **Backend Logic:** 
    *   The form input must be bound and validated using Spring's `@Valid` and `BindingResult`.
    *   Verify the candidate credentials using `passwordUtil.matches()`. On success, instantiate a session and bind the `SessionUser` object to `SessionConstants.LOGGED_IN_USER`.
    *   Redirect based on `RoleName`:
        *   `ADMIN` $\rightarrow$ `/admin/dashboard`
        *   `HR_MANAGER` $\rightarrow$ `/hr/dashboard`
        *   `INTERVIEWER` $\rightarrow$ `/interviewer/dashboard`
        *   `CANDIDATE` $\rightarrow$ `/candidate/my-applications`

### SCR-02: Password Reset
*   **URL:** `/auth/forgot-password`
*   **UI Components:** Text input field for user's recovery email address.
*   **Backend Logic:** Validate email syntax using standard Spring validators. Generate a time-bound secure token and dispatch an account recovery email containing a reset hyperlink.

### SCR-03: User Register
*   **URL:** `/auth/register`
*   **Description:** Public-facing self-service registration for new candidates.
*   **UI Components:** Registration form for `Full Name`, `Email`, `Password`, and `Confirm Password`.
*   **Backend Logic:** 
    *   Enforce comprehensive input constraint validations via `@Valid` and check `BindingResult`.
    *   Ensure password and confirm password fields are strictly identical.
    *   Enforce uniqueness checks on the Email column in the DB. Encrypt the raw password string using `passwordUtil.hash()`, default the account role to `"CANDIDATE"`, set `enabled = true`, and persist the user entity.

### SCR-04: User Profile
*   **URL:** `/profile`
*   **Description:** Display user details accessed through a top navigation drop-down menu using session-bound parameters.
*   **UI Components:** Information cards rendering the authenticated user's details. Data should be refreshed by querying the DB using the active Session ID to guarantee state synchronization.

### SCR-05: Password Change
*   **URL:** `/profile/change-password`
*   **UI Components:** Form fields for `Old Password`, `New Password`, and `Confirm New Password`.
*   **Backend Logic:** 
    *   Validate password field criteria (e.g., length, alphanumeric content) using Spring constraints via `BindingResult`.
    *   Validate the entered current password using `passwordUtil.matches()`. If authenticated, compute the cryptographic hash of the new password using `passwordUtil.hash()` and save it to the DB.

---

## 2. SYSTEM ADMINISTRATION & REPORTING
*   **Security Constraint:** All routes nested under `/admin/**` are intercepted. If `SessionUser.getRoleName()` evaluates to anything other than `"ADMIN"`, the execution context must immediately halt and issue a redirect to `/error/403`.

### SCR-07: Admin Dashboard
*   **URL:** `/admin/dashboard`
*   **Description:** Centralized control panel for users with the `ADMIN` role. Left sidebar contains navigation items mapping to `/admin/users` and `/admin/activity-log`.

### SCR-08: User Management
*   **URL:** `/admin/users`
*   **UI Components:** 
    *   Tabular layout displaying internal staff registries (`HR_MANAGER`, `INTERVIEWER`).
    *   Interactive Modal forms for internal account generation.
    *   Actionable toggles: "Deactivate" (Sets `enabled = false` to trigger automatic session invalidation upon their next request in the Interceptor tier) and "Unlock".
*   **Backend Logic:** Enforce user field structural constraints on the creation form via `@Valid`. New accounts must have their default passwords programmatically initialized using `passwordUtil.hash()`.

### SCR-09: Activity Log
*   **URL:** `/admin/activity-log`
*   **UI Components:** Historical event log data table capturing system-wide data mutations. Implement server-side pagination utilizing Spring Data's `Pageable`.

### SCR-06: HR Dashboard
*   **URL:** `/hr/dashboard`
*   **Role Constraint:** Restricted to `HR_MANAGER` or `ADMIN`.
*   **UI Components:** Aggregated analytic widgets illustrating live job positions and newly filed candidacies. Interacting with the pipeline dashboard component routes users to `/hr/reports/pipeline`.

### SCR-20: Pipeline Report
*   **URL:** `/hr/reports/pipeline`
*   **Description:** Visual performance funnel and detailed analytics showcasing progression statuses across various job openings.

---

## 3. JOB MANAGEMENT
*   **Role Constraint:** Confined to `/hr/jobs/**` paths. Requires `HR_MANAGER` or `ADMIN` roles to clear the interceptor checks.

### SCR-10: Job List (Internal)
*   **URL:** `/hr/jobs`
*   **UI Components:** A comprehensive summary table showing all job positions, tracking flags (`Draft`, `Active`, `Closed`), and associated application metric tallies.

### SCR-11: Job Form (Create/Edit)
*   **URL:** `/hr/jobs/new` or `/hr/jobs/{id}/edit`
*   **UI Components:** Input form utilizing Thymeleaf's `th:object` attribute for deep state object data-binding.
*   **Backend Logic:** Apply strict field validation rules (e.g., `@NotBlank` on Job Title, `@NotNull` on Expiry Dates) within the Controller argument declaration. If `bindingResult.hasErrors()` evaluates to `true`, instantly return the form layout displaying embedded structural error diagnostics.

### SCR-12: Job Detail (Internal)
*   **URL:** `/hr/jobs/{id}`
*   **UI Components:** Comprehensive layout featuring deep structural data viewports. Includes segmented routing tabs: Tab "Applications" (`/hr/jobs/{id}/applications`) and Tab "Report" (`/hr/reports/pipeline`).

---

## 4. CANDIDATE PORTAL
*   **Security Context:** Public access endpoints. Must not be blacklisted by the custom `AuthInterceptor` registry.

### SCR-13: Public Job List
*   **URL:** `/jobs`
*   **Description:** Unauthenticated index accessible by guests. Filters and exhibits job entities whose state explicitly reads `Active`.

### SCR-14: Public Job Detail & Application
*   **URL:** `/jobs/{id}`
*   **UI Components:** Extended text formatting area for job scopes. Contains a primary "Apply" trigger button.
*   **Application Submission Logic:**
    *   The target POST endpoint must actively inspect the `HttpSession` for `SessionConstants.LOGGED_IN_USER`.
    *   If missing $\rightarrow$ Issue a redirect command pointing to `/auth/login`.
    *   If active session exists $\rightarrow$ Accept the form payload alongside a `MultipartFile` handler for CV documents. Store the record to the database initializing the tracking phase marker to `APPLIED`.

### SCR-15: My Applications
*   **URL:** `/candidate/my-applications`
*   **UI Components:** Tabular interface tracking historical applications with real-time process updates.
*   **Business Constraints:** The "Withdraw" action control must have its visibility and server-side processing capabilities conditionally blocked unless the application state evaluates to `APPLIED` or `SCREENING`.

---

## 5. RECRUITMENT PIPELINE & INTERVIEW EVALUATION

### SCR-16: Application List
*   **URL:** `/hr/applications` or `/hr/jobs/{jobId}/applications`
*   **UI Components:** Funnel lists filterable by stage tags (`Applied`, `Screening`, `Interview`, `Offered`, `Rejected`).

### SCR-17: Application Detail
*   **URL:** `/hr/applications/{id}` or `/interviewer/applications/{id}`
*   **Controller Isolation Logic (Data Isolation):**
    *   If the current session profile maps to `INTERVIEWER`: The controller method must run an explicit database join query verifying whether this specific interviewer entity has an assigned schedule linking them to the application parameter ID. If no relation maps out $\rightarrow$ Instantly block the threat vector via `return "redirect:/error/403";`.
    *   If authorization passes: Expose the interface panel providing internal action tools or route forwards to `SCR-19`.

### SCR-18: Interview Assignment
*   **URL:** `/hr/applications/{id}/assign-interview`
*   **UI Components:** Scheduling assembly form. Contains a selection dropdown populated strictly with user profiles whose DB `RoleName` is `"INTERVIEWER"`.
*   **Backend Logic:** Bind forms via `@Valid` check mechanisms to guarantee proper timestamp values are entered.

### SCR-19: Evaluation Form
*   **URL:** `/interviewer/applications/{id}/evaluate`
*   **UI Components:** Form layout presenting a selection element for numerical score ratings, a raw text area for written qualitative feedback, and a "Submit Evaluation" trigger.
*   **Immutable State Constraint:** Form values must pass through standard Spring validator definitions before computing changes. Once processing resolves, update the matching record status to `EVALUATED`. Any subsequent GET/POST traffic directed towards this route signature by an interviewer must be programmatically short-circuited via checking evaluation state: `if("EVALUATED".equals(evaluation.getStatus())) { return "redirect:/error/403"; }`.