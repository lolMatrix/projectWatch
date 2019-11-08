package com.matrixsofware.matrixwatch;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetooth;
    private ProgressDialog progressDialog;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private ListView deviceList;
    private BluetoothListAdapter adapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream stream;
    private EditText command;
    private Button send;
    private ScheduledExecutorService service2;
    private JSONObject message;
    private NotificationReceiver nReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.matrixsofware.matrixwatch.NOTIFICATION_LISTENER");
        registerReceiver(nReceiver,filter);

        message = new JSONObject();
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        adapter = new BluetoothListAdapter(this, R.layout.device, devices);
        Log.d("Watch", String.valueOf(bluetooth.isEnabled()));
        if (bluetooth.isEnabled()) {
            // Bluetooth включен. Работаем.
            //тут обработка текста(в тесте) или работа с датой, временем и уведомлениями
        }
        else
        {
            // Bluetooth выключен. Предложим пользователю включить его.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);// по завершению, мы будем смотреть на onActivityResult
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        command = (EditText) findViewById(R.id.command);
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent i = new Intent("com.matrixsofware.matrixwatch.NOTIFICATION_SERVICE_LISTENER");
                i.putExtra("command","list");
                sendBroadcast(i);
                mainScript(command.getText().toString() + "\n");
            }
        });





    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu); // создаем меню в тулбаре
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.searchDevices:
                startSearchDevices();
                break;

            case R.id.exit:

                if(!service2.isShutdown()){
                    service2.shutdown();
                }
                if(bluetoothSocket.isConnected()){
                    try {
                        mainScript("goodbye");
                        stream.close();
                        bluetoothSocket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT){
        String status;
        if(bluetooth.isEnabled()){
            // работает, начинаем
        }
        else
        {
            status="Bluetooth выключен";
            Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            finish(); // выходим из проги

                }
            }
        }

        private void mainScript(String message){

        byte[] b = message.getBytes();

        if(stream != null){
            try {
                stream.write(b);
                stream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        }

        public void startSearchDevices(){

            if (bluetooth.isEnabled()) {
                // Bluetooth включен. Работаем.
                if(!bluetooth.isDiscovering()){//проверка на поиск устройств
                    bluetooth.startDiscovery();

                }
                else {
                    bluetooth.cancelDiscovery();
                    bluetooth.startDiscovery();
                }

                IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//начинаем поиск устройств
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                registerReceiver(bResirver , filter);
            }
            else
            {
                // Bluetooth выключен. Предложим пользователю включить его.
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);// по завершению, мы будем смотреть на onActyvityResult
            }



        }
    private BroadcastReceiver bResirver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){//проверяю статус поиска
                Log.d("lol", "start");
                progressDialog = ProgressDialog.show(MainActivity.this, "Идет поиск устройств", "Пожайлуйста, подожди");
            }
            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                progressDialog.dismiss();
                showDevices();
                Log.d("lol", "finish");

            }
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                Log.d("lol", "yes");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null){
                    if(!devices.contains(device)){
                        adapter.add(device);//если устройство не ноль и нету в листе, то добовляем в адаптер
                    }
                }
            }
        }
    };

    public void showDevices(){
        AlertDialog.Builder dList = new AlertDialog.Builder(this);
        dList.setTitle("Список устройств");

        View v = getLayoutInflater().inflate(R.layout.device_list, null);//создаю диалоговое окно с найденными устройствами
        deviceList = v.findViewById(R.id.bList);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(onSelectDevice);//устанавливаю слушатель на каждый элемент


        dList.setView(v);
        dList.setNegativeButton("OK", null);
        dList.create();
        dList.show();

    }

    private AdapterView.OnItemClickListener onSelectDevice = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice d = devices.get(position);
            setConnection(d);//выбираю из коллекции устройства и соединяюсь с ним
        }
    };

    private void setConnection(BluetoothDevice d) {
        if (d != null){
            Method m = null;
            try {
                m = d.getClass().getMethod("createRfcommSocket", new Class[]{int.class});// запрашиваю у устройства метод подключения
                bluetoothSocket = (BluetoothSocket) m.invoke(d, 1);
                bluetoothSocket.connect();//подключаюсь

                stream = bluetoothSocket.getOutputStream();

                mainScript("Connection Successful\n");
                timeForWatch(3);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void timeForWatch(final int timel){
        service2 = Executors.newSingleThreadScheduledExecutor();
        service2.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                SimpleDateFormat format = new SimpleDateFormat("dd MMM y");
                Date d = new Date();
                String date = format.format(d);
                format = new SimpleDateFormat("HH:mm");
                String time = format.format(d);
                try {
                    message.put("name", "time");
                    message.put("date", date);
                    message.put("time", time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mainScript(message.toString() + "\n");
            }
        }, 3, timel, TimeUnit.SECONDS);
    }
    class NotificationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String temp = intent.getStringExtra("notification_event");
            Log.d("lol", temp);
            mainScript(temp);
        }
    }

}
