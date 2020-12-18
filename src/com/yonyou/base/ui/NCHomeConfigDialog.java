package com.yonyou.base.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author zp
 * @Description: NChome配置UI
 * @Date 2020/12/7 11:28
 * @Version 1.0
 */
public class NCHomeConfigDialog extends DialogWrapper {
    private JPanel configPanel;

    public NCHomeConfigDialog(@Nullable Project project) {
        super(project);
        super.init();
        super.getPeer().setSize(configPanel.getWidth() + 10, configPanel.getHeight() + 10);
        setTitle("配置NC_HOME");
    }

    @Nullable
    @Override
    public JComponent createCenterPanel() {
        configPanel = new NCHomeConfigPanel().getPanel1();
        configPanel.setPreferredSize(new Dimension(700, 600));
        return configPanel;
    }


}
