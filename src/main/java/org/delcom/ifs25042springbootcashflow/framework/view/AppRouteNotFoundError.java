package org.delcom.ifs25042springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import jakarta.servlet.http.HttpServletResponse;

@PageTitle("Halaman Tidak Ditemukan - Cashflow")
public class AppRouteNotFoundError extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    private final H1 title = new H1("Halaman Tidak Ditemukan");
    private final Paragraph message = new Paragraph();

    public AppRouteNotFoundError() {
        setSizeFull();
        getElement().setAttribute("role", "main");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "#F3F4F6");

        title.getStyle().set("margin", "0");
        message.getStyle().set("margin", "0").set("color", "#4B5563");

        Button homeBtn = new Button("Kembali ke Beranda", e -> UI.getCurrent().navigate(FinanceView.class));
        homeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeBtn.getStyle().set("cursor", "pointer");

        Div card = PageCard.wrap(title, message, homeBtn);
        add(card);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        message.setText("Halaman yang Anda tuju tidak dapat ditemukan.");
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
