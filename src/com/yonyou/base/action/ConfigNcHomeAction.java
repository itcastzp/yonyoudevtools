package com.yonyou.base.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.yonyou.base.ui.NCHomeConfigDialog;
import com.yonyou.base.utils.IdeaProjectGenerateUtil;

/**
 * @author zp
 * @Description:
 * @Date 2020/12/7 10:34
 * @Version 1.0
 */
public class ConfigNcHomeAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        NCHomeConfigDialog dialog = new NCHomeConfigDialog(project);
        if (dialog.showAndGet()) {
            dialog.createCenterPanel();
            //选择路径
            int updateClassPath = Messages.showYesNoDialog("是否立即更新项目NC_Dependencies依赖(注意可能非常耗时)?"
                    , "更新NC依赖", Messages.getQuestionIcon());
            if (updateClassPath == Messages.YES) {
                IdeaProjectGenerateUtil.prepareAllNcLibsForPorject(project);
            }
        }


    }
}
