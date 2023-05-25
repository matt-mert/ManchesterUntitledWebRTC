package io.antmedia.webrtc_android_sample_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.Manifest;
import android.media.MediaPlayer;
import android.media.MediaController2;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.tavendo.autobahn.WebSocket;
import io.antmedia.webrtcandroidframework.IDataChannelObserver;
import io.antmedia.webrtcandroidframework.IWebRTCClient;
import io.antmedia.webrtcandroidframework.IWebRTCListener;
import io.antmedia.webrtcandroidframework.StreamInfo;
import io.antmedia.webrtcandroidframework.WebRTCClient;
import io.antmedia.webrtcandroidframework.apprtc.CallActivity;

import static io.antmedia.webrtcandroidframework.apprtc.CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED;
import static io.antmedia.webrtcandroidframework.apprtc.CallActivity.EXTRA_DATA_CHANNEL_ENABLED;
import static io.antmedia.webrtcandroidframework.apprtc.CallActivity.EXTRA_VIDEO_BITRATE;
import static io.antmedia.webrtcandroidframework.apprtc.CallActivity.EXTRA_VIDEO_FPS;

public class MainActivity extends AppCompatActivity implements IWebRTCListener, IDataChannelObserver {

    private String serverData = "NONE";
    private Queue<MediaPlayer> mediaPlayerQueue;
    private MediaPlayer temp;
    private int distance = 150;
    private boolean welcomeSound = false;
    private boolean bluetoothBool = false;

    /**
     * Change this address with your Ant Media Server address
     */
    public static final String SERVER_ADDRESS = "mert.damgoai.com:5443";

    /**
     * Mode can Publish, Play or P2P
     */
    private String webRTCMode = IWebRTCClient.MODE_PUBLISH;

    private boolean enableDataChannel = true;

    public static final String SERVER_URL = "wss://" + SERVER_ADDRESS + "/WebRTCAppEE/websocket";
    public static final String REST_URL = "https://" + SERVER_ADDRESS + "/WebRTCAppEE/rest/v2";

    private static final int PERMISSION_REQUEST_CODE = 100;
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private static final UUID HC05_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice hc05Device;
    private BluetoothSocket btSocket;

    private MediaPlayer welcomePlayer;
    private MediaPlayer beepPlayer;
    private MediaPlayer leftPlayer;
    private MediaPlayer rightPlayer;
    private MediaPlayer connectedPlayer;
    private MediaPlayer a10001Player;
    private MediaPlayer a10010Player;
    private MediaPlayer a10011Player;
    private MediaPlayer a11000Player;
    private MediaPlayer a11001Player;
    private MediaPlayer a11010Player;
    private MediaPlayer a11011Player;
    private MediaPlayer a1a100Player;
    private MediaPlayer a1a101Player;
    private MediaPlayer a1a110Player;
    private MediaPlayer a1a111Player;
    private MediaPlayer a20Player;
    private MediaPlayer a21Player;
    private MediaPlayer a30aPlayer;
    private MediaPlayer a31aPlayer;
    private MediaPlayer a32aPlayer;
    private MediaPlayer a33aPlayer;
    private MediaPlayer a34aPlayer;
    private MediaPlayer a35aPlayer;
    private MediaPlayer a36aPlayer;
    private MediaPlayer a37aPlayer;
    private MediaPlayer a38aPlayer;
    private MediaPlayer a39aPlayer;
    private MediaPlayer a310aPlayer;
    private MediaPlayer a311aPlayer;
    private MediaPlayer a312aPlayer;
    private MediaPlayer a313aPlayer;
    private MediaPlayer a314aPlayer;
    private MediaPlayer a315aPlayer;
    private MediaPlayer a316aPlayer;
    private MediaPlayer a317aPlayer;
    private MediaPlayer a318aPlayer;
    private MediaPlayer a319aPlayer;
    private MediaPlayer a3a0Player;
    private MediaPlayer a3a1Player;
    private MediaPlayer a3a2Player;
    private MediaPlayer a3a3Player;
    private MediaPlayer a3a4Player;
    private MediaPlayer a3a5Player;
    private MediaPlayer a3a6Player;
    private MediaPlayer a3a7Player;
    private MediaPlayer a3a8Player;
    private MediaPlayer a3a9Player;
    private MediaPlayer a3a10Player;
    private MediaPlayer a3a11Player;
    private MediaPlayer a3a12Player;
    private MediaPlayer a3a13Player;
    private MediaPlayer a3a14Player;
    private MediaPlayer a3cancelPlayer;
    private MediaPlayer a40Player;
    private MediaPlayer a41Player;
    private MediaPlayer a42Player;
    private MediaPlayer a51Player;
    private MediaPlayer a5cancelPlayer;
    private MediaPlayer a61Player;
    private MediaPlayer a6cancelPlayer;
    private MediaPlayer a70Player;
    private MediaPlayer a71Player;
    private MediaPlayer a7cancelPlayer;

