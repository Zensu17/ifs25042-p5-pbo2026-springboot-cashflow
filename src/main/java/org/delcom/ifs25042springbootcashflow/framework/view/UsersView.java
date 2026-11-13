package org.delcom.ifs25042springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.delcom.ifs25042springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25042springbootcashflow.usecase.UserUseCase;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Daftar Pengguna - Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    public UsersView(UserUseCase userUseCase) {
        setSizeFull();
        getElement().setAttribute("role", "main");
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        Grid<User> grid = new Grid<>(User.class, false);
        grid.addComponentColumn(user -> {
            if (user.getPhotoPath() == null || user.getPhotoPath().isBlank()) {
                return new Span("-");
            }
            return ImagePreviewDialog.clickableThumbnail(
                    "/uploads/" + user.getPhotoPath(),
                    "Foto",
                    "40px",
                    "50%"
            );
        }).setHeader("Foto").setAutoWidth(true);
        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(User::getUsername).setHeader("Username").setFlexGrow(1);
        grid.addColumn(User::getFullName).setHeader("Nama").setFlexGrow(1);
        grid.addColumn(user -> user.getRole().name()).setHeader("Role").setAutoWidth(true);
        grid.addComponentColumn(user -> {
            Button open = new Button("Lihat Transaksi", event ->
                    UI.getCurrent().navigate(UserTransactionsView.class, user.getId()));
            open.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            open.getStyle().set("cursor", "pointer");
            return open;
        }).setHeader("Tindakan").setAutoWidth(true);

        grid.setItems(userUseCase.listUsers());
        grid.setSizeFull();
        GridStyles.apply(grid);

        H1 title = new H1("Daftar Pengguna");
        title.getStyle().set("margin", "0");
        Paragraph subtitle = new Paragraph("Admin dapat meninjau catatan transaksi pengguna lain (hanya melihat / read-only).");
        subtitle.getStyle().set("margin", "0").set("color", "#4B5563");

        Div card = PageCard.wrap(title, subtitle, grid);
        add(card);
        expand(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser().orElse(null);
        if (current == null || !current.isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }
}
