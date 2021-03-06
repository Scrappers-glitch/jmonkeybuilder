package com.ss.editor.ui.component.asset.tree.context.menu.action;

import static com.ss.rlib.common.util.ClassUtils.unsafeCast;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.component.asset.tree.resource.ResourceElement;
import com.ss.editor.ui.event.impl.MovedFileEvent;
import com.ss.editor.ui.event.impl.RequestSelectFileEvent;
import com.ss.editor.util.EditorUtil;
import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The action to paste a file.
 *
 * @author JavaSaBr
 */
public class PasteFileAction extends FileAction {

    @FxThread
    public static void applyFor(@NotNull ResourceElement element) {
        new PasteFileAction(element).getOnAction().handle(null);
    }

    public PasteFileAction(@NotNull ResourceElement element) {
        super(element);
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return Messages.ASSET_COMPONENT_RESOURCE_TREE_CONTEXT_MENU_PASTE_FILE;
    }

    @Override
    @FxThread
    protected @Nullable Image getIcon() {
        return Icons.PASTE_16;
    }

    @Override
    @FxThread
    protected void execute(@Nullable ActionEvent event) {
        super.execute(event);

        var clipboard = Clipboard.getSystemClipboard();
        if (clipboard == null) {
            return;
        }

        List<File> files = unsafeCast(clipboard.getContent(DataFormat.FILES));
        if (files == null || files.isEmpty()) {
            return;
        }

        var currentFile = getElement().getFile();
        var isCut = "cut".equals(clipboard.getContent(EditorUtil.JAVA_PARAM));

        if (isCut) {
            files.forEach(file -> moveFile(currentFile, file.toPath()));
        } else {
            files.forEach(file -> copyFile(currentFile, file.toPath()));
        }

        clipboard.clear();
    }

    private void copyFile(@NotNull Path currentFile, @NotNull Path file) {
        if (Files.isDirectory(currentFile)) {
            processCopy(currentFile, file);
        } else {
            processCopy(currentFile.getParent(), file);
        }
    }

    private void moveFile(@NotNull Path currentFile, @NotNull Path file) {
        if (Files.isDirectory(currentFile)) {
            processMove(currentFile, file);
        } else {
            processMove(currentFile.getParent(), file);
        }
    }

    /**
     * Process of moving.
     */
    private void processMove(@NotNull Path targetFolder, @NotNull Path file) {

        var newFile = targetFolder.resolve(file.getFileName());

        try {
            Files.move(file, newFile);
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return;
        }

        var event = new MovedFileEvent();
        event.setPrevFile(file);
        event.setNewFile(newFile);

        FX_EVENT_MANAGER.notify(event);
    }

    /**
     * Process of copying.
     */
    private void processCopy(@NotNull Path targetFolder, @NotNull Path file) {

        Array<Path> toCopy = ArrayFactory.newArray(Path.class);
        Array<Path> copied = ArrayFactory.newArray(Path.class);

        if (Files.isDirectory(file)) {
            toCopy.addAll(FileUtils.getFiles(file, true));
            toCopy.sort(FileUtils.FILE_PATH_LENGTH_COMPARATOR);
            toCopy.slowRemove(file);
        }

        var freeName = FileUtils.getFirstFreeName(targetFolder, file);
        var newFile = targetFolder.resolve(freeName);

        try {
            processCopy(file, toCopy, copied, newFile);
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
        }

        var event = new RequestSelectFileEvent();
        event.setFile(newFile);

        FX_EVENT_MANAGER.notify(event);
    }

    /**
     * Process of copying.
     */
    private void processCopy(
            @NotNull Path file,
            @NotNull Array<Path> toCopy,
            @NotNull Array<Path> copied,
            @NotNull Path newFile
    ) throws IOException {

        Files.copy(file, newFile);

        copied.add(newFile);
        toCopy.forEach(path -> {

            var relativeFile = file.relativize(path);
            var targetFile = newFile.resolve(relativeFile);

            try {
                Files.copy(path, targetFile);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            var needAddToCopied = true;

            for (var copiedFile : copied) {

                if (!Files.isDirectory(copiedFile)) {
                    continue;
                }

                if (targetFile.startsWith(copiedFile)) {
                    needAddToCopied = false;
                    break;
                }
            }

            if (needAddToCopied) {
                copied.add(targetFile);
            }
        });
    }
}
