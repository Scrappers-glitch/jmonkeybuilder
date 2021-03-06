package com.ss.editor.ui.component.editor.state.impl;

import static com.ss.rlib.common.util.ObjectUtils.notNull;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.config.EditorConfig;
import com.ss.editor.ui.component.editor.state.EditorState;
import com.ss.rlib.common.util.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * The base implementation of a state container for an editor.
 *
 * @author JavaSaBr
 */
public abstract class AbstractEditorState implements EditorState {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 1;

    /**
     * The constant EDITOR_CONFIG.
     */
    @NotNull
    protected static final EditorConfig EDITOR_CONFIG = EditorConfig.getInstance();

    /**
     * The constant EMPTY_ADDITIONAL_STATES.
     */
    @NotNull
    protected static final AdditionalEditorState[] EMPTY_ADDITIONAL_STATES = new AdditionalEditorState[0];

    /**
     * The change handler.
     */
    @Nullable
    protected transient volatile Runnable changeHandler;

    /**
     * The list of additional states.
     */
    @NotNull
    private volatile AdditionalEditorState[] additionalStates;

    public AbstractEditorState() {
        this.additionalStates = EMPTY_ADDITIONAL_STATES;
    }

    @Override
    @FxThread
    public void setChangeHandler(@NotNull final Runnable changeHandler) {
        this.changeHandler = changeHandler;
        for (final AdditionalEditorState additionalState : additionalStates) {
            additionalState.setChangeHandler(changeHandler);
        }
    }

    @Override
    @FxThread
    public <T extends AdditionalEditorState> @NotNull T getOrCreateAdditionalState(@NotNull final Class<T> type,
                                                                                   @NotNull final Supplier<T> factory) {

        for (final AdditionalEditorState additionalState : additionalStates) {
            if (type.isInstance(additionalState)) {
                return type.cast(additionalState);
            }
        }

        final AdditionalEditorState newAdditionalState = factory.get();
        newAdditionalState.setChangeHandler(notNull(changeHandler));

        this.additionalStates = ArrayUtils.addToArray(additionalStates, newAdditionalState, AdditionalEditorState.class);

        return type.cast(newAdditionalState);
    }

    /**
     * Gets change handler.
     *
     * @return the change handler.
     */
    @FxThread
    protected @Nullable Runnable getChangeHandler() {
        return changeHandler;
    }

    @FxThread
    protected void notifyChange() {
        if (changeHandler != null) {
            changeHandler.run();
        }
    }

    @Override
    public String toString() {
        return "AbstractEditorState{" + "additionalStates=" + Arrays.toString(additionalStates) + '}';
    }
}
