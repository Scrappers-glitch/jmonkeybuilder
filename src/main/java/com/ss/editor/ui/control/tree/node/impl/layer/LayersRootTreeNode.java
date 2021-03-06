package com.ss.editor.ui.control.tree.node.impl.layer;

import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.extension.scene.SceneLayer;
import com.ss.editor.extension.scene.SceneNode;
import com.ss.editor.model.undo.editor.SceneChangeConsumer;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.layer.LayerNodeTree;
import com.ss.editor.model.node.layer.LayersRoot;
import com.ss.editor.ui.control.tree.action.impl.scene.CreateSceneLayerAction;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The implementation of {@link TreeNode} to present {@link LayersRoot}.
 *
 * @author JavaSaBr
 */
public class LayersRootTreeNode extends TreeNode<LayersRoot> {

    public LayersRootTreeNode(@NotNull final LayersRoot element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FxThread
    public @Nullable Image getIcon() {
        return Icons.SCENE_16;
    }

    @Override
    @FxThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        items.add(new CreateSceneLayerAction(nodeTree, this));
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        final LayersRoot element = getElement();
        final SceneChangeConsumer changeConsumer = element.getChangeConsumer();
        final SceneNode sceneNode = changeConsumer.getCurrentModel();
        return sceneNode.getName();
    }


    @Override
    @FxThread
    public @NotNull Array<TreeNode<?>> getChildren(@NotNull final NodeTree<?> nodeTree) {

        final LayersRoot element = getElement();
        final SceneChangeConsumer changeConsumer = element.getChangeConsumer();
        final SceneNode sceneNode = changeConsumer.getCurrentModel();
        final List<SceneLayer> layers = sceneNode.getLayers();

        final Array<TreeNode<?>> result = ArrayFactory.newArray(TreeNode.class);
        layers.forEach(layer -> result.add(FACTORY_REGISTRY.createFor(layer)));

        return result;
    }

    @Override
    @FxThread
    public boolean hasChildren(@NotNull final NodeTree<?> nodeTree) {
        return nodeTree instanceof LayerNodeTree;
    }
}
