package rd.trafikrapport;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton playBtn = findViewById(R.id.playBtn);
        ImageButton refreshBtn = findViewById(R.id.refreshBtn);
        TextView textViewPrg = findViewById(R.id.textViewProgram);
        TextView textViewDur = findViewById(R.id.textViewDuration);

        final TrafficViewModel trafficViewModel = new ViewModelProvider(this).get(TrafficViewModel.class);
        trafficViewModel.getPlayer().observe(this, new Observer<PlayerStatus>() {
            @Override
            public void onChanged(@Nullable PlayerStatus status) {
                // UPDATE UI

                // Set play / pause icon depending on current status
                playBtn.setImageResource((status.status == PlayerState.PLAYING) ?
                        R.drawable.pause_play : R.drawable.play);

                // Disable and dim buttons while busy fetching from server
                playBtn.setEnabled(!status.busy);
                playBtn.setAlpha((status.busy) ? 0.2f : 1f);
                refreshBtn.setEnabled(!status.busy);
                refreshBtn.setAlpha((status.busy) ? 0.2f : 1f);

                // Update program name and duration
                textViewPrg.setText(status.title);
                textViewDur.setText("(" + status.duration + ")");

                if (status.busy) textViewPrg.setText(R.string.loading);
                if (status.error.length() > 0) textViewPrg.setText(status.error);
            }
        });

        // Spinner
        Spinner spinner = findViewById(R.id.spinner);

        //ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, trafficViewModel.getSourceNames());
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_item, trafficViewModel.getSourceNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Callback callback = new Callback() {
            @Override
            public void run(int i) {
                trafficViewModel.setProgramIndex(i);
            }
        };

        SpinnerListener listener = new SpinnerListener(callback);
        spinner.setOnTouchListener(listener);
        spinner.setOnItemSelectedListener(listener);

        /*
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trafficViewModel.setProgramIndex(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

         */

        // Play / Pause button
        playBtn.setOnClickListener(v -> {
            trafficViewModel.playPause();
        });

        // Refresh button
        refreshBtn.setOnClickListener(v -> {
            trafficViewModel.refresh();
        });

    }


}