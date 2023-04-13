package com.xu.PrepareProcess.criteriagain.preprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonFileProcess {

    public static String readJsonFile(String jsonFilePath) throws IOException{
        String jsonStr = "";
        File jsonFile = new File(jsonFilePath);
        FileReader fileReader = new FileReader(jsonFile);
        Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
        int ch = 0;
        StringBuffer stringBuffer = new StringBuffer();
        while ((ch = reader.read()) != -1) {
            stringBuffer.append((char) ch);
        }
        fileReader.close();
        reader.close();
        jsonStr = stringBuffer.toString();
/*        System.out.println(jsonStr);*/
        return jsonStr;
    }


    /**
     * 解析Github下载的所有库中有关JWT-API调用信息
     * @param jsonStr
     * @return 返回所有库对应的LibrariesProperty列表
     */
    public static List<LibrariesProperty> processJsonFile(String jsonStr) {
        List<LibrariesProperty> LibrariesPropertyList = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String jwtLibrary = (String) jsonObject.get("jwt_library");
        JSONArray githubSearchResult = (JSONArray) jsonObject.get("github_search_result");
        for (int i = 0; i < githubSearchResult.size(); i++) {
            JSONObject libraryInfo = githubSearchResult.getJSONObject(i);
            String name = (String) libraryInfo.get("name");
            JSONArray full_path = (JSONArray) libraryInfo.get("full_path");
            List<String> pathList = new ArrayList<>();
            for (int j = 0; j < full_path.size(); j++) {
                JSONObject pathInfo = full_path.getJSONObject(j);
                String path = (String) pathInfo.get("path");
                pathList.add(path);
            }
            LibrariesProperty librariesProperty = new LibrariesProperty(name,jwtLibrary,"RSA-Algorithm",pathList);
            LibrariesPropertyList.add(librariesProperty);
        }
        System.out.println(LibrariesPropertyList.size());
        return LibrariesPropertyList;
    }

    /**
     * 读取每个库中保存的JWT-Result文件中的Json对象
     * @param str JWT-Result文件中读取的Json String
     * @return 返回这个库对应的Libraries Property信息
     */
    public static LibrariesProperty processLibJsonFile(String str){
        JSONObject jsonObject = JSON.parseObject(str);
        String name = (String) jsonObject.get("libraryName");
        String jwtLibraryName = (String) jsonObject.get("jwtLibraryName");
        String jwtAPI = (String) jsonObject.get("jwtApi");
        JSONArray full_path = (JSONArray) jsonObject.get("fullPath");
        List<String> pathList = new ArrayList<>();
        for (int j = 0; j < full_path.size(); j++) {
            String tempPath = (String)full_path.get(j);
            pathList.add(tempPath);
        }
        return new LibrariesProperty(name,jwtLibraryName,jwtAPI,pathList);
    }

    /**
     * 将JWT库调用信息写入库路径中，并保存为JWTResult.json文件
     * @param librariesPropertyList
     * @param librariesPath
     * @throws IOException
     */
    public static void addInformationToLibraryFolder(List<LibrariesProperty> librariesPropertyList, String librariesPath) throws IOException {
        File file = new File(librariesPath);
        String[] directories = file.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return new File(dir,name).isDirectory();
            }
        });
        System.out.println(Arrays.toString(directories));
        for(int i = 0; i < librariesPropertyList.size(); i++) {
            String libraryName = librariesPropertyList.get(i).getLibraryName();
            String[] tempSubFolderName;
            for (int j = 0; j < directories.length; j++) {
                tempSubFolderName = directories[j].split("_");
                System.out.println(Arrays.toString(tempSubFolderName));
                if(libraryName.contains(tempSubFolderName[1])&& libraryName.contains(tempSubFolderName[2])){
                    String[] subFolderList = getSubFolderList(librariesPath + File.separator + directories[j]);
                    if(subFolderList.length == 1) {
//                        将librariesPropertyList写入文件夹
                        writeLibrariesPropertyToFile(
                                librariesPath + File.separator + directories[j] + File.separator + subFolderList[0] + File.separator + "JWTResult.json",
                                librariesPropertyList.get(i));
//                        deleteFile(librariesPath + File.separator + directories[j] + File.separator + subFolderList[0] + File.separator + "JWTResult");
                    }
                }
            }
        }

    }

    public static String[] getSubFolderList(String folderPath) {
        File file = new File(folderPath);
        String[] directories = file.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return new File(dir,name).isDirectory();
            }
        });
        System.out.println(Arrays.toString(directories));
        return directories;
    }

    public static void writeLibrariesPropertyToFile(String libPath, LibrariesProperty librariesProperty) throws IOException {
        String resultJson = JSONObject.toJSONString(librariesProperty);
        System.out.println(resultJson);
        FileWriter fileWriter = new FileWriter(new File(libPath));
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(resultJson);
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public static void deleteFile(String filename) {
        File file = new File(filename);
        if (file.isFile()) {
            file.delete();
        }
    }




    public static void main(String[] args) throws IOException {
/*        String JsonStr = readJsonFile();
        List<LibrariesProperty> librariesPropertyList = processJsonFile(JsonStr);
        addInformationToLibraryFolder(librariesPropertyList, "/home/xu/xu/pythonProject/download_test");*/


        /** start region **/
        //处理jose4j-sym-hmac规则下载的所有github库
        String jsonFileUrl = "/home/xu/xu/pythonProject/JsonProcess/java_jwt_UrlJwkProvider/analysis_result.json";
        String jsonStr = readJsonFile(jsonFileUrl);
        List<LibrariesProperty> librariesPropertyList = processJsonFile(jsonStr);
        addInformationToLibraryFolder(librariesPropertyList, "/home/xu/xu/pythonProject/JsonProcess/java_jwt_UrlJwkProvider/unpressed");
        /** end region **/

    }

}
