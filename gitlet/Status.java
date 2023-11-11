package gitlet;


import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Status.
 *
 * @author Zihao
 **/
public class Status implements Serializable {
    /**
     * blobs;.
     */
    private Set<Blob> _blobs;
    /**
     * removedFile;.
     */
    private Set<String> _removedFile;

    /**
     * Status;.
     *
     * @param blobs       to blobs
     * @param removedFile to removedFile
     */
    public Status(Blob[] blobs, String[] removedFile) {
        this._blobs = new LinkedHashSet<>();
        this._removedFile = new LinkedHashSet<>();

        for (Blob blob : blobs) {
            this._blobs.add(blob);
        }
        for (String file : removedFile) {
            this._removedFile.add(file);
        }

    }

    /**
     * addBlobs;.
     *
     * @param blob to blobs
     */
    public void addBlobs(Blob blob) {
        if (!_blobs.stream().anyMatch(blob1 ->
                blob1.getFileName().equals(blob.getFileName()))) {
            this._blobs.add(blob);
        }
        if (_removedFile.contains(blob.getFileName())) {
            _removedFile.remove(blob.getFileName());
        }
    }

    /**
     * addremovedFile;.
     *
     * @param path to path
     */
    public void addremovedFile(String path) {
        this._removedFile.add(path);
        for (Blob blob : _blobs) {
            if (blob.getFileName().equals(path)) {
                _blobs.remove(blob);
                return;
            }
        }
    }
    /**
     * getBlob;.
     * @return _blobs
     */
    public Set<Blob> getBlob() {
        return _blobs;
    }
    /**
     * getRemovedFile;.
     * @return _removedFile
     */
    public Set<String> getRemovedFile() {
        return _removedFile;
    }
}
