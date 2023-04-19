package com.xu.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xu.PrepareProcess.criteriagain.preprocess.LibrariesProperty;
import com.xu.analyzer.backward.*;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.environmentInit.enumClass.Version;
import com.xu.output.AnalysisIssue;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.slicer.backward.heuristic.HeuristicBasedAnalysisResult;
import com.xu.slicer.backward.heuristic.HeuristicBasedInstructions;
import com.xu.slicer.backward.orthogonal.OrthogonalInfluenceInstructions;
import com.xu.slicer.backward.orthogonal.OrthogonalSlicingResult;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.ZipDexContainer;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.objectweb.asm.ClassReader;
import soot.*;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.options.Options;
import soot.util.Chain;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static soot.SootClass.BODIES;

/*一些常用的功能性方法*/
public class Utils {
    public static final String projectName = "JWTGuard";
    public static final String fileSep = System.getProperty("file.separator");
    private static final Pattern startComment = Pattern.compile("^\\s?\\/{1}\\*{1}");
    private static final Pattern comment = Pattern.compile("^\\s?\\*{1}");
    private static final Pattern sootClassPatternTwo =
            Pattern.compile("([a-zA-Z0-9]+[.][a-zA-Z0-9]+)\\$[0-9]+");
    private static final Pattern sootClassPattern = Pattern.compile("[<](.+)[:]");
    private static final Pattern sootMthdPattern = Pattern.compile("<((?:[a-zA-Z0-9]+))>");
    private static final Pattern sootMthdPatternTwo = Pattern.compile("((?:[a-zA-Z0-9_]+))\\(");
    private static final Pattern sootLineNumPattern = Pattern.compile("\\(\\)\\>\\[(\\d+)\\]");
    private static final Pattern sootFoundMatchPattern = Pattern.compile("\"{1}(.+)\"{1}");
    private static final Pattern sootFoundPattern = Pattern.compile("\\[(.+)\\]");

    public static final String userPath = System.getProperty("user.home");

    public static int DEPTH = 0;
    public static int[] DEPTH_COUNT;
    public static int NUM_HEURISTIC = 0;
    public static int NUM_ORTHOGONAL = 0;
    public static int NUM_CONSTS_TO_CHECK = 0;

    private static final List<String> ASSIGN_DONT_VISIT = new ArrayList<>();
    private static final List<String> INVOKE_DONT_VISIT = new ArrayList<>();

