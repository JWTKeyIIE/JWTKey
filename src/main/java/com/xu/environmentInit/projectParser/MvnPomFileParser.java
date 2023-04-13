package com.xu.environmentInit.projectParser;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maven项目的POM.XML文件解析器
 */
public class MvnPomFileParser implements BuildFileParser{
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(MvnPomFileParser.class);
    Map<String, String> moduleVsPath = new HashMap<>();
    String projectName;
    String projectVersion;

    public MvnPomFileParser(String fileName) throws ExceptionHandler {
        File xmlFile = new File(fileName);
        //用于解析XML文件
        DocumentBuilderFactory docbuildFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docbuildFactory.newDocumentBuilder();
            Document document = docBuilder.parse(xmlFile);

            //寻找项目中的子项目
            NodeList nodeList = document.getElementsByTagName("module");

            String[] splits = fileName.split("/");
            String projectName = splits[splits.length-2];
            String projectRoot = fileName.substring(0,fileName.lastIndexOf('/'));

            if(nodeList.getLength() == 0){
                moduleVsPath.put(projectName,projectRoot);
            }else {
                for(int i = 0; i < nodeList.getLength(); i++){
                    String moduleName = nodeList.item(i).getTextContent();
                    moduleVsPath.put(moduleName, projectRoot + "/" + moduleName);
                }
            }

            String groupId = document.getElementsByTagName("groupId").item(0).getNodeValue();
            String artifactId = document.getElementsByTagName("artifactId").item(0).getNodeValue();
            projectName = StringUtils.trimToEmpty(groupId) + ":" + StringUtils.trimToEmpty(artifactId);

            projectVersion = StringUtils.trimToEmpty(document.getElementsByTagName("version").item(0).getNodeValue());

        } catch (ParserConfigurationException e) {
            log.fatal("Error creating file parser");
            throw new ExceptionHandler("Error creating file parser", ExceptionId.FILE_CON);
        } catch (IOException e) {
            log.fatal("Error parsing " + fileName);
            throw new ExceptionHandler("Error parsing " + fileName, ExceptionId.FILE_O);
        } catch (SAXException e) {
            log.fatal("Error parsing " + fileName);
            throw new ExceptionHandler("Error parsing " + fileName, ExceptionId.FILE_O);
        }


    }

    @Override
    public Map<String, List<String>> getDependencyList() throws ExceptionHandler {

        String currentModule = "";
        Map<String, List<String>> moduleVsDependencies = new HashMap<>();

        for (String module : moduleVsPath.keySet()) {
            currentModule = module;

            File xmlFile = new File(moduleVsPath.get(module) + "/pom.xml");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = null;
            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new ExceptionHandler("Error creating file parser", ExceptionId.FILE_CON);
            }
            try {
                Document document = documentBuilder.parse(xmlFile);
                XPath xPath = XPathFactory.newInstance().newXPath();
                NodeList nodeList = (NodeList) xPath
                        .compile("/project/dependencies/dependency/artifactId")
                        .evaluate(document, XPathConstants.NODESET);

                List<String> dependencies = new ArrayList<>();

                for (int i = 0; i < nodeList.getLength(); i++) {
                    String dependency = nodeList.item(i).getTextContent();

                    if (moduleVsPath.containsKey(dependency)) {
                        dependencies.add(dependency);
                    }
                }
                moduleVsDependencies.put(module, dependencies);
            }
            catch (XPathExpressionException e) {
                throw new ExceptionHandler(
                        "Error parsing artifacts from" + currentModule + "/pom.xml", ExceptionId.FILE_READ);
            } catch (IOException e) {
                throw new ExceptionHandler(
                        "Error parsing " + currentModule + "/pom.xml", ExceptionId.FILE_READ);
            } catch (SAXException e) {
                throw new ExceptionHandler(
                        "Error parsing " + currentModule + "/pom.xml", ExceptionId.FILE_READ);
            }
        }
        Map<String, List<String>> moduleVsDependencyPaths = new HashMap<>();

        for(String module : moduleVsDependencies.keySet()) {
            List<String> dependencyPaths = new ArrayList<>();
            calculateAllDependenciesForModule(module, moduleVsDependencies, dependencyPaths);
            dependencyPaths.add(moduleVsPath.get(module) + "/src/main/java");
            moduleVsDependencyPaths.put(module, dependencyPaths);
        }
        return moduleVsDependencyPaths;
    }

    private void calculateAllDependenciesForModule(
            String module, Map<String, List<String>> moduleVsDependencies, List<String> dependencyPaths) {
        for (String dependency : moduleVsDependencies.get(module)) {
            dependencyPaths.add(moduleVsPath.get(dependency) + "/src/main/java");
            calculateAllDependenciesForModule(dependency, moduleVsDependencies,dependencyPaths);
        }
    }

    @Override
    public String getProjectName() {
        return this.projectName;
    }

    @Override
    public String getProjectVersion() {
        return this.projectVersion;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    @Override
    public Boolean isGradle() {
        return false;
    }
}
