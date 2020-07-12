package com.example.proximitychat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatWindowActivity extends AppCompatActivity {

    private static final int MESSAGE_READ = 1;

    //ServerClass2 serverClass;
    // ClientClass2 clientClass;

    MessageThread msgThread;
    InetAddress serverAddress;
    Socket socket;
    ServerSocket serverSocket;

    boolean isRunning;
    boolean isDead;

    int sendCount = 0;
    TextView myMsg;
    TextView yourMsg;
    Button sendMsg;
    String role;
    EditText mEditText;

    UserData userData;
    AMessage mAMessage;


    ObjectMapper mapper;


    ScheduledExecutorService executor;


    RecyclerView mMessageView;
    MessageAdapter mAdapter;
    ArrayList<AMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        messageList = new ArrayList<>();


        //AMessage testmsg = new AMessage("Hello world", new UserData("Omar","Blue"),false);

        //messageList.add(testmsg);

        mapper = new ObjectMapper();

        mMessageView = (RecyclerView) findViewById(R.id.message_recycler);
        mAdapter = new MessageAdapter(this, messageList);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMessageView.setLayoutManager(layoutManager);

        mMessageView.setAdapter(mAdapter);

        isRunning = false;
        isDead = false;


        //myMsg = findViewById(R.id.myMsg);
        //yourMsg = findViewById(R.id.yourMsg);
        sendMsg = findViewById(R.id.sendMsg);

        mEditText = findViewById(R.id.editText);

        Intent intent = getIntent();

        role = intent.getStringExtra("role");
        serverAddress = (InetAddress) intent.getSerializableExtra("info");
        executor = (ScheduledExecutorService) Executors.newScheduledThreadPool(5);


        switch (role) {
            case "Host":
                userData = new UserData("Host", "Red");
                CreateHostThreadTask createHost = new CreateHostThreadTask();

                try {
                    Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Connection Failed. Please try again.", Toast.LENGTH_SHORT).show();

                }
                Log.i("!!!!!!!!!", "HOST");

                executor.execute(createHost);
                break;
            case "Client":
                userData = new UserData("Client", "Blue");
                CreateClientThreadTask createClient = new CreateClientThreadTask();
                try {
                    Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Connection Failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
                Log.i("!!!!!!!!!", "CLIENT");

                executor.execute(createClient);


        }

        final Observer<Boolean> booleanObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                //myMsg.setText("kkkkkkkkkkkkkkkkkkkkkkkkk");
                Log.i("Changed", "Observed variable");
            }
        };

        final MyViewModel mModel = new ViewModelProvider(this).get(MyViewModel.class);

        mModel.getData().observe(this, booleanObserver);

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                try {
                    if (socket != null) {

                        // getInputStream.read allows us to see if the connection is closed
                        if (socket.getInputStream().read() == -1 && isRunning) {
                            Log.i("IMPORTANT", "Connection is dead");
                            mModel.setData(true);
                            executor.shutdown();
                            // handler.obtainMessage(2).sendToTarget();


                        }
                    } else {
                        Log.i("IMPORTANT", "Connection is dead");
                        mModel.setData(true);
                        executor.shutdown();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("IMPORTANT", "Connection is dead");
                    mModel.setData(true);
                    executor.shutdown();

                }
            }
        }, 5, 5, TimeUnit.SECONDS);


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(mEditText.getText().toString());

            }
        });
    }


    private void sendMessage(String message) {


        WriteToServerTask WriteToServer = new WriteToServerTask(message);

        executor.execute(WriteToServer);


    }

