package com.xu.analyzer.ruleCheckers.springProject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.xu.analyzer.ruleCheckers.RuleChecker;
import com.xu.analyzer.ruleCheckers.javajwt.JavaJwtRsaApiResultOutput;
import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.output.AnalysisIssue;
import com.xu.output.MessagingSystem.routing.outputStructures.OutputStructure;
import com.xu.utils.Utils;
import com.xu.utils.YmlNode;
import org.apache.logging.log4j.Logger;
import com.xu.utils.yamlUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xu.utils.Utils.getLibraryJwtUseInfo;


public class SpringValueChecker implements RuleChecker {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(SpringValueChecker.class);
    public static final List<String> CRITERIA_CLASSES = new ArrayList<>();
    public static final List<String> httpKeyWord = new ArrayList<>();
    public static final List<String> asymmetricKeyWord = new ArrayList<>();
    public static List<SpringFieldValueInfo> analysis_result;
    public static boolean hasSpringFiled;

    static {
        httpKeyWord.add("issuer");
        httpKeyWord.add("uri");
        httpKeyWord.add("jwk");
        asymmetricKeyWord.add("private");
    }

    @Override
    public void checkRule(
            SourceType type,
            List<String> projectPath,
            List<String> projectDependencyPath,
            List<String> sourcePaths,
            OutputStructure output,
            String mainKlass,
            String androidHome,
            String javaHome,
            String jwtInfoPath) throws ExceptionHandler, IOException {
        List<String> propertiesFiles = new ArrayList<>();
        switch (type) {
            case DIR:
                propertiesFiles = Utils.getPropertiesFile(sourcePaths.get(0));
                break;
            case JAR:
            case WAR:
                propertiesFiles = Utils.getPropertiesFileFromJar(type, projectPath.get(0));
                break;
        }

//        propertiesFiles = Utils.getPropertiesFile(type,sourcePaths.get(0));
        if (propertiesFiles != null) {
            log.info("There are " + propertiesFiles.size() + " property files in this project.");
//            List<String> classNames = Utils.getClassNamesFromSnippet(projectPath);
            List<String> containJwtClassName = new ArrayList<>();

            try {
                if (getLibraryJwtUseInfo(jwtInfoPath) != null) {
//                    containJwtClassName = getJwtContainClassName(Utils.getLibraryJwtUseInfo(jwtInfoPath).getFullPath());
                    containJwtClassName = Utils.getLibraryJwtUseInfo(jwtInfoPath).getFullPath();
                } else {
//                    if there is no info about jwt class, check every property file;
                    containJwtClassName = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<SpringFieldValueInfo> springFieldValueInfos = new ArrayList<>();

            if (containJwtClassName != null) {
                for (String jwtClassPath : containJwtClassName) {
                    springFieldValueInfos.addAll(checkJWTClassValue(sourcePaths.get(0).split(":")[0] + File.separator + jwtClassPath));
                }

                if (springFieldValueInfos != null) {
                    for (SpringFieldValueInfo springField : springFieldValueInfos) {
                        //对称key的存储位置
                        if (springField.getAnnotations().toLowerCase().contains("secret") && springField.getFiledType().toLowerCase().equals("string")) {
                            SpringPropertyFindResult findResult = new SpringPropertyFindResult("constant key");
                            String propertyName = propertyKeyFinder(springField.getAnnotations());
                            findResult.setPropertyName(propertyName);
                            if (springField.getAnnotations().contains(":")) {
                                findResult.setDefaultValue(defaultValueFinder(springField.getAnnotations()));
                            }
                            String propertyValue = propertyValueFinder(propertiesFiles, findResult.propertyName);
                            if (propertyValue != null) {
                                findResult.setPropertyValue(propertyValue);
                                springField.setSpringPropertyFindResult(findResult);
                                springField.setHasFindProblem(true);
                            }
                        }
                        //查找http
                        if (springField.getAnnotations().toLowerCase().contains("issuer") ||
                                springField.getAnnotations().toLowerCase().contains("uri:") ||
                                springField.getAnnotations().toLowerCase().contains("uri}") ||
                                springField.getAnnotations().toLowerCase().contains("jwk")) {
                            SpringPropertyFindResult findResult = new SpringPropertyFindResult("http/https");
                            String propertyName = propertyKeyFinder(springField.getAnnotations());
                            findResult.setPropertyName(propertyName);
                            if (springField.getAnnotations().contains(":")) {
                                findResult.setDefaultValue(defaultValueFinder(springField.getAnnotations()));
                            }
                            String propertyValue = propertyValueFinder(propertiesFiles, findResult.propertyName);
                            if (propertyValue != null) {
                                if (propertyValue.toLowerCase().contains("http:")) {
                                    findResult.setPropertyValue(propertyValue);
                                    springField.setSpringPropertyFindResult(findResult);
                                    springField.setHasFindProblem(true);
                                }
                            }
                        }
                        //查找明文存储的私钥
                        if (springField.getAnnotations().toLowerCase().contains("private") && springField.getAnnotations().toLowerCase().contains("key") && springField.getFiledType().toLowerCase().equals("string")) {
                            SpringPropertyFindResult findResult = new SpringPropertyFindResult("Asymmetric key");
                            String propertyName = propertyKeyFinder(springField.getAnnotations());
                            findResult.setPropertyName(propertyName);
                            if (springField.getAnnotations().contains(":")) {
                                findResult.setDefaultValue(defaultValueFinder(springField.getAnnotations()));
                            }
                            String propertyValue = propertyValueFinder(propertiesFiles, findResult.propertyName);
                            if (propertyValue != null) {
                                findResult.setPropertyValue(propertyValue);
                                springField.setSpringPropertyFindResult(findResult);
                                springField.setHasFindProblem(true);
                            }
                        }

                    }

                }
                //输出结果
                analysis_result = springFieldValueInfos;
                hasSpringFiled = true;
            }
            // 如果没有找到包含jwt的类，就直接遍历所有的属性文件，找其中可能存在的问题
            else {
                List<SpringFieldValueInfo> springPropertyFindResults = propertyValueFinderWithoutKey(propertiesFiles);
                if (springPropertyFindResults != null || springPropertyFindResults.size() != 0) {
                    analysis_result = springPropertyFindResults;
                    hasSpringFiled = false;
                } else {
                    log.info("There is no problem in the property files");
                }
            }
        } else {
            log.info("There is no spring property file in this project.");
        }
        createAnalysisOutput(output);
    }

    /**
     *
     */
    public static List<SpringFieldValueInfo> propertyValueFinderWithoutKey(List<String> propertyFiles) throws IOException, ExceptionHandler {
        if (propertyFiles == null) {
            return null;
        } else {
            List<SpringFieldValueInfo> results = new ArrayList<>();
            for (String propertyFile : propertyFiles) {
                Properties prop = new Properties();
                if(propertyFile.endsWith("yml")){
                    File ymlFile = new File(propertyFile);
                    String yml = yamlUtil.read(ymlFile);
                    List<YmlNode> nodeList = yamlUtil.getNodeList(yml);
                    String str = yamlUtil.printNodeList(nodeList);
                    prop.load(new StringReader(str));
                }
                else {
                    FileInputStream inputStream = new FileInputStream(propertyFile);
//                    Properties prop = new Properties();
                    prop.load(inputStream);
                }

                for (String key : prop.stringPropertyNames()) {
                    //处理对称key
                    if (key.toLowerCase().contains("jwt") && key.toLowerCase().contains("secret")) {
                        results.add(new SpringFieldValueInfo(new SpringPropertyFindResult("constant key", key, prop.getProperty(key))));
                    }
                    //处理明文私钥
                    if (key.toLowerCase().contains("private") && prop.getProperty(key).startsWith("-----BEGIN") || prop.getProperty(key).startsWith("MI")) {
                        results.add(new SpringFieldValueInfo(new SpringPropertyFindResult("plaintext private key", key, prop.getProperty(key))));
                    }
                    //处理http/https
                    if (key.toLowerCase().contains("jwt") || key.toLowerCase().contains("jwk")) {
                        if (prop.getProperty(key).contains("http://")) {
                            results.add(new SpringFieldValueInfo(new SpringPropertyFindResult("unsecure http", key, prop.getProperty(key))));
                        }
                    }
                    if (key.toLowerCase().contains("uri") && (key.toLowerCase().contains("jwk") || key.toLowerCase().contains("jwt"))) {
                        String propValue = prop.getProperty(key);
                        if (propValue.contains("${")) {
                            String subPropKey = propertyKeyFinder(propValue);
                            if (propertyValueFinder(propertyFiles, subPropKey) != null) {
                                String value = replaceEmbedPropKey(propValue, propertyValueFinder(propertyFiles, subPropKey));
                                if (value.contains("http://")) {
                                    results.add(new SpringFieldValueInfo(new SpringPropertyFindResult("unsecure http", key, value)));
                                }
                            }
                        }
                    }
                }
            }
        return results;
    }
}


    /**
     * 根据属性名属性文件列表中查找属性值
     *
     * @param propertyFiles
     * @param propertyKey
     * @return
     * @throws ExceptionHandler
     * @throws IOException
     */
    public static String propertyValueFinder(List<String> propertyFiles, String propertyKey) throws ExceptionHandler, IOException {
        if (propertyFiles == null || propertyKey == null) {
            return null;
        } else {
            for (String propertyFile : propertyFiles) {
                FileInputStream inputStream = new FileInputStream(propertyFile);
                Properties prop = new Properties();
                prop.load(inputStream);
                if (prop.getProperty(propertyKey) != null) {
                    String propValue = prop.getProperty(propertyKey);
                    // 如果获取的属性值中包含其他属性值
                    if (propValue.contains("${")) {
                        String subPropKey = propertyKeyFinder(propValue);
                        if (propertyValueFinder(propertyFiles, subPropKey) != null) {
                            return replaceEmbedPropKey(propValue, propertyValueFinder(propertyFiles, subPropKey));
                        }
                    }
                    return prop.getProperty(propertyKey);
                }

            }
            return null;
        }
    }

    /**
     * 对于嵌套的属性值，替换属性名为属性值
     *
     * @param propValue
     * @param subPropValue
     * @return
     */
    public static String replaceEmbedPropKey(String propValue, String subPropValue) {
        Pattern propKeyPattern = Pattern.compile("[${](.+)[}]");
        Matcher matcher = propKeyPattern.matcher(propValue);
        if (matcher.find()) {
            return propValue.replace(matcher.group(), subPropValue);
        } else {
            return null;
        }

    }

    /**
     * 根据Annotations获取属性值Key
     *
     * @param fieldDefStatm
     * @return
     * @throws ExceptionHandler
     */
    public static String propertyKeyFinder(String fieldDefStatm) throws ExceptionHandler {
        Pattern propertyKeyPattern = Pattern.compile("[{](.+)[:]");
        Pattern propertyKey2Pattern = Pattern.compile("[{](.+)[}]");
        if (fieldDefStatm.contains(":")) {
            Matcher matcher = propertyKeyPattern.matcher(fieldDefStatm);
            if (matcher.find()) {
                return matcher.group().replaceAll("\\{", "").replaceAll(":", "");
            } else {
                throw new ExceptionHandler("cannot analysis the property statement", ExceptionId.UNKWN);
            }
        } else {
            Matcher matcher = propertyKey2Pattern.matcher(fieldDefStatm);
            if (matcher.find()) {
                return matcher.group().replaceAll("\\{", "").replaceAll("}", "");
            } else {
                throw new ExceptionHandler("cannot analysis the property statement", ExceptionId.UNKWN);
            }
        }
    }

    /**
     * 根据Annotation获取属性的默认值
     *
     * @param fieldDefStatm
     * @return
     * @throws ExceptionHandler
     */
    public static String defaultValueFinder(String fieldDefStatm) throws ExceptionHandler {
        Pattern valueDefaultPattern = Pattern.compile("[:](.+)[}]");
        Matcher valueDefaultMatcher = valueDefaultPattern.matcher(fieldDefStatm);
        if (valueDefaultMatcher.find()) {
            return valueDefaultMatcher.group().replaceAll(":", "").replaceAll("}", "");
        } else {
            throw new ExceptionHandler("cannot analysis the property statement", ExceptionId.UNKWN);
        }
    }


    /**
     * 查找调用JWT库的类中存在的使用Spring进行赋值的字段
     *
     * @param jwtClassPath
     * @return
     * @throws FileNotFoundException
     */
    private static List<SpringFieldValueInfo> checkJWTClassValue(String jwtClassPath) throws FileNotFoundException {
        File classSourceFile = new File(jwtClassPath);
        List<SpringFieldValueInfo> fieldValueInfos = new ArrayList<>();
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> parseResult = parser.parse(classSourceFile);
        Optional<CompilationUnit> optional = parseResult.getResult();
        if (optional.isPresent()) {
            CompilationUnit unit = optional.get();
            NodeList<ImportDeclaration> imports = unit.getImports();
            for (int index = 0; index < imports.size(); index++) {
                ImportDeclaration tempImport = imports.get(index);
                if (tempImport.getNameAsString().contains("org.springframework.beans.factory.annotation.Value")) {
//                    log.info("The jwt class contain spring annotation value");
                    List<Node> childNodes = unit.getChildNodes();
                    for (Node node : childNodes) {
                        if (node instanceof ClassOrInterfaceDeclaration) {
                            List<FieldDeclaration> fields = ((ClassOrInterfaceDeclaration) node).asClassOrInterfaceDeclaration().getFields();
                            for (FieldDeclaration field : fields) {
                                if (!field.getAnnotations().isEmpty()) {
                                    if (field.getAnnotations().get(0).getNameAsString().contains("Value")) {
                                        String annotation = field.getAnnotations().get(0).getChildNodes().get(1).toString();
                                        String fieldName = field.getVariable(0).getNameAsString();
                                        String fieldType = field.getVariable(0).getType().toString();
                                        fieldValueInfos.add(new SpringFieldValueInfo(fieldName, fieldType, annotation));
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return fieldValueInfos;
    }

    public void createAnalysisOutput(OutputStructure output) throws ExceptionHandler {
        Map<String, JavaJwtRsaApiResultOutput> outputs = new HashMap<>();
        if (analysis_result.isEmpty() || analysis_result == null) {
            log.info("not find unsecure property in spring project");
        } else {
            if (hasSpringFiled) {
                for (SpringFieldValueInfo result : analysis_result) {
                    log.info("Wrong usage info: " + " problem: " + result.getSpringPropertyFindResult().getProblemType() +
                            " Field name: " + result.getFieldName() + " Value: " + result.getSpringPropertyFindResult().getPropertyValue() + " or " + result.getSpringPropertyFindResult().getDefaultValue());
                    output.addIssue(new AnalysisIssue(result));
                }
            } else {
                log.info("Find suspicions properties");
                for (SpringFieldValueInfo result : analysis_result) {
                    log.info("Wrong usage info: " + "problem: " + result.getSpringPropertyFindResult().getProblemType()
                            + "Property name: " + result.getSpringPropertyFindResult().getPropertyName() + "Property value" + result.getSpringPropertyFindResult().getPropertyValue());
                    output.addIssue(new AnalysisIssue(result));
                }
            }
        }

    }

}
