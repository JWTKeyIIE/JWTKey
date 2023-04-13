package com.xu.environmentInit;

import com.xu.environmentInit.Exception.ExceptionHandler;
import com.xu.environmentInit.Exception.ExceptionId;
import com.xu.environmentInit.enumClass.Listing;
import com.xu.environmentInit.enumClass.SourceType;
import com.xu.environmentInit.enumClass.argsIdentifier;
import com.xu.utils.Utils;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentCheck {
    private static final Logger log =
            org.apache.logging.log4j.LogManager.getLogger(ArgumentCheck.class);
    public static EnvironmentInfo parameterCheck(ArrayList<String> args) throws ExceptionHandler, IOException {
        List<String> originalArguments = new ArrayList<String>(args);

        //命令行参数处理
        Options cmdLineArgs = setOptions();
        CommandLine cmd = null;

        if(args.contains(argsIdentifier.HELP.getArg())){
            //如果需要Help文件，返回Help文件
//            log.debug("return help，需要添加异常处理文件：待处理");
            log.debug("return help");
            throw new ExceptionHandler("return Help message",ExceptionId.HELP);
        }

        log.info("Starting the parsing of arguments.");

        //处理Options中定义的参数
        try {
            cmd = new DefaultParser().parse(cmdLineArgs,args.toArray(new String[0]));
        } catch (ParseException e) {
//            log.debug("解析参数出现错误：" + e.getMessage());
            log.debug("parse parameters error");
            e.printStackTrace();
        }

        //清理收到的参数,将argsIdentifier中没有定义的参数筛选出来
//        log.info("清理输出相关的参数");
        log.info("clean undefined parameters");
        ArrayList<String> cleanArgs = new ArrayList<>(args);
        for (argsIdentifier arg : argsIdentifier.values()) {
            if (cmd.hasOption(arg.getId())) {
                cleanArgs.remove("-" + arg.getId());
                cleanArgs.remove(cmd.getOptionValue(arg.getId()));
            }
        }
        args = cleanArgs;
        log.debug("Output specific arguments: " + args.toString());


//        处理必须的参数
        SourceType type = SourceType.getFromFlag(cmd.getOptionValue(argsIdentifier.FORMAT.getId()));
        log.debug("SourceType is :" + type.getName());
        List<String> source = Arrays.asList(cmd.getOptionValues(argsIdentifier.SOURCE.getId()));
        log.debug("the size of source list:" + source.size());

        String javaHome = null;
        String androidHome = null;
        switch (type) {
            //Only APK path needs an android specified path
            case APK:
                if (cmd.hasOption(argsIdentifier.ANDROID.getArg()))
                    androidHome =
                            Utils.retrieveFilePath(
                                    cmd.getOptionValue(argsIdentifier.ANDROID.getId()), null, false, false, true);
            default:
                if (cmd.hasOption(argsIdentifier.JAVA.getArg()))
                    javaHome =
                            Utils.retrieveFilePath(
                                    cmd.getOptionValue(argsIdentifier.JAVA.getId()), null, false, false, true);
                break;
        }



//        if(cmd.hasOption(argsIdentifier.JAVA.getArg())){
//            javaHome =Utils.retrieveFilePath(
//                    cmd.getOptionValue(argsIdentifier.JAVA.getId()), null, false, false, true);
//        }

        Boolean usingInputIn = cmd.getOptionValue(argsIdentifier.SOURCE.getId()).endsWith(".in");
        log.debug("Enhanced Input in file: " + usingInputIn);

        String setMainClass = null;
        if(cmd.hasOption(argsIdentifier.MAIN.getId())){
            setMainClass = StringUtils.trimToNull(cmd.getOptionValue(argsIdentifier.MAIN.getId())); //将String中的空格进行修剪
            if(setMainClass == null){
                log.fatal("Please Enter a valid main class path.");
                throw new ExceptionHandler("Please Enter a valid main class path.", ExceptionId.ARG_VALID);
            }
        }

//        如果cmd.hasOption(Dependency)为真，则执行前一个将-d的值保存在dependencies中，否则初始化新的ArrayList
        List<String> dependencies =
                cmd.hasOption(argsIdentifier.DEPENDENCY.getId())
                ? Arrays.asList(cmd.getOptionValues(argsIdentifier.DEPENDENCY.getId()))
                : new ArrayList<>();

//      处理jwt相关参数
        String jwtLibName = "";
        if(cmd.hasOption(argsIdentifier.JWTLIB.getId()))
            jwtLibName = cmd.getOptionValue(argsIdentifier.JWTLIB.getId());

//        处理输出格式相关的参数
/*        Listing messaging =
                Listing.retrieveListingType(cmd.getOptionValue(argsIdentifier.FORMATOUT.getId()));
        log.info("Using the output: " + messaging.getType());*/
        Listing messaging = Listing.Default;
        log.info("The output formate is: " + messaging);

        log.trace("Determining the file out.");
        String fileOutPath = null;
        if (cmd.hasOption(argsIdentifier.OUT.getId()))
            fileOutPath = cmd.getOptionValue(argsIdentifier.OUT.getId());

        EnvironmentInfo info =
                Core.parameterCheck(
                        source,
                        dependencies,
                        type,
                        messaging, //messaging
                        fileOutPath,//fileOutPath
                        cmd.hasOption(argsIdentifier.NEW.getId()),
                        setMainClass,
                        cmd.hasOption(argsIdentifier.TIMESTAMP.getId()),
                        null,  // javaHome
                        args,
                        jwtLibName);

        info.setPrettyPrint(cmd.hasOption(argsIdentifier.PRETTY.getId()));
        log.debug("Pretty flag: " + cmd.hasOption(argsIdentifier.PRETTY.getId()));
        info.setShowTimes(cmd.hasOption(argsIdentifier.TIMESTAMP.getId()));
        log.debug("Time measure flage: " + cmd.hasOption(argsIdentifier.TIMESTAMP.getId()));
        info.setStreaming(cmd.hasOption(argsIdentifier.STREAM.getId()));
        log.debug("Stream flag: " + cmd.hasOption(argsIdentifier.STREAM.getId()));
        info.setDisplayHeuristics(cmd.hasOption(argsIdentifier.HEURISTICS.getId()));
        log.debug("Heuristics flag: " + cmd.hasOption(argsIdentifier.HEURISTICS.getId()));
        Utils.initDepth(Integer.parseInt(cmd.getOptionValue(argsIdentifier.DEPTH.getId(),String.valueOf(1))));
        log.debug("Scanning using a depth of " + Utils.DEPTH);

        boolean noExitJVM = cmd.hasOption(argsIdentifier.NOEXIT.getId());
        log.debug("Exiting the JVM: " + noExitJVM);
        if(noExitJVM)
            info.setKillJVM(false);

        info.setRawCommand(Utils.join(" ",originalArguments));

        return info;

    }

    /**
     * 设置本程序可以处理的命令行参数
     * @return
     */
    public static Options setOptions(){
        Options cmdLineArgs = new Options();

        Option format = Option.builder(argsIdentifier.FORMAT.getId())
                .required()
                .hasArg()
                .argName(argsIdentifier.FORMAT.getArgName())
                .desc(argsIdentifier.FORMAT.getDesc())
                .build();
        format.setType(String.class);
        format.setOptionalArg(argsIdentifier.FORMAT.getRequired());
        cmdLineArgs.addOption(format);

        Option sources =
                Option.builder(argsIdentifier.SOURCE.getId())
                        .required()
                        .hasArgs()
                        .argName(argsIdentifier.SOURCE.getArgName())
                        .desc(argsIdentifier.SOURCE.getDesc())
                        .build();
        sources.setType(String.class);
        sources.setValueSeparator(' ');
        sources.setOptionalArg(argsIdentifier.SOURCE.getRequired());
        cmdLineArgs.addOption(sources);

        Option dependency =
                Option.builder(argsIdentifier.DEPENDENCY.getId())
                        .hasArg()
                        .argName(argsIdentifier.DEPENDENCY.getArgName())
                        .desc(argsIdentifier.DEPENDENCY.getDesc())
                        .build();
        dependency.setType(String.class);
        dependency.setOptionalArg(argsIdentifier.DEPENDENCY.getRequired());
        cmdLineArgs.addOption(dependency);

        Option mainFile =
                Option.builder(argsIdentifier.MAIN.getId())
                        .hasArg()
                        .argName(argsIdentifier.MAIN.getArgName())
                        .desc(argsIdentifier.MAIN.getDesc())
                        .build();
        mainFile.setType(String.class);
        mainFile.setOptionalArg(argsIdentifier.MAIN.getRequired());
        cmdLineArgs.addOption(mainFile);

        Option javaPath =
                Option.builder(argsIdentifier.JAVA.getId())
                        .hasArg()
                        .argName(argsIdentifier.JAVA.getArgName())
                        .desc(argsIdentifier.JAVA.getDesc())
                        .build();
        javaPath.setType(File.class);
        javaPath.setOptionalArg(argsIdentifier.JAVA.getRequired());
        cmdLineArgs.addOption(javaPath);
        Option depth =
                Option.builder(argsIdentifier.DEPTH.getId())
                        .hasArg()
                        .argName(argsIdentifier.DEPTH.getArgName())
                        .desc(argsIdentifier.DEPTH.getDesc())
                        .build();
        depth.setType(String.class);
        depth.setOptionalArg(argsIdentifier.DEPTH.getRequired());
        cmdLineArgs.addOption(depth);
        Option output =
                Option.builder(argsIdentifier.OUT.getId())
                        .hasArg()
                        .argName(argsIdentifier.OUT.getArgName())
                        .desc(argsIdentifier.OUT.getDesc())
                        .build();
        output.setType(String.class);
        output.setOptionalArg(argsIdentifier.OUT.getRequired());
        cmdLineArgs.addOption(output);
        Option timing =
                new Option(argsIdentifier.TIMEMEASURE.getId(), false, argsIdentifier.TIMEMEASURE.getDesc());
        timing.setOptionalArg(argsIdentifier.TIMEMEASURE.getRequired());
        cmdLineArgs.addOption(timing);
        Option formatOut =
                Option.builder(argsIdentifier.FORMATOUT.getId())
                        .hasArg()
                        .argName(argsIdentifier.FORMATOUT.getArgName())
                        .desc(argsIdentifier.FORMATOUT.getDesc())
                        .build();
        formatOut.setOptionalArg(argsIdentifier.FORMATOUT.getRequired());
        cmdLineArgs.addOption(formatOut);

        Option jwtLib = Option.builder(argsIdentifier.JWTLIB.getId())
                .hasArg()
                .argName(argsIdentifier.JWTLIB.getArgName())
                .desc(argsIdentifier.JWTLIB.getDesc())
                .build();
        jwtLib.setOptionalArg(argsIdentifier.JWTLIB.getRequired());
        cmdLineArgs.addOption(jwtLib);

        Option prettyPrint =
                new Option(argsIdentifier.PRETTY.getId(), false, argsIdentifier.PRETTY.getDesc());
        prettyPrint.setOptionalArg(argsIdentifier.PRETTY.getRequired());
        cmdLineArgs.addOption(prettyPrint);

        Option noExit =
                new Option(argsIdentifier.NOEXIT.getId(), false, argsIdentifier.NOEXIT.getDesc());
        noExit.setOptionalArg(argsIdentifier.NOEXIT.getRequired());
        cmdLineArgs.addOption(noExit);

        Option help = new Option(argsIdentifier.HELP.getId(), false, argsIdentifier.HELP.getDesc());
        help.setOptionalArg(argsIdentifier.HELP.getRequired());
        cmdLineArgs.addOption(help);

        Option version =
                new Option(argsIdentifier.VERSION.getId(), false, argsIdentifier.VERSION.getDesc());
        version.setOptionalArg(argsIdentifier.VERSION.getRequired());
        cmdLineArgs.addOption(version);

        Option displayHeuristcs =
                new Option(argsIdentifier.HEURISTICS.getId(), false, argsIdentifier.HEURISTICS.getDesc());
        displayHeuristcs.setOptionalArg(argsIdentifier.HEURISTICS.getRequired());
        cmdLineArgs.addOption(displayHeuristcs);

        Option timeStamp =
                new Option(argsIdentifier.TIMESTAMP.getId(), false, argsIdentifier.TIMESTAMP.getDesc());
        timeStamp.setOptionalArg(argsIdentifier.TIMESTAMP.getRequired());
        cmdLineArgs.addOption(timeStamp);

        Option stream =
                new Option(argsIdentifier.STREAM.getId(), false, argsIdentifier.STREAM.getDesc());
        stream.setOptionalArg(argsIdentifier.STREAM.getRequired());
        cmdLineArgs.addOption(stream);

        Option nologs =
                new Option(argsIdentifier.NOLOGS.getId(), false, argsIdentifier.NOLOGS.getDesc());
        nologs.setOptionalArg(argsIdentifier.NOLOGS.getRequired());
        cmdLineArgs.addOption(nologs);

        Option verbose =
                new Option(argsIdentifier.VERBOSE.getId(), false, argsIdentifier.VERBOSE.getDesc());
        verbose.setOptionalArg(argsIdentifier.VERBOSE.getRequired());
        cmdLineArgs.addOption(verbose);

        Option vverbose =
                new Option(argsIdentifier.VERYVERBOSE.getId(), false, argsIdentifier.VERYVERBOSE.getDesc());
        vverbose.setOptionalArg(argsIdentifier.VERYVERBOSE.getRequired());
        cmdLineArgs.addOption(vverbose);

        Option newFile = new Option(argsIdentifier.NEW.getId(), false, argsIdentifier.NEW.getDesc());
        newFile.setOptionalArg(argsIdentifier.NEW.getRequired());
        cmdLineArgs.addOption(newFile);



        return cmdLineArgs;
    }


}