// A serverClass is created


    // Sets the text in the message box to the message string
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //I was here on 24/06/20 17:01


            try {

                if (msg.what == 0) {
                    // Message converted to string
                    byte[] readBuff = (byte[]) msg.obj;
                    // ____ This should be sent in the form of a Json object, and converted to a AMessage object
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    // Upon the message being received, the UI is changed.

                    mAMessage = mapper.readValue(readBuff, AMessage.class);

                    //yourMsg.setText(tempMsg);

                    messageList.add(mAMessage);

                    mMessageView.setAdapter(mAdapter);


                } else if (msg.what == 2) {

                    Toast.makeText(ChatWindowActivity.this, "Disconnected from the other user.", Toast.LENGTH_SHORT).show();
                }
                return true;
                // This might break the program
            } catch (StringIndexOutOfBoundsException e) {
                return false;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }
    });


    private class CreateClientThreadTask implements Runnable {

        @Override
        public void run() {
            try {

                socket = new Socket(serverAddress, 8001);
                //socket.connect(new InetSocketAddress(serverAddress, 8001), 500);
                msgThread = new MessageThread(socket);
                msgThread.start();
            } catch (Exception e) {

                e.printStackTrace();
            }

        }
    }


    public class MessageThread extends Thread {
        Socket socket;
        OutputStream outputStream;
        InputStream inputStream;

        public MessageThread(Socket sock) {
            Log.i("IMPORTANT", "Comms Thread Created");
            socket = sock;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int tryCount = 0;
            int bytes;

            isRunning = true;
            while (true) {
                try {

                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(0, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }


            }
//            Toast.makeText(ChatWindowActivity.this, String.format("AMessage receive failed after %d attempts", tryCount), Toast.LENGTH_SHORT).show();
            Log.i("IMPORTANT", String.format("AMessage receive failed after %d attempts", tryCount));
        }


        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class WriteToServerTask implements Runnable {
        String messageText;

        AMessage sentMsg;

        AMessage myMsg;

        public WriteToServerTask(String data) {
            messageText = data;
        }

        @Override
        public void run() {
            //byte[] theByteArray = messageText.getBytes();
            sentMsg = new AMessage(messageText, userData, false);
            myMsg = new AMessage(messageText, userData, true);

            try {
                // Create a message object
                // Convert the message to Json
                byte[] msgBytes = mapper.writeValueAsBytes(sentMsg);

                //Send the Json
                msgThread.write(msgBytes);

                messageList.add(sentMsg);
                mMessageView.setAdapter(mAdapter);


            } catch (JsonProcessingException e) {
                e.printStackTrace();
                Log.i("DDDD", "DDDDDD");
            }
        }
    }

    private class CloseSocketTask implements Runnable {
        @Override
        public void run() {
            try {
                socket.close();
                Log.i("IMPORTANT", "SOCKET CLOSED");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ChatEndedTask implements Runnable {
        @Override
        public void run() {
            try {
                myMsg.setText("fffffffffffffffffffffffffffff");
                Toast.makeText(ChatWindowActivity.this, "Disconnected from other user.", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CloseSocketTask closeSocket = new CloseSocketTask();
        executor.execute(closeSocket);

    }


    public void onBackPressed() {
        super.onBackPressed();
        try {
            CloseSocketTask closeSocket = new CloseSocketTask();
            executor.execute(closeSocket);

        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean chatFinished = true;
        Intent intent = new Intent();
        intent.putExtra("MESSAGE", chatFinished);
        setResult(1, intent);
        finish();


    }

    public class CreateHostThreadTask implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8001);
                socket = serverSocket.accept();
                msgThread = new MessageThread(socket);

                Log.i("IMPORTANT", "Connected Successfully");
                msgThread.start();
            } catch (IOException e) {
                e.printStackTrace();

            }

        }
    }

    private String getRandomName() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};
        return (
                adjs[(int) Math.floor(Math.random() * adjs.length)] +
                        "_" +
                        nouns[(int) Math.floor(Math.random() * nouns.length)]
        );
    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while (sb.length() < 7) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }
}



/*
 * PROGRAM NOTES
 *
 * I decided not to boot users out when they disconnect, because that is annoying to have, and with poor connection it would become frustrating.
 * */







