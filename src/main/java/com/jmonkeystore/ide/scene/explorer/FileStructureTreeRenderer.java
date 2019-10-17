package com.jmonkeystore.ide.scene.explorer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class FileStructureTreeRenderer implements TreeCellRenderer {

    private final int borderSize = 2;

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        Object item = ((DefaultMutableTreeNode) value).getUserObject();
        VirtualFile virtualFile = (VirtualFile) item;

        JLabel label = new JLabel();
        label.setBorder(BorderFactory.createEmptyBorder (borderSize, borderSize, borderSize, borderSize));
        label.setIconTextGap(5);

        label.setText(virtualFile.getName());

        if (virtualFile.isDirectory()) {
            label.setIcon(AllIcons.Actions.ProjectDirectory);
        }
        else {
            label.setIcon(AllIcons.FileTypes.Any_type);
        }

        return label;
    }

}
