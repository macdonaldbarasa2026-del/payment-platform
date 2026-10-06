package com.mbgee.paymentplatform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private val executor =
        Executors.newSingleThreadExecutor()

    private val defaultApiUrl =
        "http://10.0.2.2:8000"

    private var apiUrl = defaultApiUrl
    private var token: String? = null
    private var clientName = ""
    private var projects = JSONArray()

    override fun onCreate(
        state: Bundle?
    ) {
        super.onCreate(state)

        prefs = getSharedPreferences(
            "payment_platform",
            MODE_PRIVATE
        )

        apiUrl =
            prefs.getString(
                "api_url",
                defaultApiUrl
            ) ?: defaultApiUrl

        token =
            prefs.getString(
                "session_token",
                null
            )

        clientName =
            prefs.getString(
                "client_name",
                ""
            ) ?: ""

        if (token.isNullOrBlank()) {
            showLogin()
        } else {
            showDashboard()
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun api() =
        ApiClient(
            apiUrl,
            token
        )

    private fun root() =
        LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL

            setPadding(
                32,
                40,
                32,
                24
            )

            setBackgroundColor(
                Color.WHITE
            )
        }

    private fun title(
        value: String
    ) =
        TextView(this).apply {
            text = value
            textSize = 28f
            setTextColor(
                Color.rgb(20, 20, 20)
            )

            setPadding(
                0,
                0,
                0,
                20
            )
        }

    private fun field(
        hint: String,
        password: Boolean = false
    ) =
        EditText(this).apply {

            this.hint = hint

            inputType =
                if (password) {
                    InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT
                }
        }

    private fun button(
        label: String,
        action: () -> Unit
    ) =
        Button(this).apply {
            text = label
            setOnClickListener {
                action()
            }
        }

    private fun saveSession(
        newToken: String,
        name: String
    ) {
        token = newToken
        clientName = name

        prefs.edit()
            .putString(
                "session_token",
                newToken
            )
            .putString(
                "client_name",
                name
            )
            .putString(
                "api_url",
                apiUrl
            )
            .apply()
    }

    private fun showLogin() {

        val r = root()

        r.addView(
            title("Payment Platform")
        )

        r.addView(
            TextView(this).apply {
                text =
                    "Connect your developer account"
            }
        )

        val server =
            field("API server URL").apply {
                setText(apiUrl)
            }

        val email =
            field("Email")

        val password =
            field(
                "Password",
                true
            )

        r.addView(server)
        r.addView(email)
        r.addView(password)

        r.addView(
            button("Sign in") {

                apiUrl =
                    server.text
                        .toString()
                        .trim()
                        .trimEnd('/')

                prefs.edit()
                    .putString(
                        "api_url",
                        apiUrl
                    )
                    .apply()

                executor.execute {

                    try {

                        val result =
                            ApiClient(apiUrl)
                                .login(
                                    email.text
                                        .toString()
                                        .trim(),
                                    password.text
                                        .toString()
                                )

                        runOnUiThread {

                            if (
                                result.status
                                in 200..299
                            ) {

                                val client =
                                    result.body
                                        .optJSONObject(
                                            "client"
                                        )

                                saveSession(
                                    result.body
                                        .optString(
                                            "token"
                                        ),
                                    client?.optString(
                                        "name",
                                        ""
                                    ) ?: ""
                                )

                                showDashboard()

                            } else {
                                toast(
                                    error(
                                        result.body
                                    )
                                )
                            }
                        }

                    } catch (e: Exception) {

                        runOnUiThread {
                            toast(
                                "Connection failed: " +
                                    (e.message
                                        ?: "unknown error")
                            )
                        }
                    }
                }
            }
        )

        r.addView(
            button("Create account") {
                showRegister()
            }
        )

        setContentView(r)
    }

    private fun showRegister() {

        val r = root()

        r.addView(
            title("Create account")
        )

        val server =
            field("API server URL").apply {
                setText(apiUrl)
            }

        val name =
            field("Name")

        val email =
            field("Email")

        val password =
            field(
                "Password",
                true
            )

        r.addView(server)
        r.addView(name)
        r.addView(email)
        r.addView(password)

        r.addView(
            button("Create account") {

                apiUrl =
                    server.text
                        .toString()
                        .trim()
                        .trimEnd('/')

                executor.execute {

                    try {

                        val result =
                            ApiClient(apiUrl)
                                .register(
                                    name.text
                                        .toString()
                                        .trim(),
                                    email.text
                                        .toString()
                                        .trim(),
                                    password.text
                                        .toString()
                                )

                        runOnUiThread {

                            if (
                                result.status
                                in 200..299
                            ) {

                                val client =
                                    result.body
                                        .optJSONObject(
                                            "client"
                                        )

                                saveSession(
                                    result.body
                                        .optString(
                                            "token"
                                        ),
                                    client?.optString(
                                        "name",
                                        ""
                                    )
                                        ?: name.text
                                            .toString()
                                )

                                showDashboard()

                            } else {
                                toast(
                                    error(
                                        result.body
                                    )
                                )
                            }
                        }

                    } catch (e: Exception) {

                        runOnUiThread {
                            toast(
                                "Connection failed: " +
                                    (e.message
                                        ?: "unknown error")
                            )
                        }
                    }
                }
            }
        )

        r.addView(
            button("Back") {
                showLogin()
            }
        )

        setContentView(r)
    }

    private fun showDashboard() {

        val r = root()

        r.addView(
            title("Developer Dashboard")
        )

        r.addView(
            TextView(this).apply {
                text =
                    if (clientName.isBlank())
                        "Your workspace"
                    else
                        "Welcome, " +
                            clientName

                textSize = 16f
            }
        )

        val status =
            TextView(this).apply {
                text =
                    "Loading projects..."

                setPadding(
                    0,
                    16,
                    0,
                    16
                )
            }

        r.addView(status)

        r.addView(
            button("Create project") {
                showCreateProject()
            }
        )

        r.addView(
            button("Projects & API keys") {
                showProjects()
            }
        )

        r.addView(
            button("Payments") {
                showPayments()
            }
        )

        r.addView(
            button("Documentation") {
                showDocumentation()
            }
        )

        r.addView(
            button("Log out") {

                token = null

                prefs.edit()
                    .remove("session_token")
                    .remove("client_name")
                    .apply()

                showLogin()
            }
        )

        setContentView(r)

        loadProjects(status)
    }

    private fun loadProjects(
        status: TextView
    ) {

        executor.execute {

            try {

                val result =
                    api().projects()

                runOnUiThread {

                    if (
                        result.status
                        in 200..299
                    ) {

                        projects =
                            result.body
                                .optJSONArray(
                                    "projects"
                                )
                                ?: JSONArray()

                        status.text =
                            projects.length()
                                .toString() +
                            " project(s) connected"

                    } else if (
                        result.status == 401
                    ) {

                        token = null

                        prefs.edit()
                            .remove(
                                "session_token"
                            )
                            .apply()

                        showLogin()

                    } else {

                        status.text =
                            error(
                                result.body
                            )
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {

                    status.text =
                        "Unable to connect: " +
                        (
                            e.message
                                ?: "unknown error"
                        )
                }
            }
        }
    }

    private fun showCreateProject() {

        val r = root()

        r.addView(
            title("Create project")
        )

        val name =
            field("Project name")

        val description =
            field("Description")

        r.addView(name)
        r.addView(description)

        r.addView(
            button("Create project") {

                executor.execute {

                    try {

                        val result =
                            api()
                                .createProject(
                                    name.text
                                        .toString()
                                        .trim(),
                                    description.text
                                        .toString()
                                        .trim()
                                )

                        runOnUiThread {

                            if (
                                result.status
                                in 200..299
                            ) {

                                toast(
                                    "Project created"
                                )

                                showDashboard()

                            } else {

                                toast(
                                    error(
                                        result.body
                                    )
                                )
                            }
                        }

                    } catch (e: Exception) {

                        runOnUiThread {
                            toast(
                                "Connection failed: " +
                                    (
                                        e.message
                                            ?: "unknown error"
                                    )
                            )
                        }
                    }
                }
            }
        )

        r.addView(
            button("Back") {
                showDashboard()
            }
        )

        setContentView(r)
    }

    private fun showProjects() {

        val r = root()

        r.addView(
            title("Projects")
        )

        if (projects.length() == 0) {

            r.addView(
                TextView(this).apply {
                    text =
                        "No projects yet. " +
                        "Create a project first."

                    textSize = 16f
                }
            )
        }

        for (
            i in 0 until projects.length()
        ) {

            val project =
                projects
                    .optJSONObject(i)
                    ?: continue

            r.addView(
                button(
                    project.optString(
                        "name",
                        "Project"
                    )
                ) {
                    showProject(project)
                }
            )
        }

        r.addView(
            button("Create project") {
                showCreateProject()
            }
        )

        r.addView(
            button("Refresh") {
                showDashboard()
            }
        )

        r.addView(
            button("Back") {
                showDashboard()
            }
        )

        setContentView(r)
    }

    private fun showProject(
        project: JSONObject
    ) {

        val id =
            project.optString("id")

        val name =
            project.optString(
                "name",
                "Project"
            )

        val r = root()

        r.addView(
            title(name)
        )

        r.addView(
            TextView(this).apply {
                text =
                    "Project ID: " + id
            }
        )

        r.addView(
            button(
                "Create test API key"
            ) {
                createKey(
                    id,
                    "test"
                )
            }
        )

        r.addView(
            button(
                "Create live API key"
            ) {
                createKey(
                    id,
                    "live"
                )
            }
        )

        r.addView(
            button(
                "View API key metadata"
            ) {
                showKeys(
                    id,
                    name
                )
            }
        )

        r.addView(
            button("View payments") {
                showProjectPayments(
                    id,
                    name
                )
            }
        )

        r.addView(
            button("Back") {
                showProjects()
            }
        )

        setContentView(r)
    }

    private fun createKey(
        projectId: String,
        environment: String
    ) {

        executor.execute {

            try {

                val result =
                    api().createKey(
                        projectId,
                        environment
                    )

                runOnUiThread {

                    if (
                        result.status
                        in 200..299
                    ) {

                        val key =
                            result.body
                                .optJSONObject(
                                    "api_key"
                                )

                        showSecret(
                            key?.optString(
                                "key",
                                ""
                            ) ?: "",
                            environment
                        )

                    } else {

                        toast(
                            error(
                                result.body
                            )
                        )
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {
                    toast(
                        "Connection failed: " +
                            (
                                e.message
                                    ?: "unknown error"
                            )
                    )
                }
            }
        }
    }

    private fun showSecret(
        secret: String,
        environment: String
    ) {

        val r = root()

        r.addView(
            title("API key created")
        )

        r.addView(
            TextView(this).apply {
                text =
                    "Environment: " +
                    environment +
                    "\n\nThis secret is shown once. " +
                    "Copy it and store it securely " +
                    "on your server."

                textSize = 16f
            }
        )

        val key =
            field("API key").apply {
                setText(secret)
                setTextIsSelectable(true)
            }

        r.addView(key)

        r.addView(
            button("Copy API key") {

                val clipboard =
                    getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager

                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        "Payment Platform API key",
                        secret
                    )
                )

                toast("API key copied")
            }
        )

        r.addView(
            button("Done") {
                showDashboard()
            }
        )

        setContentView(r)
    }

    private fun showKeys(
        projectId: String,
        projectName: String
    ) {

        val r = root()

        r.addView(
            title(
                projectName +
                    " keys"
            )
        )

        val output =
            TextView(this).apply {
                text = "Loading..."
            }

        r.addView(output)

        r.addView(
            button("Back") {
                showDashboard()
            }
        )

        setContentView(r)

        executor.execute {

            try {

                val result =
                    api().keys(projectId)

                runOnUiThread {

                    if (
                        result.status
                        in 200..299
                    ) {

                        val keys =
                            result.body
                                .optJSONArray(
                                    "api_keys"
                                )
                                ?: JSONArray()

                        val text =
                            StringBuilder()

                        for (
                            i in 0 until keys.length()
                        ) {

                            val key =
                                keys.optJSONObject(i)
                                    ?: continue

                            text.append(
                                key.optString(
                                    "key_prefix"
                                )
                            )

                            text.append(
                                " — "
                            )

                            text.append(
                                key.optString(
                                    "environment"
                                )
                            )

                            text.append(
                                if (
                                    key.optBoolean(
                                        "revoked"
                                    )
                                )
                                    " — revoked\n"
                                else
                                    " — active\n"
                            )
                        }

                        output.text =
                            if (
                                text.isEmpty()
                            )
                                "No API keys yet."
                            else
                                text.toString()

                    } else {

                        output.text =
                            error(
                                result.body
                            )
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {

                    output.text =
                        "Connection failed: " +
                        (
                            e.message
                                ?: "unknown error"
                        )
                }
            }
        }
    }

    private fun showPayments() {

        if (projects.length() == 0) {

            detail(
                "Payments",
                "No projects yet."
            )

            return
        }

        val project =
            projects.optJSONObject(0)
                ?: return

        showProjectPayments(
            project.optString("id"),
            project.optString("name")
        )
    }

    private fun showProjectPayments(
        projectId: String,
        projectName: String
    ) {

        val r = root()

        r.addView(
            title(
                projectName +
                    " payments"
            )
        )

        val output =
            TextView(this).apply {
                text =
                    "Loading..."
            }

        r.addView(output)

        r.addView(
            button("Back") {
                showDashboard()
            }
        )

        setContentView(r)

        executor.execute {

            try {

                val result =
                    api().payments(
                        projectId
                    )

                runOnUiThread {

                    if (
                        result.status
                        in 200..299
                    ) {

                        val payments =
                            result.body
                                .optJSONArray(
                                    "payments"
                                )
                                ?: JSONArray()

                        if (
                            payments.length() == 0
                        ) {

                            output.text =
                                "No payments yet."

                        } else {

                            val text =
                                StringBuilder()

                            for (
                                i in 0 until payments.length()
                            ) {

                                val payment =
                                    payments
                                        .optJSONObject(i)
                                        ?: continue

                                text.append(
                                    payment.optString(
                                        "reference"
                                    )
                                )

                                text.append(
                                    " — "
                                )

                                text.append(
                                    payment.optString(
                                        "amount"
                                    )
                                )

                                text.append(
                                    " "
                                )

                                text.append(
                                    payment.optString(
                                        "currency"
                                    )
                                )

                                text.append(
                                    " — "
                                )

                                text.append(
                                    payment.optString(
                                        "status"
                                    )
                                )

                                text.append("\n")
                            }

                            output.text =
                                text.toString()
                        }

                    } else {

                        output.text =
                            error(
                                result.body
                            )
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {

                    output.text =
                        "Connection failed: " +
                        (
                            e.message
                                ?: "unknown error"
                        )
                }
            }
        }
    }

    private fun showDocumentation() {

        detail(
            "Developer Documentation",
            """
            Quick start

            1. Create an account.
            2. Create a project.
            3. Generate a test API key.
            4. Copy the secret once.
            5. Store the secret on your server.
            6. Send Authorization:
               Bearer <API_KEY>
            7. Create payments through
               POST /v1/payments.

            Never put a live secret key inside
            public Android or browser code.
            """.trimIndent()
        )
    }

    private fun detail(
        name: String,
        text: String
    ) {

        val r = root()

        r.addView(
            title(name)
        )

        r.addView(
            TextView(this).apply {
                this.text = text
                textSize = 16f
            }
        )

        r.addView(
            button("Back") {
                showDashboard()
            }
        )

        setContentView(r)
    }

    private fun error(
        body: JSONObject
    ): String =
        body.optString(
            "message"
        ).ifBlank {
            body.optString(
                "error",
                "Request failed."
            )
        }

    private fun toast(
        message: String
    ) =
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
}
