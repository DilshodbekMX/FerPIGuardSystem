package com.example.ferpiguardsystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.example.ferpiguardsystem.Model.TeachersModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.ListenableFuture;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.os.ParcelFileDescriptor;
import android.text.InputType;
import android.util.Pair;
import android.util.Size;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddUserActivity extends AppCompatActivity {
    FaceDetector detector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    ImageView face_preview;
    Interpreter tfLite;
    TextView reco_name,preview_info;
    ImageView camera_switch,actions, back_button;
    CardView recognize;
    ImageButton add_face;
    TextView button3_text;
    CameraSelector cameraSelector;
    boolean developerMode=false;
    float distance= .8f;
    boolean start=true,flipX=false;
    Context context= AddUserActivity.this;
    int cam_face=CameraSelector.LENS_FACING_BACK; //Default Back Camera

    int[] intValues;
    int inputSize=112;  //Input size for model
    boolean isModelQuantized=false;
    float[][] embeedings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE=192; //Output size of model
    private static int SELECT_PICTURE = 1;
    ProcessCameraProvider cameraProvider;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    FirebaseFirestore db;
    TeachersModel teachersModels;
    CollectionReference teachersCollection;
    String modelFile="mobile_face_net.tflite"; //model name
    ArrayList<TeachersModel> teachersModelsList;
    private ArrayList<TeachersModel> registered = new ArrayList<>(); //saved Faces
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        teachersCollection = db.collection("Teachers");

        getDataFromFireStore();
        setContentView(R.layout.activity_add_user);
        face_preview =findViewById(R.id.imageView);
        reco_name =findViewById(R.id.textView);
        preview_info =findViewById(R.id.textView2);
        add_face=findViewById(R.id.imageButton);

        back_button=findViewById(R.id.button4);
        back_button.setOnClickListener(v -> {
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setContentText("Dasturdan chiqasizmi?" )
                    .setCancelText("Yo'q")
                    .setConfirmText("Ha")
                    .showCancelButton(true)
                    .setCancelClickListener(SweetAlertDialog::cancel)
                    .setConfirmClickListener(sweetAlertDialog1 -> {
                        sweetAlertDialog1.cancel();
                        this.finishAffinity();
                    })
                    .show();
        });

        add_face.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPref = getSharedPreferences("Distance",Context.MODE_PRIVATE);
        distance = sharedPref.getFloat("distance",.8f);

        face_preview.setVisibility(View.INVISIBLE);
        recognize=findViewById(R.id.button3);
        button3_text=findViewById(R.id.button3_text);

        camera_switch=findViewById(R.id.button5);
        actions = findViewById(R.id.button2);
        actions.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddUserActivity.this);
            builder.setTitle("Tanlang:");
            // add a checkbox list
            String[] names= {"Aniqlash","Shaxslar ro'yxati","Kalit olish Tarixi"};

            builder.setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (which)
                    {
                        case 0:
                            Intent intent3 = new Intent(AddUserActivity.this, MainActivity.class);
                            intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent3);
                            break;
                        case 1:
                            Intent intent = new Intent(AddUserActivity.this, UserListActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            break;
                        case 2:
                            Intent intent2 = new Intent(AddUserActivity.this, KeyHistoryActivity.class);
                            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent2);
                            break;

                    }

                }
            });
            builder.setNegativeButton("Cancel", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });

