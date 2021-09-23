package com.enderio.core.api.client.gui;

import com.enderio.core.client.gui.widget.ScrollableListWidget;

import javax.annotation.Nonnull;

public interface ListSelectionListener<T> {

  void selectionChanged(@Nonnull ScrollableListWidget<T> list, int selectedIndex);

}
