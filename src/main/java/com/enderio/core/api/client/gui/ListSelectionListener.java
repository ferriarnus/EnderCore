package com.enderio.core.api.client.gui;

import javax.annotation.Nonnull;

import com.enderio.core.client.gui.widget.ScrollableListWidget;

public interface ListSelectionListener<T> {

  void selectionChanged(@Nonnull ScrollableListWidget<T> list, int selectedIndex);

}