//        preview_info.setText("        Recognized Face:");
        //Camera Permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        //On-screen Action Button

        //On-screen switch to toggle between Cameras.
        camera_switch.setOnClickListener(v -> {
            if (cam_face==CameraSelector.LENS_FACING_BACK) {
                cam_face = CameraSelector.LENS_FACING_FRONT;
                flipX=true;
            }
            else {
                cam_face = CameraSelector.LENS_FACING_BACK;
                flipX=false;
            }
            cameraProvider.unbindAll();
            cameraBind();
        });

        add_face.setOnClickListener((v -> addFace()));
        recognize.setOnClickListener(v -> {
            if(button3_text.getText().toString().equals("Tekshirish"))
            {
                start=true;
                button3_text.setText("Yangi Foydalanuvchi qo'shish");
                add_face.setVisibility(View.INVISIBLE);
                reco_name.setVisibility(View.VISIBLE);
                face_preview.setVisibility(View.INVISIBLE);
                preview_info.setText("");
            }
            else
            {
                button3_text.setText("Tekshirish");
                add_face.setVisibility(View.VISIBLE);
                reco_name.setVisibility(View.INVISIBLE);
                face_preview.setVisibility(View.VISIBLE);
                preview_info.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.");
            }
        });
        try {
            tfLite=new Interpreter(loadModelFile(AddUserActivity.this,modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
        cameraBind();
    }

    private void addFace()
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.activity_add_user_dialog);

        DocumentReference teachersId = teachersCollection.document();

        final EditText teacherName = dialog.findViewById(R.id.teacherName);
        final EditText teacherOrganization = dialog.findViewById(R.id.teacherOrganization);
        final EditText teacherRoomKey = dialog.findViewById(R.id.teacherRoomKey);
        final EditText teacherPhoneNumber = dialog.findViewById(R.id.teacherPhoneNumber);

        final TextInputLayout forTeacherName = dialog.findViewById(R.id.forTeacherName);
        final TextInputLayout forTeacherOrganization = dialog.findViewById(R.id.forTeacherOrganization);
        final TextInputLayout forTeacherRoomKey = dialog.findViewById(R.id.forTeacherRoomKey);
        final TextInputLayout forTeacherPhoneNumber = dialog.findViewById(R.id.forTeacherPhoneNumber);
        start=false;

        final CardView confirmTeacherDetail = dialog.findViewById(R.id.confirmTeacherDetail);
        confirmTeacherDetail.setOnClickListener(v -> {

            if (teacherName.length() == 0) {
                forTeacherName.setError("Shaxs FISH ini kiriting");
                return;
            } else if (teacherOrganization.length() == 0) {
                forTeacherOrganization.setError("Tashkilotini kiriting");
                return;

            } else if (teacherRoomKey.length() == 0) {
                forTeacherRoomKey.setError("Xona nomini kiriting");
                return;

            } else if (teacherPhoneNumber.length() == 0) {
                forTeacherPhoneNumber.setError("Telefon raqamini kiriting. (Na'muna: 901234567)");
                return;
            } else if (teacherPhoneNumber.length() < 9) {
                forTeacherPhoneNumber.setError("Noto'g'ri  telefon raqami kiritildi. (Na'muna: 901234567)");
                return;
            }
            else{
                String t_n = teacherName.getText().toString().trim().replaceAll("[^A-Za-z0-9]", "");
                String t_o = teacherOrganization.getText().toString().trim().replaceAll("[^A-Za-z0-9]", "");
                String t_r_k = teacherRoomKey.getText().toString().trim().replaceAll("[^A-Za-z0-9]", "");
                String t_p_n = teacherPhoneNumber.getText().toString();
                float[] emb = embeedings[0];
                ArrayList<Double> encodedFace = new ArrayList<>();
                for (int i = 0; i < emb.length; i++) {
                    System.out.println(i+" => "+emb[i]);
                    encodedFace.add((double) emb[i]);
                }
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setContentText("Kiritilgan ma'lumotlarni tasdiqlaysizmi?" )
                        .setCancelText("Yo'q")
                        .setConfirmText("Ha")
                        .showCancelButton(true)
                        .setCancelClickListener(SweetAlertDialog::cancel)
                        .setConfirmClickListener(sweetAlertDialog1 -> {
                            teachersModels = new TeachersModel(teachersId.getId(),t_n, t_o, t_r_k, "+998"+t_p_n, encodedFace);
                            teachersId.set(teachersModels).addOnSuccessListener(unused -> {
                                new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                        .setContentText("Yangi shaxs qo'shildi!").setConfirmText("OK!")
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            start=true;
                                            sweetAlertDialog.cancel();
                                        }).show();
                            }).addOnFailureListener(e -> {
                                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                        .setContentText("Xatolik bor!").setConfirmText("OK!")
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            start=true;
                                            sweetAlertDialog.cancel();
                                        }).show();
                            });
                            sweetAlertDialog1.cancel();
                            dialog.dismiss();
                        })
                        .show();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }
    private void getDataFromFireStore(){
        Source source = Source.CACHE;
        teachersCollection.get(source)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot d : task.getResult()) {
                                String teId = d.getId().toString();
                                String teNa = d.get("teacherName").toString();
                                String teOr = d.get("teacherOrganization").toString();
                                String tePhNu = d.get("teacherPhoneNumber").toString();
                                String teRoKe = d.get("teacherRoomKey").toString();
                                ArrayList<Double> teEnFa = (ArrayList<Double>) d.get("encodedFace");
                                TeachersModel teMod = new TeachersModel(teId,teNa, teOr, teRoKe, tePhNu, teEnFa);
                                registered.add(teMod);
                            }
                        } else {
                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setContentText("Xatolik bor!").setConfirmText("OK!")
                                    .setConfirmClickListener(SweetAlertDialog::cancel).show();
                        }
                    }
                });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    //Bind camera and preview view
    private void cameraBind()
    {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView=findViewById(R.id.previewView);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this in Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cam_face).build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                        .build();

        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                try {Thread.sleep(0);} catch (InterruptedException e) {e.printStackTrace();}
                InputImage image = null;
                @SuppressLint("UnsafeExperimentalUsageError")
                // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)
                Image mediaImage = imageProxy.getImage();
                if (mediaImage != null) {image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());}
                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        faces -> {if(faces.size()!=0) {
                                                Face face = faces.get(0); //Get first face from detected faces
                                                //mediaImage to Bitmap
                                                Bitmap frame_bmp = toBitmap(mediaImage);
                                                int rot = imageProxy.getImageInfo().getRotationDegrees();
                                                //Adjust orientation of Face
                                                Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);
                                                //Get bounding box of face
                                                RectF boundingBox = new RectF(face.getBoundingBox());
                                                //Crop out bounding box from whole Bitmap(image)
                                                Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
                                                if(flipX)
                                                    cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
                                                //Scale the acquired Face to 112*112 which is required input for model
                                                Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
                                                if(start)
                                                    recognizeImage(scaled);
                                            }
                                            else
                                            {
                                                if(registered.isEmpty())
                                                    reco_name.setText("Yangi Foydalanuvchi qo'shish");
                                                else
                                                    reco_name.setText("Hech qanday shaxs topilmadi!");
                                            }
                                        })
                                .addOnFailureListener(
                                        e -> {
                                            // Task failed with an exception
                                        })
                                .addOnCompleteListener(task -> {
                                    imageProxy.close(); //v.important to acquire next frame for analysis
                                });}});
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
    }
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void recognizeImage(final Bitmap bitmap) {
        face_preview.setImageBitmap(bitmap);
        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);
        imgData.order(ByteOrder.nativeOrder());
        intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        imgData.rewind();
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }
        Object[] inputArray = {imgData};
        Map<Integer, Object> outputMap = new HashMap<>();
        embeedings = new float[1][OUTPUT_SIZE];
        outputMap.put(0, embeedings);
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
        float distance_local = Float.MAX_VALUE;
        String id = "0";
        String label = "?";
        if (registered.size() > 0) {
            final List<Pair<String, Float>> nearest = findNearest(embeedings[0]);//Find 2 closest matching face
            if (nearest.get(0) != null) {
                final String name = nearest.get(0).first;
                distance_local = nearest.get(0).second;
                if(distance_local<distance)
                    reco_name.setText("Bu shaxs bazada "+name.toUpperCase()+" nomi bilan mavjud!");
                else
                    reco_name.setText("Bunday shaxs bazada mavjud emas. Iltimos, avval ro'yxatga oling!");
            }
        }
    }
    private List<Pair<String, Float>> findNearest(float[] emb) {
        List<Pair<String, Float>> neighbour_list = new ArrayList<Pair<String, Float>>();
        Pair<String, Float> ret = null; //to get closest match
        Pair<String, Float> prev_ret = null; //to get second closest match
        for(TeachersModel teDetail:registered){
            final String teName = teDetail.getTeacherName();
            final ArrayList<Double> knownEmb = teDetail.getEncodedFace();
            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = (float) (emb[i] - knownEmb.get(i));
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                prev_ret=ret;
                ret = new Pair<>(teName, distance);
            }
        }
        if(prev_ret==null) prev_ret=ret;
        neighbour_list.add(ret);
        neighbour_list.add(prev_ret);
        return neighbour_list;
    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }
    private static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }
    //IMPORTANT. If conversion not done ,the toBitmap conversion does not work on some devices.
    private static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width*height;
        int uvSize = width*height/4;

        byte[] nv21 = new byte[ySize + uvSize*2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert(image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        }
        else {
            long yBufferPos = -rowStride; // not an actual position
            for (; pos<ySize; pos+=width) {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert(rowStride == image.getPlanes()[1].getRowStride());
        assert(pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            try {
                vBuffer.put(1, (byte)~savePixel);
                if (uBuffer.get(0) == (byte)~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            }
            catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row=0; row<height/2; row++) {
            for (int col=0; col<width/2; col++) {
                int vuPos = col*pixelStride + row*rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }
    private Bitmap toBitmap(Image image) {

        byte[] nv21=YUV_420_888toNV21(image);


        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }


    //Similar Analyzing Procedure
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                try {
                    InputImage impphoto=InputImage.fromBitmap(getBitmapFromUri(selectedImageUri),0);
                    detector.process(impphoto).addOnSuccessListener(faces -> {
                        if(faces.size()!=0) {
                            button3_text.setText("Tekshirish");
                            add_face.setVisibility(View.VISIBLE);
                            reco_name.setVisibility(View.INVISIBLE);
                            face_preview.setVisibility(View.VISIBLE);
                            preview_info.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.");
                            Face face = faces.get(0);
                            Bitmap frame_bmp= null;
                            try {frame_bmp = getBitmapFromUri(selectedImageUri);}
                            catch (IOException e) {e.printStackTrace();}
                            Bitmap frame_bmp1 = rotateBitmap(frame_bmp, 0, flipX, false);
                            RectF boundingBox = new RectF(face.getBoundingBox());
                            Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
                            Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
                            recognizeImage(scaled);
                            addFace();
                            try {Thread.sleep(100);}
                            catch (InterruptedException e) {e.printStackTrace();}}}).addOnFailureListener(e -> {
                        start=true;
                        Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show();
                    });
                    face_preview.setImageBitmap(getBitmapFromUri(selectedImageUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }}}}
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

}










