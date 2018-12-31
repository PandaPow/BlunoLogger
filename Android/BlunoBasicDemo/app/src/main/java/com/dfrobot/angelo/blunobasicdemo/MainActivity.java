package com.dfrobot.angelo.blunobasicdemo;

// From original app(
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
//)
// For logging to a file(
import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
// Calendar object for timestamp
import java.lang.System;
import java.text.SimpleDateFormat;
//)
//For the foreground notification(
import android.app.PendingIntent;
import android.app.Notification;

//)

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;


public class MainActivity  extends BlunoLibrary {
	private Button buttonScan;
	private Button buttonSerialSend;
	private EditText serialSendText;
	private TextView serialReceivedText;

	private SimpleDateFormat dateFormat;
	private String storagePath;
	private LineGraphSeries series;
    private double lastXValue = 0d;
    private static final int NOTIFICATION_ID = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();														//onCreate Process by BlunoLibrary
        // Set default date format for text log
        dateFormat = new SimpleDateFormat("yyyy/MM/dd,HH:mm:ss");
        // Set default directory for text log
        storagePath = Environment.getExternalStorageDirectory().getPath();
        // Create a new series object of DataPoints
        series = new LineGraphSeries<DataPoint>();

        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//initial the EditText of the received data
        serialSendText=(EditText) findViewById(R.id.serialSendText);			//initial the EditText of the sending data
        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//initial the button for sending the data
        buttonSerialSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
			}
		});

        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        buttonScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
			}
		});
		GraphView graph = findViewById(R.id.serialGraphView);
        graph.getViewport().setScalable(true);
		graph.addSeries(series);
	}

	protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();														//onResume Process by BlunoLibrary
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {											//Four connection state
		case isConnected:
			buttonScan.setText("Connected");
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Scan");
			break;
		case isScanning:
			buttonScan.setText("Scanning");
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
			break;
		default:
			break;
		}
	}

	@Override
	public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
		// TODO Auto-generated method stub
        // Add data point to graph
        double newYValue= Double.parseDouble(theString);
        series.appendData(new DataPoint(lastXValue, newYValue), false, 300);
        ((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
        lastXValue += 1d;
        // Format datapoint with a timestamp
        theString = getTimestamp() + "," + theString;
        //append the text into the EditText
		serialReceivedText.append(theString);
		// Append to text file
        appendLog(theString);
		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
	}

	public String getTimestamp(){
	    long time = System.currentTimeMillis();
	    return dateFormat.format(time);
                // yyyy/MM/dd,HH:mm
    }
    public void appendLog(String text) {

        File logFile = new File(storagePath + "/bluno.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}