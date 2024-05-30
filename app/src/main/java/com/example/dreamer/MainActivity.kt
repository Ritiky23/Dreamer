import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dreamer.R
import com.example.dreamer.ui.LoginActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)

        // Find the button by its ID
        val loginButton = findViewById<Button>(R.id.login_button1)

        // Set a click listener on the button
        loginButton.setOnClickListener {
            // Create an intent to navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            // Start the LoginActivity
            startActivity(intent)
        }
    }
}
