package com.mbgee.paymentplatform

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var content: LinearLayout

    private val apiBaseUrl = "http://10.0.2.2:8000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    private fun baseLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 24)
            setBackgroundColor(Color.WHITE)
        }
    }

    private fun title(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 28f
            setTextColor(Color.rgb(20, 20, 20))
            setPadding(0, 0, 0, 24)
        }
    }

    private fun button(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { action() }
        }
    }

    private fun showLogin() {
        val root = baseLayout()

        root.addView(title("Payment Platform"))

        val subtitle = TextView(this).apply {
            text = "Developer account"
            textSize = 16f
        }
        root.addView(subtitle)

        val email = EditText(this).apply {
            hint = "Email"
            inputType = 33
        }

        val password = EditText(this).apply {
            hint = "Password"
            inputType = 129
        }

        root.addView(email)
        root.addView(password)

        root.addView(button("Sign in") {
            showDashboard()
        })

        root.addView(button("Create account") {
            showRegister()
        })

        setContentView(root)
    }

    private fun showRegister() {
        val root = baseLayout()

        root.addView(title("Create account"))

        val name = EditText(this).apply {
            hint = "Name"
        }

        val email = EditText(this).apply {
            hint = "Email"
            inputType = 33
        }

        val password = EditText(this).apply {
            hint = "Password"
            inputType = 129
        }

        root.addView(name)
        root.addView(email)
        root.addView(password)

        root.addView(button("Create account") {
            showDashboard()
        })

        root.addView(button("Back to login") {
            showLogin()
        })

        setContentView(root)
    }

    private fun showDashboard() {
        val root = baseLayout()

        root.addView(title("Developer Dashboard"))

        val search = EditText(this).apply {
            hint = "Search projects, API keys, payments..."
        }
        root.addView(search)

        val scroll = ScrollView(this)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        addSection("Overview", "View your payment platform activity.") {
            showOverview()
        }

        addSection("Projects", "Create and manage applications.") {
            showProjects()
        }

        addSection("API Keys", "Create, revoke and rotate keys.") {
            showApiKeys()
        }

        addSection("Payments", "View payment activity.") {
            showPayments()
        }

        addSection("Webhooks", "Configure payment event notifications.") {
            showWebhooks()
        }

        addSection("Documentation", "Find API integration instructions.") {
            showDocumentation()
        }

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        root.addView(button("Log out") {
            showLogin()
        })

        setContentView(root)
    }

    private fun addSection(
        name: String,
        description: String,
        action: () -> Unit
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
        }

        val heading = TextView(this).apply {
            text = name
            textSize = 20f
            setTextColor(Color.BLACK)
        }

        val desc = TextView(this).apply {
            text = description
            textSize = 14f
            setPadding(0, 8, 0, 8)
        }

        card.addView(heading)
        card.addView(desc)
        card.addView(button("Open") { action() })

        content.addView(
            card,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )
    }

    private fun detailScreen(name: String, text: String) {
        val root = baseLayout()

        root.addView(title(name))

        root.addView(TextView(this).apply {
            this.text = text
            textSize = 16f
        })

        root.addView(button("Back") {
            showDashboard()
        })

        setContentView(root)
    }

    private fun showOverview() {
        detailScreen(
            "Overview",
            """
            Payment Platform

            Projects: 0
            Active API keys: 0
            Payments: 0
            Pending payments: 0

            Your platform is ready for developer integrations.
            """.trimIndent()
        )
    }

    private fun showProjects() {
        detailScreen(
            "Projects",
            """
            No projects yet.

            Projects will represent the websites and mobile
            applications using your payment API.
            """.trimIndent()
        )
    }

    private fun showApiKeys() {
        detailScreen(
            "API Keys",
            """
            No API keys yet.

            API secrets will be displayed only when created.
            Stored secrets are never displayed again.
            """.trimIndent()
        )
    }

    private fun showPayments() {
        detailScreen(
            "Payments",
            """
            Payment activity

            No payments yet.
            """.trimIndent()
        )
    }

    private fun showWebhooks() {
        detailScreen(
            "Webhooks",
            """
            Webhooks allow your application to receive
            payment status events from the platform.
            """.trimIndent()
        )
    }

    private fun showDocumentation() {
        detailScreen(
            "Developer Documentation",
            """
            API integration

            1. Create a project.
            2. Create an API key.
            3. Store the secret securely on your server.
            4. Send authenticated requests to the payment API.
            5. Use webhooks to receive payment events.

            Never place a secret API key directly inside
            publicly distributed Android or browser code.
            """.trimIndent()
        )
    }
}
