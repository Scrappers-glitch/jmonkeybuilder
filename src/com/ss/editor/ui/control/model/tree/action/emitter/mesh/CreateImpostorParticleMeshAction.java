package com.ss.editor.ui.control.model.tree.action.emitter.mesh;

import com.ss.editor.Messages;
import com.ss.editor.ui.control.model.tree.ModelNodeTree;
import com.ss.editor.ui.control.model.tree.node.ModelNode;

import org.jetbrains.annotations.NotNull;

import tonegod.emitter.geometry.ParticleGeometry;
import tonegod.emitter.particle.ParticleDataImpostorMesh;
import tonegod.emitter.particle.ParticleDataMeshInfo;

/**
 * The action for switching the particle mesh of the {@link ParticleGeometry} to {@link
 * ParticleDataImpostorMesh}.
 *
 * @author JavaSaBr
 */
public class CreateImpostorParticleMeshAction extends AbstractCreateParticleMeshAction {

    public CreateImpostorParticleMeshAction(@NotNull final ModelNodeTree nodeTree, @NotNull final ModelNode<?> node) {
        super(nodeTree, node);
    }

    @NotNull
    @Override
    protected String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_EMITTER_CHANGE_PARTICLES_MESH_IMPOSTOR;
    }

    @NotNull
    @Override
    protected ParticleDataMeshInfo createMeshInfo() {
        return new ParticleDataMeshInfo(ParticleDataImpostorMesh.class, null);
    }
}