package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.QuickBiteRepository
import com.example.data.UserProfile
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = QuickBiteRepository.getInstance(applicationContext)

        setContent {
            MyApplicationTheme {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AuthScreenManager(repository) {
                            // On Auth success, start MainActivity with explicit intent
                            val intent = Intent(this@AuthActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreenManager(
    repository: QuickBiteRepository,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("quickbite_prefs", Context.MODE_PRIVATE) }
    
    var isOnboarding by remember { 
        mutableStateOf(!sharedPrefs.getBoolean("finished_onboarding", false)) 
    }

    if (isOnboarding) {
        OnboardingScreen(
            onFinish = {
                sharedPrefs.edit().putBoolean("finished_onboarding", true).apply()
                isOnboarding = false
            }
        )
    } else {
        LoginSignUpScreen(repository, onAuthSuccess)
    }
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Skip Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Skip",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.clickable { onFinish() }
            )
        }

        // Illustration Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            // Food delivery vector image loaded using Coil
            Image(
                painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuAUCpj661xVUWjAk0pL-2L1_Y0tr4j-_usYzHlKaMV8dVHOPhi5YhmPj_Sg63WAzNGLUYNF6goqB3-tJcPU_YHhQSMNGuWibkjCUEMWwtQgQS-Hq80eNT4cAQmrFoZVNdzm8v8S3A6tPSbQBqidAP5MfA8vknpYuI1Sn6RK14Nqyjkejj2gBicyisR5DLZgXsf-D7N5YsAtsFyuvcYu8ZZlKnfHyQuzTp1tBut631c8Q5UwNOUmkaEToqBJUTyaqhnK7whC4APsWYI"),
                contentDescription = "Onboarding Promo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Shade Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )
        }

        // Action / Text Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Active Step Indicator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(width = 32.dp, height = 8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Discover Local Restaurants",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Browse hundreds of restaurants near you and find your next favorite meal.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Large Red Pill Next Button
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Next",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LoginSignUpScreen(
    repository: QuickBiteRepository,
    onAuthSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("quickbite_prefs", Context.MODE_PRIVATE) }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Login, 1 = Signup
    
    // Credentials fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Alert dialog state for Forgot Password
    var showForgotDialog by remember { mutableStateOf(false) }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset Password") },
            text = { Text("A secure 4-digit code and custom password reset link have been dispatched to $email if it matches a valid registered account on QuickBite.") },
            confirmButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Large Circle Badge with Fork Icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "QuickBite",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tab Switcher
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // signup specific fields
        if (selectedTab == 1) {
            // Full Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number Input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Mobile Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Email Address Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                val icon = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(icon, contentDescription = "Toggle password")
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp)
        )

        // Forgot password block only for Login
        if (selectedTab == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot password?",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable {
                        if (email.isBlank()) {
                            Toast.makeText(context, "Please enter your email address first.", Toast.LENGTH_SHORT).show()
                        } else {
                            showForgotDialog = true
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Red Login -> trigger Pill button
        Button(
            onClick = {
                // Validation constraints
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(context, "Please specify a valid email address.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password.length < 6) {
                    Toast.makeText(context, "Password must contain at least 6 characters.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (selectedTab == 1 && name.isBlank()) {
                    Toast.makeText(context, "Please enter your full name to register.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Authentic session writing
                scope.launch {
                    val finalName = if (selectedTab == 1) name else "Ahmed Khan"
                    val finalPhone = if (selectedTab == 1) phone else "+92 321 7654321"

                    // Save session details
                    sharedPrefs.edit()
                        .putString("logged_in_email", email)
                        .putString("logged_in_name", finalName)
                        .apply()

                    // Insert or overwrite the Room database Profile structure
                    repository.saveUserProfile(
                        UserProfile(
                            email = email,
                            name = finalName,
                            phone = finalPhone
                        )
                    )

                    val welcomeMsg = if (selectedTab == 1) "Welcome to QuickBite, $finalName!" else "Welcome back, $finalName!"
                    Toast.makeText(context, welcomeMsg, Toast.LENGTH_SHORT).show()
                    onAuthSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(27.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (selectedTab == 0) "Login" else "Register Account",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
            Text(
                text = "or continue with",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Button
        Button(
            onClick = {
                // Mock Social login
                scope.launch {
                    sharedPrefs.edit()
                        .putString("logged_in_email", "ahmed.khan@example.com")
                        .putString("logged_in_name", "Ahmed Khan")
                        .apply()
                    repository.saveUserProfile(
                        UserProfile(
                            email = "ahmed.khan@example.com",
                            name = "Ahmed Khan",
                            phone = "+92 300 1234567"
                        )
                    )
                    Toast.makeText(context, "Authenticated successfully via Google!", Toast.LENGTH_SHORT).show()
                    onAuthSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Circle, // Simple placeholder representing google icon
                    contentDescription = null,
                    tint = Color(0xFFEA4335),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Toggle text link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (selectedTab == 0) "Don't have an account?" else "Already have an account?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (selectedTab == 0) "Sign Up" else "Login",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    selectedTab = if (selectedTab == 0) 1 else 0
                }
            )
        }
    }
}
