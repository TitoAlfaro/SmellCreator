package mit.edu.obmg.smellcreator;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SmellCreatorMain extends IOIOActivity implements
		OnSeekBarChangeListener, OnItemSelectedListener {
	final String TAG = "SmellCreatorMain";

	Spinner spnBase, spnTop;

	TextView txtTopTime, txtBaseTime, txtTimer, txtBase, txtTop;
	int baseId, topId;
	int current = 50;
	int totalTime = 20000;
	private SeekBar mRatioBar;

	private ToggleButton btnStart;

	boolean run = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smell_creator_main);

		txtBase = (TextView) findViewById(R.id.txtBaseTitle);
		txtTop = (TextView) findViewById(R.id.txtTopTitle);

		spnBase = (Spinner) findViewById(R.id.spnBase);
		spnTop = (Spinner) findViewById(R.id.spnTop);

		ArrayAdapter<CharSequence> bases = ArrayAdapter.createFromResource(
				this, R.array.bases, R.layout.spinner_layout);
		
		ArrayAdapter<CharSequence> tops = ArrayAdapter.createFromResource(
				this, R.array.tops, R.layout.spinner_layout);

		spnBase.setAdapter(bases);
		spnBase.setSelection(0);
		spnBase.setOnItemSelectedListener(this);
		spnTop.setAdapter(tops);
		spnTop.setSelection(0);
		spnTop.setOnItemSelectedListener(this);

		txtBaseTime = (TextView) findViewById(R.id.txtBaseSmell);
		txtTopTime = (TextView) findViewById(R.id.txtTopSmell);
		txtTimer = (TextView) findViewById(R.id.txtTimer);
		mRatioBar = (SeekBar) findViewById(R.id.seekBar1);
		mRatioBar.setEnabled(true);
		mRatioBar.setProgress(50);
		mRatioBar.incrementProgressBy(10);
		mRatioBar.setMax(100);
		mRatioBar.setOnSeekBarChangeListener(this);

		btnStart = (ToggleButton) findViewById(R.id.btnStart);
		btnStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					new CountDownTimer(totalTime, 1000) { 

						public void onTick(long millisUntilFinished) {
							run = true;
							txtTimer.setText("Time: " + millisUntilFinished
									/ 1000);
						}

						public void onFinish() {
							txtTimer.setText("done!");
							btnStart.setChecked(false);
							run = false;
						}
					}.start();
				} else {

				}
			}

		});
	}

	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led_, outBottle01, outBottle02, outBottle03,
				outBottle04, outBottle05, outBottle06, outBottle07,
				outBottle08;

		private DigitalOutput base = outBottle01;
		private DigitalOutput top = outBottle02;

		@Override
		protected void setup() throws ConnectionLostException,
				InterruptedException {
			led_ = ioio_.openDigitalOutput(0, true);
			outBottle01 = ioio_.openDigitalOutput(1, true);
			outBottle02 = ioio_.openDigitalOutput(2, true);
			outBottle03 = ioio_.openDigitalOutput(3, true);
			outBottle04 = ioio_.openDigitalOutput(4, true);
			outBottle05 = ioio_.openDigitalOutput(5, true);
			outBottle06 = ioio_.openDigitalOutput(6, true);
			outBottle07 = ioio_.openDigitalOutput(7, true);
			outBottle08 = ioio_.openDigitalOutput(8, true);

		}

		@Override
		public void loop() throws ConnectionLostException {
			led_.write(!btnStart.isChecked());
			outBottle01.write(false);
			outBottle02.write(false);
			outBottle03.write(false);
			outBottle04.write(false);
			outBottle05.write(false);
			outBottle06.write(false);
			outBottle07.write(false);
			outBottle08.write(false);

			while (run) {

				baseId = spnBase.getSelectedItemPosition();
				topId = spnTop.getSelectedItemPosition();

				switch (baseId) {
				case 0:
					base = outBottle01;
					break;
				case 1:
					base = outBottle02;
					break;

				default:
					break;
				}

				switch (topId) {
				case 0:
					top = outBottle03;
					break;
				case 1:
					top = outBottle04;
					break;
				case 2:
					top = outBottle05;
					break;
				case 3:
					top = outBottle06;
					break;
				case 4:
					top = outBottle07;
					break;
				case 5:
					top = outBottle08;
					break;

				default:
					break;
				}
				try {
					for (int i = 0; i < 4; i++) {
						
						base.write(true);
						setColor (txtBase, "green");
						Thread.sleep((current * totalTime/100)/4);
						base.write(false);
						setColor (txtBase, "white");
						top.write(true);
						setColor (txtTop, "green");
						Thread.sleep((totalTime - (current * totalTime/100))/4);
						top.write(false);
						setColor (txtTop, "white");
					}

				} catch (InterruptedException e) {
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
	
	void setColor (final TextView text, final String color){

		text.post(new Runnable() {
			public void run() {
				text.setBackgroundColor(Color
						.parseColor(color));
			}
		});
	}

	void Blow(DigitalOutput bottle1, DigitalOutput bottle2) {

	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		progress = progress / 10;
		progress = progress * 10;
		current = progress;
		txtBaseTime.setText("Top: " + String.valueOf(100 - progress));
		txtTopTime.setText("Base: " + String.valueOf(progress));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}
}