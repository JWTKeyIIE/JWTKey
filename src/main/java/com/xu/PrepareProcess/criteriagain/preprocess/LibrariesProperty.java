package com.xu.PrepareProcess.criteriagain.preprocess;

import java.util.List;

/**
 * 从github下载的库应有相关信息
 */
public class LibrariesProperty {
    //库名称
    String LibraryName;
    //调用的JWT库名称
    String JwtLibraryName;
    //调用的JWT API
    String JwtApi;
    //JWT API存在的路径
    List<String> fullPath;

    public LibrariesProperty(String libraryName, String jwtLibraryName, String jwtApi, List<String> fullPath) {
        LibraryName = libraryName;
        JwtLibraryName = jwtLibraryName;
        JwtApi = jwtApi;
        this.fullPath = fullPath;
    }

    public String getLibraryName() {
        return LibraryName;
    }

    public void setLibraryName(String libraryName) {
        LibraryName = libraryName;
    }

    public String getJwtLibraryName() {
        return JwtLibraryName;
    }

    public void setJwtLibraryName(String jwtLibraryName) {
        JwtLibraryName = jwtLibraryName;
    }

    public String getJwtApi() {
        return JwtApi;
    }

    public void setJwtApi(String jwtApi) {
        JwtApi = jwtApi;
    }

    public List<String> getFullPath() {
        return fullPath;
    }

    public void setFullPath(List<String> fullPath) {
        this.fullPath = fullPath;
    }
}
