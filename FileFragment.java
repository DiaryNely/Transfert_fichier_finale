package fragment;

import java.util.Objects;

public class FileFragment {
    private String fileName;
    private int fragmentIndex;
    private byte[] data;

    // Constructeur
    public FileFragment(String fileName, int fragmentIndex, byte[] data) {
        this.fileName = fileName;
        this.fragmentIndex = fragmentIndex;

        this.data = data;
    }

    // Getter pour fileName
    public String getFileName() {
        return fileName;
    }

    // Setter pour fileName
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // Getter pour fragmentIndex
    public int getFragmentIndex() {
        return fragmentIndex;
    }

    // Setter pour fragmentIndex
    public void setFragmentIndex(int fragmentIndex) {
        this.fragmentIndex = fragmentIndex;
    }

    // Getter pour data
    public byte[] getData() {
        return data;
    }

    // Setter pour data
    public void setData(byte[] data) {
        this.data = data;
    }

    // Méthode pour afficher les informations du fragment
    @Override
    public String toString() {
        return "FileFragment{" +
                "fileName='" + fileName + '\'' +
                ", fragmentIndex=" + fragmentIndex +
                ", dataLength=" + (data != null ? data.length : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FileFragment that = (FileFragment) o;
        return fragmentIndex == that.fragmentIndex && fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fragmentIndex);
    }
}
