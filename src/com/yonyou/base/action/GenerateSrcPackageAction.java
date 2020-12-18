package com.yonyou.base.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.yonyou.base.utils.IdeaProjectGenerateUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author zp
 * @Description: 生成NC中间件启动，NC客户端启动run方法
 * @Date 2020/12/15 10:19
 * @Version 1.0
 */
public class GenerateSrcPackageAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        //生成源码包src
        IdeaProjectGenerateUtil.generateNC_ProjectSrcPackage(anActionEvent);

    }
}