    private WebRTCClient webRTCClient;

    private Button startStreamingButton;
    private String operationName = "";
    private String streamId;

    private SurfaceViewRenderer cameraViewRenderer;
    private SurfaceViewRenderer pipViewRenderer;
    private Spinner streamInfoListSpinner;

    // variables for handling reconnection attempts after disconnected
    final int RECONNECTION_PERIOD_MLS = 100;
    private boolean stoppedStream = false;
    Handler reconnectionHandler = new Handler();
    Runnable reconnectionRunnable = new Runnable() {
        @Override
        public void run() {
            if (!webRTCClient.isStreaming()) {
                attempt2Reconnect();
                // call the handler again in case startStreaming is not successful
                reconnectionHandler.postDelayed(this, RECONNECTION_PERIOD_MLS);
            }
        }
    };

    private Handler toastHandler = new Handler();
    private Runnable toastRunnable = new Runnable() {
        @Override
        public void run() {
            // Toast.makeText(MainActivity.this, "test", Toast.LENGTH_SHORT).show();

            toastHandler.postDelayed(this, 2000);
        }
    };

    private Handler welcomeHandler = new Handler();
    private Runnable welcomeRunnable = new Runnable() {
        @Override
        public void run() {
            if (welcomeSound) {
                welcomePlayer.start();
                welcomeSound = false;
            }
            welcomeHandler.postDelayed(this, 4000);
        }
    };

    private Handler beepHandler = new Handler();
    private Runnable beepRunnable = new Runnable() {
        @Override
        public void run() {
            if (distance < 51) {
                beepPlayer.start();
                distance = 150;
            }
            beepHandler.postDelayed(this, 500);
        }
    };

