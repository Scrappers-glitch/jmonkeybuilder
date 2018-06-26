package com.ss.editor.ui.preview;

import com.ss.editor.annotation.FxThread;
import com.ss.editor.ui.preview.impl.DefaultFilePreviewFactory;
import com.ss.rlib.common.logging.Logger;
import com.ss.rlib.common.logging.LoggerManager;
import com.ss.rlib.common.plugin.extension.ExtensionPoint;
import com.ss.rlib.common.plugin.extension.ExtensionPointManager;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

/**
 * The registry with available factories of file previews.
 *
 * @author JavaSaBr
 */
public class FilePreviewFactoryRegistry {

    private static final Logger LOGGER = LoggerManager.getLogger(FilePreviewFactoryRegistry.class);

    /**
     * @see FilePreviewFactory
     */
    public static final String EP_PREVIEW_FACTORIES = "FilePreviewFactoryRegistry#previewFactories";

    private static final ExtensionPoint<FilePreviewFactory> PREVIEW_FACTORIES =
            ExtensionPointManager.register(EP_PREVIEW_FACTORIES);

    private static final FilePreviewFactoryRegistry INSTANCE = new FilePreviewFactoryRegistry();

    public static @NotNull FilePreviewFactoryRegistry getInstance() {
        return INSTANCE;
    }

    private FilePreviewFactoryRegistry() {
        PREVIEW_FACTORIES.register(DefaultFilePreviewFactory.getInstance());
        LOGGER.info("initialized.");
    }

    /**
     * Create available of file previews.
     *
     * @return the list of available file previews.
     */
    @FxThread
    public Array<FilePreview> createAvailablePreviews() {

        var result = Array.<FilePreview>ofType(FilePreview.class);

        PREVIEW_FACTORIES.getExtensions()
                .forEach(filePreviewFactory -> filePreviewFactory.createFilePreviews(result));

        result.sort((first, second) ->
                second.getOrder() - first.getOrder());

        return result;
    }
}
