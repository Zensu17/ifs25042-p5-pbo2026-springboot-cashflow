package org.delcom.ifs25042springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Modal untuk menampilkan gambar dalam ukuran diperbesar dengan fitur zoom interaktif.
 */
final class ImagePreviewDialog {

    private ImagePreviewDialog() {
    }

    static void open(String imageUrl, String alt) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String title = alt == null || alt.isBlank() ? "Pratinjau Gambar" : alt;

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Span headerHint = new Span("Klik gambar untuk memperbesar / memperkecil");
        headerHint.getStyle()
                .set("margin-top", "0.2rem")
                .set("color", "#BFDBFE")
                .set("font-size", "0.8rem")
                .set("display", "block");

        Div header = new Div(headerTitle, headerHint);
        header.getStyle()
                .set("background", "#2563EB")
                .set("padding", "0.9rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Image image = new Image(imageUrl, title);
        image.getStyle()
                .set("display", "block")
                .set("max-width", "100%")
                .set("max-height", "68vh")
                .set("width", "auto")
                .set("height", "auto")
                .set("object-fit", "contain")
                .set("border-radius", "8px")
                .set("cursor", "zoom-in")
                .set("transition", "transform 0.25s ease")
                .set("user-select", "none");
        image.getElement().setAttribute("title", "Klik untuk zoom");

        Div imageWrapper = new Div(image);
        imageWrapper.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center")
                .set("overflow", "auto")
                .set("max-height", "70vh")
                .set("max-width", "min(88vw, 860px)")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        Div body = new Div(imageWrapper);
        body.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center")
                .set("background", "#FFFFFF")
                .set("padding", "1rem 1.25rem")
                .set("box-sizing", "border-box")
                .set("width", "100%")
                .set("overflow", "hidden");

        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setResizable(false);
        dialog.setMaxWidth("95vw");

        AtomicReference<Double> currentScale = new AtomicReference<>(1.0);

        Runnable applyScale = () -> {
            double scale = currentScale.get();
            image.getStyle().set("transform", "scale(" + scale + ")");
            image.getStyle().set("cursor", scale > 1.0 ? "zoom-out" : "zoom-in");
        };

        image.addClickListener(event -> {
            if (currentScale.get() > 1.0) {
                currentScale.set(1.0);
            } else {
                currentScale.set(1.8);
            }
            applyScale.run();
        });

        Button zoomOutButton = new Button("Perkecil", VaadinIcon.SEARCH_MINUS.create(), event -> {
            double next = Math.max(1.0, currentScale.get() - 0.4);
            currentScale.set(next);
            applyScale.run();
        });
        zoomOutButton.getStyle().set("cursor", "pointer");

        Button zoomInButton = new Button("Perbesar", VaadinIcon.SEARCH_PLUS.create(), event -> {
            double next = Math.min(2.6, currentScale.get() + 0.4);
            currentScale.set(next);
            applyScale.run();
        });
        zoomInButton.getStyle().set("cursor", "pointer");

        Button closeButton = new Button("Tutup", VaadinIcon.CLOSE.create(), event -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.getStyle().set("cursor", "pointer");

        Div footer = new Div(zoomOutButton, zoomInButton, closeButton);
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div shell = new Div(header, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs("""
                        const overlay = this.$.overlay;
                        if (!overlay || !overlay.shadowRoot) {
                          return;
                        }
                        const content = overlay.shadowRoot.querySelector('[part="content"]');
                        if (content) {
                          content.style.padding = '0';
                          content.style.overflow = 'visible';
                          content.style.maxHeight = 'none';
                        }
                        const overlayPart = overlay.shadowRoot.querySelector('[part="overlay"]');
                        if (overlayPart) {
                          overlayPart.style.padding = '0';
                          overlayPart.style.overflow = 'hidden';
                        }
                        """);
            }
        });
        dialog.open();
    }

    static Image clickableThumbnail(String imageUrl, String alt, String height) {
        return clickableThumbnail(imageUrl, alt, height, "6px");
    }

    static Image clickableThumbnail(String imageUrl, String alt, String height, String borderRadius) {
        Image image = new Image(imageUrl, alt);
        image.setHeight(height);
        image.getStyle()
                .set("border-radius", borderRadius)
                .set("object-fit", "cover")
                .set("cursor", "pointer")
                .set("display", "block");
        image.getElement().setAttribute("title", "Klik untuk memperbesar");
        image.addClickListener(event -> open(imageUrl, alt));
        return image;
    }
}
