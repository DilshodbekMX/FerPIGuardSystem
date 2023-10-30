//package com.example.ferpiguardsystem;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.content.res.AssetFileDescriptor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.ImageFormat;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.graphics.YuvImage;
//import android.media.Image;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import java.util.Date;
//import androidx.annotation.NonNull;
//
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AlertDialog;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.ImageProxy;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//
//import com.example.ferpiguardsystem.Model.TeachersModel;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.material.textfield.TextInputLayout;
//import com.google.common.util.concurrent.ListenableFuture;
//
//
//import com.google.firebase.Timestamp;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.EventListener;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.FirebaseFirestoreException;
//import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firebase.firestore.SetOptions;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.face.Face;
//import com.google.mlkit.vision.face.FaceDetection;
//import com.google.mlkit.vision.face.FaceDetector;
//import com.google.mlkit.vision.face.FaceDetectorOptions;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.view.PreviewView;
//import androidx.cardview.widget.CardView;
//import androidx.core.content.ContextCompat;
//import androidx.lifecycle.LifecycleOwner;
//
//import android.os.ParcelFileDescriptor;
//import android.text.InputType;
//import android.util.Pair;
//import android.util.Size;
//import android.view.View;
//
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.tensorflow.lite.Interpreter;
//
//import java.io.ByteArrayOutputStream;
//import java.io.FileDescriptor;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.MappedByteBuffer;
//import java.nio.ReadOnlyBufferException;
//import java.nio.channels.FileChannel;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//import cn.pedant.SweetAlert.SweetAlertDialog;
//
//public class MainActivity extends AppCompatActivity {
//    FaceDetector detector;
//
//    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
//    PreviewView previewView;
//    ImageView face_preview;
//    Interpreter tfLite;
//    TextView reco_name,preview_info;
//    Button recognize;
//    ImageView actions, camera_switch;
//    ImageButton add_face;
//    CameraSelector cameraSelector;
//    boolean developerMode=false;
//    float distance= 1.0f;
//    boolean start=true,flipX=false;
//    Context context=MainActivity.this;
//    int cam_face=CameraSelector.LENS_FACING_BACK; //Default Back Camera
//
//    int[] intValues;
//    int inputSize=112;  //Input size for model
//    boolean isModelQuantized=false;
//    float[][] embeedings;
//    float IMAGE_MEAN = 128.0f;
//    float IMAGE_STD = 128.0f;
//    int OUTPUT_SIZE=192; //Output size of model
//    private static int SELECT_PICTURE = 1;
//    ProcessCameraProvider cameraProvider;
//    private static final int MY_CAMERA_REQUEST_CODE = 100;
//
//    String modelFile="mobile_face_net.tflite"; //model name
//    ArrayList<TeachersModel> teachersModelsList;
//    SimpleDateFormat dateFormat;
//    Date date;
//    FirebaseFirestore db;
//    TeachersModel teachersModels;
//    CollectionReference teachersCollection, takenGivenCollection;
//    String idForTodayStats,myFormat;
//    private ArrayList<TeachersModel> registered = new ArrayList<>(); //saved Faces
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        myFormat="dd-MM-yyyy";
//        dateFormat = new SimpleDateFormat(myFormat, Locale.US);
//        date = new Date();
//        idForTodayStats = String.valueOf((dateFormat.format(date)));
//        db = FirebaseFirestore.getInstance();
//        teachersCollection = db.collection("Teachers");
//        takenGivenCollection = db.collection("KeyTakenGiven");
//        registered=getDataFromFireStore(); //Load saved faces from memory when app starts
//        System.out.println(registered.size());
//        setContentView(R.layout.activity_main);
//        face_preview =findViewById(R.id.imageView);
//        reco_name =findViewById(R.id.textView);
//        preview_info =findViewById(R.id.textView2);
//        add_face=findViewById(R.id.imageButton);
//        add_face.setVisibility(View.INVISIBLE);
//
//        SharedPreferences sharedPref = getSharedPreferences("Distance",Context.MODE_PRIVATE);
//        distance = sharedPref.getFloat("distance",0.70f);
//
//        face_preview.setVisibility(View.INVISIBLE);
//        recognize=findViewById(R.id.button3);
//        camera_switch=findViewById(R.id.button5);
//        actions=findViewById(R.id.button2);
////        preview_info.setText("        Recognized Face:");
//        //Camera Permission
//        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
//        }
//        //On-screen Action Button
//        actions.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("Select Action:");
//
//                // add a checkbox list
//                String[] names= {"Add Person","Update Recognition List","Save Recognitions","Load Recognitions","Clear All Recognitions","Import Photo (Beta)","Hyperparameters","Developer Mode"};
//
//                builder.setItems(names, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        switch (which)
//                        {
//                            case 0:
//                                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                startActivity(intent);
//                                break;
//                            case 1:
//                                break;
//                            case 2:
//                                break;
//                            case 3:
//                                break;
//                            case 4:
//                                break;
//                            case 5:
//                                loadphoto();
//                                break;
//                            case 6:
//                                break;
//                            case 7:
//                                break;
//                        }
//
//                    }
//                });
//
//
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                builder.setNegativeButton("Cancel", null);
//
//                // create and show the alert dialog
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//        });
//
//        //On-screen switch to toggle between Cameras.
//        camera_switch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (cam_face==CameraSelector.LENS_FACING_BACK) {
//                    cam_face = CameraSelector.LENS_FACING_FRONT;
//                    flipX=true;
//                }
//                else {
//                    cam_face = CameraSelector.LENS_FACING_BACK;
//                    flipX=false;
//                }
//                cameraProvider.unbindAll();
//                cameraBind();
//            }
//        });
//
//        add_face.setOnClickListener((new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addFace();
//            }
//        }));
//
//
//        recognize.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(recognize.getText().toString().equals("Recognize"))
//                {
//                    start=true;
//                    recognize.setText("Add Face");
//                    add_face.setVisibility(View.INVISIBLE);
//                    reco_name.setVisibility(View.VISIBLE);
//                    face_preview.setVisibility(View.INVISIBLE);
//                    preview_info.setText("");
//                    //preview_info.setVisibility(View.INVISIBLE);
//                }
//                else
//                {
//                    recognize.setText("Recognize");
//                    add_face.setVisibility(View.VISIBLE);
//                    reco_name.setVisibility(View.INVISIBLE);
//                    face_preview.setVisibility(View.VISIBLE);
//                    preview_info.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.");
//
//
//                }
//
//            }
//        });
//
//        //Load model
//        try {
//            tfLite=new Interpreter(loadModelFile(MainActivity.this,modelFile));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //Initialize Face Detector
//        FaceDetectorOptions highAccuracyOpts =
//                new FaceDetectorOptions.Builder()
//                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//                        .build();
//        detector = FaceDetection.getClient(highAccuracyOpts);
//
//        cameraBind();
//
//
//
//    }
//
//    private ArrayList<TeachersModel> getDataFromFireStore(){
//        teachersCollection
//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable QuerySnapshot task, @Nullable FirebaseFirestoreException error) {
//                        if (!task.isEmpty()) {
//                            registered.clear();
//                            for (DocumentSnapshot d : task.getDocuments()) {
//                                String teNa = d.get("teacherName").toString();
//                                String teOr = d.get("teacherOrganization").toString();
//                                String tePhNu = d.get("teacherPhoneNumber").toString();
//                                String teRoKe = d.get("teacherRoomKey").toString();
//                                ArrayList<Double> teEnFa = (ArrayList<Double>) d.get("encodedFace");
//                                TeachersModel teMod = new TeachersModel(teNa, teOr, teRoKe, tePhNu, teEnFa);
//                                registered.add(teMod);
//                            }
//                        } else {
//                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
//                                    .setContentText("Xatolik bor!").setConfirmText("OK!")
//                                    .setConfirmClickListener(SweetAlertDialog::cancel).show();
//                        }
//                    }
//                });
//        return registered;
//    }
//    private void addFace()
//    {
//        final Dialog dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(true);
//        dialog.setContentView(R.layout.teacher_add_dialog);
//
//        final EditText teacherName = dialog.findViewById(R.id.teacherName);
//        final EditText teacherOrganization = dialog.findViewById(R.id.teacherOrganization);
//        final EditText teacherRoomKey = dialog.findViewById(R.id.teacherRoomKey);
//        final EditText teacherPhoneNumber = dialog.findViewById(R.id.teacherPhoneNumber);
//
//        final TextInputLayout forTeacherName = dialog.findViewById(R.id.forTeacherName);
//        final TextInputLayout forTeacherOrganization = dialog.findViewById(R.id.forTeacherOrganization);
//        final TextInputLayout forTeacherRoomKey = dialog.findViewById(R.id.forTeacherRoomKey);
//        final TextInputLayout forTeacherPhoneNumber = dialog.findViewById(R.id.forTeacherPhoneNumber);
//        start=false;
//
//        final CardView confirmTeacherDetail = dialog.findViewById(R.id.confirmTeacherDetail);
//        confirmTeacherDetail.setOnClickListener(v -> {
//
//            if (teacherName.length() == 0) {
//                forTeacherName.setError("O'qituvchi FISH ini kiriting");
//                return;
//            } else if (teacherOrganization.length() == 0) {
//                forTeacherOrganization.setError("Tashkilotini kiriting");
//                return;
//
//            } else if (teacherRoomKey.length() == 0) {
//                forTeacherRoomKey.setError("Xona nomini kiriting");
//                return;
//
//            } else if (teacherPhoneNumber.length() == 0) {
//                forTeacherPhoneNumber.setError("Telefon raqamini kiriting. (Na'muna: 901234567)");
//                return;
//            } else if (teacherPhoneNumber.length() < 9) {
//                forTeacherPhoneNumber.setError("Noto'g'ri  telefon raqami kiritildi. (Na'muna: 901234567)");
//                return;
//            }
//            else{
//                String t_n = teacherName.getText().toString().trim().replaceAll("[^A-Za-z0-9]", "");
//                String t_o = teacherOrganization.getText().toString().trim().replaceAll("[^A-Za-z0-9]", "");
//                String t_r_k = teacherRoomKey.getText().toString().trim().replaceAll("[^A-Za-z0-9]", "");
//                String t_p_n = teacherPhoneNumber.getText().toString();
//                float[] emb = embeedings[0];
//                ArrayList<Double> encodedFace = new ArrayList<>();
//                for (int i = 0; i < emb.length; i++) {
//                    System.out.println(i+" => "+emb[i]);
//                    encodedFace.add((double) emb[i]);
//                }
//                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
//                        .setContentText("Kiritilgan ma'lumotlarni tasdiqlaysizmi?" )
//                        .setCancelText("Yo'q")
//                        .setConfirmText("Ha")
//                        .showCancelButton(true)
//                        .setCancelClickListener(SweetAlertDialog::cancel)
//                        .setConfirmClickListener(sweetAlertDialog1 -> {
//                            teachersModels = new TeachersModel(t_n, t_o, t_r_k, "+998"+t_p_n, encodedFace);
//                            teachersCollection.document().set(teachersModels).addOnSuccessListener(unused -> {
//                                new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
//                                        .setContentText("Yangi O'qituvchi qo'shildi!").setConfirmText("OK!")
//                                        .setConfirmClickListener(sweetAlertDialog -> {
//                                            start=true;
//                                            sweetAlertDialog.cancel();
//                                        }).show();
//                            }).addOnFailureListener(e -> {
//                                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
//                                        .setContentText("Xatolik bor!").setConfirmText("OK!")
//                                        .setConfirmClickListener(sweetAlertDialog -> {
//                                            start=true;
//                                            sweetAlertDialog.cancel();
//                                        }).show();
//                            });
//                            sweetAlertDialog1.cancel();
//                            dialog.dismiss();
//                        })
//                        .show();
//            }
//        });
//        dialog.show();
//        Window window = dialog.getWindow();
//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == MY_CAMERA_REQUEST_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
//        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
//        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel = inputStream.getChannel();
//        long startOffset = fileDescriptor.getStartOffset();
//        long declaredLength = fileDescriptor.getDeclaredLength();
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
//    }
//
//    //Bind camera and preview view
//    private void cameraBind()
//    {
//        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//
//        previewView=findViewById(R.id.previewView);
//        cameraProviderFuture.addListener(() -> {
//            try {
//                cameraProvider = cameraProviderFuture.get();
//
//                bindPreview(cameraProvider);
//            } catch (ExecutionException | InterruptedException e) {
//                // No errors need to be handled for this in Future.
//                // This should never be reached.
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
//        Preview preview = new Preview.Builder()
//                .build();
//
//        cameraSelector = new CameraSelector.Builder()
//                .requireLensFacing(cam_face)
//                .build();
//
//        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//        ImageAnalysis imageAnalysis =
//                new ImageAnalysis.Builder()
//                        .setTargetResolution(new Size(640, 480))
//                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
//                        .build();
//
//        Executor executor = Executors.newSingleThreadExecutor();
//        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
//            @Override
//            public void analyze(@NonNull ImageProxy imageProxy) {
//                try {
//                    Thread.sleep(0);  //Camera preview refreshed every 10 millisec(adjust as required)
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                InputImage image = null;
//
//
//                @SuppressLint("UnsafeExperimentalUsageError")
//                // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)
//
//                Image mediaImage = imageProxy.getImage();
//
//                if (mediaImage != null) {
//                    image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
////                    System.out.println("Rotation "+imageProxy.getImageInfo().getRotationDegrees());
//                }
//
////                System.out.println("ANALYSIS");
//
//                //Process acquired image to detect faces
//                Task<List<Face>> result =
//                        detector.process(image)
//                                .addOnSuccessListener(
//                                        new OnSuccessListener<List<Face>>() {
//                                            @Override
//                                            public void onSuccess(List<Face> faces) {
//
//                                                if(faces.size()!=0) {
//
//                                                    Face face = faces.get(0); //Get first face from detected faces
////                                                    System.out.println(face);
//
//                                                    //mediaImage to Bitmap
//                                                    Bitmap frame_bmp = toBitmap(mediaImage);
//
//                                                    int rot = imageProxy.getImageInfo().getRotationDegrees();
//
//                                                    //Adjust orientation of Face
//                                                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);
//
//
//
//                                                    //Get bounding box of face
//                                                    RectF boundingBox = new RectF(face.getBoundingBox());
//
//                                                    //Crop out bounding box from whole Bitmap(image)
//                                                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
//
//                                                    if(flipX)
//                                                        cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
//                                                    //Scale the acquired Face to 112*112 which is required input for model
//                                                    Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
//
//                                                    if(start)
//                                                        recognizeImage(scaled); //Send scaled bitmap to create face embeddings.
////                                                    System.out.println(boundingBox);
//
//                                                }
//                                                else
//                                                {
//                                                    if(registered.isEmpty())
//                                                        reco_name.setText("Add Face");
//                                                    else
//                                                        reco_name.setText("No Face Detected!");
//                                                }
//
//                                            }
//                                        })
//                                .addOnFailureListener(
//                                        new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                // Task failed with an exception
//                                                // ...
//                                            }
//                                        })
//                                .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<List<Face>> task) {
//
//                                        imageProxy.close(); //v.important to acquire next frame for analysis
//                                    }
//                                });
//
//
//            }
//        });
//
//
//        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
//
//
//    }
//
//    public void recognizeImage(final Bitmap bitmap) {
//
//        // set Face to Preview
//        face_preview.setImageBitmap(bitmap);
//
//        //Create ByteBuffer to store normalized image
//
//        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);
//
//        imgData.order(ByteOrder.nativeOrder());
//
//        intValues = new int[inputSize * inputSize];
//
//        //get pixel values from Bitmap to normalize
//        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//        imgData.rewind();
//
//        for (int i = 0; i < inputSize; ++i) {
//            for (int j = 0; j < inputSize; ++j) {
//                int pixelValue = intValues[i * inputSize + j];
//                if (isModelQuantized) {
//                    // Quantized model
//                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
//                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
//                    imgData.put((byte) (pixelValue & 0xFF));
//                } else { // Float model
//                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
//                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
//                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
//
//                }
//            }
//        }
//        //imgData is input to our model
//        Object[] inputArray = {imgData};
//
//        Map<Integer, Object> outputMap = new HashMap<>();
//
//
//        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable
//        outputMap.put(0, embeedings);
//        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model
//
//
//
//        float distance_local = Float.MAX_VALUE;
//        String id = "0";
//        String label = "?";
//
//        //Compare new face with saved Faces.
//        if (registered.size() > 0) {
//            start=false;
//            final List<Pair<String, Float>> nearest = findNearest(embeedings[0]);//Find 2 closest matching face
//            if (nearest.get(0) != null) {
//                final String name = nearest.get(0).first; //get name and distance of closest matching face
//                // label = name;
//                distance_local = nearest.get(0).second;
//
//                if(distance_local<distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
//                {
//                    reco_name.setText(name);
//                    getFoundFaceDialog(nearest);
//                }
//                else{
//                    reco_name.setText("Unknown");
//                }
//
//            }}
//    }
//
//
//    private void getFoundFaceDialog(List<Pair<String, Float>> nearest) {
//        final Dialog dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(false);
//        dialog.setContentView(R.layout.get_found_dialog);
//
//
//        final TextView teacherName = dialog.findViewById(R.id.teacherName);
//        teacherName.setText(nearest.get(0).first);
//
//        final Button reset = dialog.findViewById(R.id.reset);
//        reset.setOnClickListener(view -> {
//            start = true;
//            dialog.dismiss();
//        });
//        final Button submit = dialog.findViewById(R.id.submit);
//        submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String t_n = teacherName.getText().toString().trim().replaceAll("[^A-Za-z0-9]", "");
//                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
//                        .setContentText("Kiritilgan ma'lumotlarni tasdiqlaysizmi?" )
//                        .setCancelText("Yo'q")
//                        .setConfirmText("Ha")
//                        .showCancelButton(true)
//                        .setCancelClickListener(SweetAlertDialog::cancel)
//                        .setConfirmClickListener(sweetAlertDialog1 -> {
//                            takenGivenCollection.document(idForTodayStats).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot d) {
//                                    if(d.exists()){
//                                        for(Map.Entry<String, Object> nameList:d.getData().entrySet()){
//                                            if(t_n.equals(nameList.getKey())){
//                                                Map<String, Object> map = (Map<String, Object>) nameList.getValue();
//                                                Timestamp tati = null;
//                                                Timestamp tagi = null;
//                                                for(Map.Entry<String, Object> timeList:map.entrySet()){
//                                                    if(Objects.equals(timeList.getKey().toString(), "takenTime")){
//                                                        tati = (Timestamp) timeList.getValue();
//                                                    }
//                                                    if(Objects.equals(timeList.getKey().toString(), "givenTime")){
//                                                        tagi = (Timestamp) timeList.getValue();
//                                                    }
//                                                }
//                                                if(tagi==null){
//                                                    Timestamp t_g = Timestamp.now();
//                                                    HashMap<String,Timestamp> taT = new HashMap<>();
//                                                    taT.put("takenTime", tati);
//                                                    taT.put("givenTime", t_g);
//                                                    HashMap<String, HashMap<String,Timestamp>> map2 = new HashMap<>();
//                                                    map2.put(t_n,taT);
//                                                    d.getReference().set(map2, SetOptions.merge()).addOnSuccessListener(unused -> {
//                                                        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
//                                                                .setContentText("Kalit "+t_n+" tomonidan qaytarildi!").setConfirmText("OK!")
//                                                                .setConfirmClickListener(sweetAlertDialog -> {
//                                                                    sweetAlertDialog1.cancel();
//                                                                    sweetAlertDialog.cancel();
//                                                                    dialog.dismiss();
//                                                                    start=true;}).show();
//                                                    }).addOnFailureListener(e -> {
//                                                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
//                                                                .setContentText("Xatolik bor!").setConfirmText("OK!")
//                                                                .setConfirmClickListener(sweetAlertDialog -> {
//                                                                    sweetAlertDialog1.cancel();
//                                                                    sweetAlertDialog.cancel();
//                                                                    dialog.dismiss();
//                                                                    start=true;}).show();
//                                                    });
//                                                }
//                                                else{
//                                                    Timestamp t_t = Timestamp.now();
//                                                    HashMap<String,Timestamp> taT = new HashMap<>();
//                                                    taT.put("takenTime", t_t);
//                                                    taT.put("givenTime", null);
//                                                    HashMap<String, HashMap<String,Timestamp>> map2 = new HashMap<>();
//                                                    map2.put(t_n,taT);
//                                                    d.getReference().set(map2, SetOptions.merge()).addOnSuccessListener(unused -> {
//
//                                                        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
//                                                                .setContentText("Kalit "+t_n+" tomonidan olindi!").setConfirmText("OK!")
//                                                                .setConfirmClickListener(sweetAlertDialog -> {
//                                                                    sweetAlertDialog1.cancel();
//                                                                    sweetAlertDialog.cancel();
//                                                                    dialog.dismiss();
//                                                                    start=true;}).show();
//                                                    }).addOnFailureListener(e -> {
//
//                                                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
//                                                                .setContentText("Xatolik bor!").setConfirmText("OK!")
//                                                                .setConfirmClickListener(sweetAlertDialog -> {
//                                                                    sweetAlertDialog1.cancel();
//                                                                    sweetAlertDialog.cancel();
//                                                                    dialog.dismiss();
//                                                                    start=true;}).show();
//                                                    });
//                                                }
//                                            }
//                                            else{
//                                                Timestamp t_t = Timestamp.now();
//                                                HashMap<String,Timestamp> taT = new HashMap<>();
//                                                taT.put("takenTime", t_t);
//                                                taT.put("givenTime", null);
//                                                HashMap<String, HashMap<String,Timestamp>> map2 = new HashMap<>();
//                                                map2.put(t_n,taT);
//                                                d.getReference().set(map2, SetOptions.merge()).addOnSuccessListener(unused -> {
//
//                                                    new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
//                                                            .setContentText("Kalit "+t_n+" tomonidan olindi!").setConfirmText("OK!")
//                                                            .setConfirmClickListener(sweetAlertDialog -> {
//                                                                sweetAlertDialog1.cancel();
//                                                                sweetAlertDialog.cancel();
//                                                                dialog.dismiss();
//                                                                start=true;}).show();
//                                                }).addOnFailureListener(e -> {
//
//                                                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
//                                                            .setContentText("Xatolik bor!").setConfirmText("OK!")
//                                                            .setConfirmClickListener(sweetAlertDialog -> {
//                                                                sweetAlertDialog1.cancel();
//                                                                sweetAlertDialog.cancel();
//                                                                dialog.dismiss();
//                                                                start=true;}).show();
//                                                });
//                                            }
//                                        }
//                                    }
//                                    else{
//                                        Timestamp t_t = Timestamp.now();
//                                        HashMap<String,Timestamp> taT = new HashMap<>();
//                                        taT.put("takenTime", t_t);
//                                        taT.put("givenTime", null);
//                                        HashMap<String, HashMap<String,Timestamp>> map2 = new HashMap<>();
//                                        map2.put(t_n,taT);
//                                        d.getReference().set(map2).addOnSuccessListener(unused -> {
//
//                                            new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
//                                                    .setContentText("Kalit "+t_n+" tomonidan olindi!").setConfirmText("OK!")
//                                                    .setConfirmClickListener(sweetAlertDialog -> {
//                                                        sweetAlertDialog1.cancel();
//                                                        sweetAlertDialog.cancel();
//                                                        dialog.dismiss();
//                                                        start=true;}).show();
//                                        }).addOnFailureListener(e -> {
//                                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
//                                                    .setContentText("Xatolik bor!").setConfirmText("OK!")
//                                                    .setConfirmClickListener(sweetAlertDialog -> {
//                                                        sweetAlertDialog1.cancel();
//                                                        sweetAlertDialog.cancel();
//                                                        dialog.dismiss();
//                                                        start=true;}).show();
//                                        });
//                                    }
//                                }
//                            });
//                            sweetAlertDialog1.cancel();
//                            dialog.dismiss();
//                        })
//                        .show();
//            }
//        });
//        dialog.show();
//        Window window = dialog.getWindow();
//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//    }
//
//
//
//    //Compare Faces by distance between face embeddings
//    private List<Pair<String, Float>> findNearest(float[] emb) {
//        List<Pair<String, Float>> neighbour_list = new ArrayList<Pair<String, Float>>();
//        Pair<String, Float> ret = null; //to get closest match
//        Pair<String, Float> prev_ret = null; //to get second closest match
//        for (TeachersModel teDetail:registered)
//        {
//            final String teName = teDetail.getTeacherName();
//            final ArrayList<Double> knownEmb = teDetail.getEncodedFace();
//            float distance = 0;
//            for (int i = 0; i < emb.length; i++) {
//                float diff = (float) (emb[i] - knownEmb.get(i));
//                distance += diff*diff;
//            }
//            distance = (float) Math.sqrt(distance);
//            if (ret == null || distance < ret.second) {
//                prev_ret=ret;
//                ret = new Pair<>(teName, distance);
//            }
//        }
//        if(prev_ret==null) prev_ret=ret;
//        neighbour_list.add(ret);
//        neighbour_list.add(prev_ret);
//        return neighbour_list;
//    }
//
//    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
//        int width = bm.getWidth();
//        int height = bm.getHeight();
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        // CREATE A MATRIX FOR THE MANIPULATION
//        Matrix matrix = new Matrix();
//        // RESIZE THE BIT MAP
//        matrix.postScale(scaleWidth, scaleHeight);
//
//        // "RECREATE" THE NEW BITMAP
//        Bitmap resizedBitmap = Bitmap.createBitmap(
//                bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
//        return resizedBitmap;
//    }
//    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
//        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
//                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
//        Canvas cavas = new Canvas(resultBitmap);
//
//        // draw background
//        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
//        paint.setColor(Color.WHITE);
//        cavas.drawRect(
//                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
//                paint);
//
//        Matrix matrix = new Matrix();
//        matrix.postTranslate(-cropRectF.left, -cropRectF.top);
//
//        cavas.drawBitmap(source, matrix, paint);
//
//        if (source != null && !source.isRecycled()) {
//            source.recycle();
//        }
//
//        return resultBitmap;
//    }
//    private static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
//        Matrix matrix = new Matrix();
//
//        // Rotate the image back to straight.
//        matrix.postRotate(rotationDegrees);
//
//        // Mirror the image along the X or Y axis.
//        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
//        Bitmap rotatedBitmap =
//                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//
//        // Recycle the old bitmap if it has changed.
//        if (rotatedBitmap != bitmap) {
//            bitmap.recycle();
//        }
//        return rotatedBitmap;
//    }
//    private static byte[] YUV_420_888toNV21(Image image) {
//
//        int width = image.getWidth();
//        int height = image.getHeight();
//        int ySize = width*height;
//        int uvSize = width*height/4;
//
//        byte[] nv21 = new byte[ySize + uvSize*2];
//
//        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
//        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
//        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V
//
//        int rowStride = image.getPlanes()[0].getRowStride();
//        assert(image.getPlanes()[0].getPixelStride() == 1);
//
//        int pos = 0;
//
//        if (rowStride == width) { // likely
//            yBuffer.get(nv21, 0, ySize);
//            pos += ySize;
//        }
//        else {
//            long yBufferPos = -rowStride; // not an actual position
//            for (; pos<ySize; pos+=width) {
//                yBufferPos += rowStride;
//                yBuffer.position((int) yBufferPos);
//                yBuffer.get(nv21, pos, width);
//            }
//        }
//
//        rowStride = image.getPlanes()[2].getRowStride();
//        int pixelStride = image.getPlanes()[2].getPixelStride();
//
//        assert(rowStride == image.getPlanes()[1].getRowStride());
//        assert(pixelStride == image.getPlanes()[1].getPixelStride());
//
//        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
//            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
//            byte savePixel = vBuffer.get(1);
//            try {
//                vBuffer.put(1, (byte)~savePixel);
//                if (uBuffer.get(0) == (byte)~savePixel) {
//                    vBuffer.put(1, savePixel);
//                    vBuffer.position(0);
//                    uBuffer.position(0);
//                    vBuffer.get(nv21, ySize, 1);
//                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());
//
//                    return nv21; // shortcut
//                }
//            }
//            catch (ReadOnlyBufferException ex) {
//                // unfortunately, we cannot check if vBuffer and uBuffer overlap
//            }
//
//            // unfortunately, the check failed. We must save U and V pixel by pixel
//            vBuffer.put(1, savePixel);
//        }
//        for (int row=0; row<height/2; row++) {
//            for (int col=0; col<width/2; col++) {
//                int vuPos = col*pixelStride + row*rowStride;
//                nv21[pos++] = vBuffer.get(vuPos);
//                nv21[pos++] = uBuffer.get(vuPos);
//            }
//        }
//
//        return nv21;
//    }
//    private Bitmap toBitmap(Image image) {
//
//        byte[] nv21=YUV_420_888toNV21(image);
//
//
//        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);
//
//        byte[] imageBytes = out.toByteArray();
//
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//    }
//    private void loadphoto()
//    {
//        start=false;
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
//    }
//
//    //Similar Analyzing Procedure
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == SELECT_PICTURE) {
//                Uri selectedImageUri = data.getData();
//                try {
//                    InputImage impphoto=InputImage.fromBitmap(getBitmapFromUri(selectedImageUri),0);
//                    detector.process(impphoto).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
//                        @Override
//                        public void onSuccess(List<Face> faces) {
//
//                            if(faces.size()!=0) {
//                                recognize.setText("Recognize");
//                                add_face.setVisibility(View.VISIBLE);
//                                reco_name.setVisibility(View.INVISIBLE);
//                                face_preview.setVisibility(View.VISIBLE);
//                                preview_info.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.");
//                                Face face = faces.get(0);
////                                System.out.println(face);
//
//                                //write code to recreate bitmap from source
//                                //Write code to show bitmap to canvas
//
//                                Bitmap frame_bmp= null;
//                                try {
//                                    frame_bmp = getBitmapFromUri(selectedImageUri);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                                Bitmap frame_bmp1 = rotateBitmap(frame_bmp, 0, flipX, false);
//
//                                //face_preview.setImageBitmap(frame_bmp1);
//
//
//                                RectF boundingBox = new RectF(face.getBoundingBox());
//
//
//                                Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
//
//                                Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
//                                // face_preview.setImageBitmap(scaled);
//
//                                recognizeImage(scaled);
//                                addFace();
////                                System.out.println(boundingBox);
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            start=true;
//                            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    face_preview.setImageBitmap(getBitmapFromUri(selectedImageUri));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
//        ParcelFileDescriptor parcelFileDescriptor =
//                getContentResolver().openFileDescriptor(uri, "r");
//        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
//        parcelFileDescriptor.close();
//        return image;
//    }
//}