    private Handler soundHandler = new Handler();
    private Runnable soundRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayerQueue.isEmpty()) {
                soundHandler.postDelayed(this, 100);
                return;
            }

            if (temp != null && temp.isPlaying()) {
                soundHandler.postDelayed(this, 100);
                return;
            }

            temp = mediaPlayerQueue.remove();
            temp.start();

            // Toast.makeText(MainActivity.this, "test", Toast.LENGTH_SHORT).show();
            soundHandler.postDelayed(this, 100);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        //getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());

        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        hc05Device = bluetoothAdapter.getRemoteDevice("00:22:09:01:4A:D1");

        toastHandler.post(toastRunnable);
        welcomeHandler.post(welcomeRunnable);
        beepHandler.post(beepRunnable);
        soundHandler.post(soundRunnable);

        cameraViewRenderer = findViewById(R.id.camera_view_renderer);
        pipViewRenderer = findViewById(R.id.pip_view_renderer);

        startStreamingButton = findViewById(R.id.start_streaming_button);

        streamInfoListSpinner = findViewById(R.id.stream_info_list);

        if(!webRTCMode.equals(IWebRTCClient.MODE_PLAY)) {
            streamInfoListSpinner.setVisibility(View.INVISIBLE);
        }
        else {

            streamInfoListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                boolean firstCall = true;
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    //for some reason in android onItemSelected is called automatically at first.
                    //there are some discussions about it in stackoverflow
                    //so we just have simple check
                    if (firstCall) {
                        firstCall = false;
                        return;
                    }
                    webRTCClient.forceStreamQuality(Integer.parseInt((String) adapterView.getSelectedItem()));
                    Log.i("MainActivity", "Spinner onItemSelected");
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        // Check for mandatory permissions.
        for (String permission : CallActivity.MANDATORY_PERMISSIONS) {
            if (this.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission " + permission + " is not granted", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (webRTCMode.equals(IWebRTCClient.MODE_PUBLISH)) {
            startStreamingButton.setText("Start Publishing");
            operationName = "Publishing";
        }
        else  if (webRTCMode.equals(IWebRTCClient.MODE_PLAY)) {
            startStreamingButton.setText("Start Playing");
            operationName = "Playing";
        }
        else if (webRTCMode.equals(IWebRTCClient.MODE_JOIN)) {
            startStreamingButton.setText("Start P2P");
            operationName = "P2P";
        }

        this.getIntent().putExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, true);
        this.getIntent().putExtra(EXTRA_VIDEO_FPS, 30);
        this.getIntent().putExtra(EXTRA_VIDEO_BITRATE, 1500);
        this.getIntent().putExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, true);
        this.getIntent().putExtra(EXTRA_DATA_CHANNEL_ENABLED, enableDataChannel);

        webRTCClient = new WebRTCClient( this,this);

        streamId = "manu31";
        String tokenId = "tokenId";
        webRTCClient.setVideoRenderers(pipViewRenderer, cameraViewRenderer);

       // this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);
        webRTCClient.init(SERVER_URL, streamId, webRTCMode, tokenId, this.getIntent());
        webRTCClient.setDataChannelObserver(this);

        welcomePlayer = MediaPlayer.create(this, R.raw.welcome);
        beepPlayer = MediaPlayer.create(this, R.raw.beep);
        leftPlayer = MediaPlayer.create(this, R.raw.look_left);
        rightPlayer = MediaPlayer.create(this, R.raw.look_right);
        connectedPlayer = MediaPlayer.create(this, R.raw.connected);
        a10001Player = MediaPlayer.create(this, R.raw.a10001);
        a10010Player = MediaPlayer.create(this, R.raw.a10010);
        a10011Player = MediaPlayer.create(this, R.raw.a10011);
        a11000Player = MediaPlayer.create(this, R.raw.a11000);
        a11001Player = MediaPlayer.create(this, R.raw.a11001);
        a11010Player = MediaPlayer.create(this, R.raw.a11010);
        a11011Player = MediaPlayer.create(this, R.raw.a11011);
        a1a100Player = MediaPlayer.create(this, R.raw.a1a100);
        a1a101Player = MediaPlayer.create(this, R.raw.a1a101);
        a1a110Player = MediaPlayer.create(this, R.raw.a1a110);
        a1a111Player = MediaPlayer.create(this, R.raw.a1a111);
        a20Player = MediaPlayer.create(this, R.raw.a20);
        a21Player = MediaPlayer.create(this, R.raw.a21);
        a30aPlayer = MediaPlayer.create(this, R.raw.a30a);
        a31aPlayer = MediaPlayer.create(this, R.raw.a31a);
        a32aPlayer = MediaPlayer.create(this, R.raw.a32a);
        a33aPlayer = MediaPlayer.create(this, R.raw.a33a);
        a34aPlayer = MediaPlayer.create(this, R.raw.a34a);
        a35aPlayer = MediaPlayer.create(this, R.raw.a35a);
        a36aPlayer = MediaPlayer.create(this, R.raw.a36a);
        a37aPlayer = MediaPlayer.create(this, R.raw.a37a);
        a38aPlayer = MediaPlayer.create(this, R.raw.a38a);
        a39aPlayer = MediaPlayer.create(this, R.raw.a39a);
        a310aPlayer = MediaPlayer.create(this, R.raw.a310a);
        a311aPlayer = MediaPlayer.create(this, R.raw.a311a);
        a312aPlayer = MediaPlayer.create(this, R.raw.a312a);
        a313aPlayer = MediaPlayer.create(this, R.raw.a313a);
        a314aPlayer = MediaPlayer.create(this, R.raw.a314a);
        a315aPlayer = MediaPlayer.create(this, R.raw.a315a);
        a316aPlayer = MediaPlayer.create(this, R.raw.a316a);
        a317aPlayer = MediaPlayer.create(this, R.raw.a317a);
        a318aPlayer = MediaPlayer.create(this, R.raw.a318a);
        a319aPlayer = MediaPlayer.create(this, R.raw.a319a);
        a3a0Player = MediaPlayer.create(this, R.raw.a3a0);
        a3a1Player = MediaPlayer.create(this, R.raw.a3a1);
        a3a2Player = MediaPlayer.create(this, R.raw.a3a2);
        a3a3Player = MediaPlayer.create(this, R.raw.a3a3);
        a3a4Player = MediaPlayer.create(this, R.raw.a3a4);
        a3a5Player = MediaPlayer.create(this, R.raw.a3a5);
        a3a6Player = MediaPlayer.create(this, R.raw.a3a6);
        a3a7Player = MediaPlayer.create(this, R.raw.a3a7);
        a3a8Player = MediaPlayer.create(this, R.raw.a3a8);
        a3a9Player = MediaPlayer.create(this, R.raw.a3a9);
        a3a10Player = MediaPlayer.create(this, R.raw.a3a10);
        a3a11Player = MediaPlayer.create(this, R.raw.a3a11);
        a3a12Player = MediaPlayer.create(this, R.raw.a3a12);
        a3a13Player = MediaPlayer.create(this, R.raw.a3a13);
        a3a14Player = MediaPlayer.create(this, R.raw.a3a14);
        a3cancelPlayer = MediaPlayer.create(this, R.raw.a3cancel);
        a40Player = MediaPlayer.create(this, R.raw.a40);
        a41Player = MediaPlayer.create(this, R.raw.a41);
        a42Player = MediaPlayer.create(this, R.raw.a42);
        a51Player = MediaPlayer.create(this, R.raw.a51);
        a5cancelPlayer = MediaPlayer.create(this, R.raw.a5cancel);
        a61Player = MediaPlayer.create(this, R.raw.a61);
        a6cancelPlayer = MediaPlayer.create(this, R.raw.a6cancel);
        a70Player = MediaPlayer.create(this, R.raw.a70);
        a71Player = MediaPlayer.create(this, R.raw.a71);
        a7cancelPlayer = MediaPlayer.create(this, R.raw.a7cancel);

        mediaPlayerQueue = new LinkedList<MediaPlayer>();
    }

    private void connectToBluetoothModule() {
        if (hc05Device == null) {
            Toast.makeText(this, "HC-05 device not found", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            btSocket = hc05Device.createRfcommSocketToServiceRecord(HC05_UUID);
            btSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error connecting to HC-05", Toast.LENGTH_LONG).show();
        }
    }

    private void connectToHC05() {
        Set<BluetoothDevice> pairedDevices;
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) { // Replace with the name of your HC-05 module
                    hc05Device = device;
                    break;
                }
            }
        }

        if (hc05Device == null) {
            Toast.makeText(this, "HC-05 device not found", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            btSocket = hc05Device.createRfcommSocketToServiceRecord(HC05_UUID);
            btSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error connecting to HC-05", Toast.LENGTH_LONG).show();
        }
    }

    private void sendData(String data) {
        if (btSocket == null || !btSocket.isConnected()) {
            Toast.makeText(this, "Not connected to HC-05", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            OutputStream outputStream = btSocket.getOutputStream();
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sending data", Toast.LENGTH_SHORT).show();
        }
    }

    private void startReceivingData() {
        if (btSocket == null || !btSocket.isConnected()) {
            Toast.makeText(this, "Not connected to HC-05", Toast.LENGTH_SHORT).show();
            return;
        }

        Thread receiveThread = new Thread() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;
                InputStream inputStream;

                try {
                    inputStream = btSocket.getInputStream();
                    while (true) {
                        try {
                            bytes = inputStream.read(buffer);
                            final String receivedData = new String(buffer, 0, bytes);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(receivedData.contains("SIGN")) {
                                        sendTextMessage("SIGN");
                                    }
                                    else if (receivedData.contains("CROSS")){
                                        sendTextMessage("CROSS");
                                    }
                                    else if (receivedData.contains("CANCEL")){
                                        sendTextMessage("CANCEL");
                                    }
                                    else if (receivedData.contains("Distance")){
                                        String str = receivedData.replaceAll("\\D+","");
                                        if (!str.isEmpty()) {
                                            distance = Integer.parseInt(str);
                                            // if (distance < 20) {
                                            //   sendTextMessage("CLOSE");
                                            // }
                                        }
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Error receiving data", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        receiveThread.start();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
        };

        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void changeCamera(View v) {
        webRTCClient.switchCamera();
    }

    public void pressSignButton(View v) {
        sendTextMessage("SIGN");
    }

    public void pressCrossButton(View v) {
        sendTextMessage("CROSS");
    }

    public void pressCancelButton(View v) {
        sendTextMessage("CANCEL");
    }

    public void toggleBluetoothButton(View v) {
        if (bluetoothBool) {
            bluetoothBool = false;
            ((Button) v).setText("BLUETOOTH ON");
        }
        else {
            bluetoothBool = true;
            ((Button) v).setText("BLUETOOTH OFF");
        }
    }

    public void startStreaming(View v) {

        if (bluetoothBool) {
            connectToBluetoothModule();
        }

        if (!webRTCClient.isStreaming()) {
            ((Button) v).setText("Stop " + operationName);
            webRTCClient.startStream();
            if (webRTCMode == IWebRTCClient.MODE_JOIN) {
                pipViewRenderer.setZOrderOnTop(true);
            }
            stoppedStream = false;
        }
        else {
            ((Button)v).setText("Start " + operationName);
            webRTCClient.stopStream();
            toastHandler.removeCallbacks(toastRunnable);
            welcomeHandler.removeCallbacks(welcomeRunnable);
            beepHandler.removeCallbacks(beepRunnable);
            soundHandler.removeCallbacks(soundRunnable);
            stoppedStream = true;
            return;
        }

        startReceivingData();
        welcomeSound = true;
    }

    private void attempt2Reconnect() {
        Log.w(getClass().getSimpleName(), "Attempt2Reconnect called");
        if (!webRTCClient.isStreaming()) {
            webRTCClient.startStream();
            if (webRTCMode == IWebRTCClient.MODE_JOIN) {
                pipViewRenderer.setZOrderOnTop(true);
            }
        }
    }

    @Override
    public void onPlayStarted(String streamId) {
        Log.w(getClass().getSimpleName(), "onPlayStarted");
        Toast.makeText(this, "Play started", Toast.LENGTH_LONG).show();
        webRTCClient.switchVideoScaling(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        webRTCClient.getStreamInfoList();
    }

    @Override
    public void onPublishStarted(String streamId) {
        Log.w(getClass().getSimpleName(), "onPublishStarted");
        Toast.makeText(this, "Publish started", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPublishFinished(String streamId) {
        Log.w(getClass().getSimpleName(), "onPublishFinished");
        Toast.makeText(this, "Publish finished", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPlayFinished(String streamId) {
        Log.w(getClass().getSimpleName(), "onPlayFinished");
        Toast.makeText(this, "Play finished", Toast.LENGTH_LONG).show();
    }

    @Override
    public void noStreamExistsToPlay(String streamId) {
        Log.w(getClass().getSimpleName(), "noStreamExistsToPlay");
        Toast.makeText(this, "No stream exist to play", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void streamIdInUse(String streamId) {
        Log.w(getClass().getSimpleName(), "streamIdInUse");
        Toast.makeText(this, "Stream id is already in use.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(String description, String streamId) {
        Toast.makeText(this, "Error: " + description , Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        webRTCClient.stopStream();
        toastHandler.removeCallbacks(toastRunnable);
        welcomeHandler.removeCallbacks(welcomeRunnable);
        beepHandler.removeCallbacks(beepRunnable);
        soundHandler.removeCallbacks(soundRunnable);
    }

    @Override
    public void onSignalChannelClosed(WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification code, String streamId) {
        Toast.makeText(this, "Signal channel closed with code " + code, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisconnected(String streamId) {
        Log.w(getClass().getSimpleName(), "disconnected");
        Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();

        startStreamingButton.setText("Start " + operationName);
        // handle reconnection attempt
        if (!stoppedStream) {
            Toast.makeText(this, "Disconnected Attempting to reconnect", Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!reconnectionHandler.hasCallbacks(reconnectionRunnable)) {
                    reconnectionHandler.postDelayed(reconnectionRunnable, RECONNECTION_PERIOD_MLS);
                }
            } else {
                reconnectionHandler.postDelayed(reconnectionRunnable, RECONNECTION_PERIOD_MLS);
            }
        } else {
            Toast.makeText(this, "Stopped the stream", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onIceConnected(String streamId) {
        //it is called when connected to ice
        startStreamingButton.setText("Stop " + operationName);
        // remove scheduled reconnection attempts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (reconnectionHandler.hasCallbacks(reconnectionRunnable)) {
                reconnectionHandler.removeCallbacks(reconnectionRunnable, null);
            }
        } else {
            reconnectionHandler.removeCallbacks(reconnectionRunnable, null);
        }
    }

    @Override
    public void onIceDisconnected(String streamId) {
        //it's called when ice is disconnected
    }

    public void onOffVideo(View view) {
        if (webRTCClient.isVideoOn()) {
            webRTCClient.disableVideo();
        }
        else {
            webRTCClient.enableVideo();
        }
    }

    public void onOffAudio(View view) {
        if (webRTCClient.isAudioOn()) {
            webRTCClient.disableAudio();
        }
        else {
            webRTCClient.enableAudio();
        }
    }

    @Override
    public void onTrackList(String[] tracks) {

    }

    @Override
    public void onBitrateMeasurement(String streamId, int targetBitrate, int videoBitrate, int audioBitrate) {
        Log.e(getClass().getSimpleName(), "st:"+streamId+" tb:"+targetBitrate+" vb:"+videoBitrate+" ab:"+audioBitrate);
        if(targetBitrate < (videoBitrate+audioBitrate)) {
            Toast.makeText(this, "low bandwidth", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStreamInfoList(String streamId, ArrayList<StreamInfo> streamInfoList) {
        String[] stringArray = new String[streamInfoList.size()];
        int i = 0;
        for (StreamInfo si : streamInfoList) {
            stringArray[i++] = si.getHeight()+"";
        }
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
        streamInfoListSpinner.setAdapter(modeAdapter);
    }

    /**
     * This method is used in an experiment. It's not for production
     * @param streamId
     */
    public void calculateAbsoluteLatency(String streamId) {
        String url = REST_URL + "/broadcasts/" + streamId + "/rtmp-to-webrtc-stats";

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.i("MainActivity", "recevied response " + response);
                            JSONObject jsonObject = new JSONObject(response);
                            long absoluteStartTimeMs = jsonObject.getLong("absoluteTimeMs");
                            //this is the frame id in sending the rtp packet. Actually it's rtp timestamp
                            long frameId = jsonObject.getLong("frameId");
                            long relativeCaptureTimeMs = jsonObject.getLong("captureTimeMs");
                            long captureTimeMs = frameId / 90;
                            Map<Long, Long> captureTimeMsList = WebRTCClient.getCaptureTimeMsMapList();

                            long absoluteDecodeTimeMs = 0;
                            if (captureTimeMsList.containsKey(captureTimeMs)) {
                                absoluteDecodeTimeMs = captureTimeMsList.get(captureTimeMs);
                            }

                            long absoluteLatency = absoluteDecodeTimeMs - relativeCaptureTimeMs - absoluteStartTimeMs;
                            Log.i("MainActivity", "recevied absolute start time: " + absoluteStartTimeMs
                                                        + " frameId: " + frameId + " relativeLatencyMs : " + relativeCaptureTimeMs
                                                        + " absoluteDecodeTimeMs: " + absoluteDecodeTimeMs
                                                        + " absoluteLatency: " + absoluteLatency);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MainActivity", "That didn't work!");

            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    @Override
    public void onBufferedAmountChange(long previousAmount, String dataChannelLabel) {
        Log.d(MainActivity.class.getName(), "Data channel buffered amount changed: ");
    }

    @Override
    public void onStateChange(DataChannel.State state, String dataChannelLabel) {
        Log.d(MainActivity.class.getName(), "Data channel state changed: ");
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, String dataChannelLabel) {
        ByteBuffer data = buffer.data;
        String messageText = new String(data.array(), StandardCharsets.UTF_8);
        serverData = messageText;
        String[] strArray = messageText.split(":");
        // Toast.makeText(this, "F:" + strArray[0], Toast.LENGTH_LONG).show();
        if (Objects.equals(strArray[0], "1")) {
            // IDLE STATE
            if (Objects.equals(strArray[1], "0") && Objects.equals(strArray[2], "0") &&
                    Objects.equals(strArray[3], "0") && !Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a10001Player);
            } else if (Objects.equals(strArray[1], "0") && Objects.equals(strArray[2], "0") &&
                    !Objects.equals(strArray[3], "0") && Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a10010Player);
            } else if (Objects.equals(strArray[1], "0") && Objects.equals(strArray[2], "0") &&
                    !Objects.equals(strArray[3], "0") && !Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a10011Player);
            } else if (!Objects.equals(strArray[1], "0") && Objects.equals(strArray[2], "0") &&
                    Objects.equals(strArray[3], "0") && Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a11000Player);
            } else if (!Objects.equals(strArray[1], "0") && Objects.equals(strArray[2], "0") &&
                    Objects.equals(strArray[3], "0") && !Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a11001Player);
            } else if (!Objects.equals(strArray[1], "0") && Objects.equals(strArray[2], "0") &&
                    !Objects.equals(strArray[3], "0") && Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a11010Player);
            } else if (!Objects.equals(strArray[1], "0") && Objects.equals(strArray[2], "0") &&
                    !Objects.equals(strArray[3], "0") && !Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a11011Player);
            } else if (!Objects.equals(strArray[2], "0") &&
                    Objects.equals(strArray[3], "0") && Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a1a100Player);
            } else if (!Objects.equals(strArray[2], "0") &&
                    Objects.equals(strArray[3], "0") && !Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a1a101Player);
            } else if (!Objects.equals(strArray[2], "0") &&
                    !Objects.equals(strArray[3], "0") && Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a1a110Player);
            } else if (!Objects.equals(strArray[2], "0") &&
                    !Objects.equals(strArray[3], "0") && !Objects.equals(strArray[4], "0")) {
                mediaPlayerQueue.add(a1a111Player);
            }
            // SIGN_CHECK STATE
        } else if (Objects.equals(strArray[0], "2")) {
            if (Objects.equals(strArray[1], "0")) {
                mediaPlayerQueue.add(a20Player);
            } else if (Objects.equals(strArray[1], "1")) {
                mediaPlayerQueue.add(a21Player);
            }
            // SIGN_MAIN STATE
        } else if (Objects.equals(strArray[0], "3")) {
            if (Objects.equals(strArray[1], "0")) {
                mediaPlayerQueue.add(a30aPlayer);
            } else if (Objects.equals(strArray[1], "1")) {
                mediaPlayerQueue.add(a31aPlayer);
            } else if (Objects.equals(strArray[1], "2")) {
                mediaPlayerQueue.add(a32aPlayer);
            } else if (Objects.equals(strArray[1], "3")) {
                mediaPlayerQueue.add(a33aPlayer);
            } else if (Objects.equals(strArray[1], "4")) {
                mediaPlayerQueue.add(a34aPlayer);
            } else if (Objects.equals(strArray[1], "5")) {
                mediaPlayerQueue.add(a35aPlayer);
            } else if (Objects.equals(strArray[1], "6")) {
                mediaPlayerQueue.add(a36aPlayer);
            } else if (Objects.equals(strArray[1], "7")) {
                mediaPlayerQueue.add(a37aPlayer);
            } else if (Objects.equals(strArray[1], "8")) {
                mediaPlayerQueue.add(a38aPlayer);
            } else if (Objects.equals(strArray[1], "9")) {
                mediaPlayerQueue.add(a39aPlayer);
            } else if (Objects.equals(strArray[1], "10")) {
                mediaPlayerQueue.add(a310aPlayer);
            } else if (Objects.equals(strArray[1], "11")) {
                mediaPlayerQueue.add(a311aPlayer);
            } else if (Objects.equals(strArray[1], "12")) {
                mediaPlayerQueue.add(a312aPlayer);
            } else if (Objects.equals(strArray[1], "13")) {
                mediaPlayerQueue.add(a313aPlayer);
            } else if (Objects.equals(strArray[1], "14")) {
                mediaPlayerQueue.add(a314aPlayer);
            } else if (Objects.equals(strArray[1], "15")) {
                mediaPlayerQueue.add(a315aPlayer);
            } else if (Objects.equals(strArray[1], "16")) {
                mediaPlayerQueue.add(a316aPlayer);
            } else if (Objects.equals(strArray[1], "17")) {
                mediaPlayerQueue.add(a317aPlayer);
            } else if (Objects.equals(strArray[1], "18")) {
                mediaPlayerQueue.add(a318aPlayer);
            } else if (Objects.equals(strArray[1], "19")) {
                mediaPlayerQueue.add(a319aPlayer);
            } else if (Objects.equals(strArray[1], "CANCEL")) {
                mediaPlayerQueue.add(a3cancelPlayer);
            }
            if (!Objects.equals(strArray[1], "CANCEL")) {
                if (Objects.equals(strArray[2], "0")) {
                    mediaPlayerQueue.add(a3a0Player);
                } else if (Objects.equals(strArray[2], "1")) {
                    mediaPlayerQueue.add(a3a1Player);
                } else if (Objects.equals(strArray[2], "2")) {
                    mediaPlayerQueue.add(a3a2Player);
                } else if (Objects.equals(strArray[2], "3")) {
                    mediaPlayerQueue.add(a3a3Player);
                } else if (Objects.equals(strArray[2], "4")) {
                    mediaPlayerQueue.add(a3a4Player);
                } else if (Objects.equals(strArray[2], "5")) {
                    mediaPlayerQueue.add(a3a5Player);
                } else if (Objects.equals(strArray[2], "6")) {
                    mediaPlayerQueue.add(a3a6Player);
                } else if (Objects.equals(strArray[2], "7")) {
                    mediaPlayerQueue.add(a3a7Player);
                } else if (Objects.equals(strArray[2], "8")) {
                    mediaPlayerQueue.add(a3a8Player);
                } else if (Objects.equals(strArray[2], "9")) {
                    mediaPlayerQueue.add(a3a9Player);
                } else if (Objects.equals(strArray[2], "10")) {
                    mediaPlayerQueue.add(a3a10Player);
                } else if (Objects.equals(strArray[2], "11")) {
                    mediaPlayerQueue.add(a3a11Player);
                } else if (Objects.equals(strArray[2], "12")) {
                    mediaPlayerQueue.add(a3a12Player);
                } else if (Objects.equals(strArray[2], "13")) {
                    mediaPlayerQueue.add(a3a13Player);
                } else if (Objects.equals(strArray[2], "14")) {
                    mediaPlayerQueue.add(a3a14Player);
                }
            }
            // CROSS_CHECK STATE
        } else if (Objects.equals(strArray[0], "4")) {
            if (Objects.equals(strArray[1], "0")) {
                mediaPlayerQueue.add(a40Player);
            } else if (Objects.equals(strArray[1], "1")) {
                mediaPlayerQueue.add(a41Player);
            } else if (Objects.equals(strArray[1], "2")) {
                mediaPlayerQueue.add(a42Player);
            }
            // CROSS_AHEAD STATE
        } else if (Objects.equals(strArray[0], "5")) {
            if (Objects.equals(strArray[1], "1")) {
                mediaPlayerQueue.add(a51Player);
            } else if (Objects.equals(strArray[1], "CANCEL")) {
                mediaPlayerQueue.add(a5cancelPlayer);
            }
            // CROSS_HERE STATE
        } else if (Objects.equals(strArray[0], "6")) {
            if (Objects.equals(strArray[1], "1")) {
                mediaPlayerQueue.add(a61Player);
            } else if (Objects.equals(strArray[1], "CANCEL")) {
                mediaPlayerQueue.add(a6cancelPlayer);
            }
            // DETECTION STATE
        } else if (Objects.equals(strArray[0], "7")) {
            if (Objects.equals(strArray[1], "0")) {
                mediaPlayerQueue.clear();
                mediaPlayerQueue.add(a70Player);
            } else if (Objects.equals(strArray[1], "1")) {
                mediaPlayerQueue.clear();
                mediaPlayerQueue.add(a71Player);
            } else if (Objects.equals(strArray[1], "CANCEL")) {
                mediaPlayerQueue.clear();
                mediaPlayerQueue.add(a7cancelPlayer);
            }
        }
    }

    @Override
    public void onMessageSent(DataChannel.Buffer buffer, boolean successful) {
        if (successful) {
            ByteBuffer data = buffer.data;
            final byte[] bytes = new byte[data.capacity()];
            data.get(bytes);
            String messageText = new String(bytes, StandardCharsets.UTF_8);
            // Toast.makeText(this, "Message is sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Could not send the text message", Toast.LENGTH_LONG).show();
        }
    }

    public void sendTextMessage(String messageToSend) {
        final ByteBuffer buffer = ByteBuffer.wrap(messageToSend.getBytes(StandardCharsets.UTF_8));
        DataChannel.Buffer buf = new DataChannel.Buffer(buffer, false);
        webRTCClient.sendMessageViaDataChannel(buf);
    }

    public void showSendDataChannelMessageDialog(View view) {
        if (webRTCClient != null && webRTCClient.isDataChannelEnabled()) {
            // create an alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Send Message via Data Channel");
            // set the custom layout
            final View customLayout = getLayoutInflater().inflate(R.layout.send_message_data_channel, null);
            builder.setView(customLayout);
            // add a button
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // send data from the AlertDialog to the Activity
                    EditText editText = customLayout.findViewById(R.id.message_text_input);
                    sendTextMessage(editText.getText().toString());
                   // sendDialogDataToActivity(editText.getText().toString());
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            Toast.makeText(this, R.string.data_channel_not_available, Toast.LENGTH_LONG).show();
        }
    }
}
