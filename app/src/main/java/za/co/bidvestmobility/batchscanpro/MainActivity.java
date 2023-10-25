package za.co.bidvestmobility.batchscanpro;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Define the EditText variable at the class level so it's accessible throughout
    private EditText usernameEditText;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the EditText
        usernameEditText = findViewById(R.id.username_edittext);

        //Initialize Next button
        nextButton = findViewById(R.id.next_button);
    }

    // Function to launch ScanActivity
    public void launchScanActivity(View view) {
        String username = usernameEditText.getText().toString();

        // Check if username is not empty
        if(!username.trim().isEmpty()) {
            Intent intent = new Intent(this, ScanForm.class);
            intent.putExtra("username", username);
            startActivity(intent);
        } else {
            // Optionally, show a toast if the username field is empty
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
        }
    }
}
