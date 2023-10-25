package za.co.bidvestmobility.batchscanpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;  // <-- You were missing this import.
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.Manifest;

public class ScanForm extends AppCompatActivity {

    // Declare views to be accessed
    private TextView lblDate;
    private TextView lbUser;
    private CheckBox cbBin;
    private CheckBox cbQty;
    private EditText tbBin;
    private EditText tbSN;
    private EditText tbQty;
    private Button btnClear;
    private Button btnSubmit;

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 101; // Or another unique code


    private Handler dateTimeHandler = new Handler();
    private Runnable dateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            updateDateTime();
            dateTimeHandler.postDelayed(this, 1000); // Run every second
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_form);

        // Initialize views
        lblDate = findViewById(R.id.lblDate);
        lbUser = findViewById(R.id.lbUser);
        cbBin = findViewById(R.id.cbBin);
        cbQty = findViewById(R.id.cbQty);
        tbBin = findViewById(R.id.tbBin);
        tbSN = findViewById(R.id.tbSN);
        tbQty = findViewById(R.id.tbQty);
        btnClear = findViewById(R.id.btnClear);
        btnSubmit = findViewById(R.id.btnSubmit);

        tbBin.requestFocus();

        // Start the handler for updating the date and time
        dateTimeHandler.post(dateTimeRunnable);

        String receivedUsername = getIntent().getStringExtra("username");
        if (receivedUsername == null || receivedUsername.isEmpty()) {
            receivedUsername = "Default Name"; // Fallback in case no name was passed or it's empty
        }
        lbUser.setText(receivedUsername);



        // Set onClickListener for clear button
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearForm();
            }
        });

        // Set onClickListener for submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });

        //set checkbox onCheckChanged listener
        cbBin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    tbBin.setEnabled(false);  // Disable the EditText
                    tbSN.requestFocus();
                } else {
                    tbBin.setEnabled(true);  // Enable the EditText
                    tbBin.requestFocus();
                }
            }
        });

        //set qtyCheckbox onCheckChanged listener
        cbQty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    tbQty.setEnabled(false);  // Disable the EditText
                    tbSN.requestFocus();
                } else {
                    tbQty.setEnabled(true);  // Enable the EditText
                    tbQty.requestFocus();
                    tbQty.setText("1");
                }
            }
        });

        //Text watcher to make checkbox non-responsive if tbBin EditText is empty
        tbBin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().isEmpty()) {
                    cbBin.setClickable(false); // Disable checkbox
                } else {
                    cbBin.setClickable(true); // Enable checkbox
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


    }

    // Clear the form inputs
    private void clearForm() {
        tbBin.setText("");
        tbSN.setText("");
        tbQty.setText("");
        cbBin.setChecked(false);
        Toast.makeText(this, "Form cleared!", Toast.LENGTH_SHORT).show();
    }

    // Logic to handle form submission
    // Logic to handle form submission
    private void submitForm() {
        String binText = tbBin.getText().toString().trim();
        String snText = tbSN.getText().toString().trim();
        String qtyText = tbQty.getText().toString().trim();

        // Check for empty fields
        if(binText.isEmpty() || snText.isEmpty() || qtyText.isEmpty()) {
            String emptyFields = "";

            if(binText.isEmpty()) {
                emptyFields += "Bin input, ";
            }
            if(snText.isEmpty()) {
                emptyFields += "SN input, ";
            }
            if(qtyText.isEmpty()) {
                emptyFields += "Qty input ";
            }

            Toast.makeText(this, emptyFields + "cannot be empty!", Toast.LENGTH_SHORT).show();
            return; // Exit the method early so nothing else happens
        }

        String mText = binText + "," + snText + "," + qtyText;

        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            saveToTxtFile(mText);
        } else {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
        }
    }





    // This method was nested inside submitForm(). Moved it outside.
    private void updateDateTime() {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        lblDate.setText(currentDate);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);  // This is the line you add
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String mText = "Bin: " + tbBin.getText().toString() + ", SN: " + tbSN.getText().toString() + ", Qty: " + tbQty.getText().toString();
                    saveToTxtFile(mText);
                } else {
                    Toast.makeText(this, "Storage permission is required to store data", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void saveToTxtFile(String mText) {
        String user = lbUser.getText().toString();
        String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(System.currentTimeMillis());
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS); // Path to Documents directory
            File dir = new File(path, "BatchFiles"); // Subdirectory
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = "BatchFile_" + user + "_" + timeStamp + ".txt";
            File file = new File(dir, fileName);

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            // Check if the file is new (or empty) and then write the header
            if (!file.exists() || file.length() == 0) {
                bw.write("Bin,ID,Qty\n");
            }

            bw.write(mText + "\n");
            bw.close();

            Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show();
            tbSN.requestFocus();
            tbSN.setText("");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



}
