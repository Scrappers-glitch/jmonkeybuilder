package com.ss.editor.ui.control.property.impl;

import com.ss.editor.Editor;
import com.ss.editor.JFXApplication;
import com.ss.editor.Messages;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.property.AbstractPropertyControl;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.css.CSSIds;
import com.ss.editor.ui.event.FXEventManager;
import com.ss.rlib.function.SixObjectConsumer;
import com.ss.rlib.ui.util.FXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * The implementation of the {@link AbstractPropertyControl} to edit an elements from scene.
 *
 * @param <C> the type parameter
 * @param <D> the type parameter
 * @param <T> the type parameter
 * @author JavaSaBr
 */
public abstract class AbstractElementPropertyControl<C extends ChangeConsumer, D, T> extends AbstractPropertyControl<C, D, T> {

    /**
     * The constant NO_ELEMENT.
     */
    protected static final String NO_ELEMENT = Messages.ABSTRACT_ELEMENT_PROPERTY_CONTROL_NO_ELEMENT;
    
    private static final Insets BUTTON_OFFSET = new Insets(0, 0, 0, 3);

    /**
     * The constant FX_EVENT_MANAGER.
     */
    protected static final FXEventManager FX_EVENT_MANAGER = FXEventManager.getInstance();
    /**
     * The constant JFX_APPLICATION.
     */
    protected static final JFXApplication JFX_APPLICATION = JFXApplication.getInstance();
    /**
     * The constant EDITOR.
     */
    protected static final Editor EDITOR = Editor.getInstance();

    /**
     * The type of an element.
     */
    @NotNull
    protected final Class<T> type;

    /**
     * The label with name of the element.
     */
    private Label elementLabel;

    /**
     * Instantiates a new Abstract element property control.
     *
     * @param type           the type
     * @param propertyValue  the property value
     * @param propertyName   the property name
     * @param changeConsumer the change consumer
     * @param changeHandler  the change handler
     */
    public AbstractElementPropertyControl(@NotNull final Class<T> type, @Nullable final T propertyValue,
                                          @NotNull final String propertyName, @NotNull final C changeConsumer,
                                          @NotNull final SixObjectConsumer<C, D, String, T, T, BiConsumer<D, T>> changeHandler) {
        super(propertyValue, propertyName, changeConsumer, changeHandler);
        this.type = type;
    }

    @Override
    protected void createComponents(@NotNull final HBox container) {
        super.createComponents(container);

        elementLabel = new Label(NO_ELEMENT);
        elementLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_ELEMENT_LABEL);

        final Button changeButton = new Button();
        changeButton.setId(CSSIds.ABSTRACT_PARAM_CONTROL_ELEMENT_BUTTON);
        changeButton.setGraphic(new ImageView(Icons.ADD_16));
        changeButton.setOnAction(event -> processAdd());

        final Button editButton = new Button();
        editButton.setId(CSSIds.ABSTRACT_PARAM_CONTROL_ELEMENT_BUTTON);
        editButton.setGraphic(new ImageView(Icons.REMOVE_12));
        editButton.disableProperty().bind(elementLabel.textProperty().isEqualTo(NO_ELEMENT));
        editButton.setOnAction(event -> processRemove());

        elementLabel.prefWidthProperty().bind(widthProperty()
                .subtract(changeButton.widthProperty())
                .subtract(editButton.widthProperty())
                .subtract(BUTTON_OFFSET.getLeft() * 2));

        FXUtils.addToPane(elementLabel, container);
        FXUtils.addToPane(changeButton, container);
        FXUtils.addToPane(editButton, container);

        HBox.setMargin(changeButton, BUTTON_OFFSET);
        HBox.setMargin(editButton, BUTTON_OFFSET);

        FXUtils.addClassTo(elementLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(changeButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(changeButton, CSSClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        FXUtils.addClassTo(editButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(editButton, CSSClasses.FILE_EDITOR_TOOLBAR_BUTTON);
    }

    /**
     * Show dialog to choose an element.
     */
    protected void processAdd() {
    }

    /**
     * Open this material in the material editor.
     */
    protected void processRemove() {
        changed(null, getPropertyValue());
    }

    /**
     * Gets element label.
     *
     * @return the label with name of the material.
     */
    @NotNull
    protected Label getElementLabel() {
        return Objects.requireNonNull(elementLabel);
    }
}
