package gitlet;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Commit.
 *
 * @author Zihao
 */
public class Commit implements Serializable {

    /**
     *message;.
     */
    private String _message;
    /**
     *timestamp;.
     */
    private String _timestamp;
    /**
     *String parentSHA;.
     */
    private String _parentSHA;
    /**
     *String blobs;.
     */
    private Blob[] _blobs;
    /**
     *changeList;.
     */
    private String[] _changeList;
    /**
     *removeList;.
     */
    private String[] _removeList;
    /**
     *Commit;.
     * @param message to get message
     */
    public Commit(String message) {
        this(message, "");
    }
    /**
     *Commit;.
     * @param message to get message
     * @param parentSHA to get parentSHA
     */
    public Commit(String message, String parentSHA) {
        this(message, parentSHA, new Blob[0], new String[0], new String[0]);
    }
    /**
     *Commit;.
     * @param message to get message
     * @param parentSHA to get parentSHA
     *@param blobs to get blobs
      * @param changeList to get changeList
     *     @param removeList to get removeList
     */
    public Commit(String message, String parentSHA, Blob[] blobs,
                  String[] changeList, String[] removeList) {
        DateTimeFormatter sdf = DateTimeFormatter
                .ofPattern("EEE MMM d HH:mm:ss yyyy Z");
        this._blobs = blobs;
        this._message = message;
        this._parentSHA = parentSHA;
        this._changeList = changeList;
        this._removeList = removeList;
        if (this._parentSHA.isEmpty()) {
            this._timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            this._timestamp = ZonedDateTime.now().format(sdf);
        }
    }

    /**
     *getMessage;.
     *@return message.
     */
    public String getMessage() {
        return this._message;
    }
    /**
     *getBlobs;.
     *@return blobs.
     */
    public Blob[] getBlobs() {
        return this._blobs;
    }
    /**
     *getTimestamp;.
     * @return timestamp.
     */
    public String getTimestamp() {
        return this._timestamp;
    }
    /**
     *getParrentSHA;.
     * @return parentSHA.
     */
    public String getParrentSHA() {
        return this._parentSHA;
    }

    /**
     *getChangeList;.
     * @return _changeList.
     */
    public String[] getChangeList() {
        return _changeList;
    }

    /**
     *getRemoveList;.
     * @return _removeList.
     */
    public String[] getRemoveList() {
        return _removeList;
    }
}
