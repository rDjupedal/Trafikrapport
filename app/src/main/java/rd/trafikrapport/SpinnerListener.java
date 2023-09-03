package rd.trafikrapport;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

/**
 * Custom eventlistener for spinners in order to avoid automatic calls on screenrotations etc.
 */
public class SpinnerListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private boolean userClicked = false;
    private Callback callback;

    public SpinnerListener(Callback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        userClicked = true;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (userClicked) callback.run(position);
        userClicked = false;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }
}
