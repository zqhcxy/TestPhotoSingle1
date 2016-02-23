package com.example.testphoto.model;

/**
 * 相册对象
 */
public class PhotoAlbumLVItem {
    private String pathName;
    private int fileCount;
    private String firstImagePath;
    private String folderName;

    public PhotoAlbumLVItem(){

    }
    public PhotoAlbumLVItem(String pathName, int fileCount,
                            String firstImagePath) {
        this.pathName = pathName;
        this.fileCount = fileCount;
        this.firstImagePath = firstImagePath;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    // @Override
    // public int hashCode() {
    // return pathName.hashCode();
    // }
    //
    // @Override
    // public boolean equals(Object o) {
    // if (o instanceof SelectImgGVItem){
    // SelectImgGVItem other = (SelectImgGVItem) o;
    // return this.pathName.equals(other.pathName);
    // }
    //
    // return false;
    // }

    @Override
    public String toString() {
        return "SelectImgGVItem{" + "pathName='" + pathName + '\''
                + ", fileCount=" + fileCount + ", firstImagePath='"
                + firstImagePath + '\'' + '}';
    }
}