    public static int NUM_SLICES = 0;
    public static final ArrayList<Integer> SLICE_LENGTH = new ArrayList<>();


    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Utils.class);


    /**
     * 处理字符数组中的空格
     */
    public static ArrayList<String> stripEmpty(String[] args) {
        ArrayList<String> strippedArgs = new ArrayList<>();
        for (String arg : args)
            if (StringUtils.isNotEmpty(arg))
                strippedArgs.add(arg);
        return strippedArgs;

/*        return Arrays.stream(args)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toCollection(ArrayList::new));*/
    }


    /**
     * 获取运行设备相关的信息：获取操作系统名称以及版本信息
     **/
    public static String getPlatform() {

        return System.getProperty("os.name") + "_" + System.getProperty("os.version");
    }


    /**
     * 检索通过XArgs传递的参数
     */
    public static ArrayList<String> retrievingThroughXArgs(SourceType type, Boolean deps, Boolean needsToExist) throws IOException, ExceptionHandler {
        ArrayList<String> out = new ArrayList<>();

        ArrayList<String> types = new ArrayList<>();
        if (deps) {
            types.add(".java");
            types.add(".class");
            types.add(".jar");
            types.add("dir");
        } else {
            types.add(type.getInputExtension());
        }

        //读取系统设备输入
        BufferedReader inputStream =
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String curLine;
        while ((curLine = inputStream.readLine()) != null) {
            out.addAll(retrieveFilePathTypesSingle(curLine, types, true, false, needsToExist));
        }
        return out;
    }


    /**
     * 由retrievingTroughXArgs调用，将输入的String转换成ArrayList
     *
     * @param rawFileString
     * @param types
     * @param expandPath
     * @param overwrite
     * @param needsToExist
     * @return
     */
    public static ArrayList<String> retrieveFilePathTypesSingle(
            String rawFileString,
            ArrayList<String> types,
            Boolean expandPath,
            Boolean overwrite,
            Boolean needsToExist) throws ExceptionHandler {
        ArrayList<String> fileString = new ArrayList<>();
        fileString.add(rawFileString);
        return retrieveFilePaths(fileString, types, expandPath, overwrite, needsToExist);
    }

    /**
     * 检索文件路径
     *
     * @param rawFileStrings
     * @param type
     * @param expandPath
     * @param overwrite
     * @param needsToExist
     * @return
     * @throws ExceptionHandler
     */
    public static ArrayList<String> retrieveFilePaths(
            ArrayList<String> rawFileStrings,
            ArrayList<String> type,
            Boolean expandPath,
            Boolean overwrite,
            Boolean needsToExist) throws ExceptionHandler {
        ArrayList<String> output = new ArrayList<>();

        if (type != null && type.size() == 1
                && type.get(0).equals("dir")
                && rawFileStrings.size() > 1) {
            log.debug("Please enter one source argument for this use case.");
            throw new ExceptionHandler(
                    "Please enter one source argument for this use case.", ExceptionId.GEN_VALID);
        } else if (rawFileStrings.size() == 1 && rawFileStrings.get(0).endsWith(".in")) {
            output = inputFiles(rawFileStrings.get(0), type, expandPath, overwrite, needsToExist);
        } else
            for (String rawString : rawFileStrings)
                for (String fileString : rawString.split(":")) {
                    String filePath = null;

                    if (type.size() == 0)
                        type.add(null);
                    for (String rawType : type) {
                        if (rawString.startsWith(":"))
                            rawString = rawString.replaceFirst(":", "");
                        if (null == rawType || rawType.equals("dir"))
                            filePath = retrieveFilePath(fileString, null, expandPath, false, needsToExist);
                        else if (fileString.endsWith(rawType))
                            filePath = retrieveFilePath(fileString, rawType, expandPath, false, needsToExist);

                        if (StringUtils.isNotEmpty(filePath)) {
                            output.add(filePath);
                            break;
                        }
                    }
                }
        return output;
    }

    /**
     * 解析.in格式文件中的路径内容
     *
     * @param file
     * @param type
     * @param expandPath
     * @param overwrite
     * @param needsToExist
     * @return
     */
    static ArrayList<String> inputFiles(String file,
                                        ArrayList<String> type,
                                        Boolean expandPath,
                                        Boolean overwrite,
                                        Boolean needsToExist) throws ExceptionHandler {
        ArrayList<String> filePaths = new ArrayList<>();
        String curLine = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((curLine = StringUtils.trimToNull((reader.readLine()))) != null) {
                for (String rawType : type)
                    if (curLine.endsWith(rawType))
                        if ((curLine = retrieveFilePath(curLine, rawType, expandPath, overwrite, needsToExist))
                                != null) filePaths.add(curLine);
                if (type == null || type.size() == 0)
                    if ((curLine = retrieveFilePath(curLine, null, expandPath, overwrite, needsToExist)) != null)
                        filePaths.add(curLine);
            }
            return filePaths;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ExceptionHandler("File " + file + " not found", ExceptionId.FILE_I);
        } catch (IOException | ExceptionHandler e) {
            e.printStackTrace();
            throw new ExceptionHandler("Error reading the file  " + file, ExceptionId.FILE_I);
        }
    }

    /**
     * 检索文件路径中的文件
     *
     * @param file
     * @param type
     * @param expandPath
     * @param overwrite
     * @param needsToExist
     * @return
     * @throws ExceptionHandler
     */
    public static String retrieveFilePath(
            String file, String type, Boolean expandPath, Boolean overwrite, Boolean needsToExist
    ) throws ExceptionHandler {
        log.debug("Retrieving and verifying the file: " + file);

        if (StringUtils.isEmpty(file))
            return null;

        if (type != null) {
            if (!type.equals("dir") && !file.toLowerCase().toLowerCase().endsWith(type)) {
                log.debug("File "
                        + file
                        + " doesn't have the right file type for "
                        + type
                        + ", often over-zealous checking.");
                return null;
            }
        }
        File tempFile = new File(file);

        Boolean exists = tempFile.exists() || overwrite;

        if (!exists) {
            if (needsToExist) {
                log.info(tempFile.getName() + "does not exist.");
                throw new ExceptionHandler(tempFile.getName() + "does not exist.", ExceptionId.ARG_VALID);
            } else return null;
        }

        Boolean isDir = tempFile.isDirectory() || overwrite;
        Boolean isFile = tempFile.isFile() || overwrite;

        if (type != null)
            switch (type) {
                case ".class":
                    try {
                        DataInputStream stream = new DataInputStream(new FileInputStream(file));
                        //每一个java字节码文件(.class)都是以相同的4字节内容开始的——十六进制的CAFEBABE
                        if (stream.readInt() != 0xcafebabe) {
                            log.error("The class file " + file + " is not a valid java.class file.");
                            throw new ExceptionHandler("The class file " + file + " is not a valid java.class file.",
                                    ExceptionId.ARG_VALID);
                        } else {
                            //读取.class文件的UnsignedShort,第一个是副版本号，先移出
                            stream.readUnsignedShort();
                            //获取class文件的主版本号
                            Version fileVersion = Version.retrieveByMajor(stream.readUnsignedShort());
                            if (!fileVersion.supportedFile()) {
                                log.error(
                                        "The class file (compiled by a JDK Version "
                                                + fileVersion.getVersionNumber()
                                                + ") is not supported."
                                );
                                throw new ExceptionHandler("The class file (compiled by a JDK Version "
                                        + fileVersion.getVersionNumber()
                                        + ") is not supported.",
                                        ExceptionId.ARG_VALID);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        log.fatal("Error reading the file " + file + ".");
                        throw new ExceptionHandler(
                                "Error reading the file " + file + ".", ExceptionId.FILE_READ);
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.fatal("Error reading the file " + file + ".");
                        throw new ExceptionHandler(
                                "Error reading the file " + file + ".", ExceptionId.FILE_READ);
                    }
                case ".java":
                case ".jar":
                case ".apk":
                    if (!isFile) {
                        log.fatal(tempFile.getName() + " is not a valid file.");
                        throw new ExceptionHandler(
                                tempFile.getName() + " is not a valid file.", ExceptionId.ARG_VALID);
                    }
                    break;
                case "dir":
                    if (!isDir) {
                        log.fatal(tempFile.getName() + " is not a valid directory.");
                        throw new ExceptionHandler(
                                tempFile.getName() + " is not a valid directory.", ExceptionId.ARG_VALID);
                    }
                    break;
                default:
                    if (!isFile && !isDir) {
                        log.fatal(tempFile.getName() + " is not a valid file or directory.");
                        throw new ExceptionHandler(
                                tempFile.getName() + " is not a valid file or directory.", ExceptionId.ARG_VALID);
                    }
                    break;
            }
        else if (!isFile && !isDir) {
            log.fatal(tempFile.getName() + " is not a valid file or directory.");
            throw new ExceptionHandler(
                    tempFile.getName() + " is not a valid file or directory.", ExceptionId.ARG_VALID);
        }

        try {
            if (expandPath) return tempFile.getCanonicalPath();
            else return file;
        } catch (Exception e) {
            log.fatal("Error retrieving the path of the file " + tempFile.getName() + ".");
            throw new ExceptionHandler(
                    "Error retrieving the path of the file " + tempFile.getName() + ".",
                    ExceptionId.FILE_AFK);
        }
    }

    /**
     * 检索文件路径的类型
     *
     * @param rawFileString
     * @param expandPath
     * @param overwrite
     * @param needsToExist
     * @return
     * @throws ExceptionHandler
     */
    public static ArrayList<String> retrieveFilePathTypes(
            ArrayList<String> rawFileString,
            Boolean expandPath,
            Boolean overwrite,
            Boolean needsToExist) throws ExceptionHandler {
        return retrieveFilePaths(
                rawFileString,
                new ArrayList<String>() {
                    {
                        add("java");
                        add(".class");
                        add(".jar");
                        add("dir");

                    }
                },
                expandPath,
                overwrite,
                needsToExist
        );
    }

    /**
     * 检索文件路径的类型
     *
     * @param rawFileString
     * @param type
     * @param expandPath
     * @param overwrite
     * @param needsToExist
     * @return
     * @throws ExceptionHandler
     */
    public static ArrayList<String> retrieveFilePathTypes(
            ArrayList<String> rawFileString,
            SourceType type,
            Boolean expandPath,
            Boolean overwrite,
            Boolean needsToExist
    ) throws ExceptionHandler {
/*        ArrayList<String> types = new ArrayList<>();
        if(type != null){
            types.add(type.getInputExtension());
        }
        return retrieveFilePaths(rawFileString,types,expandPath,overwrite,needsToExist);*/
        return retrieveFilePaths(
                rawFileString,
                type == null
                        ? new ArrayList<>()
                        : new ArrayList<String>() {
                    {
                        add(type.getInputExtension());
                    }
                },
                expandPath,
                overwrite,
                needsToExist);
    }

    /**
     * 获取完整的类名称，包名+类名
     *
     * @param sourceJavaFile
     * @return
     * @throws ExceptionHandler
     */
    public static List<String> retrieveFullyQualifiedName(String... sourceJavaFile) throws ExceptionHandler {
        return retrieveFullyQualifiedName(Arrays.asList(sourceJavaFile));
    }

    /**
     * 获取完整的类名称，包名+类名
     *
     * @param sourceJavaFile
     * @return
     * @throws ExceptionHandler
     */
    public static List<String> retrieveFullyQualifiedName(List<String> sourceJavaFile) throws ExceptionHandler {
        List<String> fullPath = new ArrayList<>();
        for (String in : sourceJavaFile)
            fullPath.add(Utils.retrieveFullyQualifiedName(in));
        return fullPath;
    }

    /**
     * 获取完整的类名称，包名+类名
     *
     * @param in
     * @return
     * @throws ExceptionHandler
     */
    public static String retrieveFullyQualifiedName(String in) throws ExceptionHandler {
        String sourcePackage = trimFilePath(in);
        if (in.toLowerCase().endsWith(".java")) {
            sourcePackage = sourcePackage.replace(".java", "");
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(in));
                String firstLine = bufferedReader.readLine();
                Matcher matcher = null;
                while (StringUtils.isBlank(firstLine)
                        || (matcher = startComment.matcher(firstLine)).find()
                        || (matcher = comment.matcher(firstLine)).find()) {
                    firstLine = bufferedReader.readLine();
                }
                if (firstLine.startsWith("package ") && firstLine.toLowerCase().endsWith(";")) {
                    sourcePackage =
                            firstLine.substring("package ".length(), firstLine.length() - 1)
                                    + "."
                                    + sourcePackage;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new ExceptionHandler("Error parsing file: " + in, ExceptionId.FILE_READ);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ExceptionHandler("Error parsing file: " + in, ExceptionId.FILE_READ);
            }
        } else if (in.toLowerCase().endsWith(".class")) {
            try {
                ClassReader reader = new ClassReader(new FileInputStream(in));
                sourcePackage = reader.getClassName().replace(fileSep, ".");
            } catch (IOException e) {
                e.printStackTrace();
                throw new ExceptionHandler("Error parsing file: " + in, ExceptionId.FILE_READ);
            }

        }
        return sourcePackage;
    }

    /**
     * 获取Jars路径
     *
     * @param path
     * @return
     */
    public static List<String> getJarsInDirectory(String path) {

        if (null == path || path.trim().equals("")) return new ArrayList<>();

        List<String> jarFiles = new ArrayList<>();
        File dir = new File(path);

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();

            if (files == null) {
                return jarFiles;
            }

            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".jar")) {
                    jarFiles.add(file.getAbsolutePath());
                }
            }
        } else if (dir.getName().toLowerCase().endsWith(".jar")) {
            //TODO - Verify and see if this is needed
            //jarFiles.add(dir.getAbsolutePath());
        }

        return jarFiles;
    }

    /**
     * 此方法从绝对路径修剪文件路径和包。
     * 例如：src/main/java/com/test/me/main.java -> main.java
     *
     * @param fullFilePath
     * @return
     */
    public static String trimFilePath(String fullFilePath) {
        String[] folderSplit = fullFilePath.split(Pattern.quote(fileSep));
        return folderSplit[folderSplit.length - 1];
    }

    /**
     * 获取文件的相对路径
     *
     * @param filePath
     * @return
     * @throws ExceptionHandler
     */
    public static String getRelativeFilePath(String filePath) throws ExceptionHandler {
        try {
            //getCanonicalPath：获取标准的绝对路径（会除去路径中的..等符号）
            String fullPath = new File(filePath).getCanonicalPath();
            // “~”，将fullPath中的‘/'转换成“”
            return Utils.osPathJoin("~", fullPath.replaceAll(Utils.userPath + fileSep, ""));
        } catch (IOException e) {
            log.fatal("Error reading file: " + filePath);
            throw new ExceptionHandler("Error reading file: " + filePath, ExceptionId.FILE_I);
        }
    }

    /**
     * 将元素elements使用系统文件路径分隔符相连
     *
     * @param elements
     * @return
     */
    public static String osPathJoin(String... elements) {
        return join(Utils.fileSep, elements);
    }

    /**
     * 在java文件中检索包
     *
     * @param sourceFiles
     * @return
     * @throws ExceptionHandler
     */
    public static String retrievePackageFromJavaFiles(List<String> sourceFiles) throws ExceptionHandler {
        String commonPath = null;

        for (String in : sourceFiles) {
            for (String file : in.split(".")) {
                String tempPath = in.replace(retrievePackageFromJavaFiles(file), "");

                if (commonPath == null)
                    commonPath = tempPath;
                else if (!commonPath.equals(tempPath)) {
                    String removable = commonPath.replace(in, "");
                    commonPath = commonPath.replace(removable, "");
                }
            }
        }
        return commonPath;
    }

    /**
     * 在java文件中检索包
     *
     * @param file
     * @return
     * @throws ExceptionHandler
     */
    public static String retrievePackageFromJavaFiles(String file) throws ExceptionHandler {
        return retrieveFullyQualifiedName(file).replaceAll("\\.", fileSep) + ".java";
    }


    /**
     * 将elements中的元素合并，中间使用delimiter的间隔符
     *
     * @param delimiter
     * @param elements
     * @return
     */
    public static String join(String delimiter, List<String> elements) {
        if (elements == null) return null;

        StringBuilder tempString = new StringBuilder();
        for (String in : elements) {
            if (null != (in = StringUtils.trimToNull(in))) {
                tempString.append(in);
                if (!in.equals(elements.get(elements.size() - 1))) tempString.append(delimiter);
            }
        }

        return tempString.toString();
    }

    /**
     * 将elements中的元素合并，中间使用delimiter的间隔符
     *
     * @param delimiter
     * @param elements
     * @return
     */
    public static String join(String delimiter, String... elements) {
        return join(delimiter, Arrays.asList(elements));
    }

    /**
     * 从sourcePaths中获取类名
     *
     * @param sourcePaths
     * @return
     */
    public static List<String> getClassNamesFromSnippet(List<String> sourcePaths) {

        List<String> classNames = new ArrayList<>();

        for (String sourcePath : sourcePaths) {

            List<File> files = listf(sourcePath);

            if (files == null) {
                return classNames;
            }

            for (File file : files) {
                String name = file.getAbsolutePath();
                if (name.toLowerCase().endsWith(".java")) {
                    String className = name.substring(sourcePath.length() + 1, name.length() - 5);
                    classNames.add(className.replaceAll("/", "."));
                }
            }
        }

        return classNames;
    }

    /**
     * 列举目录中所有的文件
     *
     * @param directoryName
     * @return
     */
    public static List<File> listf(String directoryName) {
        File directory = new File(directoryName);

        File[] fList = directory.listFiles();
        List<File> resultList = new ArrayList<>(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isFile()) {
            } else if (file.isDirectory()) {
                resultList.addAll(listf(file.getAbsolutePath()));
            }
        }

        return resultList;
    }

    public static String getDefaultFileOut(String packageName, String fileExtension) {
        StringBuilder output = new StringBuilder("_" + projectName + "_");

        if (StringUtils.isNotEmpty(packageName))
            output.append(StringUtils.trimToNull(packageName)).append("-");

        output.append(UUID.randomUUID().toString()).append("_").append(fileExtension);

        return Utils.osPathJoin(System.getProperty("user.dir"), output.toString());
    }

    /**
     * 初始化深度
     *
     * @param depth
     */
    public static void initDepth(int depth) {
        DEPTH = depth;
        DEPTH_COUNT = new int[depth];
    }

    /**
     * 获取XML文件
     *
     * @param projectJarPath
     * @param excludes
     * @return
     */
    public static Map<String, String> getXmlFiles(String projectJarPath, List<String> excludes) throws ExceptionHandler {
        Map<String, String> fileStrs = new HashMap<>();

        if (new File(projectJarPath).isDirectory()) {
            return fileStrs;
        }

        List<String> fileNames = getXmlFileNamesFromJarArchive(projectJarPath, excludes);

        for (String fileName : fileNames) {
            InputStream stream = readFileFromZip(projectJarPath, fileName);
            fileStrs.put(fileName, convertStreamToString(stream));
        }

        return fileStrs;
    }

    private static List<String> getXmlFileNamesFromJarArchive(String jarPath, List<String> excludes) throws ExceptionHandler {
        List<String> classNames = new ArrayList<>();
        try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(jarPath));

            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                for (String exclude : excludes) {
                    if (!entry.isDirectory()
                            && entry.getName().endsWith(".xml")
                            && !entry.getName().endsWith(exclude)) {
                        String className = entry.getName();
                        classNames.add(className);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            log.fatal("File " + jarPath + " is not found.");
            throw new ExceptionHandler("File " + jarPath + " is not found.", ExceptionId.FILE_AFK);
        } catch (IOException e) {
            log.fatal("Error Reading " + jarPath + ".");
            throw new ExceptionHandler("Error Reading " + jarPath + ".", ExceptionId.FILE_I);
        }
        return classNames;
    }


    private static InputStream readFileFromZip(String jarPath, String file) throws ExceptionHandler {
        try {
            ZipFile zipFile = new ZipFile(jarPath);
            ZipEntry entry = zipFile.getEntry(file);
            return zipFile.getInputStream(entry);
        } catch (IOException e) {
            log.fatal("Error Reading " + jarPath + ".");
            throw new ExceptionHandler("Error Reading " + jarPath + ".", ExceptionId.FILE_I);
        }
    }

    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * @param javaPath
     * @return
     */
    public static String getBaseSoot(String javaPath) {
        String rt = Utils.osPathJoin(javaPath, "jre", "lib", "rt.jar");
        String jce = Utils.osPathJoin(javaPath, "jre", "lib", "jce.jar");

        setSunBootPath(javaPath, rt);

        return Utils.join(":", javaPath, rt, jce);
    }

    public static String osSurround(String... elements) {
        return surround(System.getProperty("file.separator"), elements);
    }

    public static String surround(String delimiter, String... elements) {
        return surround(delimiter, Arrays.asList(elements));
    }

    public static String surround(String delimiter, List<String> elements) {
        String current = StringUtils.trimToNull(join(delimiter, elements));

        if (!current.startsWith(delimiter)) current = delimiter + current;

        if (!current.endsWith(delimiter)) current += delimiter;

        return current;
    }

    /**
     * setSunBootPath.
     *
     * @param basePath a {@link String} object.
     * @param rt       a {@link String} object.
     */
    public static void setSunBootPath(String basePath, String rt) {
        System.setProperty("sun.boot.class.path", rt);
        System.setProperty("java.ext.dirs", osSurround(basePath, "lib"));
    }

    /**
     * buildSootClassPath.
     *
     * @param paths a {@link String} object.
     * @return a {@link String} object.
     */
    public static String buildSootClassPath(String... paths) {
        return buildSootClassPath(Arrays.asList(paths));
    }

    /**
     * buildSootClassPath.
     *
     * @param paths a {@link List} object.
     * @return a {@link String} object.
     */
    public static String buildSootClassPath(List<String> paths) {

        StringBuilder classPath = new StringBuilder();

        for (String path : paths) {

            if (path.toLowerCase().endsWith(".jar")) {
                classPath.append(path);
                classPath.append(":");
            } else {
                File dir = new File(path);

                if (dir.isDirectory()) {
                    File[] files = dir.listFiles();

                    if (files == null) {
                        continue;
                    }

                    for (File file : files) {
                        if (file.getName().toLowerCase().endsWith(".jar")) {
                            classPath.append(file.getAbsolutePath());
                            classPath.append(":");
                        }
                    }
                }
            }
        }

        return classPath.toString();
    }


    /**
     * getClassHierarchyAnalysis. 获取类调用关系分析
     *
     * @param classNames a {@link List} object.
     * @return a {@link Map} object.
     */
    public static Map<String, List<SootClass>> getClassHierarchyAnalysis(List<String> classNames) {

        Map<String, List<SootClass>> classHierarchyMap = new HashMap<>();

        for (String className : classNames) {

            SootClass sClass = Scene.v().getSootClass(className);
            Chain<SootClass> parents = sClass.getInterfaces();

            if (sClass.hasSuperclass()) {
                SootClass superClass = sClass.getSuperclass();

                List<SootClass> childList =
                        classHierarchyMap.computeIfAbsent(superClass.getName(), k -> new ArrayList<>());

                if (childList.isEmpty()) {
                    childList.add(superClass);
                }
                childList.add(sClass);
            }

            for (SootClass parent : parents) {
                List<SootClass> childList =
                        classHierarchyMap.computeIfAbsent(parent.getName(), k -> new ArrayList<>());

                if (childList.isEmpty()) {
                    childList.add(parent);
                }
                childList.add(sClass);
            }
        }

        return classHierarchyMap;
    }

    /**
     * 在Soot分析结果中寻找受影响的参数
     *
     * @param analysisResult
     * @return
     */
    public static List<Integer> findInfluencingParamters(List<UnitContainer> analysisResult) {
        List<Integer> influencingParam = new ArrayList<>();

        for (int index = analysisResult.size() - 1; index >= 0; index--) {
            UnitContainer unit = analysisResult.get(index);

            for (ValueBox useBox : unit.getUnit().getUseBoxes()) {
                String useBoxStr = useBox.getValue().toString();
                if (useBoxStr.contains("@parameter")) {
                    Integer param =
                            Integer.valueOf(useBoxStr.substring("@parameter".length(), useBoxStr.indexOf(':')));
                    influencingParam.add(param);

                }
            }
        }
        return influencingParam;
    }

    /**
     * 确定赋值调用语句的参数，是否包含当前UseBox中的值
     *
     * @param useBox
     * @param unit
     * @return
     */
    public static int isArgOfAssignInvoke(ValueBox useBox, Unit unit) {

        //如果当前unit中的语句是Assign赋值语句，并且包含invoke，即使赋值调用语句
        if (unit instanceof JAssignStmt && unit.toString().contains("invoke ")) {

            //获取调用的表达式
            InvokeExpr invokeExpr = ((JAssignStmt) unit).getInvokeExpr();
            //获取调用的表达式需要的参数
            List<Value> args = invokeExpr.getArgs();
            for (int index = 0; index < args.size(); index++) {
                if (args.get(index).equivTo(useBox.getValue())) {
                    return index;
                }
            }
        }
        return -1;
    }


    /**
     * 判断当前UseBox中的参数，是否为调用语句中的参数
     *
     * @param useBox
     * @param unit
     * @return
     */
    public static int isArgOfInvoke(ValueBox useBox, Unit unit) {
        if (unit instanceof JInvokeStmt) {

            InvokeExpr invokeExpr = ((JInvokeStmt) unit).getInvokeExpr();
            List<Value> args = invokeExpr.getArgs();
            for (int index = 0; index < args.size(); index++) {
                if (args.get(index).equivTo(useBox.getValue())) {
                    return index;
                }
            }
        }

        return -1;
    }

    /**
     * 判断当前Unit语句是否为字节数组语句，其中的参数是否为当前UseBox中的参数
     *
     * @param useBox
     * @param unit
     * @return
     */
    public static boolean isArgOfByteArrayCreation(ValueBox useBox, Unit unit) {
        if (unit.toString().contains(" newarray ")) {
            for (ValueBox valueBox : unit.getUseBoxes()) {
                if (valueBox.getValue().equivTo(useBox.getValue())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查常量参数 是否为调用语句的参数
     *
     * @param analysis Analysis analysis 所有的Analysis结果
     * @param index    当前的AnalysisResult结果
     * @param outSet
     * @return
     */
    public static UnitContainer isArgumentOfInvoke(
            Analysis analysis, int index, List<UnitContainer> outSet) {
        NUM_CONSTS_TO_CHECK++;

        UnitContainer baseUnit = analysis.getAnalysisResult().get(index);

        if (baseUnit.getUnit() instanceof JInvokeStmt) {

            InvokeExpr invokeExpr = ((JInvokeStmt) baseUnit.getUnit()).getInvokeExpr();

            List<Value> args = invokeExpr.getArgs();

            for (Value arg : args) {
                if (arg instanceof Constant) {
                    return baseUnit;
                }
            }
        }

        outSet.add(analysis.getAnalysisResult().get(index));

        for (int i = index; i >= 0; i--) {

            UnitContainer curUnit = analysis.getAnalysisResult().get(i);

            List<UnitContainer> inset = new ArrayList<>(outSet);

            for (UnitContainer insetIns : inset) {
                boolean outSetContainsCurUnit = !outSet.toString().contains(curUnit.toString());
                if (insetIns instanceof PropertyFakeUnitContainer) {
                    String property = ((PropertyFakeUnitContainer) insetIns).getOriginalProperty();

                    if (curUnit.getUnit() instanceof JInvokeStmt) {
                        if (curUnit.getUnit().toString().contains(property + ".<")) {
                            if (outSetContainsCurUnit) {
                                outSet.add(curUnit);
                            }
                        } else {

                            InvokeExpr invokeExpr = ((JInvokeStmt) curUnit.getUnit()).getInvokeExpr();

                            List<Value> args = invokeExpr.getArgs();

                            for (Value arg : args) {
                                if (arg.toString().contains(property)) {
                                    return curUnit;
                                }
                            }
                        }

                    } else {
                        for (ValueBox useBox : curUnit.getUnit().getUseBoxes()) {
                            if (useBox.getValue().toString().contains(property)) {
                                if (outSetContainsCurUnit) {
                                    outSet.add(curUnit);
                                }
                            }
                        }
                    }
                } else if (insetIns instanceof ParamFakeUnitContainer) {

                    int param = ((ParamFakeUnitContainer) insetIns).getParam();
                    String method = ((ParamFakeUnitContainer) insetIns).getCallee();

                    for (ValueBox useBox : curUnit.getUnit().getUseBoxes()) {
                        String useboxStr = useBox.getValue().toString();
                        if (useboxStr.contains("@parameter")) {
                            Integer parameter =
                                    Integer.valueOf(
                                            useboxStr.substring("@parameter".length(), useboxStr.indexOf(':')));
                            if (parameter.equals(param) && curUnit.getMethod().equals(method)) {
                                if (outSetContainsCurUnit) {
                                    outSet.add(curUnit);
                                }
                            }
                        }
                    }
                } else if (insetIns.getUnit() instanceof JAssignStmt) {
                    if (curUnit.getUnit() instanceof JInvokeStmt) {

                        for (ValueBox defBox : insetIns.getUnit().getDefBoxes()) {

                            if (((JInvokeStmt) curUnit.getUnit()).containsInvokeExpr()) {

                                InvokeExpr invokeExpr = ((JInvokeStmt) curUnit.getUnit()).getInvokeExpr();
                                List<Value> args = invokeExpr.getArgs();

                                for (Value arg : args) {
                                    if (arg.equivTo(defBox.getValue())
                                            || isArrayUseBox(curUnit, insetIns, defBox, arg)) {
                                        return curUnit;
                                    }
                                }
                            } else if (curUnit.getUnit().toString().contains(defBox + ".<")) {
                                if (outSetContainsCurUnit) {
                                    outSet.add(curUnit);
                                }
                            }
                        }

                    } else {
                        for (ValueBox defBox : insetIns.getUnit().getDefBoxes()) {

                            if ((defBox.getValue().toString().equals("r0")
                                    && insetIns.getUnit().toString().startsWith("r0."))
                                    || (defBox.getValue().toString().equals("this")
                                    && insetIns.getUnit().toString().startsWith("this."))) {
                                continue;
                            }

                            for (ValueBox useBox : curUnit.getUnit().getUseBoxes()) {

                                if (defBox.getValue().equivTo(useBox.getValue())
                                        || isArrayUseBox(curUnit, insetIns, defBox, useBox.getValue())) {
                                    if (outSetContainsCurUnit) {
                                        outSet.add(curUnit);
                                    }
                                }
                            }
                        }
                    }

                } else {

                    if (curUnit.getUnit() instanceof JInvokeStmt) {
                        for (ValueBox defBox : insetIns.getUnit().getDefBoxes()) {
                            if (curUnit.getUnit().toString().contains(defBox + ".<")) {
                                if (outSetContainsCurUnit) {
                                    outSet.add(curUnit);
                                }
                            } else {

                                InvokeExpr invokeExpr = ((JInvokeStmt) curUnit.getUnit()).getInvokeExpr();

                                List<Value> args = invokeExpr.getArgs();

                                for (Value arg : args) {
                                    if (arg.equivTo(defBox.getValue())
                                            || isArrayUseBox(curUnit, insetIns, defBox, arg)) {
                                        return curUnit;
                                    }
                                }
                            }
                        }

                    } else {
                        for (ValueBox defBox : insetIns.getUnit().getDefBoxes()) {

                            if ((defBox.getValue().toString().equals("r0")
                                    && insetIns.getUnit().toString().startsWith("r0."))
                                    || (defBox.getValue().toString().equals("this")
                                    && insetIns.getUnit().toString().startsWith("this."))) {
                                continue;
                            }

                            for (ValueBox useBox : curUnit.getUnit().getUseBoxes()) {

                                if (defBox.getValue().equivTo(useBox.getValue())
                                        || isArrayUseBox(curUnit, insetIns, defBox, useBox.getValue())) {
                                    if (outSetContainsCurUnit) {
                                        outSet.add(curUnit);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 检查是否为数组调用Box
     *
     * @param curUnit
     * @param insetIns
     * @param defBox
     * @param useBox
     * @return
     */
    private static boolean isArrayUseBox(
            UnitContainer curUnit, UnitContainer insetIns, ValueBox defBox, Value useBox) {
        return (defBox.getValue().toString().contains(useBox.toString())
                && curUnit.getMethod().equals(insetIns.getMethod())
                && useBox.getType() instanceof ArrayType);
    }

    /**
     * 创建赋值调用语句的UnitContainer
     * * 例如：byte[] keyBytes = DatatypeConverter.parseBase64Binary(key);
     * * key 为 r7
     * * UnitContainer
     * * {unit=
     * * r0 = staticinvoke <javax.xml.bind.DatatypeConverter:
     * * byte[] parseBase64Binary(java.lang.String)>(r7),
     * * method='<tester.SymCrypto: byte[] encrypt(java.lang.String,java.lang.String)>'}
     *
     * @param currInstruction
     * @param caller
     * @param depth
     * @return
     */
    public static UnitContainer createAssignInvokeUnitContainer(
            Unit currInstruction, String caller, int depth) {

        //如果是提前定义好的不需要处理的赋值语句
        for (String dontVisit : ASSIGN_DONT_VISIT) {
            if (currInstruction.toString().contains(dontVisit)) {
                UnitContainer unitContainer = new UnitContainer();
                unitContainer.setUnit(currInstruction);
                unitContainer.setMethod(caller);
                return unitContainer;
            }
        }

        AssignInvokeUnitContainer unitContainer = new AssignInvokeUnitContainer();
        SootMethod method = ((JAssignStmt) currInstruction).getInvokeExpr().getMethod();
        if (method != null && method.isConcrete()) {
            // 强制将给定的类解析到指定的级别，即使已经解析完
            Scene.v().forceResolve(method.getDeclaringClass().getName(), BODIES);

            List<UnitContainer> intraAnalysis = null;

            DEPTH_COUNT[depth - 1]++;

            if (depth == 1) {

                //启发式算法
                NUM_HEURISTIC++;

                HeuristicBasedInstructions returnInfluencingInstructions =
                        new HeuristicBasedInstructions(method, "return");

                intraAnalysis = returnInfluencingInstructions.getAnalysisResult().getAnalysis();

            } else {
                NUM_ORTHOGONAL++;

                OrthogonalInfluenceInstructions other =
                        new OrthogonalInfluenceInstructions(method, "return", depth - 1);
                intraAnalysis = other.getOrthogonalSlicingResult().getAnalysisResult();

            }

            //获取参数 Get Args
            List<Integer> args = Utils.findInfluencingParamters(intraAnalysis);

            //获取字段 Get fields
            Set<String> usedFields = new HashSet<>();
            for (UnitContainer iUnit : intraAnalysis) {
                for (ValueBox usebox : iUnit.getUnit().getUseBoxes()) {
                    if (usebox.getValue().toString().startsWith("r0.")
                            || usebox.getValue().toString().startsWith("this.")) {
                        usedFields.add(usebox.getValue().toString());
                    }
                }
            }
            unitContainer.setArgs(args);
            unitContainer.setAnalysisResult(intraAnalysis);
            unitContainer.setMethod(caller);
            unitContainer.setProperties(usedFields);
        }

        return unitContainer;
    }

    public static UnitContainer createInvokeUnitContainer(
            Unit currInstruction, String caller, List<String> usedFields, int depth) {

        for (String dontVisit : INVOKE_DONT_VISIT) {
            if (currInstruction.toString().contains(dontVisit)) {
                UnitContainer unitContainer = new UnitContainer();
                unitContainer.setUnit(currInstruction);
                unitContainer.setMethod(caller);
                return unitContainer;
            }
        }
        InvokeUnitContainer unitContainer = new InvokeUnitContainer();
        SootMethod method = ((JInvokeStmt) currInstruction).getInvokeExpr().getMethod();

        if (method.isConcrete()) {
            Scene.v().forceResolve(method.getDeclaringClass().getName(), BODIES);
            if (depth == 1) {

                for (String field : usedFields) {

                    HeuristicBasedInstructions influencingInstructions =
                            new HeuristicBasedInstructions(method, field);
                    HeuristicBasedAnalysisResult propAnalysis = influencingInstructions.getAnalysisResult();

                    if (propAnalysis.getAnalysis() != null) {
                        // Get args
                        List<Integer> args = Utils.findInfluencingParamters(propAnalysis.getAnalysis());
                        unitContainer.setArgs(args);
                        unitContainer.setMethod(caller);
                        unitContainer.getDefinedFields().add(field);
                        unitContainer.setAnalysisResult(propAnalysis.getAnalysis());
                    }
                }
            } else {

                for (String field : usedFields) {

                    OrthogonalInfluenceInstructions other =
                            new OrthogonalInfluenceInstructions(method, field, depth - 1);
                    OrthogonalSlicingResult orthoAnalysis = other.getOrthogonalSlicingResult();

                    if (orthoAnalysis.getAnalysisResult() != null) {
                        // Get args
                        List<Integer> args = Utils.findInfluencingParamters(orthoAnalysis.getAnalysisResult());
                        unitContainer.setArgs(args);
                        unitContainer.setMethod(caller);
                        unitContainer.getDefinedFields().add(field);
                        unitContainer.setAnalysisResult(orthoAnalysis.getAnalysisResult());
                    }
                }
            }
        }
        return unitContainer;
    }

    public static void createAnalysisOutput(
            Map<String, String> xmlFileStr,
            List<String> sourcePaths,
            Map<UnitContainer, List<String>> predictableSourcMap,
            String rule,
            OutputStructure output,
            String jwtLibName)
            throws ExceptionHandler {
        Integer ruleNumber = Integer.parseInt(rule);

        for (UnitContainer unit : predictableSourcMap.keySet())
            if (predictableSourcMap.get(unit).size() <= 0)
                output.addIssue(new AnalysisIssue(unit, ruleNumber, "", sourcePaths, jwtLibName));
            else
                for (String sootString : predictableSourcMap.get(unit))
                    output.addIssue(
                            new AnalysisIssue(
                                    unit,
                                    ruleNumber,
                                    "Found: \"" + sootString.replaceAll("\"", "") + "\"",
                                    sourcePaths, jwtLibName));
    }

    /**
     * 在日志简单打印分析结果
     *
     * @param xmlFileStr
     * @param sourcePaths
     * @param predictableSourceMap
     * @param rule
     * @param output
     * @throws ExceptionHandler
     */
    public static void createAnalysisOutput_log(
            Map<String, String> xmlFileStr,
            List<String> sourcePaths,
            Map<UnitContainer, List<String>> predictableSourceMap,
            String rule,
            OutputStructure output
    ) throws ExceptionHandler {
        /*Integer ruleNumber = Integer.parseInt(rule);*/

        for (UnitContainer unit : predictableSourceMap.keySet()) {
            log.info("Analysis result");
            if (predictableSourceMap.get(unit).size() <= 0)
                log.info("no result");
            else
                for (String sootString : predictableSourceMap.get(unit))
                    log.info(" Found : " + sootString.replaceAll("\"", "") + sourcePaths);

        }
/*            if (predictableSourceMap.get(unit).size() <= 0)
                output.addIssue(new AnalysisIssue(unit, ruleNumber, "", sourcePaths));
            else
                for (String sootString : predictableSourceMap.get(unit))
                    output.addIssue(
                            new AnalysisIssue(
                                    unit,
                                    ruleNumber,
                                    "Found: \"" + sootString.replaceAll("\"", "") + "\"",
                                    sourcePaths));*/
    }

    /**
     * 获取包含jwt库的类的名称
     *
     * @param fullPath
     * @return
     */
    public static List<String> getJwtContainClassName(List<String> fullPath) {
        List<String> jwtContainClassName = new ArrayList<>();
        for (String tempFullPath : fullPath) {
            String[] splitResult = tempFullPath.split("/");
            String className = splitResult[splitResult.length - 1];
            jwtContainClassName.add(className.substring(0, className.length() - 5));
        }
        return jwtContainClassName;
    }

    public static List<String> getFullJwtContainClassName(List<String> fullPath) {
        List<String> jwtContainClassName = new ArrayList<>();
        for (String tempFullPath : fullPath) {
            String[] splitResult = tempFullPath.split("/");
            String className = splitResult[splitResult.length - 2] + "." + splitResult[splitResult.length - 1];
            jwtContainClassName.add(className.substring(0, className.length() - 5));
        }
        return jwtContainClassName;
    }

    /***
     * 获取被分析的库中有关JWT库调用的信息
     * @param path
     * @return
     */
    public static LibrariesProperty getLibraryJwtUseInfo(String path) throws IOException {
        String jsonStr = readJsonFile(path);
        if (jsonStr != null && jsonStr != "") {
            return processLibJsonFile(jsonStr);
        } else {
            return null;
        }
    }

    /**
     * 读Json文件
     *
     * @param jsonFilePath
     * @return
     * @throws IOException
     */
    private static String readJsonFile(String jsonFilePath) {
        String jsonStr = "";
        File jsonFile = new File(jsonFilePath);
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(jsonFile);
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
        } catch (FileNotFoundException e) {
            log.error("LibraryJwtUseInfo File not found");
            return null;
        } catch (UnsupportedEncodingException e) {
            return jsonStr;
        } catch (IOException e) {
            log.error("IOException");
            return null;
        }
        return jsonStr;
    }

    /**
     * 读取每个库中保存的JWT-Result文件中的Json对象
     *
     * @param str JWT-Result文件中读取的Json String
     * @return 返回这个库对应的Libraries Property信息
     */
    private static LibrariesProperty processLibJsonFile(String str) {
        JSONObject jsonObject = JSON.parseObject(str);
        String name = (String) jsonObject.get("libraryName");
        String jwtLibraryName = (String) jsonObject.get("jwtLibraryName");
        String jwtAPI = (String) jsonObject.get("jwtApi");
        JSONArray full_path = (JSONArray) jsonObject.get("fullPath");
        List<String> pathList = new ArrayList<>();
        for (int j = 0; j < full_path.size(); j++) {
            String tempPath = (String) full_path.get(j);
            pathList.add(tempPath);
        }
        return new LibrariesProperty(name, jwtLibraryName, jwtAPI, pathList);
    }

    /**
     * getClassNamesFromJarArchive. 从JAR包中获取类名
     *
     * @param jarPath a {@link String} object.
     * @return a {@link List} object.
     * @throws ExceptionHandler if any.
     */
    public static List<String> getClassNamesFromJarArchive(String jarPath) throws ExceptionHandler {
        List<String> classNames = new ArrayList<>();
        try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(jarPath));
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.');
                    classNames.add(className.substring(0, className.length() - ".class".length()));
                }
            }
            return classNames;
        } catch (IOException e) {
            log.fatal("Error with file " + jarPath);
            throw new ExceptionHandler("Error with file " + jarPath, ExceptionId.FILE_I);
        }
    }

    /**
     * 从war包中获取类名
     *
     * @param warPath
     * @return
     * @throws ExceptionHandler
     */
    public static List<String> getClassNamesFromWarArchive(String warPath) throws ExceptionHandler {
        List<String> classNames = new ArrayList<>();

        try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(warPath));
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("WEB-INF/classes/", "").replace("/", ".");
                    classNames.add(className.substring(0, className.length() - ".class".length()));
                }
            }

            return classNames;
        } catch (FileNotFoundException e) {
            log.fatal("Error with file " + warPath);
            throw new ExceptionHandler("Error with file " + warPath, ExceptionId.FILE_I);
        } catch (IOException e) {
            log.fatal("Error with file " + warPath);
            throw new ExceptionHandler("Error with file " + warPath, ExceptionId.FILE_I);
        }
    }

    /**
     * 解析War包转换成Jar包
     *
     * @param warPath
     * @return
     */
    public static String parseWarToJar(String warPath) {
        File warFile = new File(warPath);
        String warName = warFile.getName();
        String unzipPath = warFile.getParent() + "/temp/" + warFile.getName().substring(0, warName.length() - ".war".length());
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(warFile));
            ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.JAR,
                    bufferedInputStream);

            JarArchiveEntry entry = null;
            while ((entry = (JarArchiveEntry) in.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(unzipPath, entry.getName()).mkdir();
                } else {
                    OutputStream out = FileUtils.openOutputStream(new File(unzipPath, entry.getName()));
                    IOUtils.copy(in, out);
                    out.close();
                }
            }
            in.close();
            packageToJar(unzipPath + "/WEB-INF/classes", warFile.getParent() + "/temp/temp.jar");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArchiveException e) {
            e.printStackTrace();
        }
        return warFile.getParent() + "/temp/temp.jar";
    }

    public static void packageToJar(String classesPath, String destJarFile) {
        File outFile = new File(destJarFile);
        try {
            outFile.createNewFile();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outFile));
            ArchiveOutputStream out = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.JAR,
                    bufferedOutputStream);

            if (classesPath.charAt(classesPath.length() - 1) != '/') {
                classesPath += '/';
            }

            Iterator<File> files = FileUtils.iterateFiles(new File(classesPath), null, true);
            while (files.hasNext()) {
                File file = files.next();
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, file.getPath().replace(
                        classesPath, ""));
                out.putArchiveEntry(zipArchiveEntry);
                IOUtils.copy(new FileInputStream(file), out);
                out.closeArchiveEntry();
            }
            out.finish();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArchiveException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据SootString检索类名
     */
    public static String retrieveClassNameFromSootString(String sootString) {
        Matcher secondMatches = sootClassPatternTwo.matcher(sootString);
        if (secondMatches.find()) return secondMatches.group(1);

        Matcher matches = sootClassPattern.matcher(sootString);
        if (matches.find()) return matches.group(1);

        return "UNKNOWN";
    }

    /**
     * 从SootString中检索方法名
     *
     * @param sootString
     * @return
     */
    public static String retrieveMethodFromSootString(String sootString) {
        Matcher matches = sootMthdPattern.matcher(sootString);

        if (matches.find()) return matches.group(1);

        Matcher secondMatches = sootMthdPatternTwo.matcher(sootString);
        if (secondMatches.find()) return secondMatches.group(1);

        return "UNKNOWN";
    }

    /**
     * @param sootString
     * @return
     */
    public static Integer retrieveLineNumFromSootString(String sootString) {
        Matcher matches = sootLineNumPattern.matcher(sootString);

        if (matches.find()) return Integer.parseInt(matches.group(1));
        return -1;
    }

    public static String retrieveFoundMatchFromSootString(String sootString) {
        Matcher matches = sootFoundMatchPattern.matcher(sootString);

        if (matches.find()) return StringUtils.trimToNull(matches.group(1));

        return "UNKNOWN";
    }

    public static String retrieveFoundPatternFromSootString(String sootString) {
        Matcher matches = sootFoundPattern.matcher(sootString);

        if (matches.find()) return matches.group(1).replaceAll("\"", "");
        return "UNKNOWN";
    }

    /**
     * start spring boot project process methods
     */
    public static List<String> getPropertiesFile(String sourcePath) {
        List<String> propertiesFiles = new ArrayList<>();

        String rootDir = sourcePath.split(":")[0];
//        find the property file under root dir
        String rootConfig = rootDir + File.separator + "config";
        if (checkDirExist(rootConfig) != null) {
            propertiesFiles.addAll(findApplicationPropFiles(rootConfig));
        }
        propertiesFiles.addAll(findApplicationPropFiles(rootDir));
        String resourcePath = getResourcePath(rootDir);
        if (resourcePath != null) {
            propertiesFiles.addAll(findApplicationPropFiles(resourcePath));
            String resourceConfigPath = resourcePath + File.separator + "config";
            if (checkDirExist(resourceConfigPath) != null) {
                propertiesFiles.addAll(findApplicationPropFiles(resourceConfigPath));
            }
        }
        return propertiesFiles;
    }

    public static String checkDirExist(String dir) {
        File file = new File(dir);
        if (file.exists())
            return dir;
        else return null;
    }

    /**
     * 给定根目录，查找根目录下的src/main/resources
     *
     * @param rootDir
     * @return
     */
    public static String getResourcePath(String rootDir) {
        String resourcePath = rootDir + File.separator + "src/main/resources";
        return checkDirExist(resourcePath);
    }

    /**
     * @param configDir
     * @return
     */
    public static List<String> findApplicationPropFiles(String configDir) {
        List<String> applicationFiles = new ArrayList<>();
        File file = new File(configDir);
        FilenameFilter filenameFilterAppProp = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.contains("application.") || name.contains("application-"))
                    return true;
                return false;
            }
        };
        File[] files = file.listFiles(filenameFilterAppProp);
        if (files.length > 0) {
            for (File propFile : files) {
                String fileExt = FilenameUtils.getExtension(propFile.toString());
                if (fileExt.equals("properties") || fileExt.equals("yml") || fileExt.equals("yaml"))
                    applicationFiles.add(propFile.toString());

            }
        }
        return applicationFiles;
    }

    /**
     * 从Jar包中获取Properties Files
     */
    public static List<String> getPropertiesFileFromJar(SourceType type, String jarPath) {
        List<String> propertiesFiles = new ArrayList<>();
        File jarFile = new File(jarPath);
        String jarName = jarFile.getName();
        String unzipPath;
        switch (type) {
            case JAR:
                unzipPath = jarFile.getParent() + "/temp/" + jarFile.getName().substring(0, jarName.length() - ".jar".length());
                break;
            case WAR:
                unzipPath = jarFile.getParent() + "/temp/" + jarFile.getName().substring(0, jarName.length() - ".war".length());
                break;
            default:
                unzipPath = jarFile.getParent() + "/temp/" + jarFile.getName().substring(0, jarName.length() - ".jar".length());
                break;
        }

        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(jarFile));
            ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.JAR,
                    bufferedInputStream);

            JarArchiveEntry entry = null;
            while ((entry = (JarArchiveEntry) in.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(unzipPath, entry.getName()).mkdir();
                } else if (entry.getName().endsWith("properties") || entry.getName().endsWith("ymal") || entry.getName().endsWith("yml")) {
                    File tempFile = new File(unzipPath, entry.getName());
                    OutputStream out = FileUtils.openOutputStream(tempFile);
                    propertiesFiles.add(tempFile.getAbsolutePath());
                    IOUtils.copy(in, out);
                    out.close();
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArchiveException e) {
            e.printStackTrace();
        }
        return propertiesFiles;
    }

    /**
     * Unique Analysis
     */
    public static void loadSootClasses(List<String> classes) {
        Options.v().set_keep_line_number(true);
        Options.v().set_allow_phantom_refs(true);

        if (classes != null) for (String clazz : classes) Options.v().classes().add(clazz);

        Scene.v().loadBasicClasses();
    }

    public static List<String> retrieveTrimmedSourcePaths(List<String> files)
            throws ExceptionHandler {
        List<String> filePaths = new ArrayList<>();
        for (String relativeFile : files) {
            String relativeFilePath = "";

            File file = new File(relativeFile);

            try {
                relativeFilePath = file.getCanonicalPath().replace(file.getName(), "");
            } catch (IOException e) {
                log.fatal("Issue retrieving the file path from file: " + file);
                throw new ExceptionHandler(
                        ExceptionId.FILE_READ, "Issue retrieving the file path from file: " + file);
            }

            if (!filePaths.contains(relativeFilePath)) filePaths.add(relativeFilePath);
        }
        return filePaths;
    }

    public static String retrieveBaseSourcePath(List<String> sourcePaths, String dependencyPath) {
        String tempDependencyPath = sourcePaths.get(0);
        for (String in : sourcePaths)
            if (!in.equals(tempDependencyPath)) {
                tempDependencyPath = System.getProperty("user.dir");
                break;
            }
        return Utils.osPathJoin(tempDependencyPath, dependencyPath);
    }

    public static List<String> getClassNamesFromApkArchive(String apkfile) throws ExceptionHandler {
        List<String> classNames = new ArrayList<>();

        File zipFile = new File(apkfile);

        try {
            ZipDexContainer zipContainer =
                    (ZipDexContainer) DexFileFactory.loadDexContainer(zipFile, Opcodes.forApi(23));

            for (String dexEntryName : zipContainer.getDexEntryNames()) {
                DexFile dexFile =
                        DexFileFactory.loadDexEntry(zipFile, dexEntryName, true, Opcodes.forApi(23))
                                .getDexFile();

                for (ClassDef classDef : dexFile.getClasses()) {
                    String className = classDef.getType().replace('/', '.');
                    if (!className.toLowerCase().startsWith("landroid.")) {
                        classNames.add(className.substring(1, className.length() - 1));
                    }
                }
            }
            return classNames;
        } catch (IOException e) {
            log.fatal("Error with dex file classes.dex");
            throw new ExceptionHandler("Error with dex file classes.dex", ExceptionId.FILE_I);
        }
    }
}
