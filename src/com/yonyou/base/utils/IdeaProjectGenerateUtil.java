package com.yonyou.base.utils;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.ShortenCommandLine;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.project.manage.SourceFolderManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zp
 * @Description:
 * @Date 2020/12/8 14:32
 * @Version 1.0
 */
public class IdeaProjectGenerateUtil {

    private static final List<File> ncAntLibJars = new ArrayList<>();
    private static final List<File> ncCommonLibJars = new ArrayList<>();
    private static final List<File> ncMiddlewareLibJars = new ArrayList<>();
    private static final List<File> ncFrameworkLibJars = new ArrayList<>();
    private static final List<File> ncModulePublicLibs = new ArrayList<>();
    private static final List<File> ncModulePrivateLibs = new ArrayList<>();
    private static final List<File> ncModuleClientLibs = new ArrayList<>();
    public static final String JAR = ".jar";
    public static final String NC_SERVER_MAIN = "ufmiddle.start.tomcat.StartDirectServer";
    public static final String NC_CLIENT_MAIN = "nc.starter.test.JStarter";

//    static {
//        prepareLibs();
//
//    }

    private static void prepareLibs() {
        getAllFilesWithFileSuffix(ncAntLibJars, new File(NCConfigUtil.getNcHome(), "ant"), JAR);
        getAllFilesWithFileSuffix(ncCommonLibJars, new File(NCConfigUtil.getNcHome(), "lib"), JAR);
        getAllFilesWithFileSuffix(ncCommonLibJars, new File(NCConfigUtil.getNcHome(), "external"), JAR);
        getAllFilesWithFileSuffix(ncMiddlewareLibJars, new File(NCConfigUtil.getNcHome(), "middleware"), JAR);
        getAllFilesWithFileSuffix(ncFrameworkLibJars, new File(NCConfigUtil.getNcHome(), "framework"), JAR);
        getNCModulesLibAndClasses(ncModulePublicLibs, new File(NCConfigUtil.getNcHome(), "modules"), "lib", true);
        getNCModulesLibAndClasses(ncModulePublicLibs, new File(NCConfigUtil.getNcHome(), "modules"), "classes", false);
        getNCModulesLibAndClasses(ncModuleClientLibs, new File(NCConfigUtil.getNcHome(), "modules"), "client" + File.separator + "lib", true);
        getNCModulesLibAndClasses(ncModuleClientLibs, new File(NCConfigUtil.getNcHome(), "modules"), "client" + File.separator + "classes", false);
        getNCModulesLibAndClasses(ncModulePrivateLibs, new File(NCConfigUtil.getNcHome(), "modules"), "META-INF" + File.separator + "lib", true);
        getNCModulesLibAndClasses(ncModulePrivateLibs, new File(NCConfigUtil.getNcHome(), "modules"), "META-INF" + File.separator + "classes", false);
    }

    public static void prepareAllNcLibsForPorject(Project project) {
        prepareLibs();
        addNCLIBstoProject(project, ncAntLibJars, "NC_Ant_Libs");
        addNCLIBstoProject(project, ncCommonLibJars, "NC_Common_Libs");
        addNCLIBstoProject(project, ncMiddlewareLibJars, "NC_Middleware_Libs");
        addNCLIBstoProject(project, ncFrameworkLibJars, "NC_Framework_Libs");
        addNCLIBstoProject(project, ncModulePublicLibs, "NC_Public_Libs");
        addNCLIBstoProject(project, ncModulePrivateLibs, "NC_Private_Libs");
        addNCLIBstoProject(project, ncModuleClientLibs, "NC_Client_Libs");

    }

