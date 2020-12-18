package com.yonyou.base.utils;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author zp
 * @Description:
 * @Date 2020/12/7 14:58
 * @Version 1.0
 */
public class NCConfigUtil {
    //唯一标识
    private static final int SS_ID = System.identityHashCode(new Object());
    /*** NC插件所用配置文件***/
    public static final String NC_PLUGIN_PROP = "nc.xml";
    public static final String[] DB_DRIVERS = new String[]{"oracle.jdbc.OracleDriver", "oracle.jdbc.OracleDriver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "com.ibm.db2.jcc.DB2Driver"};
    /**** NC 相关属性集合 ***/
    private static Properties configPropertis = new Properties();

    private static final String PROJECT_PATH;
    public static final File devPlugins;

    static {
        DataManager instance = DataManager.getInstance();
        DataContext dataContext = instance.getDataContext();
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        try {
            PROJECT_PATH = project.getBasePath();
            devPlugins = new File(PROJECT_PATH + File.separator + NC_PLUGIN_PROP);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void setNCHomePath(String textField_home) {

        try {
            NCPropXmlUtil.getNcProp(textField_home);
            configPropertis.put("NC_HOME_DIR", textField_home);
            configPropertis.storeToXML(new FileOutputStream(devPlugins), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String getNcHomePath() {
        if (devPlugins.exists()) {
            try {
                configPropertis.loadFromXML(new FileInputStream(devPlugins));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (String) configPropertis.get("NC_HOME_DIR");
    }

    public static File getNcHome() {
        return new File((String) getNcHomePath());
    }

}
