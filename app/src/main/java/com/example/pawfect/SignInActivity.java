package com.example.pawfect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private MaterialButton signInButton;
    private View signUpLink;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupClickListeners();

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        }
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        signInButton = findViewById(R.id.signInButton);
        signUpLink = findViewById(R.id.signUpLink);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.signing_in));
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> {
            if (validateForm()) {
                signIn();
            }
        });

        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        String email = emailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.required_field));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.invalid_email_format));
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.required_field));
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.weak_password));
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }

    private void signIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Log.d("SignInActivity", "signInWithEmail:success");
                        Toast.makeText(SignInActivity.this, R.string.sign_in_success, Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Log.w("SignInActivity", "signInWithEmail:failure", task.getException());
                        Toast.makeText(SignInActivity.this, R.string.sign_in_failed, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}



