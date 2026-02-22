package com.example.gaspricesnearme

import android.R
import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.sign

// ---------------------------------------------------------
// Sign in Page 1-2
// ---------------------------------------------------------

@Composable
fun SignInScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("")}
    var verificationId by remember { mutableStateOf<String?>(null) }
    var showOtpField by remember { mutableStateOf(false)}
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            auth.signInWithCredential(credential)
                .addOnSuccessListener { onLoginSuccess() }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            isLoading = false
            errorMessage = e.message
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = id
            isLoading = false
            showOtpField = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Gas Prices Near Me",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Header
        Text(
            text = "Sign In",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Subtitle
        Text(
            text = "Enter your phone number to sign in to this app",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Input
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("(xxx)-xxx-xxxx") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        //OTP
        if (showOtpField) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = otpCode,
                onValueChange = {otpCode = it},
                label = { Text("Enter OTP")},
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true

            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { onLoginSuccess() }
                        .addOnFailureListener { errorMessage = it.message }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Verify OTP", color = Color.White)
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                val formattedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+1$phoneNumber"
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(formattedNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            enabled = !isLoading
        ) {
            if (isLoading)
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            else
                Text("Continue", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "or",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    try {
                        val credentialManager = CredentialManager.create(context)
                        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder("676624561870-2gq5m4f27i1cv3sklhtmvnhshh7oa7dj.apps.googleusercontent.com")
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(signInWithGoogleOption)
                            .build()
                        val result = credentialManager.getCredential(context,request)
                        val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnSuccessListener { onLoginSuccess() }
                            .addOnFailureListener { errorMessage = it.message }
                    } catch (e: GetCredentialException) {
                        errorMessage = e.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Continue with Google", color = Color.Black)
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Switch to Sign Up
        Text(
            text = "Don't have an account? Sign Up",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onNavigateToSignUp() }
        )
    }
}

// --------------------------------------------------------------------------
// PREVIEW FUNCTION
// --------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    SignInScreen(
        onLoginSuccess = {},      // Dummy empty action
        onNavigateToSignUp = {}   // Dummy empty action
    )
}