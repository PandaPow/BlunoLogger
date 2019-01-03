package com.dfrobot.angelo.blunobasicdemo;

// From original app(
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
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
//For keeping the app awake(
import android.view.WindowManager;
//)
//For visualizing the graph(
import java.util.Iterator;
import android.graphics.Color;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.LegendRenderer;
//)

public class MainActivity  extends BlunoLibrary {
    //UI objects
	private Button buttonScan;
	private Button buttonSerialSend;
	private ToggleButton buttonScreen;
	private TextView serialReceivedText;
    private GraphView graph;
	//Default date format for timestamp
	private SimpleDateFormat dateFormat;
	//Storage path where text file will end up
	private String storagePath;
	//Number of seconds offset between series - minimum second points displayed at all times after initialization
	private static int secOffset = 60;
	//Maximum minutes to display
	private static int minuteMax = 480;
	//Graph displaying objects
	//Keep two series so the past 60 seconds is always shown on the screen
	private LineGraphSeries secondSeriesA;
    private LineGraphSeries secondSeriesB;
	private LineGraphSeries minuteSeries;
    private double secCounter = 0d;
    private double minCounter = 0d;
    //Used to track how data is added to graph
    private int state = 0;
	
	@SuppressLint("SimpleDateFormat")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        //onCreate Process by BlunoLibrary
        onCreateProcess();
        // Set default date format for text log
        dateFormat = new SimpleDateFormat("yyyy/MM/dd,HH:mm:ss");
        // Set default directory for text log
        storagePath = Environment.getExternalStorageDirectory().getPath();
        // Create a new secondSeries object of DataPoints
        secondSeriesA = new LineGraphSeries<DataPoint>();
        secondSeriesB = new LineGraphSeries<DataPoint>();
		minuteSeries = new LineGraphSeries<DataPoint>();
        //set the Uart Baudrate on BLE chip to 115200
        serialBegin(115200);
        //initial the EditText of the received data
        serialReceivedText = findViewById(R.id.serialReveicedText);
        //initial the button for sending the data
        buttonSerialSend = findViewById(R.id.buttonSerialSend);
        buttonSerialSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                //send the data to the BLUNO
				serialSend("init");
			}
		});
        //initial the button for scanning the BLE device
        buttonScan = findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                //Alert Dialog for selecting the BLE device
				buttonScanOnClickProcess();
			}
		});

        //initial the button for keeping the device from falling asleep
        buttonScreen = findViewById(R.id.buttonScreenOn);
		buttonScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				buttonScreenOnCheckedProcess(isChecked);
			}
		});
		//Keep awake by default
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// Set up graph
        initialize_graph();

	}

	private void initialize_graph(){
        graph = findViewById(R.id.serialGraphView);
        // Set axis bounds
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(2*secOffset+1);
        //Set titles for legend
        secondSeriesA.setTitle(getString(R.string.seconds_title));
        secondSeriesB.setTitle(getString(R.string.seconds_title));
        minuteSeries.setTitle(getString(R.string.minutes_title));
        //Set colors for graphing. Second series A and B should be the same color
        secondSeriesA.setColor(Color.rgb(255,178,102));
        secondSeriesB.setColor(Color.rgb(255,178,102));
        minuteSeries.setColor(Color.rgb(115,255,255));
        //Show and align legend
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getLegendRenderer().setVisible(true);
        //Set titles
        graph.getGridLabelRenderer().setHorizontalAxisTitle(getString(R.string.hor_title));
        graph.getGridLabelRenderer().setVerticalAxisTitle(getString(R.string.vert_title));
        graph.setTitle(getString(R.string.graph_title));
        graph.setTitleTextSize(40);
        //Add series to graph
        graph.addSeries(secondSeriesA);
        graph.addSeries(minuteSeries);
        graph.getGridLabelRenderer().setPadding(28);
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
        //onPause Process by BlunoLibrary
        onPauseProcess();
    }

	protected void onStop() {
		super.onStop();
        //onStop Process by BlunoLibrary
		onStopProcess();
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();
        //onDestroy Process by BlunoLibrary
        onDestroyProcess();
    }

	@Override
    //Once connection state changes, this function will be called
	public void onConectionStateChange(connectionStateEnum theConnectionState) {
		switch (theConnectionState) {
        //Four connection states
		case isConnected:
			buttonScan.setText(getString(R.string.connected));
			break;
		case isConnecting:
			buttonScan.setText(getString(R.string.connecting));
			break;
		case isToScan:
			buttonScan.setText(getString(R.string.scan));
			break;
		case isScanning:
			buttonScan.setText(getString(R.string.scanning));
			break;
		case isDisconnecting:
			buttonScan.setText(getString(R.string.is_disconnecting));
			break;
		default:
			break;
		}
	}

	public void buttonScreenOnCheckedProcess(boolean isChecked){
		if (isChecked) {
			// The toggle is enabled; keep the screen on until button is unchecked
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			// The toggle is disabled; allow screen to fall asleep
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@Override
	public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
		// TODO Auto-generated method stub
        // Get data point value
        double newYValue= Double.parseDouble(theString);
        ((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
        // Format datapoint with a timestamp
        theString = getTimestamp() + "," + theString;
        //append the text into the EditText
		serialReceivedText.append(theString);
		// Append to text file
        appendLog(theString);
		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
		//process data point
        processNewValue(newYValue);
	}

	private void processNewValue(double value){
		secCounter += 1d;
		switch (state) {
            case 0:
                secondSeriesA.appendData(new DataPoint(secCounter,value),false,3* secOffset,false);
                if((int)secCounter == secOffset){
                    minCounter += 1d;
                    minuteSeries.appendData(new DataPoint(minCounter,averageSeries(secondSeriesA)),false,minuteMax,false);
                    secCounter = 0;
                    state = 2;
                }
                break;
            case 1:
                secondSeriesB.appendData(new DataPoint(secCounter+ secOffset,value),false,3* secOffset,false);
                secondSeriesA.appendData(new DataPoint(secCounter,value),false,3* secOffset,false);
                graph.onDataChanged(true,true);
                if ((int)secCounter == secOffset){
                    minCounter += 1d;
                    boolean scrolltoEnd = false;
                    if (minCounter > 2* secOffset){
                        scrolltoEnd = true;
                    }
                    minuteSeries.appendData(new DataPoint(minCounter,averageSeries(secondSeriesA)),scrolltoEnd,minuteMax,false);
                    secCounter = 0;
                    state = 3;
                }
                break;
            case 2:
                secondSeriesA.appendData(new DataPoint(secCounter+ secOffset,value),false,3* secOffset,false);
                secondSeriesB.appendData(new DataPoint(secCounter,value),false,3* secOffset,false);
                graph.onDataChanged(true,true);
                if ((int)secCounter == secOffset){
                    minCounter += 1d;
                    boolean scrolltoEnd = false;
                    if (minCounter > 2* secOffset){
                        scrolltoEnd = true;
                    }
                    minuteSeries.appendData(new DataPoint(minCounter,averageSeries(secondSeriesB)),scrolltoEnd,minuteMax,false);
                    secCounter = 0;
                    state = 4;
                }
                break;
            case 3:
                secondSeriesA.appendData(new DataPoint(secCounter+ secOffset,value),false,3* secOffset,false);
                graph.onDataChanged(true,true);
                graph.removeSeries(secondSeriesB);
                graph.addSeries(secondSeriesA);
                graph.onDataChanged(true,true);
                DataPoint[] newSeriesB = new DataPoint[1];
                newSeriesB[0] = new DataPoint(secCounter,value);
                secondSeriesB.resetData(newSeriesB);
                state = 2;
                break;
            case 4:
                secondSeriesB.appendData(new DataPoint(secCounter+ secOffset,value),false,3* secOffset,false);
                graph.onDataChanged(true,true);
                graph.removeSeries(secondSeriesA);
                graph.addSeries(secondSeriesB);
                graph.onDataChanged(true,true);
                DataPoint[] newSeriesA = new DataPoint[1];
                newSeriesA[0] = new DataPoint(secCounter,value);
                secondSeriesA.resetData(newSeriesA);
                state = 1;
                break;
        }
	}

	private double averageSeries(LineGraphSeries E){
	    Iterator values = E.getValues(E.getLowestValueX(),E.getHighestValueX());
	    double count = 0;
	    double sum = 0;
	    while(values.hasNext()){
	        sum += ((DataPoint)values.next()).getY();
	        count += 1;
        }
        return sum/count;
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