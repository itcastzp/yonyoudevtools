package com.yonyou.base.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.yonyou.base.utils.NCConfigUtil;
import com.yonyou.base.utils.NCPropXmlUtil;
import nc.bs.framework.tool.config.prop.DataSourceMeta;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author zp
 * @Description:
 * @Date 2020/12/9 16:34
 * @Version 1.0
 */
public class NCHomeConfigPanel extends JScrollPane {
    private final ExecutorService es = Executors.newSingleThreadExecutor();
    private JPanel panel1;
    private JTextField ncHome;
    private JTextField dataBasePort;
    private JTextField dataBaseUrl;
    private JButton chooseDirButton;
    private JComboBox comboBox_datasource;
    private JTextField sid;
    private JTextField oidMark;
    private JComboBox comboBox_dbtype;
    private JTextField maxConnectCount;
    private JTextField minConnectCount;
    private JTextField username;
    private JTextField password;
    private JTextField ncClientIp;
    private JTextField ncClientPort;
    private JButton testDB;

    public NCHomeConfigPanel() {
        refreshPanel();
        chooseDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onChoseDir(e);
            }
        });
        testDB.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Class.forName("oracle.jdbc.OracleDriver");

                    Connection connection = DriverManager.getConnection(NCHomeConfigPanel.this.dataBaseUrl.getText(), NCHomeConfigPanel.this.username.getToolTipText(), NCHomeConfigPanel.this.password.getText());
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM DUAL");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        if (resultSet.getString(1) != null) {
                            Messages.showInfoMessage("测试数据库连接成功", "测试连接");
                        }
                    }
                } catch (SQLException ex) {
                    Messages.showErrorDialog(ex.getMessage(), "测试数据库连接失败！！！");
                } catch (ClassNotFoundException ex) {
                    Messages.showErrorDialog(ex.getMessage(), "测试数据库连接失败！！！");
                }
            }
        });
        panel1.addContainerListener(new ContainerAdapter() {
        });
    }

    public void testDB() {
        {
            Future<String> objectFuture = es.submit(() -> {
                try {
                    Connection connection = DriverManager.getConnection(NCHomeConfigPanel.this.dataBaseUrl.getText(), NCHomeConfigPanel.this.username.getToolTipText(), NCHomeConfigPanel.this.password.getText());
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM DUAL");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                } catch (SQLException ex) {
                    Messages.showErrorDialog("SQLException", ex.getMessage());
                }
                return null;
            });
            try {
                Object o = objectFuture.get(3, TimeUnit.SECONDS);
                Messages.showInfoMessage("测试数据库连接成功", String.valueOf(o));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Messages.showErrorDialog("测试数据库连接失败！！！", "测试连接");
                throw new RuntimeException(ex);
            } catch (TimeoutException ex) {
                objectFuture.cancel(true);
                Messages.showErrorDialog("测试数据库连接超时...", "测试连接");
            } catch (Exception ex) {
                Messages.showErrorDialog("测试数据库连接失败！！！", "测试连接");
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 选择NC路径
     *
     * @param actionEvent
     */
    public void onChoseDir(ActionEvent actionEvent) {
        /*@see html https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/documents.html*/
        VirtualFile virtualFile = FileChooser.chooseFile(new FileChooserDescriptor(false, true
                        , false, false, false, false), null
                , null);

        if (virtualFile == null) {
            return;
        }
        String path = virtualFile.getPath();
        File home = new File(path);
        if (home.exists() && home.isDirectory()) {
            this.getNcHome().setText(home.getPath());
            readNCConfigAndRefresh();
        } else {
            Messages.showMessageDialog("请选择正确的NC_HOME文件夹！", "错误", null);
        }
    }

    /**
     * 读取 NC的各种基本配置信息
     */
    public void readNCConfigAndRefresh() {
        NCConfigUtil.setNCHomePath(this.getNcHome().getText());
        refreshPanel();
    }

    private void refreshPanel() {
        if (NCConfigUtil.getNcHomePath() != null) {
            this.getNcHome().setText(NCConfigUtil.getNcHomePath());
            this.getNcClientIp().setText(NCPropXmlUtil.getNcClientIP());
            this.getNcClientPort().setText(NCPropXmlUtil.getNcClientPort());
            List<DataSourceMeta> dataSourceVOList = NCPropXmlUtil.getDataSourceVOList();

            DataSourceMeta dataSourceMeta = dataSourceVOList.get(0);
            String dataSourceName = dataSourceMeta.getDataSourceName();
            this.getComboBox_datasource().removeAllItems();
            dataSourceVOList.forEach(ds -> {
                this.getComboBox_datasource().addItem(ds.getDataSourceName());
            });
            //默认选中第一个下拉菜单内容
            this.getComboBox_datasource().setSelectedIndex(0);
            String databaseUrl = dataSourceVOList.get(0).getDatabaseUrl();
            this.getDataBaseUrl().setText(databaseUrl);
            this.getOidMark().setText(dataSourceVOList.get(0).getOidMark());
            int sidIndex = databaseUrl.lastIndexOf("/");
            int portIndex = databaseUrl.lastIndexOf(":");
            String sid = databaseUrl.substring(sidIndex + 1);
            String port = databaseUrl.substring(portIndex + 1, sidIndex);
            this.getSid().setText(sid);
            this.getDataBasePort().setText(port);
            this.getDataBaseUrl().setText(dataSourceVOList.get(0).getDatabaseUrl());
            this.getUsername().setText(dataSourceVOList.get(0).getUser());
            this.getPassword().setText(dataSourceVOList.get(0).getPassword());
            this.getMinConnectCount().setText(String.valueOf(dataSourceVOList.get(0).getMinCon()));
            this.getMaxConnectCount().setText(String.valueOf(dataSourceVOList.get(0).getMaxCon()));
        }


    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("HomeConfig");
        frame.setContentPane(new NCHomeConfigPanel().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public JPanel getPanel1() {
        return panel1;
    }

    public void setPanel1(JPanel panel1) {
        this.panel1 = panel1;
    }

    public JTextField getNcHome() {
        return ncHome;
    }

    public void setNcHome(JTextField ncHome) {
        this.ncHome = ncHome;
    }

    public JTextField getDataBasePort() {
        return dataBasePort;
    }

    public void setDataBasePort(JTextField dataBasePort) {
        this.dataBasePort = dataBasePort;
    }

    public JTextField getDataBaseUrl() {
        return dataBaseUrl;
    }

    public void setDataBaseUrl(JTextField dataBaseUrl) {
        this.dataBaseUrl = dataBaseUrl;
    }

    public JButton getChooseDirButton() {
        return chooseDirButton;
    }

    public void setChooseDirButton(JButton chooseDirButton) {
        this.chooseDirButton = chooseDirButton;
    }

    public JComboBox getComboBox_datasource() {
        return comboBox_datasource;
    }

    public void setComboBox_datasource(JComboBox comboBox_datasource) {
        this.comboBox_datasource = comboBox_datasource;
    }

    public JTextField getSid() {
        return sid;
    }

    public void setSid(JTextField sid) {
        this.sid = sid;
    }

    public JTextField getOidMark() {
        return oidMark;
    }

    public void setOidMark(JTextField oidMark) {
        this.oidMark = oidMark;
    }

    public JComboBox getComboBox_dbtype() {
        return comboBox_dbtype;
    }

    public void setComboBox_dbtype(JComboBox comboBox_dbtype) {
        this.comboBox_dbtype = comboBox_dbtype;
    }

    public JTextField getMaxConnectCount() {
        return maxConnectCount;
    }

    public void setMaxConnectCount(JTextField maxConnectCount) {
        this.maxConnectCount = maxConnectCount;
    }

    public JTextField getMinConnectCount() {
        return minConnectCount;
    }

    public void setMinConnectCount(JTextField minConnectCount) {
        this.minConnectCount = minConnectCount;
    }

    public JTextField getUsername() {
        return username;
    }

    public void setUsername(JTextField username) {
        this.username = username;
    }

    public JTextField getPassword() {
        return password;
    }

    public void setPassword(JTextField password) {
        this.password = password;
    }

    public JTextField getNcClientIp() {
        return ncClientIp;
    }

    public void setNcClientIp(JTextField ncClientIp) {
        this.ncClientIp = ncClientIp;
    }

    public JTextField getNcClientPort() {
        return ncClientPort;
    }

    public void setNcClientPort(JTextField ncClientPort) {
        this.ncClientPort = ncClientPort;
    }
}
