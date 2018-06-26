package com.ss.editor.ui.component.editor;

import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.ui.component.editor.impl.*;
import com.ss.editor.ui.component.editor.impl.material.MaterialFileEditor;
import com.ss.editor.ui.component.editor.impl.model.ModelFileEditor;
import com.ss.editor.ui.component.editor.impl.scene.SceneFileEditor;
import com.ss.editor.util.EditorUtil;
import com.ss.rlib.common.logging.Logger;
import com.ss.rlib.common.logging.LoggerManager;
import com.ss.rlib.common.plugin.extension.ExtensionPoint;
import com.ss.rlib.common.plugin.extension.ExtensionPointManager;
import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * THe registry of editors.
 *
 * @author JavaSaBr
 */
public class EditorRegistry {

    private static final Logger LOGGER = LoggerManager.getLogger(EditorRegistry.class);

    public static final String ALL_FORMATS = "*";

    /**
     * @see EditorDescription
     */
    public static final String EP_DESCRIPTIONS = "EditorRegistry#descriptions";

    private static final ExtensionPoint<EditorDescription> DESCRIPTIONS =
            ExtensionPointManager.register(EP_DESCRIPTIONS);

    private static final Supplier<Array<EditorDescription>> ARRAY_FACTORY =
            Array.supplier(EditorDescription.class);

    private static final EditorRegistry INSTANCE = new EditorRegistry();

    public static @NotNull EditorRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * The table with editor descriptions.
     */
    @NotNull
    private final ConcurrentObjectDictionary<String, Array<EditorDescription>> editorDescriptions;

    /**
     * The table with mapping editor id to editor description.
     */
    @NotNull
    private final ConcurrentObjectDictionary<String, EditorDescription> editorIdToDescription;

    private final AtomicBoolean initialized;

    private EditorRegistry() {
        this.initialized = new AtomicBoolean(false);
        this.editorDescriptions = ConcurrentObjectDictionary.ofType(String.class, Array.class);
        this.editorIdToDescription = ConcurrentObjectDictionary.ofType(String.class, EditorDescription.class);

        DESCRIPTIONS.register(TextFileEditor.DESCRIPTION)
                .register(MaterialFileEditor.DESCRIPTION)
                .register(ModelFileEditor.DESCRIPTION)
                .register(ImageViewerEditor.DESCRIPTION)
                .register(GLSLFileEditor.DESCRIPTION)
                .register(MaterialDefinitionFileEditor.DESCRIPTION)
                .register(AudioViewerEditor.DESCRIPTION)
                .register(SceneFileEditor.DESCRIPTION);

        LOGGER.info("initialized.");
    }

    @FromAnyThread
    private void checkAndInitialize() {
        if (initialized.compareAndSet(false, true)) {
            DESCRIPTIONS.getExtensions().forEach(this::register);
        }
    }

    /**
     * Add the new description.
     *
     * @param description the description of the editor.
     */
    @FromAnyThread
    private void register(@NotNull EditorDescription description) {

        description.getExtensions()
                .forEach(description, this::register);

        getEditorIdToDescription()
                .runInWriteLock(description.getEditorId(), description, ObjectDictionary::put);
    }

    /**
     * Get the table with editor descriptions.
     *
     * @return the table with editor descriptions.
     */
    @FromAnyThread
    private @NotNull ConcurrentObjectDictionary<String, Array<EditorDescription>> getEditorDescriptions() {
        return editorDescriptions;
    }

    /**
     * Get the table with mapping editor id to editor description.
     *
     * @return the table with mapping editor id to editor description.
     */
    @FromAnyThread
    private @NotNull ConcurrentObjectDictionary<String, EditorDescription> getEditorIdToDescription() {
        return editorIdToDescription;
    }

    @FromAnyThread
    private void register(@NotNull String extension, @NotNull EditorDescription description) {
        getEditorDescriptions().runInWriteLock(extension, description,
                (dict, ext, toAdd) -> dict.get(ext, ARRAY_FACTORY).add(toAdd));
    }

    /**
     * Get an editor description by the editor id.
     *
     * @param editorId the editor id.
     * @return the description or null.
     */
    @FromAnyThread
    public @Nullable EditorDescription getDescription(@NotNull String editorId) {
        checkAndInitialize();
        return getEditorIdToDescription()
                .getInReadLock(editorId, ObjectDictionary::get);
    }

    /**
     * Create an editor for the file.
     *
     * @param file the edited file.
     * @return the editor for this file or null.
     */
    @FromAnyThread
    public @Nullable FileEditor createEditorFor(@NotNull Path file) {
        checkAndInitialize();

        if (Files.isDirectory(file)) {
            return null;
        }

        var extension = FileUtils.getExtension(file);
        var editorDescriptions = getEditorDescriptions();

        var descriptions = editorDescriptions.get(extension);

        EditorDescription description;

        if (descriptions != null) {
            description = descriptions.first();
        } else {
            descriptions = editorDescriptions.get(ALL_FORMATS);
            description = descriptions == null ? null : descriptions.first();
        }

        if (description == null) {
            return null;
        }

        var constructor = description.getConstructor();
        try {
            return constructor.call();
        } catch (Exception e) {
            EditorUtil.handleException(LOGGER, this, e);
        }

        return null;
    }

    /**
     * Create an editor for the file.
     *
     * @param description the editor description.
     * @param file        the edited file.
     * @return the editor or null.
     */
    @FromAnyThread
    public @Nullable FileEditor createEditorFor(@NotNull EditorDescription description, @NotNull Path file) {
        checkAndInitialize();

        var constructor = description.getConstructor();
        try {
            return constructor.call();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets available editors for.
     *
     * @param file the file
     * @return the list of available editors for the file.
     */
    @FromAnyThread
    public @NotNull Array<EditorDescription> getAvailableEditorsFor(@NotNull Path file) {
        checkAndInitialize();

        var result = Array.<EditorDescription>ofType(EditorDescription.class);
        var extension = FileUtils.getExtension(file);

        var editorDescriptions = getEditorDescriptions();
        var descriptions = editorDescriptions.get(extension);

        if (descriptions != null) {
            result.addAll(descriptions);
        }

        var universal = editorDescriptions.get(ALL_FORMATS);

        if (universal != null) {
            result.addAll(universal);
        }

        return result;
    }
}
