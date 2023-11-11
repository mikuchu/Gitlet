package gitlet;
import java.io.Serializable;

/**
 *  A component that contant the information
 *  for the Commit class.
 *
 * @author Zihao.
 */

public class Blob implements Serializable {
    /** @param _content virable contant information.
     */
    private String _content;
    /**
     * @param _fileName virable contain Name of file.
     */
    private String _fileName;
    /**
     * @param _sha virable contain the code.
     */
    private String _sha;


    /**
     * A Blob for which.
     *  @param  fileName  to get _filename
     *  @param  content  to get _content
     *  @param  sha  to get _sha
     */
    Blob(String fileName, String content, String sha) {
        this._fileName = fileName;
        this._content = content;
        this._sha = sha;
    }

    /**
     * A funtion to get the name of file in Blob.
     * @return _fileName
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * A funciton to get the content of file in Blob.
     * @return _content
     */
    public String getContent() {
        return _content;
    }

    /**
     * A function to get the code in Blob.
     * @return _sha
     */
    public String getSha() {
        return _sha;
    }

}

