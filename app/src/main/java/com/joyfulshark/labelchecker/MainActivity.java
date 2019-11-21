package com.joyfulshark.labelchecker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button _btnSelectPicture;
    ImageView _imvLabeledPicture;
    TextView _txvLabelsList;

    final int REQUEST_PICK_FILE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _btnSelectPicture = findViewById(R.id.btn_select_pic);
        _imvLabeledPicture = findViewById(R.id.imv_labeled_pic);
        _txvLabelsList = findViewById(R.id.txv_labels_list);
        _btnSelectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_PICK_FILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                _imvLabeledPicture.setImageBitmap(bitmap);
                labelImage(bitmap);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to open file!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void labelImage(Bitmap bitmap) {
        FirebaseVisionImage fbVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector();
        detector.detectInImage(fbVisionImage)
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionLabel>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionLabel> labels) {
                                ArrayList<String> labelNames = new ArrayList<>();
                                for (FirebaseVisionLabel label : labels) {
                                    labelNames.add(label.getLabel());
                                }
                                _txvLabelsList.setText(android.text.TextUtils.join(", ", labelNames));
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Labeling failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
    }
}
