package com.example.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public final class LoginActivity extends WarehouseActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameInput = findViewById(R.id.login_username);
        passwordInput = findViewById(R.id.login_password);
        errorMessage = findViewById(R.id.login_error);
        findViewById(R.id.login_submit).setOnClickListener(view -> logIn());
        passwordInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return false;
            }
            logIn();
            return true;
        });
    }

    private void logIn() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (username.isEmpty() || password.trim().isEmpty()) {
            showError(R.string.login_required);
            return;
        }
        if (!database.checkLogin(username, password)) {
            showError(R.string.login_wrong);
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showError(int message) {
        errorMessage.setText(message);
        errorMessage.setVisibility(View.VISIBLE);
    }
}
