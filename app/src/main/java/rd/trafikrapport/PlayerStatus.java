package rd.trafikrapport;

/**
 * Container of current player status
 */
public class PlayerStatus {

    protected String title;                         // The title of the audio clip
    protected String audioPath;                     // Path to the audio file
    protected String duration;                      // Duration of the clip
    protected String error = "";                    // Any error message to be displayed to user
    protected int status = PlayerState.STOPPED;     // Status of the MediaPlayer
    protected boolean busy = false;                 // If the program is busy fetching a resource

}