    /**
     * 获取指定路径下所有指定后缀的文件（包括后代级）
     *
     * @param fileList   存放files的集合
     * @param path       指定的路径
     * @param fileSuffix 文件后缀名
     * @return
     */
    public static List<File> getAllFilesWithFileSuffix(List<File> fileList
            , File path, String fileSuffix) {
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                getAllFilesWithFileSuffix(fileList, file, fileSuffix);
            } else if (file.getName().endsWith(fileSuffix))
                fileList.add(file);
        }
        return fileList;
    }

    /**
     * 获得所有NC的模块 内的 指定的文件夹里的依赖路径
     *
     * @param files
     * @param ncModules NC 产品模块文件夹根路径
     * @param dirName   依赖路径文件夹名字，比如 META-INF + File.separatorChar + lib
     * @param isJarDir  是否是jar文件文件夹，true会搜索所有jar，不然认为是class文件 直接返回这个文件夹
     * @return
     */
    public static void getNCModulesLibAndClasses(List<File> files, File ncModules, String dirName, boolean isJarDir) {
        File[] listFiles = ncModules.listFiles();
        Arrays.stream(listFiles).forEach(dir -> {
            ArrayList<File> jars = new ArrayList<>();
            if (dir.isDirectory()) {
                File f = new File(dir, File.separatorChar + dirName);
                if (f.exists()) {
                    if (isJarDir) {
                        getAllFilesWithFileSuffix(jars, f, JAR);
                        files.addAll(jars);
                    } else {
                        files.add(f);
                    }
                }
            }
        });

    }


    public static List<File> getNcAntLibJars() {
        return ncAntLibJars;
    }

    public static List<File> getNcCommonLibJars() {
        return ncCommonLibJars;
    }

    public static List<File> getNcMiddlewareLibJars() {
        return ncMiddlewareLibJars;
    }

    public static List<File> getNcFrameworkLibJars() {
        return ncFrameworkLibJars;
    }

    public static List<File> getNcModulePublicLibs() {
        return ncModulePublicLibs;
    }

    public static List<File> getNcModulePrivateLibs() {
        return ncModulePrivateLibs;
    }

    public static List<File> getNcModuleClientLibs() {
        return ncModuleClientLibs;
    }

    public static void main(String[] args) {
        List<File> myjarList = new ArrayList<>();
        getAllFilesWithFileSuffix(myjarList, new File("E:\\home\\ncc1909-base\\modules\\baseapp"), ".jar");
        System.out.println(myjarList);
        System.out.println(getAllFilesWithFileSuffix(myjarList, new File("E:\\home\\ncc1909-base\\modules\\baseapp"), ".jar").size());
    }

    public static void generateNC_ProjectSrcPackage(AnActionEvent event) {
        Module[] modules = ModuleManager.getInstance(event.getProject()).getModules();
        Arrays.stream(modules).forEach(module -> {
            prepareForModule2(module);
        });
    }

    public static void generateNC_ProjectRunConfigurations(AnActionEvent event) {
        Project project = event.getProject();
        RunManager runManager = RunManager.getInstance(project);
        //获取Application类型的run配置。
        List<RunConfiguration> configurationsList = runManager.getConfigurationsList(ApplicationConfigurationType
                .getInstance());
        BitSet bitSet = new BitSet(2);
        configurationsList.forEach(runConfiguration -> {
            ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) runConfiguration;

            boolean isServer = applicationConfiguration.getMainClassName().equals(NC_SERVER_MAIN);
            boolean isClient = applicationConfiguration.getMainClassName().equals(NC_CLIENT_MAIN);
            //存在该中间件配置，那么进行更新，比如HOME路径变更。此时我们需要加载变更后的HOME下的资源
            if (isServer) {
                bitSet.set(1);
                Map<String, String> envs = applicationConfiguration.getEnvs();
                envs.put("FIELD_NC_HOME", NCConfigUtil.getNcHomePath());
                applicationConfiguration.setEnvs(envs);
                applicationConfiguration.setWorkingDirectory(NCConfigUtil.getNcHomePath());
            }
            if (isClient) {
                bitSet.set(2);
                Map<String, String> envs = applicationConfiguration.getEnvs();
                envs.put("FIELD_CLINET_IP", NCPropXmlUtil.getNcClientPort());
                envs.put("FIELD_CLINET_PORT", NCPropXmlUtil.getNcClientIP());
                envs.put("FIELD_NC_HOME", NCConfigUtil.getNcHomePath());
                applicationConfiguration.setEnvs(envs);
                applicationConfiguration.setWorkingDirectory(NCConfigUtil.getNcHomePath());
            }
        });
        if (!bitSet.get(1)) {
            ApplicationConfiguration serverApplicationConfiguration = new ApplicationConfiguration("NC中间件", project,
                    ApplicationConfigurationType.getInstance());
            serverApplicationConfiguration.setMainClassName(NC_SERVER_MAIN);
            //不能启动多个中间件，一个项目一个中间件服务器。
            serverApplicationConfiguration.setAllowRunningInParallel(false);
            HashMap<String, String> envs = new HashMap<>();
            envs.put("FIELD_NC_HOME", NCConfigUtil.getNcHomePath());
            serverApplicationConfiguration.setEnvs(envs);
            serverApplicationConfiguration.setVMParameters(
                    " -Dnc.http.port=" + NCPropXmlUtil.getNcProp(null).getDomain().getServer().getHttp()[0].getPort()
                            + " -Dcom.sun.management.jmxremote "
                            //+ "-Dcom.sun.management.jmxremote.port=11241 "
                            + "-Dcom.sun.management.jmxremote.ssl=false "
                            + "-Dcom.sun.management.jmxremote.authenticate=false "
                            + "-Dnc.exclude.modules=datamig,ecp,egbaseinfo,egdocmg,egitctrl,egriskmg,egrkaudit,gpm," +
                            "hrbm,hrcm,hrcp,hrdm,hrhi,hrjf,hrjq,hrma,hrp,hrpe,hrrm,hrrpt,hrss,hrta,hrtrn,hrwa,oaar," +
                            "oaco,oaep,oainf,oakm,oamt,oaod,oapo,oapp,oapub,srm,srmem,srmsm,swcm_pu,webad,webbd," +
                            "webdbl,webimp,webrt,websm " //${FIELD_EX_MODULES}
                            + " -Dnc.runMode=develop -Dnc.server.location=$FIELD_NC_HOME$"
                            + " -DEJBConfigDir=$FIELD_NC_HOME$/ejbXMLs"
                            + " -DExtServiceConfigDir=$FIELD_NC_HOME$/ejbXMLs"
                            + " -Xmx768m -XX:MaxPermSize=256m -DEnableSqlDebug=true -XX:+HeapDumpOnOutOfMemoryError "
                            + "-DSqlDebugSkipKey=bd_del_log,pub_alertruntime,pub_alertregistry,bi_schd_host,wfm_task," +
                            "pub_async,cp_sysinittemp,bi_schd_taskqueue,md_module,ec_muc_affili,ec_muc_member "
//                                 + " -Duap.hotwebs=" + ProjectNCConfigUtil.getNcHotWebsList()
            );
            serverApplicationConfiguration.setWorkingDirectory(NCConfigUtil.getNcHomePath());
            serverApplicationConfiguration.setModule(ModuleManager.getInstance(project).getModules()[0]);
            serverApplicationConfiguration.setShowConsoleOnStdErr(true);
            serverApplicationConfiguration.setShowConsoleOnStdOut(true);
            serverApplicationConfiguration.setShortenCommandLine(ShortenCommandLine.MANIFEST);
            RunnerAndConfigurationSettings runnerAndConfigurationSettings = new RunnerAndConfigurationSettingsImpl(RunManagerImpl.getInstanceImpl(project), serverApplicationConfiguration);
            //将配置添加到Runmanager
            runManager.addConfiguration(runnerAndConfigurationSettings, false);
        }

        if (!bitSet.get(2)) {
            ApplicationConfiguration clientAppicationMainConfiguration = new ApplicationConfiguration("NC客户端", project,
                    ApplicationConfigurationType.getInstance());
            clientAppicationMainConfiguration.setVMParameters(
                    " -Dcom.sun.management.jmxremote "
                            + " -Dcom.sun.management.jmxremote.ssl=false "
                            + " -Dcom.sun.management.jmxremote.authenticate=false "
                            + " -Dnc.runMode=develop"
                            + " -Dnc.jstart.server=$FIELD_CLINET_IP$"
                            + " -Dnc.jstart.port=$FIELD_CLINET_PORT$" +
                            " -Xmx512m -XX:MaxPermSize=256m -Dnc.fi.autogenfile=N "
            );
            clientAppicationMainConfiguration.setWorkingDirectory(NCConfigUtil.getNcHomePath());
            Map<String, String> envs = new HashMap<>(3);
            envs.put("FIELD_CLINET_IP", NCPropXmlUtil.getNcClientPort());
            envs.put("FIELD_CLINET_PORT", NCPropXmlUtil.getNcClientIP());
            envs.put("FIELD_NC_HOME", NCConfigUtil.getNcHomePath());
            clientAppicationMainConfiguration.setEnvs(envs);
            clientAppicationMainConfiguration.setMainClassName(NC_CLIENT_MAIN);
            clientAppicationMainConfiguration.setShowConsoleOnStdOut(true);
            clientAppicationMainConfiguration.setShowConsoleOnStdErr(true);
            clientAppicationMainConfiguration.setWorkingDirectory(NCConfigUtil.getNcHomePath());
            clientAppicationMainConfiguration.setModule(ModuleManager.getInstance(project).getModules()[0]);
            clientAppicationMainConfiguration.setShortenCommandLine(ShortenCommandLine.MANIFEST);

            RunnerAndConfigurationSettings runnerAndConfigurationSettings = new RunnerAndConfigurationSettingsImpl(RunManagerImpl.getInstanceImpl(project), clientAppicationMainConfiguration);
            //将配置添加到Runmanager
            runManager.addConfiguration(runnerAndConfigurationSettings, false);

        }
    }

    private static void prepareForModule(Module module) {
        File homeDir = new File(module.getModuleFilePath()).getParentFile();
        File src = new File(homeDir, "src");
        File srcPublc = new File(src, "public");
        SourceFolderManager.getInstance(module.getProject()).addSourceFolder(module, "src/public", JavaSourceRootType.SOURCE);

        File srcClient = new File(src, "client");

        srcClient.mkdirs();

        SourceFolderManager.getInstance(module.getProject()).addSourceFolder(module, "src/client", JavaSourceRootType.SOURCE);
        File srcPrivate = new File(src, "private");

        srcPrivate.mkdirs();

        SourceFolderManager.getInstance(module.getProject()).addSourceFolder(module, "src/private", JavaSourceRootType.SOURCE);

        File moduleSrc = new File(homeDir, "META-INF");

        moduleSrc.mkdirs();

        File umpFile = new File(moduleSrc, "module.xml");
        if (!umpFile.exists()) {
            try {
                PrintWriter out = new PrintWriter(new FileOutputStream(umpFile));
                out.print("<?xml version=\"1.0\" encoding=\"gb2312\"?>\n" +
                        "<module name=\""
                        + module.getName()
                        + "\">\n" +
                        "\t<public>\n" +
                        "\t</public>\n" +
                        "\t<private>\n" +
                        "\t</private>\n" +
                        "</module>");
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        File umpDir = new File(new File(module.getModuleFilePath()).getParentFile(), "META-INF");
        File nchome = NCConfigUtil.getNcHome();
        if (nchome == null || !nchome.isDirectory()) {
            return;
        }

        File modeluUmpDir = new File(nchome, File.separatorChar + "modules"
                + File.separatorChar + module.getName() + File.separatorChar + "META-INF");
        modeluUmpDir.mkdirs();
        File[] projectFiles = umpDir.listFiles(f -> f.isFile());
        Stream.of(projectFiles).forEach(file -> {
            try {
                Files.copy(file.toPath(), new File(modeluUmpDir, file.getName()).toPath(), StandardCopyOption
                        .REPLACE_EXISTING);
            } catch (Exception e) {
            }
        });
    }

    private static void prepareForModule2(Module module) {
        ContentEntry[] contentEntries = ModuleRootManager.getInstance(module).getContentEntries();
        Arrays.stream(contentEntries).forEach(contentEntry -> {
            File homeDir = new File(module.getModuleFilePath()).getParentFile();
            File f = new File(homeDir, "src/public");
            f.mkdir();
            contentEntry.addSourceFolder(VirtualFileManager.constructUrl("file", f.getPath()), false);
            File f2 = new File(homeDir, "src/client");
            f2.mkdir();
            contentEntry.addSourceFolder(VirtualFileManager.constructUrl("file", f2.getPath()), false);
            File f3 = new File(homeDir, "src/private");
            f3.mkdir();
            contentEntry.addSourceFolder(VirtualFileManager.constructUrl("file", f3.getPath()), false);
        });
        SourceFolderManager.getInstance(module.getProject()).addSourceFolder(module, "src/public", JavaSourceRootType.SOURCE);
        SourceFolderManager.getInstance(module.getProject()).addSourceFolder(module, "src/client", JavaSourceRootType.SOURCE);
        SourceFolderManager.getInstance(module.getProject()).addSourceFolder(module, "src/private", JavaSourceRootType.SOURCE);
    }

    public static void addNCLIBstoProject(Project project, List<File> files, String libraryName) {
        final LibraryTable.ModifiableModel model = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();
        Library library = model.getLibraryByName(libraryName);
        // 库存在创建新的
        if (library != null) {
            model.removeLibrary(library);
        }
        library = model.createLibrary(libraryName);

        final Library.ModifiableModel libraryModel = library.getModifiableModel();

        //参数转换成路径集合
        List<String> classesRoots = files.stream().map(file -> file.getPath()).collect(Collectors.toList());

        // 加入新的依赖路径
        for (String root : classesRoots) {
            if (root.toLowerCase().endsWith("_src.jar")) {
                libraryModel.addRoot(VirtualFileManager.constructUrl("jar", root + "!/"), OrderRootType.SOURCES);
            } else if (root.toLowerCase().endsWith(".jar")) {
                // 注意jar格式jar:{path_to_jar}.jar!/
                libraryModel.addRoot(VirtualFileManager.constructUrl("jar", root + "!/"), OrderRootType.CLASSES);
            } else if (root.toLowerCase().endsWith(".class")) {
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.CLASSES);
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.SOURCES);
            } else {
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.CLASSES);
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.SOURCES);
            }
        }
        // 提交库变更
        ApplicationManager.getApplication().runWriteAction(() -> {
            libraryModel.commit();
            model.commit();
        });

        // 向项目模块依赖中增加新增的库
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (ModuleRootManager.getInstance(module).getModifiableModel().findLibraryOrderEntry(library) == null) {
                ModuleRootModificationUtil.addDependency(module, library);
            }
        }
    }
}
