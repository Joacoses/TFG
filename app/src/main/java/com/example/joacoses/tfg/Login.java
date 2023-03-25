package com.example.joacoses.tfg;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Login extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;



    //notificaciones
    private Button boton;

    private int notificationId = 0;

    private String CHANNEL_ID = "4444";

    //firestore
    //FirebaseFirestore db = FirebaseFirestore.getInstance();
    //Map<String, Object> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        login();
    }



    // .................................................................
    // login() -->
    // .................................................................
    //En esta funcion se  crea un Intent nuevo con la actividad "MainActivity"
    //Posteriormente se inicializa dicha actividad
    //Si el usuario no existe, obtenemos los datos de los usuarios, como el nombre y el email
    //Sino, comprobamos los datos con Auth
    private void login() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario != null) {
            // Guardar los datos del usuario en Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = usuario.getUid();
            Map<String, Object> user = new HashMap<>();
            user.put("name", usuario.getDisplayName());
            user.put("email", usuario.getEmail());
            user.put("nfc",usuario.getUid());

            //Toast.makeText(this, "Has iniciado sesión con: "+ usuario.getEmail(), Toast.LENGTH_LONG).show();
            /*Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);*/
            db.collection("usuarios").document(userId).set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // El usuario se guardó exitosamente
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Hubo un error al guardar el usuario
                        }
                    });



            if(usuario.isEmailVerified())
            {
                Toast.makeText(this, "Has iniciado sesión con: "+ usuario.getEmail(), Toast.LENGTH_LONG).show();

                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
            else
            {
                usuario.sendEmailVerification();
                //-----------------------------
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://"+"mail.google.com/mail/u/0/?tab=rm&ogbl#inbox"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.logoredondo)
                        .setContentTitle("Oximap")
                        .setContentText("Se ha enviado un correo a tu email para verificar tu usuario")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(notificationId, builder.build());
                //-----------------------------


                Toast.makeText(this, "Verifica la direccion de correo",
                        Toast.LENGTH_LONG).show();
                startActivityForResult(AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(true).build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build())).build()

                        //.setIsSmartLockEnabled(false)
                        , RC_SIGN_IN);
            }

        } else {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);
        }

    }




    // .................................................................
    //requestCode, resultCode: int, data: Intent -->
    // onActivityResult() -->
    // .................................................................
    //Ejecuta login, y en caso de que no se pueda ejecutar, salta un error con el motivo
    public void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                login();
                //finish(); //Quitar
            } else {
                String s = "";
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (response == null) s = "Cancelado";
                else switch (response.getError().getErrorCode()) {
                    case ErrorCodes.NO_NETWORK: s="Sin conexión a Internet"; break;
                    case ErrorCodes.PROVIDER_ERROR: s="Error en proveedor"; break;
                    case ErrorCodes.DEVELOPER_ERROR: s="Error desarrollador"; break;
                    default: s="Otros errores de autentificación";
                }
                Toast.makeText(this, s, Toast.LENGTH_LONG).show();
            }
        }
    }

    //notificaciones
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", importance);
            channel.setDescription("description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        else {
            Toast.makeText(getApplicationContext(),"Se te ha enviado un correo",Toast.LENGTH_LONG);
        }

    }//clase

}
