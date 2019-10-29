package com.example.investigacion_firestore_img;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore mFirestore;
    FirebaseStorage mStorage;
    FirebaseAuth mAuth;
    ImageView mImage;

    final long ONE_MEGABYTE = 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        //Esto lo hice porque necesitaba autenticarme para consultar las imágenes, no es necesario realizarlo en el proyecto.
        mAuth = FirebaseAuth.getInstance();
        mImage = findViewById(R.id.mImage);

        mAuth.signInAnonymously();

        readData();
    }

    private void readData(){
        mFirestore.collection("users").whereEqualTo("name", "Raul").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    String image = task.getResult().getDocuments().get(0).getString("image");
                    mStorage.getReference().child(image).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            //Este proceso es necesario, ya que el método nos devuelve la imagen como un array de bytes.
                            //ImageView no interpreta ese array de bytes y hay que realizar la conversión.
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, mImage.getWidth(), mImage.getHeight(), false));
                        }
                    }).addOnCanceledListener(new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            Log.e("DOWNLOAD", "ERROR");
                        }
                    });
                }
            }
        });
    }
}
