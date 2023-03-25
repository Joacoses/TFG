package com.example.joacoses.tfg;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTT extends AppCompatActivity implements MqttCallbackExtended {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mqtt);
        MqttClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883","Joan");
        connect();

    }
    @Override
    protected void onResume() {
        super.onResume();
        connect();
        //subscribeToTopic();
    }

    private MqttAndroidClient mqttAndroidClient;

    public void MqttClient(Context context, String brokerUrl, String clientId) {
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId);
        mqttAndroidClient.setCallback(this);
    }

    public void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);

        try {
            IMqttToken token = mqttAndroidClient.connect(mqttConnectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Conexión exitosa
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Fallo en la conexión
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        String topic = "tfgtopic/#"; // Reemplaza con el topic al que se publican los datos desde el Arduino
        int qos = 1; // Calidad de servicio de la suscripción (1 es una buena opción para garantizar la entrega)
        try {
            IMqttToken subToken = mqttAndroidClient.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Suscripción exitosa
                    Log.i("MqttClient", "mqtt");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Fallo en la suscripción
                    Log.i("MqttClient", "fallo mqtt");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        // Notificación de conexión completa
    }

    @Override
    public void connectionLost(Throwable cause) {
        // Notificación de desconexión
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // Procesa los datos recibidos en el mensaje
        String payload = new String(message.getPayload());
        Log.i("MqttClient", "Mensaje mqtt recibido: " + payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Notificación de entrega completa
    }
}
