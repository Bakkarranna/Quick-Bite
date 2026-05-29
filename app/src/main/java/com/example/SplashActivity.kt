package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SplashScreenContent {
                    navigateToNext()
                }
            }
        }
    }

    private fun navigateToNext() {
        val sharedPrefs = getSharedPreferences("quickbite_prefs", Context.MODE_PRIVATE)
        val email = sharedPrefs.getString("logged_in_email", null)
        val name = sharedPrefs.getString("logged_in_name", null)

        // Sanitize personal credentials instantly
        if (email != null && (email.contains("bakarrkhann") || email.contains("bakar") || name?.contains("Bakar") == true)) {
            sharedPrefs.edit()
                .putString("logged_in_email", "ahmad.khan@example.com")
                .putString("logged_in_name", "Ahmad Khan")
                .apply()
        }

        val finalEmail = sharedPrefs.getString("logged_in_email", null)

        val intent = if (finalEmail != null) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AuthActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun SplashScreenContent(onFinish: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate progress filling from 0% to 100% over 2.2 seconds
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000)
        )
        delay(300)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF4B3E),
                        Color(0xFFD93B2F)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative blobs
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .size(240.dp)
                .clip(RoundedCornerShape(120.dp))
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Rounded Icon Container
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "Fork",
                    tint = Color(0xFFB81313),
                    modifier = Modifier.size(64.dp)
                )
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Lightning",
                    tint = Color(0xFFFFA500),
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = 20.dp, y = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text
            Text(
                text = "QuickBite",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fresh food, fast delivery",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Animated loader at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f),
            )
        }
    }
}
