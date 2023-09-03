package rd.trafikrapport;

import android.media.AudioManager;
import android.media.MediaPlayer;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The ViewModel handling all logic and holding the MediaPlayer
 */
public class TrafficViewModel extends ViewModel {

    private final String[] sourceNames = new String[]{
            "Stockholm",
            "Göteborg",
            "Malmö",
            "Ekot"};

    private final String[] sourceUrls = new String[]{
        "https://api.sr.se/api/rss/pod/10326",
        "https://api.sr.se/api/rss/pod/18748",
        "https://api.sr.se/api/rss/pod/19126",
        "https://api.sr.se/api/rss/pod/3795"
    };

    private final MutableLiveData<PlayerStatus> playerLiveData = new MutableLiveData<>();
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private final PlayerStatus playerStatus = new PlayerStatus();
    private int selection = 0;

    public TrafficViewModel() {
        fetchLatest();
    }

    /**
     * Communicates back to the UI
     * @return Observable
     */
    public MutableLiveData<PlayerStatus> getPlayer() {
        return playerLiveData;
    }

    public String[] getSourceNames() {
        return sourceNames;
    }

    /**
     * Called when user changes program selection
     * @param index of selection
     */
    protected void setProgramIndex(int index) {
        this.selection = index;
        fetchLatest();
    }

    /**
     * Download the XML containg metainfo about the latest program on a background thread
     * and then use a callback to call the parser method.
     */
    protected void fetchLatest() {
        mediaPlayer.stop();
        playerStatus.status = PlayerState.STOPPED;
        playerStatus.busy = true;
        updateUI();

        NetworkResponse callback = new NetworkResponse() {
            @Override
            public void success(String data) {
                parseXML(data);
                playerStatus.error = "";
                playerStatus.busy = false;
                updateUI();
            }

            @Override
            public void error(Exception e) {
                setError(R.string.serverError + e.getMessage());
                playerStatus.busy = false;
                updateUI();
            }
        };

        XmlRetriever xmlRetriever = new XmlRetriever(callback);
        xmlRetriever.execute(sourceUrls[selection]);
    }

    /**
     * Updates the UI by emitting the current status as an Observable object
     */
    private void updateUI() {
        playerLiveData.postValue(playerStatus);
    }

    protected void setError(String error) {
        playerStatus.error = error;
        updateUI();
    }

    /**
     * Parses XML - Extracts path to audio file etc.
     * Called from callback after successful downloading xml
     * @param xml to parse
     */
    public void parseXML(String xml) {
        int start = xml.indexOf("<item>");
        int end = xml.indexOf("</item>");

        String lastItem = xml.substring(start, end);
        start = lastItem.indexOf("<title>") + 16;
        end = lastItem.indexOf("</title>") - 3;
        playerStatus.title = lastItem.substring(start, end);
        if (playerStatus.title.contains("(")) {
            playerStatus.title = playerStatus.title.substring(0, playerStatus.title.indexOf("("));
        }

        start = lastItem.indexOf("<itunes:duration>");
        end = lastItem.indexOf("</itunes:duration>");
        playerStatus.duration = lastItem.substring(start + 17, end);

        start = lastItem.indexOf("<enclosure url=\"") + 16;
        end = lastItem.indexOf("\"", start +1);
        playerStatus.audioPath = lastItem.substring(start, end);

    }

    /**
     * Sets up and then prepares the Mediaplayer on a background thread
     */
    private void preparePlayer() {

        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnErrorListener((mplayer, i, i1) -> {
            setError(R.string.mediaErr + i + "  " + i1);
            return false;
        });

        mediaPlayer.setOnCompletionListener(listener -> playerStatus.status = 0);

        try {
            mediaPlayer.setDataSource(playerStatus.audioPath);

            // Start playing the audio after finishing preparing on background thread
            mediaPlayer.setOnPreparedListener(mp -> mp.start());

            // Prepares on a background thread
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            setError(R.string.mediaErr + e.getMessage());
        }
    }

    /**
     * Handles play / pause commands depending on current status
     */
    protected void playPause() {

        if (playerStatus.status == PlayerState.STOPPED && playerStatus.audioPath.length() > 1 ) {
            playerStatus.status = PlayerState.PLAYING;
            preparePlayer();
            updateUI();
            return;
        }

        if (playerStatus.status == PlayerState.PAUSED) {
            playerStatus.status = PlayerState.PLAYING;
            mediaPlayer.start();
            updateUI();
            return;
        }

        if (playerStatus.status == PlayerState.PLAYING) {
            playerStatus.status = PlayerState.PAUSED;
            mediaPlayer.pause();
            updateUI();
        }
    }

    protected void refresh() {
        fetchLatest();
    }
}
