package com.yonyou.base.utils;

import com.intellij.openapi.ui.Messages;
import com.thoughtworks.xstream.XStream;
import nc.bs.framework.tool.config.prop.DataSourceMeta;
import nc.bs.framework.tool.config.prop.PropInfo;
import nc.bs.framework.tool.config.prop.SingleServerInfo;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author zp
 * @Description:
 * @Date 2020/12/7 14:59
 * @Version 1.0
 */
public class NCPropXmlUtil {
    /***** prop.xml 默认的相对NC HOME位置 ***/
    public static final String DEFUAL_NC_PROP_PATH = File.separatorChar + "ierp" + File.separatorChar + "bin" + File.separatorChar + "prop.xml";

    //    public static void loadConfFromFile(String ncHomePath) {
//        Class<DataSourceMeta> ncDataSourceVOClass = DataSourceMeta.class;
//        Field[] fields = ncDataSourceVOClass.getDeclaredFields();
//        try {
//            SAXReader reader = new SAXReader();
//            Document document = reader.read(new File(ncHomePath, DEFUAL_NC_PROP_PATH));
//            List<Node> datasources = document.selectNodes("//dataSource");
//            for (Node datasource : datasources) {
//                DataSourceMeta vo = new DataSourceMeta();
//                for (Field field : fields) {
//                    Node dummyNode = datasource.selectSingleNode(field.getName());
//                    ReflectionUtil.setField(DataSourceMeta.class, vo, String.class, field.getName(), dummyNode.getText());
//                }
//                dataSourceVOList.add(vo);
//            }
//        } catch (DocumentException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(dataSourceVOList);
//    }


    public static void main(String[] args) {

        System.out.println(new File("E:\\home2\\ncc1909-base", DEFUAL_NC_PROP_PATH));
    }

    public static PropInfo getNcProp(String homePath) {
        try {
            XStream xstream = new XStream();
            xstream.alias("root", PropInfo.class);
            xstream.alias("dataSource", DataSourceMeta.class);
            //隐式该集合属性，可防止生成重复路径如root/dataSource/dataSource
            xstream.addImplicitArray(PropInfo.class, "dataSource");
            xstream.addImplicitArray(PropInfo.class, "internalServiceArray");
            xstream.addImplicitArray(PropInfo.class, "webServer");
            xstream.addImplicitArray(SingleServerInfo.class, "http");
            xstream.addImplicitArray(SingleServerInfo.class, "https");
            xstream.addImplicitArray(SingleServerInfo.class, "ajp");
            //忽略isBase属性，VO中没有
            xstream.ignoreUnknownElements();
//            xstream.omitField(DataSourceMeta.class, "isBase");
            return (PropInfo) xstream.fromXML(new File(homePath == null ? NCConfigUtil.getNcHomePath() : homePath, DEFUAL_NC_PROP_PATH));
        } catch (Exception e) {
            Messages.showErrorDialog("NCHOME下的prop文件找不到。请检查" + homePath + "或者" + NCConfigUtil.getNcHomePath() +
                    "是否存在！", "读取NC配置错误");
            throw new RuntimeException("初始化NC配置出错！！！检查NC_HOME路径", e);
        }
    }

    public static List<DataSourceMeta> getDataSourceVOList() {
        return Arrays.asList(getNcProp(null).getDataSource());
    }

    public static String getNcClientIP() {
        return getNcProp(null).getDomain().getServer().getOneIP();
    }

    public static String getNcClientPort() {
        return String.valueOf(getNcProp(null).getDomain().getServer().getHttp()[0].getPort());
    }

}
