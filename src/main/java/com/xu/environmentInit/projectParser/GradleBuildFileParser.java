package com.xu.environmentInit.projectParser;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GradleBuildFileParser implements BuildFileParser{
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(GradleBuildFileParser.class);

    Map<String, String> moduleVsPath = new HashMap<>();
    String projectName;
    String projectVersion;

    public GradleBuildFileParser(String fileName) {
        try {
            final String content =
                    new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);

            List<ASTNode> astNodes = new AstBuilder().buildFromString(content);

            String[] splits = fileName.split("/");
            String projectName = splits[splits.length-2];
            final String projectRoot = fileName.substring(0,fileName.lastIndexOf('/'));

            //.gradle文件格式为Groovy代码
            GroovyCodeVisitor visitor = new CodeVisitorSupport() {
                @Override
                public void visitMethodCallExpression(MethodCallExpression call) {
                    List<Expression> args =
                            ((ArgumentListExpression)call.getArguments()).getExpressions();
                    for(Expression arg : args){
                        moduleVsPath.put(arg.getText(), projectRoot + "/" + arg.getText());
                    }
                }
            };
            for (ASTNode astNode : astNodes) {
                astNode.visit(visitor);
            }
            if (moduleVsPath.isEmpty()) {
                moduleVsPath.put(projectName, projectRoot);
            }

            log.debug("Attempting to Read the gradle.property file");
            Properties gradleProperties = new Properties();
            gradleProperties.load(new FileInputStream(new File(fileName.replace("settings.gradle", "gradle.property"))));

            log.debug("Attempting to retrieve the project name");
            projectName =
                    StringUtils.trimToNull(
                            gradleProperties.getProperty(
                                    "projectName", gradleProperties.getProperty("groupName")));

            log.debug("Attempting to retrieve the project version");
            projectVersion =
                    StringUtils.trimToNull(
                            gradleProperties.getProperty(
                                    "theVersion",
                                    gradleProperties.getProperty(
                                            "version", gradleProperties.getProperty("versionNumber"))));


        } catch (IOException e) {
            log.warn("Error reading file " + fileName);
        }
    }


    @Override
    public Map<String, List<String>> getDependencyList() throws ExceptionHandler {
        String buildFile = "";

        Map<String, List<String>> moduleVsDependencies = new HashMap<>();
        try {
            for (String module : moduleVsPath.keySet()) {
                final List<String> dependencies = new ArrayList<>();
                buildFile = moduleVsPath.get(module) + "/build.gradle";


                String content =
                        new String(Files.readAllBytes(Paths.get(buildFile)), StandardCharsets.UTF_8);
                List<ASTNode> astNodes = new AstBuilder().buildFromString(content);

                GroovyCodeVisitor visitor = new CodeVisitorSupport() {
                    @Override
                    public void visitClosureExpression(ClosureExpression expression) {
                        Statement block = expression.getCode();
                        if(block instanceof BlockStatement) {
                            BlockStatement blockStatement = (BlockStatement) block;
                            for(Statement statement : blockStatement.getStatements()) {
                                String stmtStr = statement.getText();

                                if(stmtStr.contains("this.compile(this.project(:")) {
                                    String dependency = stmtStr.substring(stmtStr.indexOf(':' )+ 1,stmtStr.indexOf(')'));
                                    dependencies.add(dependency);
                                }
                            }
                        }
                    }
                };
                for(ASTNode astNode : astNodes) {
                    astNode.visit(visitor);
                }
                moduleVsDependencies.put(module,dependencies);
            }
            Map<String,List<String>> moduleVsDependencyPaths = new HashMap<>();


            for (String module : moduleVsDependencies.keySet()) {
                List<String> dependencyPaths = new ArrayList<>();
                calculateAllDependenciesForModule(module, moduleVsDependencies, dependencyPaths);
                dependencyPaths.add(moduleVsPath.get(module) + "/src/main/java");
                moduleVsDependencyPaths.put(module, dependencyPaths);
            }

            return moduleVsDependencyPaths;

        } catch (IOException e) {
            log.fatal("Error reading file " + buildFile);
            throw new ExceptionHandler("Error reading file " + buildFile, ExceptionId.FILE_I);
        }
    }

    private void calculateAllDependenciesForModule(
            String module, Map<String, List<String>> moduleVsDependencies, List<String> dependencyPaths) {
        for (String dependency : moduleVsDependencies.get(module)) {
            dependencyPaths.add(moduleVsPath.get(dependency) + "/src/main/java");
            calculateAllDependenciesForModule(dependency, moduleVsDependencies,dependencyPaths);
        }
    }


    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    @Override
    public String getProjectName() {
        return this.projectName;
    }

    @Override
    public String getProjectVersion() {
        return this.projectVersion;
    }

    @Override
    public Boolean isGradle() {
        return true;
    }
}
