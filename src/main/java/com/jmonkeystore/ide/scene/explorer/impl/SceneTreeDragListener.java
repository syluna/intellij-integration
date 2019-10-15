package com.jmonkeystore.ide.scene.explorer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SceneTreeDragListener implements DropTargetListener  {

    DropTarget dropTarget;
    Tree targetTree;

    public SceneTreeDragListener(Tree tree) {
        targetTree = tree;
        this.dropTarget = new DropTarget(targetTree, this);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        // System.out.println("dragEnter(DropTargetDragEvent dtde)");
    }

    private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
        Point p = dtde.getLocation();
        DropTargetContext dtc = dtde.getDropTargetContext();
        JTree tree = (JTree) dtc.getComponent();
        TreePath path = tree.getClosestPathForLocation(p.x, p.y);
        return (TreeNode) path.getLastPathComponent();
    }

    private TreeNode getNodeForEvent(DropTargetDropEvent dtde) {
        Point p = dtde.getLocation();
        DropTargetContext dtc = dtde.getDropTargetContext();
        JTree tree = (JTree) dtc.getComponent();
        TreePath path = tree.getClosestPathForLocation(p.x, p.y);
        return (TreeNode) path.getLastPathComponent();
    }

    private ArrayList<File> getFiles(Transferable transferable) {

        // this explicitly does not allow dragging files from a JAR.
        // When dragging jar files it does not return a file list or anything I can work out for now.
        // We'll have to come back to this.

        try {
            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && transferable.getTransferData(DataFlavor.javaFileListFlavor) instanceof ArrayList) {

                ArrayList<File> files = (ArrayList<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                return files;

            }
        } catch (UnsupportedFlavorException | IOException e) {
            return null;
        }

        return null;
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

        // compatible drags:
        // j3o over node
        // j3m over geometry/spatial

        // the treeNode we are dragging TO.
        DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) getNodeForEvent(dtde);

        // Transferable transferable = dtde.getTransferable();
        ArrayList<File> files = getFiles(dtde.getTransferable());

        if (files != null && !files.isEmpty()) {

            // work out what files can be dragged based on the node we are dragging to.

            if (targetNode.getUserObject() instanceof Node) {

                boolean canDrop = files.stream().allMatch(file -> file.getName().endsWith(".j3o"));

                if (!canDrop) {
                    dtde.rejectDrag();
                }
                else {
                    dtde.acceptDrag(1);
                }
            }
            else { // the target does not support items being dragged into it.
                dtde.rejectDrag();
            }
        }
        else { // files are null or empty, we can't do anything.
            dtde.rejectDrag();
        }

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // System.out.println("dropActionChanged(DropTargetDragEvent dtde)");
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // System.out.println("dragExit(DropTargetEvent dte)");
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {

        // only called when the drop event was accepted, so we don't need to sanitize anything.

        DefaultMutableTreeNode targetTreeNode = (DefaultMutableTreeNode) getNodeForEvent(dtde);

        ArrayList<File> files = getFiles(dtde.getTransferable());

        // there's no need for this check. The drag event would reject an empty file list, but it makes the compiler happy.
        if (files == null) {
            return;
        }

        // if the target was a node and it was accepted, it must be a list of .j3o's
        if (targetTreeNode.getUserObject() instanceof Node) {

            Node targetNode = (Node) targetTreeNode.getUserObject();
            JmeEngineService engineService = ServiceManager.getService(JmeEngineService.class);

            ArrayList<Spatial> spatials = new ArrayList<>();
            files.forEach(file -> {

                // intellij usually prepends local filesystem paths with "file://" so we're going to do the same.
                // We've come to rely on it for determining if the path is inside a JAR or not.
                Spatial model = engineService.getExternalAssetLoader().load("file://" + file.getAbsolutePath(), Spatial.class);

                if (model != null) {
                    spatials.add(model);
                }

            });

            engineService.enqueue(() -> {
                spatials.forEach(targetNode::attachChild);
                EventQueue.invokeLater(() -> ServiceManager.getService(SceneExplorerService.class).refreshScene());
            });

        }

    }
}
