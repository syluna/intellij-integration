package com.jmonkeystore.ide.scene.explorer.impl.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.jmonkeystore.ide.scene.explorer.FileStructureTreeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewSkyBoxDialog extends DialogWrapper {

    private JPanel contentPanel;
    private JScrollPane treeScrollPane;

    final List<String> extensions = new ArrayList<>();

    private VirtualFile selectedFile;

    public NewSkyBoxDialog(@NotNull Project project) {
        super(project, true);
        setTitle("New SkyBox...");
        init();

        createTree(project);
    }

    private void createTree(Project project) {

        VirtualFile resourcePath = Arrays.stream(ProjectRootManager.getInstance(project).getContentSourceRoots())
                .filter(virtualFile -> virtualFile.getPath().toLowerCase().endsWith("src/main/resources"))
                .findFirst()
                .orElse(null);

        extensions.add(".jpg");
        extensions.add((".jpeg"));
        extensions.add(".png");
        extensions.add(".gif");
        extensions.add(".hdr");
        extensions.add(".dds");

        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(resourcePath);
        DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
        Tree tree = new Tree(treeModel);
        tree.setCellRenderer(new FileStructureTreeRenderer());

        traverseFileStructure(treeRoot, resourcePath);

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) ((Tree) e.getSource()).getLastSelectedPathComponent();

            VirtualFile virtualFile = (VirtualFile) treeNode.getUserObject();
            String extension = virtualFile.getExtension();

            // don't allow the user to select a directory or unsupported image.
            if (!virtualFile.isDirectory()) {
                selectedFile = virtualFile;
            }
            else { // if the user selected one thing, then another, it would still be the old selection.
                selectedFile = null;
            }

        });

        treeScrollPane.setViewportView(tree);
    }

    private void traverseFileStructure(DefaultMutableTreeNode treeNode, VirtualFile resourcePath) {

        VirtualFile[] children = resourcePath.getChildren();

        for (VirtualFile file : children) {

            DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(file);
            treeNode.add(childTreeNode);

            if (file.isDirectory() || file.getExtension() != null && extensions.contains(file.getExtension().toLowerCase())) {
                traverseFileStructure(childTreeNode, file);
            }

        }

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

    public VirtualFile getSelectedFile() {
        return selectedFile;
    }
}
