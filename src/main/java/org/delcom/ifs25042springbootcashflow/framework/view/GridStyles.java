package org.delcom.ifs25042springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/**
 * Styling grid agar lebih mudah dibaca (header kontras + stripe).
 */
final class GridStyles {

    private GridStyles() {
    }

    static void apply(Grid<?> grid) {
        grid.addClassName("app-grid");
        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COLUMN_BORDERS,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );
        grid.getStyle()
                .set("border", "1px solid #CBD5E1")
                .set("border-radius", "10px")
                .set("overflow", "hidden");
    }
}
