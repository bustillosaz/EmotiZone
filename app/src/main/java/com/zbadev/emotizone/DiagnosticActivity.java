package com.zbadev.emotizone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zbadev.emotizone.ml.ModelFinal;
import com.zbadev.emotizone.ml.ModelUnquant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiagnosticActivity extends AppCompatActivity {

    // Declaración de variables para los elementos de la UI
    TextView result, confidence;
    ImageView imageView;
    Button picture;
    int imageSize = 224; // Tamaño de la imagen a procesar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic);

        // Inicialización de los elementos de la UI
        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);

        // Configuración del evento onClick para el botón de tomar foto/seleccionar imagen
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPictureDialog();
            }
        });
    }

    // Método para mostrar el diálogo de selección de acción (capturar foto o elegir de la galería)
    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("SELECCIONAR ACCIÓN:");
        String[] pictureDialogItems = {
                "Capturar foto desde la cámara",
                "Seleccionar imagen de la galería"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                takePhotoFromCamera();
                                break;
                            case 1:
                                choosePhotoFromGallery();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    // Método para capturar una foto desde la cámara
    public void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(DiagnosticActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 1);
        } else {
            ActivityCompat.requestPermissions(DiagnosticActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    // Método para elegir una foto de la galería
    public void choosePhotoFromGallery() {
        if (ContextCompat.checkSelfPermission(DiagnosticActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, 2);
        } else {
            ActivityCompat.requestPermissions(DiagnosticActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
    }

    // Método para clasificar la imagen utilizando un modelo de TensorFlow Lite
    public void classifyImage(Bitmap image) {
        try {
            //ModelFinal model = ModelFinal.newInstance(getApplicationContext());
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Redimensionar la imagen al tamaño requerido por el modelo
            Bitmap resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

            // Normalizar los datos de la imagen de [0, 255] a [0, 1]
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            resizedImage.getPixels(intValues, 0, resizedImage.getWidth(), 0, 0, resizedImage.getWidth(), resizedImage.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Ejecutar la inferencia del modelo y obtener el resultado
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            // Definir las clases de emociones
            /*String[] classes = {"Estres Alto", "Estres Moderado", "Estres Bajo", "Relajado", "Feliz", "Ansiedad", "Triste", "Otro"};*/
            String[] classes = {"Estres Alto", "Estres Moderado", "Estres Bajo", "Relajacion", "Felicidad", "Ansiedad", "Tristeza", "Otros"};
            String detectedEmotion = classes[maxPos];
            result.setText(classes[maxPos]);

            // Mostrar mensaje de alerta si la clase es:
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < classes.length; i++) {
                s.append(String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100));
            }
            confidence.setText(s.toString());

            // Mostrar mensaje de alerta según la clase
            String message = "";
            String title = "";
            boolean showDetailsButton = true;

            switch (classes[maxPos]) {
                case "Estres Alto":
                    title = "HOY ESTAS CON ESTRES ALTO";
                    message = getString(R.string.estres_alto);
                    showDetailsButton = false;
                    break;
                case "Estres Moderado":
                    title = "HOY ESTAS CON ESTRES MODERADO";
                    message = getString(R.string.estres_moderado);
                    showDetailsButton = false;
                    break;
                case "Estres Bajo":
                    title = "HOY ESTAS CON ESTRES BAJO";
                    message = getString(R.string.estres_bajo);
                    showDetailsButton = false;
                    break;
                case "Relajacion":
                    title = "HOY ESTAS RELAJADO";
                    message = getString(R.string.relajado);
                    showDetailsButton = false;
                    break;
                case "Felicidad":
                    title = "HOY ESTAS FELIZ";
                    message = getString(R.string.feliz);
                    showDetailsButton = false;
                    break;
                case "Ansiedad":
                    title = "HOY ESTAS ANSIOSO";
                    message = getString(R.string.ansioso);
                    showDetailsButton = false;
                    break;
                case "Tristeza":
                    title = "HOY ESTAS TRISTE";
                    message = getString(R.string.tristeza);
                    showDetailsButton = false;
                    break;
                case "Otros":
                    title = "VUELVE A INTENTAR";
                    message = getString(R.string.otros);
                    showDetailsButton = true;
                    break;
            }
            if (!message.isEmpty()) {
                //showAlertDialog(message, title, showDetailsButton);
                showAlertDialog(message, title, detectedEmotion, showDetailsButton);
            }

            model.close();
        } catch (IOException e) {
            Log.e("DiagnosticActivity", "Error durante la inferencia del modelo", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap image = null;

            switch (requestCode) {
                case 1: // Cámara
                    image = (Bitmap) data.getExtras().get("data");
                    int dimension = Math.min(image.getWidth(), image.getHeight());
                    image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                    imageView.setImageBitmap(image);
                    image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                    classifyImage(image);
                    break;
                case 2: // Galería
                    Uri uri = data.getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        image = BitmapFactory.decodeStream(inputStream);
                        imageView.setImageBitmap(image);
                        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                        classifyImage(image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    // Método para mostrar un diálogo de alerta
    private void showAlertDialog(String message, String title, String detectedEmotion, boolean showDetailsButton) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);

        if (showDetailsButton) {
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(DiagnosticActivity.this, "hola", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }else {
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Obtener el usuario actual
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String userName = user.getDisplayName();
                        String userEmail = user.getEmail();
                        // Guardar el estado emocional en Firestore
                        saveEmotionalStateToFirestore(userName, userEmail, detectedEmotion);
                    }
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Detalles", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Aquí puedes definir la acción para el botón de detalles
                }
            });
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Clase modelo para el estado emocional
    private static class EmotionalState {
        private String userName;
        private String userEmail;
        private String emotionalState;
        private String date;

        public EmotionalState(String userName, String userEmail, String emotionalState, String date) {
            this.userName = userName;
            this.userEmail = userEmail;
            this.emotionalState = emotionalState;
            this.date = date;
        }

        // Getters y setters
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getEmotionalState() {
            return emotionalState;
        }

        public void setEmotionalState(String emotionalState) {
            this.emotionalState = emotionalState;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    // Método para guardar el estado emocional en Firestore
    private void saveEmotionalStateToFirestore(String userName, String userEmail, String emotionalState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        EmotionalState emotionalStateData = new EmotionalState(userName, userEmail, emotionalState, date);

        db.collection("emotionalStates")
                .add(emotionalStateData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(DiagnosticActivity.this, "Estado emocional guardado correctamente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DiagnosticActivity.this, "Error al guardar el estado emocional", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
